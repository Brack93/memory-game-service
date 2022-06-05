package com.angelo.realtime.player.model;

public enum Messages {
	
	FRIENDS("friends"),
	REQUESTS("requests"),
	REFRESH("refresh"),
	UNFOLLOW("unfollow"),
	TIMEOUT("timeout"),
	PING("ping"),
	OK("ok"),
	ERROR("error"),
	NOT_FOUND("404"),
	BOOKING("booking"),
	BUSY("busy"),
	REFUSED("refused");
	
	private final String value;
	
	private Messages(final String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return this.value;
	}
	
	public static String[] asStringArray(final Messages[] messages) {
		
		String[] converted = new String[messages.length];
		for (int i = 0; i < converted.length; i++) {
			converted[i] = messages[i].toString();
		}
		return converted;
	}

}
