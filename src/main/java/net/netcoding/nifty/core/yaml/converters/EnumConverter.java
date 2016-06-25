package net.netcoding.nifty.core.yaml.converters;

import net.netcoding.nifty.core.yaml.InternalConverter;

import java.lang.reflect.ParameterizedType;

public class EnumConverter extends Converter {

	public EnumConverter(InternalConverter converter) {
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