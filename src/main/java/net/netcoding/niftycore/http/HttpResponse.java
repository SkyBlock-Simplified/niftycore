package net.netcoding.niftycore.http;

public class HttpResponse {

	private final HttpStatus status;
	private final HttpBody body;

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