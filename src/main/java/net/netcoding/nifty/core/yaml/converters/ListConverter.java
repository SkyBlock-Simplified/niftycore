package net.netcoding.nifty.core.yaml.converters;

import net.netcoding.nifty.core.util.ListUtil;
import net.netcoding.nifty.core.yaml.InternalConverter;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class ListConverter extends Converter {

	public ListConverter(InternalConverter converter) {
		super(converter);
	}

	@Override
	public Object fromConfig(Class<?> type, Object section, ParameterizedType genericType) throws Exception {
		java.util.List<Object> values = (java.util.List<Object>)section;
		java.util.List<Object> newList = new ArrayList<>();

		try {
			newList = (java.util.List<Object>)type.newInstance();
		} catch (Exception ignore) { }

		if (genericType != null && genericType.getActualTypeArguments()[0] instanceof Class) {
			Converter converter = this.getConverter((Class<?>)genericType.getActualTypeArguments()[0]);

			if (converter != null) {
				for (Object value : values)
					newList.add(converter.fromConfig((Class<?>)genericType.getActualTypeArguments()[0], value, null));
			} else if (ListUtil.notEmpty(values))
				newList.addAll(values);
		} else if (ListUtil.notEmpty(values))
			newList.addAll(values);

		return newList;
	}

	@Override
	public Object toConfig(Class<?> type, Object obj, ParameterizedType genericType) throws Exception {
		java.util.List<Object> values = (java.util.List<Object>)obj;
		java.util.List<Object> newList = new ArrayList<>();

		if (ListUtil.notEmpty(values)) {
			for (Object value : values) {
				Converter converter = this.getConverter(value.getClass());
				newList.add(converter != null ? converter.toConfig(value.getClass(), value, null) : value);
			}
		}

		return newList;
	}

	@Override
	public boolean supports(Class<?> type) {
		return java.util.List.class.isAssignableFrom(type);
	}

}