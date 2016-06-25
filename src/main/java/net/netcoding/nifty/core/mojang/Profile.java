package net.netcoding.nifty.core.mojang;

import java.util.UUID;

public interface Profile {

	/**
	 * Gets the name associated to this profile.
	 *
	 * @return Current profile name.
	 */
	String getName();

	/**
	 * Gets the unique identifier associated to this profile.
	 *
	 * @return Current profile UUID.
	 */
	UUID getUniqueId();

	/**
	 * Checks if this profile is online.
	 *
	 * @return True if online, otherwise false.
	 */
	boolean isOnline();

}