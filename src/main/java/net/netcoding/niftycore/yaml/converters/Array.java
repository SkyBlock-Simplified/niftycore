package net.netcoding.niftycore.yaml.converters;

import java.lang.reflect.ParameterizedType;

import net.netcoding.niftycore.yaml.InternalConverter;

@SuppressWarnings("unchecked")
public class Array extends Converter {

	public Array(InternalConverter converter) {
		super(converter);
	}

	private static <T> T[] getArray(Class<T> type, java.util.List<Object> list) {
		T[] array = (T[])java.lang.reflect.Array.newInstance(type, list.size());
		return list.toArray(array);
	}

	@Override
	public Object fromConfig(Class<?> type, Object section, ParameterizedType genericType) throws Exception {
		if (type.isAssignableFrom(section.getClass())) return section;
		java.util.List<Object> values = (java.util.List<Object>)section;
		return getArray(type, values);
	}

	@Override
	public Object toConfig(Class<?> type, Object obj, ParameterizedType genericType) throws Exception {
		return obj;
	}

	@Override
	public boolean supports(Class<?> type) {
		return type.isArray();
	}

}