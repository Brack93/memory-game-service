package com.angelo.realtime.player.model;

public class MessageWrapperHelper {

	private static final int REFUSING_MESSAGE_LENGTH = 4;
	private static final int BOOKING_MESSAGE_LENGTH = 5;
	private static final int TO_ID_INDEX = 1;
	
	public static boolean isBookingMessage(final MessagesWrapper messagesWrapper) {
		return messagesWrapper.getMessages().length == BOOKING_MESSAGE_LENGTH 
				&& messagesWrapper.getMessages()[0].equals(Messages.BOOKING.toString());
	}
	
	public static boolean isRefusingMessage(final MessagesWrapper messagesWrapper) {
		return messagesWrapper.getMessages().length == REFUSING_MESSAGE_LENGTH 
				&& messagesWrapper.getMessages()[0].equals(Messages.REFUSED.toString());
	}

	public static long getToId(final String[] messages) {
		return Long.parseLong(messages[TO_ID_INDEX]);
	}
	
	public static void printMessages(final MessagesWrapper messagesWrapper) {
		for (String string : messagesWrapper.getMessages()) {
			System.out.println(string);
		}
	}
	
}
