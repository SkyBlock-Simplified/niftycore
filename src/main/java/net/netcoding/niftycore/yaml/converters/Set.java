package net.netcoding.niftycore.yaml.converters;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import net.netcoding.niftycore.yaml.InternalConverter;

@SuppressWarnings("unchecked")
public class Set extends Converter {

	public Set(InternalConverter converter) {
		super(converter);
	}

	@Override
	public Object fromConfig(Class<?> type, Object section, ParameterizedType genericType) throws Exception {
		java.util.List<Object> values = (java.util.List<Object>)section;
		java.util.Set<Object> newList = new HashSet<Object>();

		try {
			newList = (java.util.Set<Object>)type.newInstance();
		} catch (Exception e) { }

        if (genericType != null && genericType.getActualTypeArguments()[0] instanceof Class) {
            Converter converter = this.getConverter((Class<? extends InternalConverter>)genericType.getActualTypeArguments()[0]);

            if (converter != null) {
                for ( int i = 0; i < values.size(); i++ )
                    newList.add(converter.fromConfig((Class<? extends InternalConverter>)genericType.getActualTypeArguments()[0], values.get(i), null));
            } else
                newList.addAll(values);
        } else
            newList.addAll( values );

		return newList;
	}

	@Override
	public Object toConfig(Class<?> type, Object obj, ParameterizedType genericType) throws Exception {
		java.util.Set<Object> values = (java.util.Set<Object>)obj;
		java.util.List<Object> newList = new ArrayList<Object>();
		Iterator<Object> iterator = values.iterator();

		while (iterator.hasNext()) {
			Object value = iterator.next();
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