package com.angelo.realtime.game.ai.task;

import java.util.Date;
import java.util.TimerTask;

import com.angelo.realtime.game.GameSession;
import com.angelo.realtime.game.GameSessionManager;
import com.angelo.realtime.game.ai.GameClient;

/**
 * Check if AI is needed to match player in queue.
 */
public class AsyncAICheck extends TimerTask {

	@Override
	public void run() {
		try {

			for (GameSessionManager gameSessionManager : GameSessionManager.SESSION_MANAGER_COLLECTION) {

				final GameSession gameSession = gameSessionManager.getGameSession();
				
				if (isPlayerStuckInQueue(gameSession)) {
					gameSession.setBooked(-1);
					new GameClient(gameSession.getLevel());
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isPlayerStuckInQueue(final GameSession gameSession) {
		return gameSession != null && gameSession.getTurn() == 0 && gameSession.getEndGame() == -1
				&& gameSession.getBooked() == 0 && (new Date().getTime() - gameSession.getGameSessionId() >= 10000);
	}
}
