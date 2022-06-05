package com.angelo.realtime.game;

import java.util.ArrayList;
import com.angelo.realtime.util.DateUtility;

import lombok.Getter;
import lombok.Setter;

import static com.angelo.realtime.util.RandomUtility.*;

@Getter
@Setter
public class GameSession {

	private static final int ALL_FIGURE_COUNT = 60;
	private static final ArrayList<Integer> ALL_FIGURE;
	static {
		ALL_FIGURE = new ArrayList<Integer>();
		for (int i = 0; i < ALL_FIGURE_COUNT; i++) {
			ALL_FIGURE.add(i);
		}
	}

	private final ArrayList<Integer> gameFigures = new ArrayList<Integer>();
	private final int level;
	private final long gameSessionId;
	private String played = "";
	private long mainTimeStamp = -1;
	private long opponentTimeStamp = -1;
	private int endGame = -1;
	private int turn = -1;
	private long booked = 0;

	public GameSession(int level, long booked) {

		this.level = level;
		this.booked = booked;
		gameSessionId = DateUtility.getCurrentTimestamp();
		initializeRandomFigures();

	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GameSession other = (GameSession) obj;
		if (gameSessionId != other.gameSessionId)
			return false;
		return true;
	}

	private void initializeRandomFigures() {
		ArrayList<Integer> allCopy = new ArrayList<Integer>();
		ALL_FIGURE.forEach(val -> {
			allCopy.add(val);
		});
		ArrayList<Integer> figures = new ArrayList<Integer>();
		ArrayList<Integer> duplicatedIndex = new ArrayList<Integer>();
		ArrayList<Integer> duplicatedFigures = new ArrayList<Integer>();

		for (int j = 0; j < level; j++) {
			duplicatedIndex.add(j);
			int rIndex = getRandomIndexFromList(allCopy);
			figures.add(allCopy.get(rIndex));
			allCopy.remove(rIndex);
		}

		for (int j = 0; j < level; j++) {

			int rIndex = getRandomIndexFromList(duplicatedIndex);
			duplicatedFigures.add(figures.get(duplicatedIndex.get(rIndex)));
			duplicatedIndex.remove(rIndex);
			gameFigures.add(figures.get(j));
			gameFigures.add(duplicatedFigures.get(j));
			played = played + "00";// aggiungo uno 0 per ogni carta
		}
	}
}
