package net.netcoding.niftycore.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;

public class ServerSocketWrapper {

	private final ServerSocket serverSocket;

	public ServerSocketWrapper(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public Socket accept() throws IOException {
		return this.serverSocket.accept();
	}

	public ServerSocketChannel getChannel() {
		return this.serverSocket.getChannel();
	}

	public InetAddress getInetAddress() {
		return this.serverSocket.getInetAddress();
	}

	public int getLocalPort() {
		return this.serverSocket.getLocalPort();
	}

	public SocketAddress getLocalSocketAddress() {
		return this.serverSocket.getLocalSocketAddress();
	}

	public Socket getSocket() throws IOException {
		return this.serverSocket.accept();
	}

	public String getSocketAddress() {
		return this.serverSocket.getInetAddress().getHostAddress();
	}

	public int getSoTimeout() throws IOException {
		return this.serverSocket.getSoTimeout();
	}

	public boolean isClosed() {
		return this.serverSocket == null || this.serverSocket.isClosed();
	}

	public boolean isSocketListening() {
		return !this.isClosed();
	}

	public void setReuseAddress(boolean on) throws SocketException {
		this.serverSocket.setReuseAddress(on);
	}

	public void setSoTimeout(int timeout) throws SocketException {
		this.serverSocket.setSoTimeout(timeout);
	}

}