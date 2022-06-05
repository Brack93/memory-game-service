package com.angelo.realtime.game;

import javax.websocket.OnError;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import java.util.TimerTask;
import java.util.Timer;
import javax.websocket.OnOpen;
import java.io.IOException;

import javax.websocket.server.PathParam;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;
import java.util.Map;
import java.util.Optional;

import java.util.Set;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.angelo.realtime.game.ai.task.AsyncAICheck;
import com.angelo.realtime.game.model.GameMessage;
import com.angelo.realtime.game.model.PlayerStatus;
import com.angelo.realtime.game.model.Roles;
import com.angelo.realtime.game.serialize.GameDecoder;
import com.angelo.realtime.game.serialize.GameEncoder;
import com.angelo.realtime.util.DateUtility;

import lombok.Getter;

import static com.angelo.realtime.game.GameSessionHelper.*;

/**
 * Socket endpoint for game session.
 * @param level the number of figures pair.
 * @param booked the queue parameter: 
 * <li> 0 -> search random opponent </li>
 * <li> playerId -> reserved for a private game </li>
 * <li> -1 -> special value reserved for AI client </li>
 */
@Getter
@Component
@ServerEndpoint(value = GameSessionManager.ENDPOINT, decoders = { GameDecoder.class },
encoders = { GameEncoder.class })
public class GameSessionManager {
	
	public static final String ENDPOINT = "/game/{level}/{booked}";
	public static final long TIMEOUT = 180L;
	public static final Set<GameSessionManager> SESSION_MANAGER_COLLECTION = new CopyOnWriteArraySet<GameSessionManager>();
	private static boolean clientPingerEnabled = false;
	
	Session session;
	Roles role;
	
	/**
	 * Same object reference will be shared between two opponents, i.e. two GameSessionManager.
	 */
	GameSession gameSession;

	public GameSessionManager() {
		if (!clientPingerEnabled) {
			clientPingerEnabled = true;
			new Timer().scheduleAtFixedRate(new AsyncClientPinger(), 0, 30000);
			new Timer().scheduleAtFixedRate(new AsyncAICheck(), 0, 10000);
		}
	}
	
	@OnOpen
	public void onOpen(final Session session, @PathParam("level") final Integer level,
			@PathParam("booked") final Long booked) throws IOException {
		
		System.out.println("arrivata richiesta iscrizione da client");
		this.session = session;
		
		Map<Session, GameMessage> messageMap = 
				SESSION_MANAGER_COLLECTION.stream()
				.filter(gameSessionManager -> isMatchingQueuedCriteria(gameSessionManager, level, booked))
				.findFirst().map(this::finalizeMatchmaking)
				.orElseGet(() -> queuePlayer(level, booked));
		
		sendMessageMap(messageMap);
		SESSION_MANAGER_COLLECTION.add(this);
		System.out.println("Player in partita: " + SESSION_MANAGER_COLLECTION.size());
	}
	
	@OnMessage
	public void onMessage(final Session session, final GameMessage message) throws IOException {

		if (isUntrustedSource(message.getGameSession())) return;
		
		registerClientAction(message.getGameSession());
		getOpponentSessionManager().ifPresent(this::manageClientAction);
	}

	@OnClose
	public void onClose(final Session session) throws IOException {
		System.out.println("un giocatore ha abbandonato la partita");

		if (gameSession != null && gameSession.getEndGame() == -1) {

			if (this.role == Roles.MAIN) {
				gameSession.setEndGame(2);
			} else {
				gameSession.setEndGame(1);
			}

			getOpponentSessionManager().ifPresent(opponentSessionManager -> {
				final GameMessage opponentGameMessage = new GameMessage(gameSession, opponentSessionManager.getRole());
				sendMessageToSession(getImmutableEntry(opponentSessionManager.getSession(), opponentGameMessage));
			});

		}

		GameSessionManager.SESSION_MANAGER_COLLECTION.remove(this);
		System.out.println("Player in partita: " + SESSION_MANAGER_COLLECTION.size());
	}

	@OnError
	public void onError(final Session session, final Throwable throwable) {
	}
	
	private GameMessage getGameMessage() {
		return new GameMessage(gameSession, this.getRole());
	}
	
	private void manageClientAction(final GameSessionManager opponentSessionManager) {
		
		final GameMessage opponentMessage = new GameMessage(gameSession, opponentSessionManager.getRole());
		final Session opponentSession = opponentSessionManager.getSession();
		final Map<Session, GameMessage> messageMap = Stream.of(
				getImmutableEntry(session, getGameMessage()),
				getImmutableEntry(opponentSession, opponentMessage))
				.collect(provideMessageMapCollector());
		
		final PlayerStatus status = GameSessionHelper.getPlayerStatus(gameSession);

		switch (status) {
		case PLAYING:
			managePlayingStatus(messageMap);
			break;
		case KEEPPLAYING:
			manageKeepPlayingStatus(messageMap);
			break;
		case ENDTURN:
			manageEndTurnStatus(messageMap);
			break;

		default:
			break;
		}
	}
	
	private void managePlayingStatus(final Map<Session, GameMessage> messageMap) {
		
		sendMessageMap(messageMap);
	}
	
	private void manageKeepPlayingStatus(final Map<Session, GameMessage> messageMap) {
		
		final char player = (gameSession.getTurn() == 1) ? '1' : '2';
		final String played = gameSession.getPlayed();
		gameSession.setPlayed(played.replace('s', player));
		final int endGame = GameSessionHelper.getAnyWinner(gameSession);
		gameSession.setEndGame(endGame);
		sendMessageMap(messageMap);
	}
	
	private void manageEndTurnStatus(final Map<Session, GameMessage> messageMap) {
		
		final int oldTurn = gameSession.getTurn();
		gameSession.setTurn(-1);
		sendMessageMap(messageMap);
		final String played = gameSession.getPlayed();
		gameSession.setPlayed(played.replace('s', '0'));
		final int turn = (oldTurn == 1) ? 2 : 1;
		gameSession.setTurn(turn);
		final Timer timer = new Timer(true);
		timer.schedule((TimerTask) new AsyncGameOperation(timer, messageMap), 4000L);
	}
	
	private void registerClientAction(final GameSession gameSession) {
		BeanUtils.copyProperties(gameSession, this.gameSession);
		
		if (this.role == Roles.MAIN) {
			this.gameSession.setMainTimeStamp(DateUtility.getCurrentTimestamp());
		} else if (this.role == Roles.OPPONENT) {
			this.gameSession.setOpponentTimeStamp(DateUtility.getCurrentTimestamp());
		}
		
		System.out.println("Messaggio ricevuto con played: " + gameSession.getPlayed());
	}
	
	/**
	 * Check if given game level and booked type match a player in queue.
	 */
	private static boolean isMatchingQueuedCriteria(final GameSessionManager gameSessionManager, final int level, final long booked) {
		final GameSession gameSession = gameSessionManager.getGameSession();
		return gameSession.getTurn() == 0 && gameSession.getEndGame() == -1 
				&& gameSession.getLevel() == level
				&& gameSession.getBooked() == booked;
	}
	
	private Map<Session, GameMessage> queuePlayer(final int level, final long booked) {
		this.role = Roles.MAIN;
		gameSession = new GameSession((int) level, (long) booked);
		gameSession.setTurn(0);
		gameSession.setMainTimeStamp(DateUtility.getCurrentTimestamp());
		GameMessage gameMessage = new GameMessage(gameSession, Roles.MAIN);
		System.out.println("Main si è registrato");
		return Stream.of(getImmutableEntry(session, gameMessage))
				.collect(provideMessageMapCollector());
	}
	
	private Map<Session, GameMessage> finalizeMatchmaking(final GameSessionManager queuedGameSessionManager) {
		
		gameSession = queuedGameSessionManager.getGameSession();
		gameSession.setTurn((Math.random() < 0.5) ? 1 : 2);
		gameSession.setOpponentTimeStamp(DateUtility.getCurrentTimestamp());
		this.role = Roles.OPPONENT;
		
		final GameMessage gameMessage = new GameMessage(gameSession, Roles.OPPONENT);
		final GameMessage opponentGameMessage = new GameMessage(gameSession, queuedGameSessionManager.getRole());
		
		final Session opponentSession = queuedGameSessionManager.getSession();
		
		System.out.println("opponent si è registrato");
		
		return Stream.of(
				getImmutableEntry(session, gameMessage),
				getImmutableEntry(opponentSession, opponentGameMessage))
				.collect(provideMessageMapCollector());
		
	}
	
	private boolean isUntrustedSource(final GameSession gameSession) {
		return this.getGameSession().getGameSessionId() != gameSession.getGameSessionId();
	}
	
	private Optional<GameSessionManager> getOpponentSessionManager() {
		return SESSION_MANAGER_COLLECTION.stream()
				.filter(this::filterOpponentSessionPredicate).findFirst();
	}
	
	private boolean filterOpponentSessionPredicate(final GameSessionManager gameSessionManager) {
		return gameSessionManager.getGameSession().equals(this.getGameSession())
				&& gameSessionManager.getSession().getId() != this.getSession().getId();				
	}

}
