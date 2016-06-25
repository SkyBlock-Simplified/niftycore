package net.netcoding.nifty.core.http;

public class HttpBody {

	public static final HttpBody EMPTY = new HttpBody(null);
	private final String bodyString;

	public HttpBody(String bodyString) {
		this.bodyString = bodyString;
	}

	public byte[] getBytes() {
		return bodyString != null ? bodyString.getBytes() : new byte[0];
	}

	@Override
	public String toString() {
		return this.bodyString;
	}

}