package net.netcoding.nifty.core.yaml.converters;

import net.netcoding.nifty.core.reflection.Reflection;
import net.netcoding.nifty.core.yaml.InternalConverter;
import net.netcoding.nifty.core.yaml.YamlMap;
import net.netcoding.nifty.core.yaml.ConfigSection;

import java.lang.reflect.ParameterizedType;
import java.util.Map;

public class ConfigConverter extends Converter {

	public ConfigConverter(InternalConverter converter) {
		super(converter);
	}

	@Override
	public Object fromConfig(Class<?> type, Object section, ParameterizedType genericType) throws Exception {
		YamlMap obj = (YamlMap)newInstance(type);

		for (Class<? extends Converter> clazz : this.getCustomConverters())
			obj.addCustomConverter(clazz);

		obj.loadFromMap((section instanceof Map) ? (Map<?, ?>)section : ((ConfigSection)section).getRawMap(), type);
		return obj;
	}

	@Override
	public Object toConfig(Class<?> type, Object obj, ParameterizedType genericType) throws Exception {
		if (obj instanceof Map)
			return obj;
		else {
			YamlMap map = (YamlMap)obj;

			for (Class<? extends Converter> clazz : this.getCustomConverters())
				map.addCustomConverter(clazz);

			return map.saveToMap(obj.getClass());
		}
	}

	private static Object newInstance(Class<?> type) {
		Class<?> enclosingClass = type.getEnclosingClass();

		if (enclosingClass != null)
			return new Reflection(type).newInstance(newInstance(enclosingClass));

		return new Reflection(type).newInstance();
	}

	@Override
	public boolean supports(Class<?> type) {
		return YamlMap.class.isAssignableFrom(type);
	}

}