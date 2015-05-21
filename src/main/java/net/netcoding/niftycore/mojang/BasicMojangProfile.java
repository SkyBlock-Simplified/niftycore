package net.netcoding.niftycore.mojang;

import net.netcoding.niftycore.minecraft.MinecraftServer;

public class BasicMojangProfile extends MojangProfile {

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public MinecraftServer<? extends MojangProfile> getServer() {
		return null;
	}

	@Override
	public boolean isOnlineAnywhere() {
		return false;
	}

}