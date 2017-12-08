package com.aucobo.helpers.rabbitmq.exceptions;

/**
 * Is thrown when the amqp admin
 * could not be auowired.
 * 
 * @author Norman Moeschter-Schenck
 * @version 0.0.1
 * @since 2017-12-08
 */
public class NoRabbitAdminException extends Exception {
	private static final long serialVersionUID = 1L;

	public NoRabbitAdminException() {

    }

    public NoRabbitAdminException(String message) {
        super(message);
    }
}
