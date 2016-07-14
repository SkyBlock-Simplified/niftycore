package net.netcoding.nifty.core.reflection.exceptions;

import net.netcoding.nifty.core.reflection.Reflection;

/**
 * {@link ReflectionException ReflectionExceptions} are thrown when the {@link Reflection} class is unable<br>
 * to perform a specific action.
 */
public final class ReflectionException extends RuntimeException {

	public ReflectionException(String message) {
		super(message);
	}

	public ReflectionException(Throwable throwable) {
		super(throwable);
	}

	public ReflectionException(String message, Throwable throwable) {
		super(message, throwable);
	}

}