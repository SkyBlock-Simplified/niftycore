package net.netcoding.nifty.core.http;

public class HttpResponse {

	private final HttpBody body;
	private final HttpStatus status;

	public HttpResponse(HttpStatus status, HttpBody body) {
		this.body = body;
		this.status = status;
	}

	public HttpBody getBody() {
		return this.body;
	}

	public HttpStatus getStatus() {
		return this.status;
	}

}