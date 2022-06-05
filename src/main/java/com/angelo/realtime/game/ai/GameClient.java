package com.angelo.realtime.game.ai;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import com.angelo.realtime.game.ai.task.AsyncAIOperation;
import com.angelo.realtime.game.model.GameMessage;
import com.angelo.realtime.game.model.Roles;
import com.angelo.realtime.game.serialize.GameDecoder;
import com.angelo.realtime.game.serialize.GameEncoder;

import static com.angelo.realtime.util.StringUtility.*;

/**
 * AI instance used when player in queue waits too long for real opponent.
 */
@ClientEndpoint(decoders = { GameDecoder.class }, encoders = { GameEncoder.class })
public class GameClient {	
	
	private static final long AI_MOVE_DELAY = 2000L;
	private ArrayList<Integer> figures;
	private int role = 0;
	private String clientMemory;

	public GameClient(final int level) {
		
		String port = getServerPort();	
		connectToServer(port, level);		
	}

	@OnOpen
	public void onOpen(Session session) {
		//need message to initialize AI. AI is ALWAYS the opponent.
	}

	@OnMessage
	public void onMessage(final Session session, final GameMessage gameMessage) {

		if (isPingMessage(gameMessage)) return;
		
		initializeAI(session, gameMessage);			
		String played = gameMessage.getGameSession().getPlayed();			
		memorizeShowedFigures(played);
		
		if (isGameEnd(gameMessage)) {
			closeSession(session);
			return;
		}		
		
		if (isAiTurn(gameMessage)) {
			Timer timer = new Timer(true);
			timer.schedule((TimerTask) new AsyncAIOperation(timer, session, gameMessage, this.clientMemory,
					this.figures), AI_MOVE_DELAY);
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {

	}
	
	private String getServerPort() {
		
		return  Optional.ofNullable(System.getenv("PORT")).orElseGet(() -> {
			System.out.println("port environment not assigned");
			return "8080";});		
	}
	
	private void connectToServer(final String port, final int level) {
		
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		try {
			container.connectToServer(this, new URI("ws://localhost:" + port + "/game/" + level + "/-1"));
		} catch (DeploymentException | IOException | URISyntaxException e) {				
			e.printStackTrace();
		}
	}
	
	private void closeSession(final Session session) {
		
		try {
			session.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initializeAI(final Session session, final GameMessage gameMessage) {
		
		if (this.role == 0) {			
			this.figures = gameMessage.getGameSession().getGameFigures();
			this.role = gameMessage.getRole().equals(Roles.MAIN.toString()) ? 1 : 2;
			if (this.role == 1) {
				closeSession(session); //AI non può essere il main
				return;
			}
			clientMemory = gameMessage.getGameSession().getPlayed();
		}
	}
	
	/**
	 * AI attempt to memorize showed figure with 50% probability.
	 */
	private void memorizeShowedFigures(final String played) {
		
		for (int i = 0; i < played.length(); i++) {
			char c = played.charAt(i);
			if (c == 's' && Math.random() <= 0.51d) {
				clientMemory = replaceWithCharAt(clientMemory, 'm', i);
			} else if (c == '1' || c == '2') {
				clientMemory = replaceWithCharAt(clientMemory, 'p', i);
			}
		}
		
		System.out.println("played: " + played);
		System.out.println("memory: " + clientMemory);
	}
	
	private boolean isPingMessage(final GameMessage gameMessage) {
		return gameMessage.getRole().equals(Roles.PING.toString());
	}
	
	private boolean isGameEnd(final GameMessage gameMessage) {
		return gameMessage.getGameSession().getEndGame() != -1;
	}
	
	private boolean isAiTurn(final GameMessage gameMessage) {
		return gameMessage.getRole().equals(Roles.OPPONENT.toString())
				&& gameMessage.getGameSession().getTurn() == 2;
	}	

}
