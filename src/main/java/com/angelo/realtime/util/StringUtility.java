package com.angelo.realtime.util;

public final class StringUtility {

	private StringUtility() {
		
	}
	
	public static String replaceWithCharAt(final String string, final char c, final int index) {
		return  string.substring(0, index) + c + string.substring(index + 1);
	}
}
