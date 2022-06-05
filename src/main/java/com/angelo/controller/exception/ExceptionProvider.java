package com.angelo.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ExceptionProvider {

	public ResponseStatusException provideNotFoundException() {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, ExceptionReason.NOT_FOUND_REASON.toString());
	}
	
	public ResponseStatusException provideConflictException(final ExceptionReason reason) {
		return new ResponseStatusException(HttpStatus.CONFLICT, reason.toString());
	}
}
