package net.netcoding.niftycore.reflection;

import net.netcoding.niftycore.util.StringUtil;

public class FieldEntry {

	private Class<?> clazz;
	private String key;
	private final Object value;

	public FieldEntry(Class<?> clazz, Object value) {
		this.clazz = clazz;
		this.value = value;
	}

	public FieldEntry(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	boolean iskeyBased() {
		return StringUtil.notEmpty(this.key);
	}

	public Class<?> getClazz() {
		return this.clazz;
	}

	public String getKey() {
		return this.key;
	}

	public Object getValue() {
		return this.value;
	}

}