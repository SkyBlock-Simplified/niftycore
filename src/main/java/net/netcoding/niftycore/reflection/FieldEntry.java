package net.netcoding.niftycore.reflection;

public class FieldEntry {

	private final String key;
	private final Object value;

	public FieldEntry(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public Object getValue() {
		return this.value;
	}

}