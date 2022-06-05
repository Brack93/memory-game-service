package com.angelo.realtime.game.model;

import com.angelo.realtime.game.GameSession;

import lombok.Getter;

@Getter
public class GameMessage {

	private GameSession gameSession;
	private final String role;
	
	public GameMessage(final GameSession gameSession, final Roles role) {
		this.role = role.toString();
		this.gameSession = gameSession;
	}

}
