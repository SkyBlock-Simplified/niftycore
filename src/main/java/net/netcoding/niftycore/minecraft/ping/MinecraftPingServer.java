package net.netcoding.niftycore.minecraft.ping;

import net.netcoding.niftycore.minecraft.MinecraftServer;
import net.netcoding.niftycore.mojang.MojangProfile;

import com.google.gson.Gson;

public abstract class MinecraftPingServer<T extends MojangProfile> extends MinecraftServer<T> {

	protected static final transient Gson GSON = new Gson();
	private final transient MinecraftPingListener<T> listener;

	public MinecraftPingServer(MinecraftPingListener<T> listener) {
		this.listener = listener;
	}

	protected final MinecraftPingListener<T> getListener() {
		return this.listener;
	}

	public abstract void onPing();

	public abstract void ping();

}