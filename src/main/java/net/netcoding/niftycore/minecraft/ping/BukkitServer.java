package net.netcoding.niftycore.minecraft.ping;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import net.netcoding.niftycore.minecraft.MinecraftServer;
import net.netcoding.niftycore.minecraft.scheduler.MinecraftScheduler;
import net.netcoding.niftycore.mojang.BasicMojangProfile;
import net.netcoding.niftycore.mojang.MojangProfile;
import net.netcoding.niftycore.util.DataUtil;

import com.google.common.io.ByteArrayDataOutput;

public class BukkitServer<T extends MojangProfile> extends MinecraftPingServer<T> {

	private transient int socketTimeout = 2000;

	public BukkitServer(String ip, MinecraftPingListener<T> listener) {
		this(ip, 25565, listener);
	}

	public BukkitServer(String ip, int port, MinecraftPingListener<T> listener) {
		this(new InetSocketAddress(ip, port), listener);
	}

	public BukkitServer(InetSocketAddress address, MinecraftPingListener<T> listener) {
		super(listener);
		this.setAddress(address);
	}

	protected int getSocketTimeout() {
		return this.socketTimeout;
	}

	@SuppressWarnings("unchecked")
	protected final Class<T> getSuperClass() {
		ParameterizedType superClass = (ParameterizedType)this.getClass().getGenericSuperclass();
		return (Class<T>)(superClass.getActualTypeArguments().length == 0 ? BasicMojangProfileOverride.class : superClass.getActualTypeArguments()[0]);
	}

	@SuppressWarnings("unchecked")
	protected final Class<T[]> getSuperClassArray() {
		ParameterizedType superClass = (ParameterizedType)this.getClass().getGenericSuperclass();
		return (Class<T[]>)(superClass.getActualTypeArguments().length == 0 ? BasicMojangProfileOverride[].class : superClass.getActualTypeArguments());
	}

	@Override
	public void onPing() {
		if (this.getListener() != null)
			this.getListener().onPing(this);
	}

	@Override
	public void ping() {
		if (this.getAddress() == null) return;

		try {
			MinecraftScheduler.runAsync(new Runnable() {
				@SuppressWarnings("unchecked")
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
												playerList.addAll(Arrays.asList(GSON.fromJson(GSON.toJson(players.getSample()), getSuperClassArray())));
										}

										if (getSuperClass().isAssignableFrom(BasicMojangProfileOverride.class)) {
											for (T player : playerList) {
												BasicMojangProfileOverride profile = (BasicMojangProfileOverride)player;
												profile.setServer(BukkitServer.this);
											}
										}
									}
								}
							}
						}
					} catch (Exception ex) {
						setOnline(false);
						reset();
					} finally {
						onPing();
					}
				}
			});
		} catch (Exception ex) {
			this.setOnline(false);
			this.reset();
			this.onPing();
		}
	}

	protected void setAddress(String ip, int port) {
		this.setAddress(new InetSocketAddress(ip, port));
	}

	protected void setAddress(InetSocketAddress address) {
		this.address = address;
	}

	protected void setSocketTimeout(int timeout) {
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

	private class BasicMojangProfileOverride extends BasicMojangProfile {

		private MinecraftServer<? extends MojangProfile> server;

		@Override
		public MinecraftServer<? extends MojangProfile> getServer() {
			return this.server;
		}

		void setServer(MinecraftServer<? extends MojangProfile> server) {
			this.server = server;
		}

	}

}