package net.netcoding.nifty.core.api.ping;

import net.netcoding.nifty.core.api.MinecraftServer;
import net.netcoding.nifty.core.mojang.MojangProfile;

import com.google.gson.Gson;

public abstract class PingServer<T extends MojangProfile> extends MinecraftServer<T> {

	protected static final transient Gson GSON = new Gson();
	private final transient MinecraftPingListener<T> listener;

	public PingServer(MinecraftPingListener<T> listener) {
		this.listener = listener;
	}

	protected final MinecraftPingListener<T> getListener() {
		return this.listener;
	}

	public abstract void onPing();

	public abstract void ping();

}