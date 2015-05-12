package net.netcoding.niftycore.minecraft.ping;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.google.common.io.ByteArrayDataOutput;

import net.netcoding.niftycore.minecraft.MinecraftServer;
import net.netcoding.niftycore.minecraft.scheduler.MinecraftScheduler;
import net.netcoding.niftycore.mojang.MojangProfile;
import net.netcoding.niftycore.util.DataUtil;
import net.netcoding.niftycore.util.gson.Gson;

public class BukkitServer extends MinecraftServer {

	private static final transient Gson GSON = new Gson();
	private transient int socketTimeout = 2000;
	private final transient BukkitServerPingListener listener;

	public BukkitServer(String ip, BukkitServerPingListener listener) {
		this(ip, 25565, listener);
	}

	public BukkitServer(String ip, int port, BukkitServerPingListener listener) {
		this(new InetSocketAddress(ip, port), listener);
	}

	public BukkitServer(InetSocketAddress address, BukkitServerPingListener listener) {
		this.setAddress(address);
		this.listener = listener;
	}

	public int getSocketTimeout() {
		return this.socketTimeout;
	}

	public void ping() {
		if (this.getAddress() == null) return;

		try {
			MinecraftScheduler.runAsync(new Runnable() {
				@Override
				public void run() {
					try (Socket socket = new Socket()) {
						socket.setSoTimeout(getSocketTimeout());
						socket.connect(getAddress(), getSocketTimeout());

						try (OutputStream outputStream = socket.getOutputStream()) {
							try (DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
								DataUtil.writeByteArray(dataOutputStream, prepareHandshake());
								DataUtil.writeByteArray(dataOutputStream, preparePing());

								try (InputStream inputStream = socket.getInputStream()) {
									try (DataInputStream dataInputStream = new DataInputStream(inputStream)) {
										StatusResponse response = processResponse(dataInputStream);

										setVersion(response.getVersion().getName(), response.getVersion().getProtocol());
										setMotd(response.getMotd());
										setMaxPlayers(response.getPlayers().getMax());
										setOnline(true);
										playerList.clear();
										StatusResponse.Players players = response.getPlayers();

										if (players != null) {
											if (players.getSample() != null)
												playerList.addAll(Arrays.asList(GSON.fromJson(GSON.toJson(players.getSample()), MojangProfile[].class)));
										}
									}
								}
							}
						}
					} catch (Exception ex) {
						setOnline(false);
						reset();
					} finally {
						if (listener != null)
							listener.onPing(BukkitServer.this);
					}
				}
			});
		} catch (Exception ex) {
			this.setOnline(false);
			this.reset();

			if (listener != null)
				listener.onPing(BukkitServer.this);
		}
	}

	public void setAddress(String ip, int port) {
		this.setAddress(new InetSocketAddress(ip, port));
	}

	public void setAddress(InetSocketAddress address) {
		this.address = address;
	}

	public void setName(String serverName) {
		this.serverName = serverName;
	}

	public void setSocketTimeout(int timeout) {
		this.socketTimeout = timeout;
	}

    private byte[] preparePing() throws IOException {
        return new byte[] { 0x00 };
    }

    private byte[] prepareHandshake() throws IOException {
    	ByteArrayDataOutput handshake = DataUtil.newDataOutput();
		handshake.writeByte(0x00);
		DataUtil.writeVarInt(handshake, 4);
		DataUtil.writeString(handshake, getAddress().getHostString());
		handshake.writeShort(getAddress().getPort());
		DataUtil.writeVarInt(handshake, 1);
		return handshake.toByteArray();
    }

    private StatusResponse processResponse(DataInputStream input) throws IOException {
		DataUtil.readVarInt(input); // Packet Size

		int id = DataUtil.readVarInt(input); // Packet ID
		if (id != 0) throw new IOException("Invalid packetID.");

		int length = DataUtil.readVarInt(input); // Packet Length
		if (length < 1) throw new IOException("Invalid string length.");

		byte[] data = new byte[length];
		input.readFully(data);
		return GSON.fromJson(new String(data, StandardCharsets.UTF_8), StatusResponse.class);
    }

}