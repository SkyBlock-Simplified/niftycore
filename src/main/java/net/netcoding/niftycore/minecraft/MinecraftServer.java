package net.netcoding.niftycore.minecraft;

import net.netcoding.niftycore.mojang.MojangProfile;
import net.netcoding.niftycore.util.concurrent.ConcurrentSet;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;

public abstract class MinecraftServer<T extends MojangProfile> {

	protected InetSocketAddress address;
	protected int maxPlayers = 0;
	protected String motd = "";
	protected boolean online = false;
	protected final ConcurrentSet<T> playerList = new ConcurrentSet<>();
	protected String serverName = "";
	protected MinecraftVersion version = MinecraftVersion.DEFAULT;

	protected MinecraftServer() { }

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof MinecraftServer)) return false;
		if (this == obj) return true;
		MinecraftServer<T> server = (MinecraftServer<T>)obj;
		if (!server.getAddress().getAddress().getHostAddress().equals(this.getAddress().getAddress().getHostAddress())) return false;
		if (server.getAddress().getPort() != this.getAddress().getPort()) return false;
		return true;
	}

	public InetSocketAddress getAddress() {
		return this.address;
	}

	public int getMaxPlayers() {
		return this.maxPlayers;
	}

	public String getMotd() {
		return this.motd;
	}

	public String getName() {
		return this.serverName;
	}

	public int getPlayerCount() {
		return this.playerList.size();
	}

	public Collection<T> getPlayerList() {
		return Collections.unmodifiableSet(this.playerList);
	}

	public MinecraftVersion getVersion() {
		return this.version;
	}

	@Override
	public int hashCode() {
		return 31 * (address == null ? super.hashCode() : address.hashCode());
	}

	public boolean isOnline() {
		return this.online;
	}

	protected void reset() {
		this.motd = "";
		this.maxPlayers = 0;
		this.playerList.clear();
		this.version = MinecraftVersion.DEFAULT;
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
		this.version = new MinecraftVersion(name, protocol);
	}

}