package com.aucobo.helpers.exceptions;

/**
 * Is thrown when a rabbitmq queue
 * does not exist but is expected to exist.
 * 
 * @author Norman Möschter-Schenck
 * @version 0.0.1
 * @since 2017-11-14
 */
public class NoQueueFoundException extends Exception {
	private static final long serialVersionUID = 1L;

	public NoQueueFoundException() {

    }

    public NoQueueFoundException(String message) {
        super(message);
    }
}
