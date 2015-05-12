package net.netcoding.niftycore.database.notifications;

import net.netcoding.niftycore.util.StringUtil;

public enum TriggerEvent {

	INSERT("Insert"),
	DELETE("Delete"),
	UPDATE("Update");

	private String id;

	private TriggerEvent(String paramString) {
		this.id = paramString;
	}

	public static TriggerEvent fromString(String value) {
		for (TriggerEvent event : TriggerEvent.values()) {
			if (event.toString().equalsIgnoreCase(value))
				return event;
		}

		throw new IllegalArgumentException(StringUtil.format("No constant with text {0} found!", value));
	}

	public String toLowercase() {
		return this.id.toLowerCase();
	}

	public String toString() {
		return this.id;
	}

	public String toUppercase() {
		return this.id.toUpperCase();
	}

}