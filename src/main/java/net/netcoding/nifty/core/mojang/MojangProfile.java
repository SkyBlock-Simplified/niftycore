package net.netcoding.nifty.core.mojang;

import net.netcoding.nifty.core.api.MinecraftServer;
import net.netcoding.nifty.core.util.StringUtil;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Container for a players unique id and name.
 */
public abstract class MojangProfile implements OnlineProfile {

	private static final Pattern UUID_FIX = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
	private String id = "";
	private UUID uuid;
	protected String name;
	private String ip = "";
	private int port = 0;
	private InetSocketAddress ipAddress;
	private boolean legacy = false;
	private boolean demo = false;
	private long updated = System.currentTimeMillis();
	// http://skins.minecraft.net/MinecraftSkins/<username>.png

	@Override
	public final boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!Profile.class.isAssignableFrom(obj.getClass())) return false;
		Profile profile = (Profile)obj;
		return this.getUniqueId().equals(profile.getUniqueId());
	}

	@Override
	public final InetSocketAddress getAddress() {
		if (StringUtil.notEmpty(this.ip) && this.ipAddress == null && this.isOnline())
			this.ipAddress = new InetSocketAddress(this.ip, this.port);

		return this.ipAddress;
	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the server this profile belongs to.
	 *
	 * @return BungeeServer Server object.
	 */
	public abstract MinecraftServer<? extends MojangProfile> getServer();

	/**
	 * Gets the players UUID.
	 *
	 * @return Player UUID.
	 */
	@Override
	public final UUID getUniqueId() {
		if (this.uuid == null)
			this.uuid = UUID.fromString(UUID_FIX.matcher(this.id.replace("-", "")).replaceAll("$1-$2-$3-$4-$5"));

		return this.uuid;
	}

	/**
	 * Checks if this profile has an assigned ip address.
	 *
	 * @return True if address exists, otherwise false.
	 */
	public final boolean hasAddress() {
		return this.getAddress() != null;
	}

	/**
	 * Checks if this players profile is expired.
	 *
	 * @return True if expired, otherwise false.
	 */
	public final boolean hasExpired() {
		return System.currentTimeMillis() - this.updated >= 1800000;
	}

	@Override
	public final int hashCode() {
		return this.getUniqueId().hashCode();
	}

	/**
	 * Gets if the account is unpaid.
	 *
	 * @return True if unpaid, otherwise false.
	 */
	public final boolean isDemo() {
		return this.demo;
	}

	/**
	 * Gets if the account has not been migrated to Mojang.
	 *
	 * @return True if not migrated, otherwise false.
	 */
	public final boolean isLegacy() {
		return this.legacy;
	}

	/**
	 * Checks if this profile is online anywhere on BungeeCord.
	 *
	 * @return True if online, otherwise false.
	 */
	@Override
	public abstract boolean isOnline();

	@Override
	public String toString() {
		return StringUtil.format("'{'{0},{1}'}'", this.getUniqueId(), this.getName());
	}

}