package net.netcoding.nifty.core.mojang;

import net.netcoding.nifty.core.api.MinecraftServer;
import net.netcoding.nifty.core.util.json.JsonMessage;

public class BasicMojangProfile extends MojangProfile<BasicMojangProfile> {

	@Override
	public MinecraftServer<? extends MojangProfile> getServer() {
		throw new UnsupportedOperationException("Improperly defined repository resulted in basic profiles!");
	}

	@Override
	public boolean isOnline() {
		throw new UnsupportedOperationException("Improperly defined repository resulted in basic profiles!");
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