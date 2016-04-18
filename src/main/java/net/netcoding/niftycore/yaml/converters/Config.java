package net.netcoding.niftycore.yaml.converters;

import net.netcoding.niftycore.yaml.ConfigSection;
import net.netcoding.niftycore.yaml.InternalConverter;
import net.netcoding.niftycore.yaml.YamlConfig;

import java.lang.reflect.ParameterizedType;
import java.util.Map;

public class Config extends Converter {

	public Config(InternalConverter converter) {
		super(converter);
	}

	@Override
	public Object fromConfig(Class<?> type, Object section, ParameterizedType genericType) throws Exception {
		YamlConfig obj = (YamlConfig)newInstance(type);

		for (Class<? extends Converter> clazz : this.getCustomConverters())
			obj.addCustomConverter(clazz);

		obj.loadFromMap((section instanceof Map) ? (Map<?, ?>)section : ((ConfigSection)section).getRawMap(), type);
		return section;
	}

	@Override
	public Object toConfig(Class<?> type, Object obj, ParameterizedType genericType) throws Exception {
		return (obj instanceof Map) ? obj : ((YamlConfig)obj).saveToMap(obj.getClass());
	}

	public Object newInstance(Class<?> type) throws Exception {
		Class<?> enclosingClass = type.getEnclosingClass();

		if (enclosingClass != null)
			return type.getConstructor(enclosingClass).newInstance(newInstance(enclosingClass));

		return type.newInstance();
	}

	@Override
	public boolean supports(Class<?> type) {
		return YamlConfig.class.isAssignableFrom(type);
	}

}