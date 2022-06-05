package com.angelo.realtime.game.model;

public enum Roles {
	MAIN("main"),
	OPPONENT("opponent"),
	TIMEOUT("timeout"),
	PING("ping");
	
	private final String value;
	
	private Roles(final String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return this.value;
	}
}
