package net.netcoding.niftycore.minecraft.ping;

import net.netcoding.niftycore.minecraft.MinecraftServer;

import com.google.gson.Gson;

public abstract class MinecraftPingServer extends MinecraftServer {

	protected static final transient Gson GSON = new Gson();
	private final transient MinecraftPingListener listener;

	public MinecraftPingServer(MinecraftPingListener listener) {
		this.listener = listener;
	}

	protected final MinecraftPingListener getListener() {
		return this.listener;
	}

	public abstract void onPing();

	public abstract void ping();

}