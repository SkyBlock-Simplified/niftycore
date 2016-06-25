package net.netcoding.nifty.core.api.ping;

import net.netcoding.nifty.core.api.MinecraftServer;
import net.netcoding.nifty.core.mojang.MojangProfile;

public interface MinecraftPingListener<T extends MojangProfile> {

	void onPing(MinecraftServer<T> server);

}