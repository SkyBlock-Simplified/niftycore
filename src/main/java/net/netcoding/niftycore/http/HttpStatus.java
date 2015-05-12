package net.netcoding.niftycore.http;

public enum HttpStatus {

	CONTINUE(100, "Continue"),
	SWITCHING_PROTOCOLS(101, "Switching Protocols"),
	PROCESSING(102, "Processing"),
	OK(200, "OK"),
	CREATED(201, "Created"),
	ACCEPTED(202, "Accepted"),
	NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
	NO_CONTENT(204, "No Content"),
	RESET_CONTENT(205, "Reset Content"),
	PARTIAL_CONTENT(206, "Partial Content"),
	MULTI_STATUS(207, "Multi-Status"),
	ALREADY_REPORTED(208, "Already Reported"),
	IM_USED(226, "IM Used"),
	MULTIPLE_CHOICES(300, "Multiple Choices"),
	MOVED_PERMANENTLY(301, "Moved Permanently"),
	FOUND(302, "Found"),
	SEE_OTHER(303, "See Other"),
	NOT_MODIFIED(304, "Not Modified"),
	USE_PROXY(305, "Use Proxy"),
	SWITCH_PROXY(306, "Switch Proxy"),
	TEMPORARY_REDIRECT(307, "Temporary Redirect"),
	PERMANENT_REDIRECT(308, "Permanent Redirect"),
	BAD_REQUEST(400, "Bad Request"),
	UNAUTHORIZED(401, "Unauthorized"),
	PAYMENT_REQUIRED(402, "Payment Required"),
	FORBIDDEN(403, "Forbidden"),
	NOT_FOUND(404, "Not Found"),
	METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
	NOT_ACCEPTABLE(406, "Not Acceptable"),
	PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
	REQUEST_TIMEOUT(408, "Request Timeout"),
	CONFLICT(409, "Conflict"),
	GONE(410, "Gone"),
	LENGTH_REQUIRED(411, "Length Required"),
	PRECONDITION_FAILED(412, "Precondition Failed"),
	REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
	REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),
	UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
	REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
	EXPECTATION_FAILED(417, "Expectation Failed"),
	IM_A_TEAPOT(418, "I'm a teapot"),
	AUTHENTICATION_TIMEOUT(419, "Authentication Timeout"),
	METHOD_FAILURE(420, "Method Failure"),
	UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
	LOCKED(423, "Locked"),
	FAILED_DEPENDENCY(424, "Failed Dependency"),
	UPGRADE_REQUIRED(426, "Upgrade Required"),
	PRECONDITION_REQUIRED(428, "Precondition Required"),
	TOO_MANY_REQUESTS(429, "Too Many Requests"),
	REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
	LOGIN_TIMEOUT(440, "Login Timeout"),
	NO_RESPONSE(444, "No Response"),
	RETRY_WITH(449, "Retry With"),
	BLOCKED_BY_WINDOWS_PARENTAL_CONTROLS(450, "Blocked by Windows Parental Controls"),
	UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"),
	REDIRECT(451, "Redirect"),
	REQUEST_HEADER_TOO_LARGE(494, "Request Header Too Large"),
	CERT_ERROR(495, "Cert Error"),
	NO_CERT(496, "No Cert"),
	HTTP_TO_HTTPS(497, "HTTP to HTTPS"),
	TOKEN_EXPIRED_INVALID(498, "Token expired/invalid"),
	CLIENT_CLOSED_REQUEST(499, "Client Closed Request"),
	INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
	NOT_IMPLEMENTED(501, "Not Implemented"),
	BAD_GATEWAY(502, "Bad Gateway"),
	SERVICE_UNAVAILABLE(503, "Service Unavailable"),
	GATEWAY_TIMEOUT(504, "Gateway Timeout"),
	HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
	VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiate"),
	INSUFFICIENT_STORAGE(507, "Insufficient Storage"),
	LOOP_DETECTED(508, "Loop Detected"),
	BANDWIDTH_LIMIT_EXCEEDED(509, "Bandwidth Limit Exceeded"),
	NOT_EXTENDED(510, "Not Extended"),
	NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required"),
	NETWORK_READ_TIMEOUT_ERROR(598, "Network read timeout error (Unknown)"),
	NETWORK_CONNECT_TIMEOUT_ERROR(599, "Network connect timeout error (Unknown)"),
	SOCKET_ERROR(990, "Socket Error"),
	IO_ERROR(991, "IO Error"),
	UNKNOWN_ERROR(999, "Unknown Error");

	private final int code;
	private final String message;

	private HttpStatus(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return this.code;
	}

	public String getMessage() {
		return this.message;
	}

	public static HttpStatus getByCode(int code) {
		for (HttpStatus httpCode : HttpStatus.values()) {
			if (httpCode.getCode() == code)
				return httpCode;
		}

		return HttpStatus.OK;
	}

	public HttpState getState() {
		if (this.getCode() >= 990)
			return HttpState.JAVA_ERROR;
		if (this.getCode() >= 500)
			return HttpState.SERVER_RROR;
		else if (this.getCode() >= 400)
			return HttpState.CLIENT_ERROR;
		else if (this.getCode() >= 300)
			return HttpState.REDIRECTION;
		else if (this.getCode() >= 200)
			return HttpState.SUCCESS;
		else if (this.getCode() >= 100)
			return HttpState.INFORMATIONAL;

		return HttpState.OTHER;
	}

}