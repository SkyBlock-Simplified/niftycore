package net.netcoding.nifty.core.yaml.exceptions;

@SuppressWarnings("serial")
public class InvalidConfigurationException extends RuntimeException {

	public InvalidConfigurationException() { }

	public InvalidConfigurationException(String message) {
		super(message);
	}

	public InvalidConfigurationException(Throwable cause) {
		super(cause);
	}

	public InvalidConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

}