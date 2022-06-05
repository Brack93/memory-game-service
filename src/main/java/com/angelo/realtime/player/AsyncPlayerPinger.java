package com.angelo.realtime.player;

import java.util.TimerTask;

import com.angelo.realtime.player.model.Messages;
import com.angelo.realtime.player.model.MessagesWrapper;
import com.angelo.realtime.util.DateUtility;

import static com.angelo.realtime.player.PlayerSessionHelper.*;

public class AsyncPlayerPinger extends TimerTask {

	@Override
	public void run() {

		for (PlayerSessionManager p : PlayerSessionManager.SESSION_MANAGER_COLLECTION) {
			if (DateUtility.isTimeOut(PlayerSessionManager.TIMEOUT_SECONDS, p.getLastActionTimestamp())) {
				String[] messages = { Messages.TIMEOUT.toString() };
				sendMessageToSessionAndClose(new MessagesWrapper(messages), p.getSession());

			} else {
				String[] messages = { Messages.PING.toString() };
				sendMessageToSession(new MessagesWrapper(messages), p.getSession());
			}
		}
	}
}
