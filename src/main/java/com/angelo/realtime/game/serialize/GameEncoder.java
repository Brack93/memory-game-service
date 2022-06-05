package com.angelo.realtime.game.serialize;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.angelo.realtime.game.model.GameMessage;
import com.google.gson.Gson;

public class GameEncoder implements Encoder.Text<GameMessage> {

    private static Gson gson = new Gson();

    @Override
    public String encode(GameMessage message) throws EncodeException {
        return gson.toJson(message);
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
        // Custom initialization logic
    }

    @Override
    public void destroy() {
        // Close resources
    }
}
