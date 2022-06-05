package com.angelo.controller.exception;

public enum ExceptionReason {

	ALREADY_FOLLOWING_REASON("Already following"), 
	FOLLOW_YOURSELF_REASON("Can't follow yourself"),
	NOT_FOUND_REASON("User not found"), 
	USERNAME_NOT_AVAILABLE("Username not available");

	private final String value;

	private ExceptionReason(final String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return this.value;
	}
}
