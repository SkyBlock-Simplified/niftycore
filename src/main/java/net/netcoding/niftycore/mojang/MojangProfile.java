package net.netcoding.niftycore.mojang;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.regex.Pattern;

import net.netcoding.niftycore.minecraft.MinecraftServer;
import net.netcoding.niftycore.util.StringUtil;

/**
 * Container for a players unique id and name.
 */
public abstract class MojangProfile {

	private static final Pattern UUID_FIX = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
	private String id;
	private UUID uuid;
	protected String name;
	private String ip;
	private int port;
	private boolean legacy = false;
	private boolean demo = false;
	private InetSocketAddress ipAddress;
	private long updated = System.currentTimeMillis();
	// http://skins.minecraft.net/MinecraftSkins/<username>.png

	protected MojangProfile() { }

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof MojangProfile)) return false;
		if (this == obj) return true;
		MojangProfile profile = (MojangProfile)obj;
		return this.getUniqueId().equals(profile.getUniqueId());
	}

	/**
	 * Gets the ip address of the player if they are online.
	 * 
	 * @return Socket address of the player if online, otherwise null.
	 */
	public InetSocketAddress getAddress() {
		if (this.isOnlineAnywhere()) {
			if (StringUtil.notEmpty(this.ip) && this.ipAddress == null)
				this.ipAddress = InetSocketAddress.createUnresolved(this.ip, this.port);
		}

		return this.ipAddress;
	}

	/**
	 * Gets the players name associated to this UUID.
	 * 
	 * @return Current player name.
	 */
	public abstract String getName();

	/**
	 * Gets the server this profile belongs to.
	 * 
	 * @return BungeeServer Server object.
	 */
	public abstract MinecraftServer getServer();

	/**
	 * Gets the players UUID.
	 * 
	 * @return Player UUID.
	 */
	public UUID getUniqueId() {
		if (this.uuid == null)
			this.uuid = UUID.fromString(UUID_FIX.matcher(this.id.replace("-", "")).replaceAll("$1-$2-$3-$4-$5"));

		return this.uuid;
	}

	/**
	 * Checks if this profile has an assigned ip address.
	 * 
	 * @return True if address exists, otherwise false.
	 */
	public boolean hasAddress() {
		return this.getAddress() != null;
	}

	/**
	 * Checks if this players profile is expired.
	 * 
	 * @return True if expired, otherwise false.
	 */
	public boolean hasExpired() {
		return System.currentTimeMillis() - this.updated >= 1800000;
	}

	@Override
	public int hashCode() {
		return this.getUniqueId().hashCode();
	}

	/**
	 * Gets if the account is unpaid.
	 * 
	 * @return True if unpaid, otherwise false.
	 */
	public boolean isDemo() {
		return this.demo;
	}

	/**
	 * Gets if the account has not been migrated to Mojang.
	 * 
	 * @return True if not migrated, otherwise false.
	 */
	public boolean isLegacy() {
		return this.legacy;
	}

	/**
	 * Checks if this profile is found anywhere on BungeeCord.
	 * 
	 * @return True if online, otherwise false.
	 */
	public abstract boolean isOnlineAnywhere();

	/*/**
	 * Sends a packet to the profiles client, if they are online.
	 * 
	 * @param packet Packet to send.
	 */
	//public abstract void sendPacket(Object packet) throws Exception;

	@Override
	public String toString() {
		return StringUtil.format("'{'{0},{1}'}'", this.getUniqueId(), this.getName());
	}

}