package net.netcoding.nifty.core.yaml.converters;

import net.netcoding.nifty.core.util.ListUtil;
import net.netcoding.nifty.core.yaml.InternalConverter;

import java.lang.reflect.ParameterizedType;
import java.util.HashSet;

@SuppressWarnings("unchecked")
public class SetConverter extends Converter {

	public SetConverter(InternalConverter converter) {
		super(converter);
	}

	@Override
	public Object fromConfig(Class<?> type, Object section, ParameterizedType genericType) throws Exception {
		java.util.Set<Object> values = (java.util.Set<Object>)section;
		java.util.Set<Object> newSet = new HashSet<>();

		try {
			newSet = (java.util.Set<Object>)type.newInstance();
		} catch (Exception ignore) { }

        if (genericType != null && genericType.getActualTypeArguments()[0] instanceof Class) {
            Converter converter = this.getConverter((Class<?>)genericType.getActualTypeArguments()[0]);

            if (converter != null) {
	            for (Object value : values)
		            newSet.add(converter.fromConfig((Class<?>)genericType.getActualTypeArguments()[0], value, null));
            } else if (ListUtil.notEmpty(values))
	            newSet.addAll(values);
        } else if (ListUtil.notEmpty(values))
	        newSet.addAll(values);

		return newSet;
	}

	@Override
	public Object toConfig(Class<?> type, Object obj, ParameterizedType genericType) throws Exception {
		java.util.Set<Object> values = (java.util.Set<Object>)obj;
		java.util.Set<Object> newSet = new HashSet<>();

		if (ListUtil.notEmpty(values)) {
			for (Object value : values) {
				Converter converter = this.getConverter(value.getClass());
				newSet.add(converter != null ? converter.toConfig(value.getClass(), value, null) : value);
			}
		}

		return newSet;
	}

	@Override
	public boolean supports(Class<?> type) {
		return java.util.Set.class.isAssignableFrom(type);
	}

}