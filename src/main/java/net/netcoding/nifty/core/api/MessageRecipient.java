package net.netcoding.nifty.core.api;

import net.netcoding.nifty.core.util.json.JsonMessage;
import net.netcoding.nifty.core.util.RegexUtil;

public interface MessageRecipient {

	/**
	 * Send a json message to the recipient.
	 *
	 * @param message Json message to send.
	 */
	void sendMessage(JsonMessage message) throws Exception;

	/**
	 * Send a message to the recipient.
	 *
	 * @param message Message to send.
	 */
	void sendMessage(String message);

	/**
	 * Send a raw message to the recipient.
	 *
	 * @param message Raw message to send.
	 */
	default void sendRawMessage(String message) {
		this.sendMessage(RegexUtil.strip(message, RegexUtil.VANILLA_PATTERN));
	}

}