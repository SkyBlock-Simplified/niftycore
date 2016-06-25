package net.netcoding.nifty.core.util.json.events;

import net.netcoding.nifty.core.util.json.JsonRepresentedObject;

public final class HoverEvent extends JsonEvent<JsonRepresentedObject> {

	public HoverEvent(Type type, JsonRepresentedObject value) {
		super(type.getAction(), value);
	}

	public enum Type {
		ACHIEVEMENT("show_achievement"),
		ITEM("show_item"),
		TEXT("show_text");

		private final String action;

		Type(String action) {
			this.action = action;
		}

		public String getAction() {
			return this.action;
		}

		public static Type getFromAction(String action) {
			for (Type type : values()) {
				if (type.getAction().equals(action))
					return type;
			}

			return null;
		}

	}

}