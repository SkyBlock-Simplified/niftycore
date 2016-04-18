package net.netcoding.niftycore.mojang;

import net.netcoding.niftycore.minecraft.MinecraftServer;
import net.netcoding.niftycore.util.json.JsonMessage;

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

	@Override
	public void sendMessage(JsonMessage message) throws Exception {
		throw new UnsupportedOperationException("Improperly defined repository resulted in basic profiles!");
	}

	@Override
	public void sendMessage(String message) {
		throw new UnsupportedOperationException("Improperly defined repository resulted in basic profiles!");
	}

}