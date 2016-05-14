package net.netcoding.niftycore.yaml;

import net.netcoding.niftycore.yaml.annotations.ConfigMode;
import net.netcoding.niftycore.yaml.annotations.Path;
import net.netcoding.niftycore.yaml.annotations.PreserveStatic;
import net.netcoding.niftycore.yaml.converters.Converter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public abstract class YamlMap {

	final transient InternalConverter converter = new InternalConverter();

	public final void addCustomConverter(Class<? extends Converter> converter) {
		this.converter.addCustomConverter(converter);
	}

	protected static ConfigSection convertFromMap(Map<?, ?> config) {
		ConfigSection section = new ConfigSection();
		section.map.putAll(config);
		return section;
	}

	protected static boolean doSkip(Field field) {
		if (Modifier.isTransient(field.getModifiers()) || Modifier.isFinal(field.getModifiers()))
			return true;

		if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(PreserveStatic.class))
			return !field.getAnnotation(PreserveStatic.class).value();

		return false;
	}

	protected final String getPathMode(Field field) {
		String path = field.getName();

		if (this.getClass().isAnnotationPresent(ConfigMode.class)) {
			switch (this.getClass().getAnnotation(ConfigMode.class).type()) {
				case FIELD_IS_KEY:
					path = path.replace("_", ".");
					break;
				case PATH_BY_UNDERSCORE:
					break;
				case DEFAULT:
				default:
					if (path.contains("_"))
						path = path.replace("_", ".");

					break;
			}
		}

		return path;
	}

	public void loadFromMap(Map<?, ?> section, Class<?> clazz) throws Exception {
		if (!clazz.getSuperclass().equals(YamlConfig.class))
			loadFromMap(section, clazz.getSuperclass());

		for (Field field : this.getClass().getDeclaredFields()) {
			if (doSkip(field)) continue;
			String path = this.getPathMode(field);

			if (field.isAnnotationPresent(Path.class))
				path = field.getAnnotation(Path.class).value();

			if (Modifier.isPrivate(field.getModifiers()))
				field.setAccessible(true);

			this.converter.fromConfig((YamlConfig)this, field, convertFromMap(section), path);
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> saveToMap(Class<?> clazz) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();

		if (!ConfigMapper.class.isAssignableFrom(clazz) && !clazz.getSuperclass().equals(Object.class)) {
			Map<String, Object> map = saveToMap(clazz.getSuperclass());

			for (Map.Entry<String, Object> entry : map.entrySet())
				returnMap.put(entry.getKey(), entry.getValue());
		}

		for (Field field : this.getClass().getDeclaredFields()) {
			if (doSkip(field)) continue;
			String path = this.getPathMode(field);

			if (field.isAnnotationPresent(Path.class))
				path = field.getAnnotation(Path.class).value();

			if (Modifier.isPrivate(field.getModifiers()))
				field.setAccessible(true);

			try {
				returnMap.put(path, field.get(this));
			} catch (IllegalAccessException ignore) { }
		}

		Converter converter = this.converter.getConverter(Map.class);
		return (Map<String, Object>)converter.toConfig(HashMap.class, returnMap, null);
	}

}