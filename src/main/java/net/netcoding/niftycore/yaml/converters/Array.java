package net.netcoding.niftycore.yaml.converters;

import net.netcoding.niftycore.yaml.InternalConverter;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;

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
		Class<?> singleType = type.getComponentType();
		java.util.List<Object> values;

		if (section instanceof java.util.List)
			values = (java.util.List) section;
		else
			Collections.addAll(values = new ArrayList(), (Object[]) section);

		Object ret = java.lang.reflect.Array.newInstance(singleType, values.size());
		Converter converter = this.getConverter(singleType);

		if (converter == null)
			return values.toArray((Object[]) ret);

		for (int i = 0; i < values.size(); i++)
			java.lang.reflect.Array.set(ret, i, converter.fromConfig(singleType, values.get(i), genericType));

		return ret;
	}

	@Override
	public Object toConfig(Class<?> type, Object obj, ParameterizedType genericType) throws Exception {
		Class<?> singleType = type.getComponentType();
		Converter converter = this.getConverter(singleType);

		if (converter == null)
			return obj;

		Object[] ret = new Object[java.lang.reflect.Array.getLength(obj)];
		for (int i = 0; i < ret.length; i++)
			ret[i] = converter.toConfig(singleType, java.lang.reflect.Array.get(obj, i), genericType);

		return ret;
	}

	@Override
	public boolean supports(Class<?> type) {
		return type.isArray();
	}

}