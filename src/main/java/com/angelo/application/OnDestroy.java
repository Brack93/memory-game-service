package com.angelo.application;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.angelo.realtime.player.PlayerSessionProxy;

@Component
public class OnDestroy {
	
	private final PlayerSessionProxy playerSessionProxy;
	
	@Autowired
	public OnDestroy(final PlayerSessionProxy playerSessionProxy) {
		System.out.println("On destroy bean creato");
		this.playerSessionProxy = playerSessionProxy;
	}
	
	
	@PreDestroy
	 public void onExit() {
		playerSessionProxy.disconnectAllPlayers();
	    System.out.println("stopping");	    
	 }
	
}
