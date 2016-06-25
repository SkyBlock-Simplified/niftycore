package net.netcoding.nifty.core.http;

import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.core.http.exceptions.HttpConnectionException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class HttpClient {

	private static final int DEFAULT_TIMEOUT = 3000;

	public static HttpResponse get(URL url, HttpHeader... headers) throws HttpConnectionException {
		return get(url, DEFAULT_TIMEOUT, Arrays.asList(headers));
	}

	public static HttpResponse get(URL url, int timeout, HttpHeader... headers) throws HttpConnectionException {
		return get(url, timeout, Arrays.asList(headers));
	}

	public static HttpResponse get(URL url, Proxy proxy, HttpHeader... headers) throws HttpConnectionException {
		return get(url, DEFAULT_TIMEOUT, proxy, Arrays.asList(headers));
	}

	public static HttpResponse get(URL url, int timeout, Proxy proxy, HttpHeader... headers) throws HttpConnectionException {
		return get(url, timeout, proxy, Arrays.asList(headers));
	}

	public static HttpResponse get(URL url, List<HttpHeader> headers) throws HttpConnectionException {
		return get(url, null, headers);
	}

	public static HttpResponse get(URL url, int timeout, List<HttpHeader> headers) throws HttpConnectionException {
		return get(url, timeout, null, headers);
	}

	public static HttpResponse get(URL url, Proxy proxy, List<HttpHeader> headers) throws HttpConnectionException {
		return get(url, DEFAULT_TIMEOUT, proxy, headers);
	}

	public static HttpResponse get(URL url, int timeout, Proxy proxy, List<HttpHeader> headers) throws HttpConnectionException {
		HttpStatus status;
		HttpBody response;

		try {
			HttpURLConnection connection = (HttpURLConnection)url.openConnection(proxy == null ? Proxy.NO_PROXY : proxy);

			for (HttpHeader header : headers)
				connection.setRequestProperty(header.getName(), header.getValue());

			connection.setRequestMethod("GET");
			connection.setConnectTimeout(timeout);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			StringBuilder buffer = new StringBuilder();
			status = HttpStatus.getByCode(connection.getResponseCode());
			InputStream stream = (status.getCode() < 400) ? connection.getInputStream() : connection.getErrorStream();

			try (InputStreamReader streamReader = new InputStreamReader(stream)) {
				try (BufferedReader reader = new BufferedReader(streamReader)) {
					String line;

					while (StringUtil.notEmpty(line = reader.readLine())) {
						buffer.append(line);
						buffer.append('\n');
					}
				}
			}

			response = new HttpBody(buffer.toString());

			if (connection.getResponseCode() >= 400)
				throw new HttpConnectionException(status, response);
		} catch (HttpConnectionException hcex) {
			throw hcex;
		} catch (SocketException sex) {
			throw new HttpConnectionException(HttpStatus.SOCKET_ERROR, sex);
		} catch (IOException ioex) {
			throw new HttpConnectionException(HttpStatus.IO_ERROR, ioex);
		} catch (Exception ex) {
			throw new HttpConnectionException(ex);
		}

		return new HttpResponse(status, response);
	}

	public static HttpResponse post(URL url, HttpHeader... headers) throws HttpConnectionException {
		return post(url, DEFAULT_TIMEOUT, headers);
	}

	public static HttpResponse post(URL url, int timeout, HttpHeader... headers) throws HttpConnectionException {
		return post(url, null, null, timeout, headers);
	}

	public static HttpResponse post(URL url, HttpBody body, HttpHeader... headers) throws HttpConnectionException {
		return post(url, null, body, DEFAULT_TIMEOUT, headers);
	}

	public static HttpResponse post(URL url, Proxy proxy, HttpHeader... headers) throws HttpConnectionException {
		return post(url, proxy, null, DEFAULT_TIMEOUT, headers);
	}

	public static HttpResponse post(URL url, HttpBody body, int timeout, HttpHeader... headers) throws HttpConnectionException {
		return post(url, null, body, timeout, headers);
	}

	public static HttpResponse post(URL url, Proxy proxy, int timeout, HttpHeader... headers) throws HttpConnectionException {
		return post(url, proxy, null, timeout, headers);
	}

	public static HttpResponse post(URL url, Proxy proxy, HttpBody body, int timeout, HttpHeader... headers) throws HttpConnectionException {
		return post(url, proxy, body, timeout, Arrays.asList(headers));
	}

	public static HttpResponse post(URL url, List<HttpHeader> headers) throws HttpConnectionException {
		return post(url, null, null, DEFAULT_TIMEOUT, headers);
	}

	public static HttpResponse post(URL url, int timeout, List<HttpHeader> headers) throws HttpConnectionException {
		return post(url, null, null, timeout, headers);
	}

	public static HttpResponse post(URL url, Proxy proxy, List<HttpHeader> headers) throws HttpConnectionException {
		return post(url, proxy, null, DEFAULT_TIMEOUT, headers);
	}

	public static HttpResponse post(URL url, HttpBody body, List<HttpHeader> headers) throws HttpConnectionException {
		return post(url, null, body, DEFAULT_TIMEOUT, headers);
	}

	public static HttpResponse post(URL url, Proxy proxy, int timeout, List<HttpHeader> headers) throws HttpConnectionException {
		return post(url, proxy, null, timeout, headers);
	}

	public static HttpResponse post(URL url, HttpBody body, int timeout, List<HttpHeader> headers) throws HttpConnectionException {
		return post(url, null, body, timeout, headers);
	}

	public static HttpResponse post(URL url, Proxy proxy, HttpBody body, int timeout, List<HttpHeader> headers) throws HttpConnectionException {
		HttpStatus status;
		HttpBody response;

		try {
			StringBuilder buffer = new StringBuilder();
			HttpURLConnection connection = (HttpURLConnection)url.openConnection(proxy == null ? Proxy.NO_PROXY : proxy);

			for (HttpHeader header : headers)
				connection.setRequestProperty(header.getName(), header.getValue());

			connection.setRequestMethod("POST");
			connection.setConnectTimeout(timeout);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			if (body != null && body.getBytes().length > 0) {
				DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
				writer.write(body.getBytes());
			}

			status = HttpStatus.getByCode(connection.getResponseCode());
			InputStream stream = (status.getCode() < 400) ? connection.getInputStream() : connection.getErrorStream();

			try (InputStreamReader streamReader = new InputStreamReader(stream)) {
				try (BufferedReader reader = new BufferedReader(streamReader)) {
					String line;

					while (StringUtil.notEmpty(line = reader.readLine())) {
						buffer.append(line);
						buffer.append('\n');
					}
				}
			}

			response = new HttpBody(buffer.toString());

			if (connection.getResponseCode() >= 400)
				throw new HttpConnectionException(status, response);
		} catch (HttpConnectionException hcex) {
			throw hcex;
		} catch (SocketException sex) {
			throw new HttpConnectionException(HttpStatus.SOCKET_ERROR, sex);
		} catch (IOException ioex) {
			throw new HttpConnectionException(HttpStatus.IO_ERROR, ioex);
		} catch (Exception ex) {
			throw new HttpConnectionException(ex);
		}

		return new HttpResponse(status, response);
	}

}