package net.netcoding.niftycore.yaml;

import net.netcoding.niftycore.yaml.converters.Converter;
import net.netcoding.niftycore.yaml.exceptions.InvalidConverterException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class InternalConverter {

	private final transient LinkedHashSet<Converter> converters = new LinkedHashSet<>();
	private final transient List<Class<? extends Converter>> customConverters = new ArrayList<>();

	public InternalConverter() {
		try {
			this.addConverter(net.netcoding.niftycore.yaml.converters.Array.class);
			this.addConverter(net.netcoding.niftycore.yaml.converters.Config.class);
			this.addConverter(net.netcoding.niftycore.yaml.converters.List.class);
			this.addConverter(net.netcoding.niftycore.yaml.converters.Map.class);
			this.addConverter(net.netcoding.niftycore.yaml.converters.Primitive.class);
			this.addConverter(net.netcoding.niftycore.yaml.converters.Set.class);
		} catch (InvalidConverterException icex) {
			throw new IllegalStateException(icex);
		}
	}

	private void addConverter(Class<? extends Converter> converter) throws InvalidConverterException {
		try {
			this.converters.add(converter.getConstructor(InternalConverter.class).newInstance(this));
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

	public void addCustomConverter(Class<? extends Converter> converter) throws InvalidConverterException {
		this.addConverter(converter);
		this.customConverters.add(converter);
	}

	public void fromConfig(Config config, Field field, ConfigSection root, String path) throws Exception {
		Object obj = field.get(config);
		Converter converter;

		if (obj != null) {
			converter = this.getConverter(obj.getClass());

			if (converter != null) {
				field.set(config, converter.fromConfig(obj.getClass(), root.get(path), (field.getGenericType() instanceof ParameterizedType) ? (ParameterizedType) field.getGenericType() : null));
				return;
			}

			converter = this.getConverter(field.getType());

			if (converter != null) {
				field.set(config, converter.fromConfig(field.getType(), root.get(path), (field.getGenericType() instanceof ParameterizedType) ? (ParameterizedType) field.getGenericType() : null));
				return;
			}
		} else {
			converter = this.getConverter(field.getType());

			if (converter != null) {
				field.set(config, converter.fromConfig(field.getType(), root.get(path), (field.getGenericType() instanceof ParameterizedType) ? (ParameterizedType) field.getGenericType() : null));
				return;
			}
		}

		field.set(config, root.get(path));
	}

	public Converter getConverter(Class<?> type) {
		for(Converter converter : this.converters) {
			if (converter.supports(type))
				return converter;
		}

		return null;
	}

	public List<Class<? extends Converter>> getCustomConverters() {
		return this.customConverters;
	}

	public void toConfig(Config config, Field field, ConfigSection root, String path) throws Exception {
		Object obj = field.get(config);

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