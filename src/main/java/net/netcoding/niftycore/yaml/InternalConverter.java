package net.netcoding.niftycore.yaml;

import net.netcoding.niftycore.yaml.annotations.PreserveStatic;
import net.netcoding.niftycore.yaml.converters.Converter;
import net.netcoding.niftycore.yaml.exceptions.InvalidConverterException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class InternalConverter {

	private final transient LinkedHashSet<Converter> converters = new LinkedHashSet<>();
	private final transient LinkedHashSet<Converter> customConverters = new LinkedHashSet<>();
	private final transient Set<Class<? extends Converter>> customConverterClasses = new HashSet<>();

	public InternalConverter() {
		try {
			this.addConverter(net.netcoding.niftycore.yaml.converters.Primitive.class);
			this.addConverter(net.netcoding.niftycore.yaml.converters.Config.class);
			this.addConverter(net.netcoding.niftycore.yaml.converters.Array.class);
			this.addConverter(net.netcoding.niftycore.yaml.converters.List.class);
			this.addConverter(net.netcoding.niftycore.yaml.converters.Enum.class);
			this.addConverter(net.netcoding.niftycore.yaml.converters.Map.class);
			this.addConverter(net.netcoding.niftycore.yaml.converters.Set.class);
		} catch (InvalidConverterException icex) {
			throw new IllegalStateException(icex);
		}
	}

	private void addConverter(Class<? extends Converter> converter) throws InvalidConverterException {
		this.addConverter(converter, this.converters);
	}

	private void addConverter(Class<? extends Converter> converter, LinkedHashSet<Converter> converters) throws InvalidConverterException {
		try {
			converters.add(converter.getConstructor(InternalConverter.class).newInstance(this));
		} catch (NoSuchMethodException nsmex) {
			throw new InvalidConverterException("Converter does not implement a constructor which takes the InternalConverter instance!", nsmex);
		} catch (InvocationTargetException itex) {
			throw new InvalidConverterException("Converter could not be invoked!", itex);
		} catch (InstantiationException iex) {
			throw new InvalidConverterException("Converter could not be instantiated!", iex);
		} catch (IllegalAccessException iaex) {
			throw new InvalidConverterException("Converter does not implement a public Constructor which takes the InternalConverter instance!", iaex);
		}
	}

	protected void addCustomConverter(Class<? extends Converter> converter) throws InvalidConverterException {
		this.addConverter(converter, this.customConverters);
		this.customConverterClasses.add(converter);
	}

	public void fromConfig(YamlMap yamlMap, Field field, ConfigSection root, String path) throws Exception {
		Object obj = field.get(yamlMap);
		Converter converter;

		if (obj != null) {
			converter = this.getConverter(obj.getClass());

			if (converter != null) {
				if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(PreserveStatic.class)) {
					if (!field.getAnnotation(PreserveStatic.class).value())
						return;
				}

				field.set(yamlMap, converter.fromConfig(obj.getClass(), root.get(path), (field.getGenericType() instanceof ParameterizedType) ? (ParameterizedType) field.getGenericType() : null));
				return;
			}

			converter = this.getConverter(field.getType());

			if (converter != null) {
				if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(PreserveStatic.class)) {
					if (!field.getAnnotation(PreserveStatic.class).value())
						return;
				}

				field.set(yamlMap, converter.fromConfig(field.getType(), root.get(path), (field.getGenericType() instanceof ParameterizedType) ? (ParameterizedType) field.getGenericType() : null));
				return;
			}
		} else {
			converter = this.getConverter(field.getType());

			if (converter != null) {
				if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(PreserveStatic.class)) {
					if (!field.getAnnotation(PreserveStatic.class).value())
						return;
				}

				field.set(yamlMap, converter.fromConfig(field.getType(), root.get(path), (field.getGenericType() instanceof ParameterizedType) ? (ParameterizedType) field.getGenericType() : null));
				return;
			}
		}

		if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(PreserveStatic.class)) {
			if (!field.getAnnotation(PreserveStatic.class).value())
				return;
		}

		field.set(yamlMap, root.get(path));
	}

	public final Converter getConverter(Class<?> type) {
		for (Converter converter : this.customConverters) {
			if (converter.supports(type))
				return converter;
		}

		for (Converter converter : this.converters) {
			if (converter.supports(type))
				return converter;
		}

		return null;
	}

	public final Set<Class<? extends Converter>> getCustomConverters() {
		return Collections.unmodifiableSet(this.customConverterClasses);
	}

	public void toConfig(YamlMap yamlMap, Field field, ConfigSection root, String path) throws Exception {
		Object obj = field.get(yamlMap);
		Converter converter;

		if (obj != null) {
			converter = this.getConverter(obj.getClass());

			if (converter != null) {
				root.set(path, converter.toConfig(obj.getClass(), obj, (field.getGenericType() instanceof ParameterizedType) ? (ParameterizedType) field.getGenericType() : null));
				return;
			}

			converter = this.getConverter(field.getType());

			if (converter != null) {
				root.set(path, converter.toConfig(field.getType(), obj, (field.getGenericType() instanceof ParameterizedType) ? (ParameterizedType) field.getGenericType() : null));
				return;
			}
		}

		root.set(path, obj);
	}

}