package com.angelo.realtime.player;

import java.io.IOException;
import java.util.Optional;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import com.angelo.realtime.player.model.MessagesWrapper;

public class PlayerSessionHelper {
	
	static Optional<Session> getSessionByPlayer(long playerId) {
		
		return getPlayerSessionManagerByPlayerId(playerId).map(PlayerSessionManager::getSession);		
	}

	static Optional<PlayerSessionManager> getPlayerSessionManagerByPlayerId(long playerId) {
		
		return PlayerSessionManager.SESSION_MANAGER_COLLECTION.stream()
				.filter(playerSessionManager -> playerSessionManager.getPlayerId() == playerId)
				.findFirst();
	}
	
	static void sendMessageToSession(final MessagesWrapper messagesWrapper, final Session session) {
		try {
			System.out.println("send message to player reached");
			session.getBasicRemote().sendObject(messagesWrapper);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (EncodeException e) {
			e.printStackTrace();
		}
	}
	
	static void sendMessageToSessionAndClose(final MessagesWrapper messagesWrapper, final Session session) {
		try {
			System.out.println("send message to player reached");
			session.getBasicRemote().sendObject(messagesWrapper);
			session.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (EncodeException e) {
			e.printStackTrace();
		}
	}
}
