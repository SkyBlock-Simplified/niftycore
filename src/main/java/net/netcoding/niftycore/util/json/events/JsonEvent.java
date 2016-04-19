package net.netcoding.niftycore.util.json.events;

abstract class JsonEvent<T> {

	private final String name;
	private final T value;

	public JsonEvent(String name, T value) {
		this.name = name;
		this.value = value;
	}

	public final String getName() {
		return this.name;
	}

	public final T getValue() {
		return this.value;
	}

}