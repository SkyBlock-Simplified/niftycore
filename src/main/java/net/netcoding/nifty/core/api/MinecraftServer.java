package net.netcoding.nifty.core.api;

import net.netcoding.nifty.core.mojang.MojangProfile;
import net.netcoding.nifty.core.util.concurrent.ConcurrentSet;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public abstract class MinecraftServer<T extends MojangProfile> implements Server<T> {

	protected InetSocketAddress address;
	protected int maxPlayers = 0;
	protected String motd = "";
	protected boolean online = false;
	protected final ConcurrentSet<T> playerList = new ConcurrentSet<>();
	protected String serverName = "";
	protected Version version = Version.DEFAULT;

	protected MinecraftServer() { }

	@Override
	public final boolean equals(Object obj) {
		if (obj != null) {
			if (this == obj)
				return true;
			else if (MinecraftServer.class.isAssignableFrom(obj.getClass())) {
				MinecraftServer server = (MinecraftServer)obj;

				if (server.getAddress().getAddress().getHostAddress().equals(this.getAddress().getAddress().getHostAddress())) {
					if (server.getAddress().getPort() == this.getAddress().getPort())
						return true;
				}
			}
		}

		return false;
	}

	@Override
	public final InetSocketAddress getAddress() {
		return this.address;
	}

	@Override
	public final int getMaxPlayers() {
		return this.maxPlayers;
	}

	@Override
	public final String getMotd() {
		return this.motd;
	}

	@Override
	public final String getName() {
		return this.serverName;
	}

	@Override
	public final T getPlayer(String name) {
		for (T profile : this.getPlayerList()) {
			if (profile.getName().equalsIgnoreCase(name))
				return profile;
		}

		return null;
	}

	@Override
	public final T getPlayer(UUID uniqueId) {
		for (T profile : this.getPlayerList()) {
			if (profile.getUniqueId().equals(uniqueId))
				return profile;
		}

		return null;
	}

	@Override
	public final int getPlayerCount() {
		return this.playerList.size();
	}

	@Override
	public Collection<T> getPlayerList() {
		return Collections.unmodifiableCollection(this.playerList);
	}

	@Override
	public final Version getVersion() {
		return this.version;
	}

	@Override
	public final int hashCode() {
		return 31 * (this.getAddress() == null ? super.hashCode() : this.getAddress().hashCode());
	}

	@Override
	public final boolean isOnlineMode() {
		return this.online;
	}

	protected void reset() {
		this.motd = "";
		this.maxPlayers = 0;
		this.playerList.clear();
		this.version = Version.DEFAULT;
	}

	protected void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	protected void setMotd(String motd) {
		this.motd = motd;
	}

	protected void setOnline(boolean online) {
		this.online = online;
	}

	protected void setVersion(String name, int protocol) {
		this.version = new Version(name, protocol);
	}

}