package net.netcoding.nifty.core.util.misc;

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

	public final Socket accept() throws IOException {
		return this.serverSocket.accept();
	}

	public final ServerSocketChannel getChannel() {
		return this.serverSocket.getChannel();
	}

	public final InetAddress getInetAddress() {
		return this.serverSocket.getInetAddress();
	}

	public final int getLocalPort() {
		return this.serverSocket.getLocalPort();
	}

	public final SocketAddress getLocalSocketAddress() {
		return this.serverSocket.getLocalSocketAddress();
	}

	public final Socket getSocket() throws IOException {
		return this.serverSocket.accept();
	}

	public final String getSocketAddress() {
		return this.serverSocket.getInetAddress().getHostAddress();
	}

	public final int getSoTimeout() throws IOException {
		return this.serverSocket.getSoTimeout();
	}

	public final boolean isClosed() {
		return this.serverSocket == null || this.serverSocket.isClosed();
	}

	public final boolean isSocketListening() {
		return !this.isClosed();
	}

	public final void setReuseAddress(boolean on) throws SocketException {
		this.serverSocket.setReuseAddress(on);
	}

	public final void setSoTimeout(int timeout) throws SocketException {
		this.serverSocket.setSoTimeout(timeout);
	}

}