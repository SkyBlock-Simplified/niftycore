package net.netcoding.niftycore.yaml.converters;

import net.netcoding.niftycore.yaml.InternalConverter;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class List extends Converter {

	public List(InternalConverter converter) {
		super(converter);
	}

	@Override
	public Object fromConfig(Class<?> type, Object section, ParameterizedType genericType) throws Exception {
		java.util.List<Object> newList = new ArrayList<>();
		java.util.List<Object> values = (java.util.List<Object>)section;

		try {
			newList = (java.util.List<Object>)type.newInstance();
		} catch (Exception ignore) { }

		if (genericType != null && genericType.getActualTypeArguments()[0] instanceof Class) {
			Converter converter = this.getConverter((Class<?>)genericType.getActualTypeArguments()[0]);

			if (converter != null) {
				for (Object value : values)
					newList.add(converter.fromConfig((Class<?>)genericType.getActualTypeArguments()[0], value, null));
			} else
				newList = values;
		} else
			newList = values;

		return newList;
	}

	@Override
	public Object toConfig(Class<?> type, Object obj, ParameterizedType genericType) throws Exception {
		java.util.List<Object> values = (java.util.List<Object>)obj;
		java.util.List<Object> newList = new ArrayList<>();

		for (Object value : values) {
			Converter converter = this.getConverter(value.getClass());
			newList.add(converter != null ? converter.toConfig(value.getClass(), value, null) : value);
		}

		return newList;
	}

	@Override
	public boolean supports(Class<?> type) {
		return java.util.List.class.isAssignableFrom(type);
	}

}