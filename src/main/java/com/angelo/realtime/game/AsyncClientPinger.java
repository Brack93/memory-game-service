package com.angelo.realtime.game;

import java.util.TimerTask;

import javax.websocket.Session;

import com.angelo.realtime.game.model.GameMessage;
import com.angelo.realtime.game.model.Roles;
import com.angelo.realtime.util.DateUtility;

import static com.angelo.realtime.game.GameSessionHelper.*;

public class AsyncClientPinger extends TimerTask {

	@Override
	public void run() {

		for (GameSessionManager gameSessionManager : GameSessionManager.SESSION_MANAGER_COLLECTION) {

			final GameSession gameSession = gameSessionManager.getGameSession();
			final Session session = gameSessionManager.getSession();
			final Roles role = gameSessionManager.getRole();
			if (gameSession != null && isPlayerTimeout(gameSession, role)) {

				GameMessage timeoutMessage = new GameMessage(gameSession, Roles.TIMEOUT);
				sendMessageToSessionAndClose(getImmutableEntry(session, timeoutMessage));

			} else {

				GameMessage pingMessage = new GameMessage(gameSession, Roles.PING);
				sendMessageToSession(getImmutableEntry(session, pingMessage));

			}
		}
	}

	private boolean isPlayerTimeout(final GameSession gameSession, final Roles role) {
		return (role == Roles.MAIN && isMainTimeout(gameSession))
				|| (role == Roles.OPPONENT && isOpponentTimeout(gameSession));
	}

	private boolean isMainTimeout(final GameSession gameSession) {
		return DateUtility.isTimeOut(GameSessionManager.TIMEOUT, gameSession.getMainTimeStamp());
	}

	private boolean isOpponentTimeout(final GameSession gameSession) {
		return DateUtility.isTimeOut(GameSessionManager.TIMEOUT, gameSession.getOpponentTimeStamp())
				&& gameSession.getBooked() >= 0;
	}
}