package com.angelo.controller.social;

import java.util.Comparator;
import java.util.List;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.angelo.controller.exception.ExceptionProvider;
import com.angelo.controller.exception.ExceptionReason;
import com.angelo.controller.social.model.PlayersWrapper;
import com.angelo.dao.IPlayerDAO;
import com.angelo.dao.Player;
import com.angelo.realtime.player.PlayerSessionProxy;
import com.angelo.realtime.player.model.Messages;
import com.angelo.realtime.player.model.MessagesWrapper;
import static com.angelo.controller.social.PlayerSocialHelper.*;

@RestController
public class PlayerSocialController {

	private final IPlayerDAO playerDAO;
	
	private final PlayerSessionProxy playerSessionProxy;
	
	private final ExceptionProvider exceptionProvider;
	
	@Autowired
	public PlayerSocialController(final IPlayerDAO playerDAO, final PlayerSessionProxy playerSessionProxy,
			final ExceptionProvider exceptionProvider) {
		this.playerDAO = playerDAO;
		this.playerSessionProxy = playerSessionProxy;
		this.exceptionProvider = exceptionProvider;
	}
	
	/**
	 * Get received friend requests not accepted yet.
	 * @param nick the player nickname.
	 * @return the resulting players list.
	 */
	@GetMapping("/followers")
	PlayersWrapper getFollowers(@RequestParam String nick) {
		
		return playerDAO.findByNick(nick)
				.map(player -> removeElementsFromList(player.getFollowers(), player.getFollowing()))
				.orElseThrow(exceptionProvider::provideNotFoundException);
	}

	/**
	 * Get sent friend requests not accepted yet.
	 * @param nick the player nickname.
	 * @return the resulting players list.
	 */
	@GetMapping("/following")
	PlayersWrapper getFollowing(@RequestParam String nick) {
		
		return playerDAO.findByNick(nick)
				.map(player -> removeElementsFromList(player.getFollowing(), player.getFollowers()))
				.orElseThrow(exceptionProvider::provideNotFoundException);
	}

	/**
	 * Get friends of provided player.
	 * @param nick the player nickname.
	 * @return the resulting players list.
	 */
	@GetMapping("/friends")
	PlayersWrapper getFriends(@RequestParam String nick) {
		
		return playerDAO.findByNick(nick)
				.map(player -> getCommonElements(player.getFollowing(), player.getFollowers()))
				.orElseThrow(exceptionProvider::provideNotFoundException);	
	}

	/**
	 * Send a friend request.
	 * @param myNick the player who is sending the request.
	 * @param followNick the player who will receive the request.
	 * @return the followed player.
	 */
	@PostMapping("/follow")
	Player newFiend(@RequestParam String myNick, @RequestParam String followNick) {
		
		if (followNick.equals(myNick)) {
			throw exceptionProvider.provideConflictException(ExceptionReason.FOLLOW_YOURSELF_REASON);
		}

		Player myPlayer = findPlayerByNick(myNick);
		Player followPlayer = findPlayerByNick(followNick);

		if (isPlayerInList(followPlayer, myPlayer.getFollowing())) {
			throw exceptionProvider.provideConflictException(ExceptionReason.ALREADY_FOLLOWING_REASON);
		}

		addPlayerToList(followPlayer, myPlayer.getFollowing());
		playerDAO.save(myPlayer);

		Messages targetView = isPlayerInList(myPlayer, followPlayer.getFollowing()) ? Messages.FRIENDS
				: Messages.REQUESTS;
		String[] actions = Messages.asStringArray(new Messages[] { Messages.REFRESH, targetView });
		playerSessionProxy.sendMessageToPlayer(new MessagesWrapper(actions), followPlayer.getId());

		return followPlayer;
	}

	/**
	 * Remove a friend.
	 * @param myNick the player who is deleting the friendship.
	 * @param followNick the player to be removed both from following and followers.
	 * @return the unfollowed player.
	 */
	@DeleteMapping("/unfollow")
	Player removeFiend(@RequestParam String myNick, @RequestParam String followNick) {
		if (followNick.equals(myNick)) {
			throw exceptionProvider.provideConflictException(ExceptionReason.FOLLOW_YOURSELF_REASON);
		}
		Player myPlayer = findPlayerByNick(myNick);
		Player followPlayer = findPlayerByNick(followNick);

		if (removePlayerFromList(followPlayer, myPlayer.getFollowing())) {
			playerDAO.save(myPlayer);
		}
		if (removePlayerFromList(myPlayer, followPlayer.getFollowing())) {
			playerDAO.save(followPlayer);
		}

		String[] actions = { Messages.UNFOLLOW.toString() };

		playerSessionProxy.sendMessageToPlayer(new MessagesWrapper(actions), followPlayer.getId());
		return followPlayer;
	}

	private Player findPlayerByNick(final String nick) {
		
		return playerDAO.findByNick(nick).orElseThrow(exceptionProvider::provideNotFoundException);
	}
	
	private PlayersWrapper removeElementsFromList(final List<Player> originalList, final List<Player> elements) {
		
		return originalList.stream().filter(player -> playerIsNotInList(player, elements))
				.collect(collectingAndThen(toList(), PlayersWrapper::new));
		}
	
	private PlayersWrapper getCommonElements(final List<Player> playerList1, final List<Player> playerList2) {
		
		return playerList1.stream().filter(player -> isPlayerInList(player, playerList2))
				.sorted(Comparator.comparing(Player::getOnline).reversed())
				.collect(collectingAndThen(toList(), PlayersWrapper::new));
	}

}
