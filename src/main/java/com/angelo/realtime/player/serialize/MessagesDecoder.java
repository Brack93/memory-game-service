package com.angelo.realtime.player.serialize;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.angelo.realtime.player.model.MessagesWrapper;
import com.google.gson.Gson;

public class MessagesDecoder implements Decoder.Text<MessagesWrapper> {
	private static Gson gson = new Gson();

	@Override
	public MessagesWrapper decode(String s) throws DecodeException {
		return gson.fromJson(s, MessagesWrapper.class);
	}

	@Override
	public boolean willDecode(String s) {
		return (s != null);
	}

	@Override
	public void init(EndpointConfig endpointConfig) {	
	}

	@Override
	public void destroy() {
	}
}
