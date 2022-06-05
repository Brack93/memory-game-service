package com.angelo.realtime.player;

import org.springframework.stereotype.Component;

import com.angelo.dao.Player;
import com.angelo.realtime.player.model.MessagesWrapper;

@Component
public class PlayerSessionProxy {
	
	public void sendMessageToPlayer(MessagesWrapper messagesWrapper, long playerId) {
		PlayerSessionManager.sendMessageToPlayer(messagesWrapper, playerId);
	}
	
	public void setLastActionTimestamp(Player player) {
		PlayerSessionManager.setLastActionTimestamp(player);
	}
	
	public void disconnectAllPlayers() {
		PlayerSessionManager.disconnectAllPlayers();
	}
}
