package net.netcoding.niftycore.yaml.converters;

import net.netcoding.niftycore.yaml.InternalConverter;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashSet;

@SuppressWarnings("unchecked")
public class Set extends Converter {

	public Set(InternalConverter converter) {
		super(converter);
	}

	@Override
	public Object fromConfig(Class<?> type, Object section, ParameterizedType genericType) throws Exception {
		java.util.List<Object> values = (java.util.List<Object>)section;
		java.util.Set<Object> newList = new HashSet<>();

		try {
			newList = (java.util.Set<Object>)type.newInstance();
		} catch (Exception ignore) { }

        if (genericType != null && genericType.getActualTypeArguments()[0] instanceof Class) {
            Converter converter = this.getConverter((Class<? extends InternalConverter>)genericType.getActualTypeArguments()[0]);

            if (converter != null) {
	            for (Object value : values)
		            newList.add(converter.fromConfig((Class<? extends InternalConverter>) genericType.getActualTypeArguments()[0], value, null));
            } else
                newList.addAll(values);
        } else
            newList.addAll(values);

		return newList;
	}

	@Override
	public Object toConfig(Class<?> type, Object obj, ParameterizedType genericType) throws Exception {
		java.util.Set<Object> values = (java.util.Set<Object>)obj;
		java.util.List<Object> newList = new ArrayList<>();

		for (Object value : values) {
			Converter converter = this.getConverter(value.getClass());
			newList.add(converter != null ? converter.toConfig(value.getClass(), value, null) : value);
		}

		return newList;
	}

	@Override
	public boolean supports(Class<?> type) {
		return java.util.Set.class.isAssignableFrom(type);
	}

}