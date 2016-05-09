package net.netcoding.niftycore.yaml.converters;

import net.netcoding.niftycore.yaml.InternalConverter;

import java.lang.reflect.ParameterizedType;

public class Enum extends Converter {

	public Enum(InternalConverter converter) {
		super(converter);
	}

	@Override
	public Object fromConfig(Class type, Object obj, ParameterizedType genericType) throws Exception {
		return java.lang.Enum.valueOf(type, obj.toString());
	}

	@Override
	public Object toConfig(Class<?> type, Object obj, ParameterizedType genericType) throws Exception {
		return ((java.lang.Enum<?>)obj).name();
	}

	@Override
	public boolean supports(Class<?> type) {
		return java.lang.Enum.class.isAssignableFrom(type);
	}

}