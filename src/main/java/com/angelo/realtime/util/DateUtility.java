package com.angelo.realtime.util;

public final class DateUtility {

	private DateUtility() {
	}

	public static java.sql.Date getCurrentSqlDate() {
		
		java.util.Date utilDate = new java.util.Date();
		return new java.sql.Date(utilDate.getTime());
	}

	public static boolean isTimeOut(long seconds, long timeStamp) {
		
		if (timeStamp > 0) {
			java.util.Date utilDate = new java.util.Date();
			long diff = utilDate.getTime() - timeStamp;
			System.out.println("timeOut millisecond diff: " + diff);
			return diff > seconds * 1000;
		} else {
			System.out.println("timeOut negativo");
			return false;
		}
	}

	public static long getCurrentTimestamp() {
		return new java.util.Date().getTime();
	}
}
