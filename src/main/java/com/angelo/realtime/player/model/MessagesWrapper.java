package com.angelo.realtime.player.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MessagesWrapper {
	
	private final String[] messages;
	
	public MessagesWrapper(final String[] messages) {
		this.messages = messages;
	}

}
