package net.netcoding.niftycore.minecraft.ping;

import net.netcoding.niftycore.minecraft.MinecraftServer;
import net.netcoding.niftycore.mojang.MojangProfile;

public interface MinecraftPingListener<T extends MojangProfile> {

	void onPing(MinecraftServer<T> server);

}