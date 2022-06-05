package com.angelo.realtime.game;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.websocket.Session;

import com.angelo.realtime.game.model.GameMessage;

import static com.angelo.realtime.game.GameSessionHelper.*;

public class AsyncGameOperation extends TimerTask
{
    private final Map<Session, GameMessage> messageMap;
    private final Timer timer;
    
    public AsyncGameOperation(final Timer timer, Map<Session, GameMessage> messageMap) {
        this.messageMap = messageMap;
        this.timer = timer;
    }
    
    @Override
    public void run() {
        sendMessageMap(messageMap);
        this.timer.cancel();        
    }
}
