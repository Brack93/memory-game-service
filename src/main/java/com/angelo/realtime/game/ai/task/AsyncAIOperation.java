package com.angelo.realtime.game.ai.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import com.angelo.realtime.game.model.GameMessage;

import static com.angelo.realtime.util.RandomUtility.*;
import static com.angelo.realtime.util.StringUtility.*;

public class AsyncAIOperation extends TimerTask {
	private final Session session;
	private final GameMessage message;
	private final String memory;
	private final ArrayList<Integer> figures;
	private final Timer timer;

	public AsyncAIOperation(final Timer timer, final Session session, final GameMessage message, final String memory,
			final ArrayList<Integer> figures) {

		this.session = session;
		this.message = message;
		this.memory = memory;
		this.figures = figures;
		this.timer = timer;
	}

	@Override
	public boolean cancel() {
		return super.cancel();
	}

	@Override
	public void run() {

		int firstCard = getFirstMoved().orElse(-1);

		if (firstCard < 0) {

			firsMove();

		} else {

			secondMove(firstCard);
		}

		timer.cancel();

	}
	
	private OptionalInt getFirstMoved() {

		final String played = message.getGameSession().getPlayed();
		return IntStream.range(0, played.length())
				.filter(i -> played.charAt(i) == 's').findFirst();
	}

	private int getSecondIndexOfMemorizedFigureCouple() {

		int coupleIndex = -1;
		for (int i = 0; i < memory.length(); i++) {			
			char c1 = memory.charAt(i);
			if (c1 == 'm') {
				for (int j = i + 1; j < memory.length(); j++) {
					char c2 = memory.charAt(j);
					if (c2 == 'm' && figures.get(i) == figures.get(j)) {
						coupleIndex = j;
						break;
					}
				}
			}			
			if (coupleIndex >= 0)
				break;
		}

		return coupleIndex;
	}
	
	private void sendMove(final int index) {
		
		final String played = message.getGameSession().getPlayed();
		String newPlayed = replaceWithCharAt(played, 's', index);
		message.getGameSession().setPlayed(newPlayed);
		
		try {
			session.getBasicRemote().sendObject(message);
		} catch (IOException | EncodeException e) {			
			e.printStackTrace();
		}
	}
	
	private void firsMove() {
		
		int coupleIndex = getSecondIndexOfMemorizedFigureCouple();
		int index = -1;
		if (coupleIndex >= 0) {
			System.out.println("Ho trovato coppia memorizzata!");
			index = coupleIndex;
		} else {
			List<Integer> notMemorized = getNotMemorized();
			int rIndex = -1;
			if (notMemorized.size() > 0) {
				rIndex = getRandomIndexFromList(notMemorized);
				index = notMemorized.get(rIndex);
			} else {
				List<Integer> memorized = getMemorized();
				rIndex = getRandomIndexFromList(memorized);
				index = memorized.get(rIndex);
			}
		}		
		sendMove(index);
	}
	
	private void secondMove(final int firstCard) {
		
		int index = -1;
		for (int i = 0; i < memory.length(); i++) {
			char c = memory.charAt(i);
			if (c == 'm' && i != firstCard && figures.get(i) == figures.get(firstCard)) {
				index = i;
			}
		}

		if (index < 0) {
			List<Integer> notMemorized = getNotMemorized();
			int rIndex = getRandomIndexFromList(notMemorized);
			index = notMemorized.get(rIndex);
		}

		sendMove(index);
	}

	private List<Integer> getNotMemorized() {
		return getIndicesOf('0');
	}

	private List<Integer> getMemorized() {
		return getIndicesOf('m');
	}

	private List<Integer> getIndicesOf(final char c) {

		return IntStream.range(0, memory.length()).filter(i -> memory.charAt(i) == c).boxed()
				.collect(Collectors.toList());
	}
}
