package com.angelo.controller.social;

import java.util.List;
import java.util.Optional;

import com.angelo.dao.Player;

public final class PlayerSocialHelper {

	static void addPlayerToList(final Player player, final List<Player> list) {
		list.add(player);
	}

	static Boolean removePlayerFromList(final Player player, final List<Player> list) {
		return findPlayerInList(player, list)
				.map(target -> {list.remove(target); return true;})
				.orElse(false);		
	}
	
	static boolean playerIsNotInList(final Player player, final List<Player> list) {
		return list.stream().noneMatch(otherPlayer -> otherPlayer.getId() == player.getId());
	}

	static boolean isPlayerInList(final Player player, final List<Player> list) {
		return list.stream().anyMatch(otherPlayer -> otherPlayer.getId() == player.getId());
	}
	
	private static Optional<Player> findPlayerInList(final Player player, final List<Player> list) {
		return list.stream()
				.filter(otherPlayer -> otherPlayer.getId() == player.getId())
				.findFirst();
	}
}
