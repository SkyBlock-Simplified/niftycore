package net.netcoding.niftycore.util.json.events;

public final class ClickEvent extends JsonEvent<String> {

	public ClickEvent(Type type, String value) {
		super(type.getAction(), value);
	}

	public enum Type {
		COMMAND("run_command"),
		FILE("open_file"),
		LINK("open_url"),
		SUGGEST("suggest_command");

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