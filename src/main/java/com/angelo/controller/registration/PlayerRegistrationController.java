package com.angelo.controller.registration;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.angelo.controller.exception.ExceptionProvider;
import com.angelo.controller.exception.ExceptionReason;
import com.angelo.dao.IPlayerDAO;
import com.angelo.dao.Player;

@RestController
public class PlayerRegistrationController {

	private final IPlayerDAO playerDAO;
	
	private final ExceptionProvider exceptionProvider;
	
	@Autowired
	PlayerRegistrationController(final IPlayerDAO playerDAO, final ExceptionProvider exceptionProvider) {
		this.playerDAO = playerDAO;
		this.exceptionProvider = exceptionProvider;
	}
	
	/**
	 * Method to delete a player entity.
	 * @param id the player id.
	 */
	@DeleteMapping("/deletePlayer")
	void deletePlayer(@RequestParam Long id) {
		playerDAO.deleteById(id);
	}
	
	/**
	 * Method to register new player entity.
	 * @param nick the player choosen nickname.
	 * @param italian the player device language code.
	 * @return the created player entity.
	 */
	@PostMapping("/addPlayer")
	Player newPlayer(@RequestParam String nick, @RequestParam Optional<Short> italian) {
		
		if (playerDAO.findByNick(nick).isPresent()) {
			throw exceptionProvider.provideConflictException(ExceptionReason.USERNAME_NOT_AVAILABLE);
		} else {
			Player player = new Player();
			player.setNick(nick);
			player.setItalian(italian.orElse((short) 1));
			return playerDAO.save(player);
		}		
	}
}
