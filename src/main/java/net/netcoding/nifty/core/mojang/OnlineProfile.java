package net.netcoding.nifty.core.mojang;

import net.netcoding.nifty.core.api.MessageRecipient;

import java.net.InetSocketAddress;

public interface OnlineProfile extends Profile, MessageRecipient {

	/**
	 * Gets the ip address of the player if they are online.
	 *
	 * @return Socket address of the player if online, otherwise null.
	 */
	InetSocketAddress getAddress();

}