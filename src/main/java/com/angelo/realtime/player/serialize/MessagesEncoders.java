package com.angelo.realtime.player.serialize;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.angelo.realtime.player.model.MessagesWrapper;
import com.google.gson.Gson;

public class MessagesEncoders implements Encoder.Text<MessagesWrapper> {
	
	private static Gson gson = new Gson();

    @Override
    public String encode(MessagesWrapper message) throws EncodeException {
        return gson.toJson(message);
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
    }

    @Override
    public void destroy() {
    }
}
