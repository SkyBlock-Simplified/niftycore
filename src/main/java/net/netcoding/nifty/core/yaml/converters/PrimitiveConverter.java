package net.netcoding.nifty.core.yaml.converters;

import net.netcoding.nifty.core.yaml.InternalConverter;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashSet;

public class PrimitiveConverter extends Converter {

	private static final transient HashSet<String> types = new HashSet<>();

	static {
		types.addAll(Arrays.asList("boolean", "character", "byte", "short", "integer", "long", "float", "double", "string"));
	}

	public PrimitiveConverter(InternalConverter converter) {
		super(converter);
	}

	@Override
	public Object fromConfig(Class<?> type, Object section, ParameterizedType genericType) throws Exception {
		switch(type.getSimpleName().toLowerCase()) {
			case "short":
				return (section instanceof Short) ? section : new Integer((int)section).shortValue();
			case "byte":
				return (section instanceof Byte) ? section : new Integer((int)section).byteValue();
			case "float":
				if (section instanceof Integer) return new Double((int)section).intValue();
				return (section instanceof Float) ? section : new Double((double)section).floatValue();
			case "character":
				return (section instanceof Character) ? section : ((String)section).charAt(0);
			case "string":
				return (section instanceof String) ? section : section.toString();
			default:
				return section;
		}
	}

	@Override
	public Object toConfig(Class<?> type, Object obj, ParameterizedType genericType) throws Exception {
		return obj;
	}

	@Override
	public boolean supports(Class<?> type) {
		return types.contains(type.getSimpleName().toLowerCase());
	}

}