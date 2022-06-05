package com.angelo.realtime.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import com.angelo.realtime.game.model.GameMessage;
import com.angelo.realtime.game.model.PlayerStatus;

public final class GameSessionHelper {

	private GameSessionHelper() {
		
	}
	
	static void sendMessageMap(final Map<Session, GameMessage> messageMap) {
		messageMap.entrySet().stream().forEach(GameSessionHelper::sendMessageToSession);
	}
	
	static void sendMessageToSession(final Map.Entry<Session,GameMessage> messageEntry) {
		try {
			messageEntry.getKey().getBasicRemote().sendObject((Object) messageEntry.getValue());
		} catch (IOException | EncodeException e) {
			e.printStackTrace();
		}
	}
	
	static void sendMessageToSessionAndClose(final Map.Entry<Session,GameMessage> messageEntry) {
		try {
			messageEntry.getKey().getBasicRemote().sendObject((Object) messageEntry.getValue());
			messageEntry.getKey().close();
		} catch (IOException | EncodeException e) {
			e.printStackTrace();
		}
	}
	
	static Entry<Session, GameMessage> getImmutableEntry(final Session session, final GameMessage gameMessage) {
		return new SimpleImmutableEntry<Session, GameMessage>(session, gameMessage);
	}
	
	static Collector<Entry<Session, GameMessage>, ?, Map<Session, GameMessage>> provideMessageMapCollector() {
		return Collectors.<Entry<Session, GameMessage>, Session, GameMessage>toMap(entry -> entry.getKey(), entry -> entry.getValue());
	}
	
	static int getAnyWinner(final GameSession gameSession) {
		int any = -1;
		boolean gameEnd = true;
		final ArrayList<Integer> oneIndex = new ArrayList<Integer>();
		final ArrayList<Integer> twoIndex = new ArrayList<Integer>();
		final String played = gameSession.getPlayed();
		for (int i = 0; i < played.length(); ++i) {
			if (played.charAt(i) == 's' || played.charAt(i) == '0') {
				gameEnd = false;
				break;
			}
			if (played.charAt(i) == '1') {
				oneIndex.add(i);
			} else if (played.charAt(i) == '2') {
				twoIndex.add(i);
			}
		}
		if (gameEnd) {
			if (oneIndex.size() > twoIndex.size()) {
				any = 1;
			} else if (oneIndex.size() < twoIndex.size()) {
				any = 2;
			} else {
				any = 0;
			}
		}
		return any;
	}

	static PlayerStatus getPlayerStatus(final GameSession gameSession) {
		final ArrayList<Integer> sIndex = getPlayedShow(gameSession);
		if (sIndex.size() < 2) {
			System.out.println("status playing");
			return PlayerStatus.PLAYING;
		}
		if (gameSession.getGameFigures().get(sIndex.get(0)) == gameSession.getGameFigures().get(sIndex.get(1))) {
			System.out.println("status keep playing");
			return PlayerStatus.KEEPPLAYING;
		}
		System.out.println("status end turn");
		return PlayerStatus.ENDTURN;
	}
	
	private static ArrayList<Integer> getPlayedShow(final GameSession gameSession) {
		final String played = gameSession.getPlayed();
		final ArrayList<Integer> sIndex = new ArrayList<Integer>();
		for (int i = 0; i < played.length(); ++i) {
			if (played.charAt(i) == 's') {
				sIndex.add(i);
			}
		}
		return sIndex;
	}	
	
}
