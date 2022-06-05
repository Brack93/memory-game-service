package com.angelo.realtime.player;

import java.io.IOException;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.angelo.dao.IPlayerDAO;
import com.angelo.dao.Player;
import com.angelo.realtime.player.model.Messages;
import com.angelo.realtime.player.model.MessagesWrapper;
import com.angelo.realtime.player.serialize.MessagesDecoder;
import com.angelo.realtime.player.serialize.MessagesEncoders;
import com.angelo.realtime.util.DateUtility;

import lombok.Getter;

import static com.angelo.realtime.player.model.MessageWrapperHelper.*;
import static com.angelo.realtime.player.PlayerSessionHelper.*;

/**
 * Socket endpoint for player session.
 * @param id the player id.
 */
@Getter
@Component
@ServerEndpoint(value = PlayerSessionManager.ENDPOINT, decoders = { MessagesDecoder.class },
encoders = {MessagesEncoders.class})
public class PlayerSessionManager {

	public static final String ENDPOINT = "/player/{id}";
	public static final long TIMEOUT_SECONDS = 360L;
	public static final Set<PlayerSessionManager> SESSION_MANAGER_COLLECTION = new CopyOnWriteArraySet<PlayerSessionManager>();
	private static final long PINGER_PERIOD= 30000;
	private static final long SHORT_TIMEOUT_MILLISECONDS = 1000L;
	private static boolean playerPingerEnabled = false;
	private static IPlayerDAO playerDAO;
	private Session session;
	private long playerId;
	private long lastActionTimestamp = -1;

	public PlayerSessionManager() {
		if (!playerPingerEnabled) {
			playerPingerEnabled = true;
			new Timer().scheduleAtFixedRate(new AsyncPlayerPinger(), 0, PINGER_PERIOD);
		}
	}

	@Autowired
	private void setPlayerDAO(final IPlayerDAO playerDAOInstance) {
		if (playerDAO == null) {
			playerDAO = playerDAOInstance;
		}
	}
	
	@OnOpen
	public void onOpen(final Session session, @PathParam("id") final Long id) throws IOException {
		
		printNewConnection(id);
		
		Messages[] response = playerDAO.findById(id)
				.map(player -> registerPlayerSession(player, session))
				.orElseGet(() -> invalidSession(session));
		sendMessageToSession(new MessagesWrapper(Messages.asStringArray(response)), session);
		
		printConnectedPlayersCount();		
	}
	
	/**
	 * Expecting messages structured as {action, "toId", "fromId", "fromName"}.
	 */
	@OnMessage
	public void onMessage(final Session session, MessagesWrapper messagesWrapper) {
		
		printMessages(messagesWrapper);
		
		String[] messages = messagesWrapper.getMessages();
		final long toId = getToId(messages);
		
		if (isBookingMessage(messagesWrapper)) {			
			String[] responseMessages = playerDAO.findById(toId)
					.filter(foundPlayer -> foundPlayer.getBusy() == 0)
					.map(freePlayer -> messagesWrapper.getMessages())
					.orElse(new String[] { Messages.BUSY.toString(), messages[2], messages[1]});
			
			long responseToId = getToId(responseMessages);
			sendMessageToPlayer(new MessagesWrapper(responseMessages), responseToId);
			
		} else if (isRefusingMessage(messagesWrapper)) {
			sendMessageToPlayer(messagesWrapper, toId);
		}
	}

	@OnClose
	public void onClose(final Session session) throws IOException {
		
		playerDAO.findById(getPlayerId()).ifPresent(this::setPlayerOffline);
		SESSION_MANAGER_COLLECTION.remove(this);
		
		printConnectedPlayersCount();
	}
	
	static void sendMessageToPlayer(MessagesWrapper messagesWrapper, long playerId) {
		
		getSessionByPlayer(playerId).ifPresent(session -> sendMessageToSession(messagesWrapper, session));
	}
	
	/**
	 * When player is busy game session manager is responsible of managing player session.
	 */
	static void setLastActionTimestamp(Player player) {
		
		getPlayerSessionManagerByPlayerId(player.getId())
		.ifPresent(playerSessionManager -> {
			playerSessionManager.lastActionTimestamp = player.getBusy() == 0 ? DateUtility.getCurrentTimestamp() : -1;
		});
	}
	
	static void disconnectAllPlayers() {
		
		for (PlayerSessionManager playerSessionManager : SESSION_MANAGER_COLLECTION) {
			PlayerSessionManager.playerDAO.findById(playerSessionManager.getPlayerId())
			.ifPresent(playerSessionManager::setPlayerOffline);
		}
	}
	
	private void setPlayerOffline(final Player player) {
		System.out.println("disconnessione da " + player.getNick());
		player.setOnline((short) 0);
		playerDAO.save(player);
	}
	
	private Messages[] registerPlayerSession(final Player player, final Session session) {
		player.setOnline((short) 1);
		player.setTimestamp(DateUtility.getCurrentSqlDate());
		this.lastActionTimestamp = DateUtility.getCurrentTimestamp();
		playerDAO.save(player);
		this.session = session;
		this.playerId = player.getId();
		SESSION_MANAGER_COLLECTION.add(this);
		return new Messages[] {Messages.OK};
	}
	
	private Messages[] invalidSession(final Session session) {
		session.setMaxIdleTimeout(SHORT_TIMEOUT_MILLISECONDS);
		return new Messages[] {Messages.ERROR, Messages.NOT_FOUND};
	}

	private void printConnectedPlayersCount() {
		System.out.println("Player connessi: "+SESSION_MANAGER_COLLECTION.size());
	}
	
	private void printNewConnection(final Long id) {
		System.out.println("richiesta connessione da " + id);
	}
}
