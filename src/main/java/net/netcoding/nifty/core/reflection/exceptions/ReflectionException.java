package net.netcoding.nifty.core.reflection.exceptions;

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