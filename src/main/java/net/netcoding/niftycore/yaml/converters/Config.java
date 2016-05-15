package net.netcoding.niftycore.yaml.converters;

import net.netcoding.niftycore.yaml.ConfigSection;
import net.netcoding.niftycore.yaml.InternalConverter;
import net.netcoding.niftycore.yaml.YamlMap;

import java.lang.reflect.ParameterizedType;
import java.util.Map;

public class Config extends Converter {

	public Config(InternalConverter converter) {
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

	public Object newInstance(Class<?> type) throws Exception {
		Class<?> enclosingClass = type.getEnclosingClass();

		if (enclosingClass != null)
			return type.getConstructor(enclosingClass).newInstance(newInstance(enclosingClass));

		return type.newInstance();
	}

	@Override
	public boolean supports(Class<?> type) {
		return YamlMap.class.isAssignableFrom(type);
	}

}