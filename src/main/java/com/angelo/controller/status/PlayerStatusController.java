package com.angelo.controller.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.angelo.controller.exception.ExceptionProvider;
import com.angelo.dao.IPlayerDAO;
import com.angelo.dao.Player;
import com.angelo.realtime.player.PlayerSessionProxy;

@RestController
public class PlayerStatusController {

	private final IPlayerDAO playerDAO;
	
	private final PlayerSessionProxy playerSessionProxy;
	
	private final ExceptionProvider exceptionProvider;

	@Autowired
	public PlayerStatusController(final IPlayerDAO playerDAO, final PlayerSessionProxy playerSessionProxy,
			final ExceptionProvider exceptionProvider) {
		
		this.playerDAO = playerDAO;
		this.playerSessionProxy = playerSessionProxy;
		this.exceptionProvider = exceptionProvider;
	}
	
	/**
	 * Update a player busy status.
	 * @param nick the nickname of the player to be updated.
	 * @param busy the new status.
	 * @return the updated player entity.
	 */
	@PutMapping("/setBusy")
	Player setBusy(@RequestParam String nick, @RequestParam short busy) {
		
		return playerDAO.findByNick(nick)
				.map(player -> updateBusyStatus(player, busy))
				.orElseThrow(exceptionProvider::provideNotFoundException);		
	}

	/**
	 * Update a player online status.
	 * @param nick the nickname of the player to be updated.
	 * @param busy the new status.
	 * @return the updated player entity.
	 */
	@PutMapping("/setOnline")
	Player setOnline(@RequestParam String nick, @RequestParam short online) {
		
		return playerDAO.findByNick(nick)
				.map(player -> updateOnlineStatus(player, online))
				.orElseThrow(exceptionProvider::provideNotFoundException);		
	}
	
	private Player updateBusyStatus(final Player player, final short busy) {
		player.setBusy(busy);
		playerSessionProxy.setLastActionTimestamp(player);
		playerDAO.save(player);
		return player;
	}
	
	private Player updateOnlineStatus(final Player player, final short online) {
		player.setOnline(online);
		playerDAO.save(player);
		return player;
	}

}
