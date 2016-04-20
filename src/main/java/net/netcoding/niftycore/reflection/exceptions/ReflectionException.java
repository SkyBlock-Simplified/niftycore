package net.netcoding.niftycore.reflection.exceptions;

public class ReflectionException extends RuntimeException {

	public ReflectionException(Throwable throwable) {
		super(throwable);
	}

	public ReflectionException(String message) {
		super(message);
	}

}