package net.netcoding.nifty.core.api;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.UUID;

public interface Server<P> {

	InetSocketAddress getAddress();

	int getMaxPlayers();

	String getMotd();

	String getName();

	P getPlayer(String name);

	P getPlayer(UUID uniqueId);

	int getPlayerCount();

	Collection<? extends P> getPlayerList();

	Version getVersion();

	boolean isOnlineMode();

	class Version {

		static final Version DEFAULT = new Version("", 0);
		private final String name;
		private final int protocol;

		public Version(String name, int protocol) {
			this.name = name;
			this.protocol = protocol;
		}

		public String getName() {
			return this.name;
		}

		public int getProtocol() {
			return this.protocol;
		}

	}

}