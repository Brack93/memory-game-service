package com.angelo.realtime.util;

import java.util.List;

public final class RandomUtility {

	private RandomUtility( ) {
		
	}
	
	public static <T> int getRandomIndexFromList(final List<T> list) {
		return (int) Math.floor(Math.random() * list.size());
	}
}
