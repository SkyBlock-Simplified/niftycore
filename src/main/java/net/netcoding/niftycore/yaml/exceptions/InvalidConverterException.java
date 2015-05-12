package net.netcoding.niftycore.yaml.exceptions;

@SuppressWarnings("serial")
public class InvalidConverterException extends RuntimeException {

	public InvalidConverterException() { }

	public InvalidConverterException(String message) {
		super(message);
	}

	public InvalidConverterException(Throwable cause) {
		super(cause);
	}

	public InvalidConverterException(String message, Throwable cause) {
		super(message, cause);
	}

}