package net.netcoding.niftycore.yaml.converters;

import net.netcoding.niftycore.yaml.InternalConverter;

import java.lang.reflect.ParameterizedType;

public abstract class Converter {

	protected final transient InternalConverter converter;

	public Converter(InternalConverter converter) {
		this.converter = converter;
	}

	protected Converter getConverter(Class<?> type) {
		return this.converter.getConverter(type);
	}

	protected java.util.Set<Class<? extends Converter>> getCustomConverters() {
		return this.converter.getCustomConverters();
	}

	/**
	 * This method gets called when we want to load something out of the File. You get that what you give into the Config
	 * via toConfig as Object passed. The type is the Destination Field Type which this Object should be layed in.
	 *
	 * @param type The type (Class) of the Field
	 * @param obj The Object from toConfig
	 * @param genericType If the Class has some generic informations, otherwise null
	 * @return The correct Object which can be hold by the Field
	 * @throws Exception Some generic exception when something went wrong. This gets caught by the Converter
	 */
	public abstract Object fromConfig(Class<?> type, Object obj, ParameterizedType genericType) throws Exception;

	/**
	 * This method gets called on save. It gets the Fields Type and the object the Config wants to save into it. This
	 * is needed to pretty print INTO the config.
	 *
	 * @param type The type (Class) of the Field
	 * @param obj The object which is stored in the Config Object
	 * @param genericType If the Class has some generic informations, otherwise null
	 * @return An Object (mostly a Map or a List)
	 * @throws Exception Some generic exception when something went wrong. This gets caught by the Converter
	 */
	public abstract Object toConfig(Class<?> type, Object obj, ParameterizedType genericType) throws Exception;

	/**
	 * This checks if this Converter can convert the given Class
	 *
	 * @param type The type (Class) of the Field to check
	 * @return true if this can convert that otherwise false
	 */
	public abstract boolean supports(Class<?> type);

}