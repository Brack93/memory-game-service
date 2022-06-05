package com.angelo.controller.social.model;

import java.util.List;

import com.angelo.dao.Player;

import lombok.Getter;

@Getter
public class PlayersWrapper {

	private final List<Player> players;

	public PlayersWrapper(final List<Player> players) {
		this.players = players;
	}
	
}
