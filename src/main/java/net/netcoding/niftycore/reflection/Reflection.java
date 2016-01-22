package net.netcoding.niftycore.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import net.netcoding.niftycore.util.ListUtil;
import net.netcoding.niftycore.util.StringUtil;

public class Reflection {

	private static final transient ConcurrentHashMap<Class<?>, Class<?>> CORRESPONDING_TYPES = new ConcurrentHashMap<>();
	private static final transient ConcurrentHashMap<Class<?>[], Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();
	private static final transient ConcurrentHashMap<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
	private final String className;
	private final String subPackage;
	private final String packagePath;

	static {
		CORRESPONDING_TYPES.put(Byte.class, byte.class);
		CORRESPONDING_TYPES.put(Short.class, short.class);
		CORRESPONDING_TYPES.put(Integer.class, int.class);
		CORRESPONDING_TYPES.put(Long.class, long.class);
		CORRESPONDING_TYPES.put(Character.class, char.class);
		CORRESPONDING_TYPES.put(Float.class, float.class);
		CORRESPONDING_TYPES.put(Double.class, double.class);
		CORRESPONDING_TYPES.put(Boolean.class, boolean.class);
	}

	public Reflection(String className, String packagePath) {
		this(className, "", packagePath);
	}

	public Reflection(String className, String subPackage, String packagePath) {
		this.className = className;
		this.subPackage = StringUtil.stripNull(subPackage).replaceAll("\\.$", "").replaceAll("^\\.", "");
		this.packagePath = packagePath;
	}

	private static Class<?> getPrimitiveType(Class<?> clazz) {
		return clazz != null ? CORRESPONDING_TYPES.containsKey(clazz) ? CORRESPONDING_TYPES.get(clazz) : clazz : null;
	}

	private static boolean isEqualsTypeArray(Class<?>[] a, Class<?>[] o) {
		if (a.length != o.length) return false;

		for (int i = 0; i < a.length; i++) {
			if (o[i] != null && !a[i].equals(o[i]) && !a[i].isAssignableFrom(o[i]))
				return false;
		}

		return true;
	}

	private static Class<?>[] toPrimitiveTypeArray(Class<?>[] classes) {
		Class<?>[] types = new Class<?>[ListUtil.notEmpty(classes) ? classes.length : 0];

		for (int i = 0; i < types.length; i++)
			types[i] = getPrimitiveType(classes[i]);

		return types;
	}

	private static Class<?>[] toPrimitiveTypeArray(Object[] objects) {
		Class<?>[] types = new Class<?>[ListUtil.notEmpty(objects) ? objects.length : 0];

		for (int i = 0; i < types.length; i++)
			types[i] = getPrimitiveType(objects[i] != null ? objects[i].getClass() : null);

		return types;
	}

	public String getClassName() {
		return this.className;
	}

	public String getClassPath() {
		return this.getPackagePath() + (StringUtil.notEmpty(this.subPackage) ? "." + this.subPackage : "") + "." + this.getClassName();
	}

	public Class<?> getClazz() throws Exception {
		if (!CLASS_CACHE.containsKey(this.getClassPath())) CLASS_CACHE.put(this.getClassPath(), Class.forName(this.getClassPath()));
		return CLASS_CACHE.get(this.getClassPath());
	}

	public Constructor<?> getConstructor(Class<?>... paramTypes) throws Exception {
		Class<?>[] types = toPrimitiveTypeArray(paramTypes);

		if (CONSTRUCTOR_CACHE.containsKey(types))
			return CONSTRUCTOR_CACHE.get(types);

		for (Constructor<?> constructor : this.getClazz().getConstructors()) {
			Class<?>[] constructorTypes = toPrimitiveTypeArray(constructor.getParameterTypes());

			if (isEqualsTypeArray(constructorTypes, types)) {
				CONSTRUCTOR_CACHE.put(constructorTypes, constructor);
				return constructor;
			}
		}

		throw new Exception(StringUtil.format("The constructor {0} was not found!", Arrays.asList(types)));
	}

	public Field getField(Class<?> type) throws Exception {
		for (Field field : this.getClazz().getDeclaredFields()) {
			if (field.getType().equals(type) || type.isAssignableFrom(field.getType())) {
				field.setAccessible(true);
				return field;
			}
		}

		throw new Exception(StringUtil.format("The field with type {0} was not found!", type));
	}

	public Field getField(String name) throws Exception {
		Field field = this.getClazz().getDeclaredField(name);

		if (field == null)
			throw new Exception(StringUtil.format("The field {0} was not found!", name));

		field.setAccessible(true);
		return field;
	}

	public Method getMethod(Class<?> type, Class<?>... paramTypes) throws Exception {
		Class<?>[] types = toPrimitiveTypeArray(paramTypes);

		for (Method method : this.getClazz().getMethods()) {
			Class<?>[] methodTypes = toPrimitiveTypeArray(method.getParameterTypes());

			if ((method.getReturnType().equals(type) || type.isAssignableFrom(method.getReturnType())) && isEqualsTypeArray(methodTypes, types)) {
				method.setAccessible(true);
				return method;
			}
		}

		throw new Exception(StringUtil.format("The method with return type {0} was not found with parameters {1}!", type, Arrays.asList(types)));
	}

	public Method getMethod(String name, Class<?>... paramTypes) throws Exception {
		Class<?>[] types = toPrimitiveTypeArray(paramTypes);

		for (Method method : this.getClazz().getMethods()) {
			Class<?>[] methodTypes = toPrimitiveTypeArray(method.getParameterTypes());

			if (method.getName().equals(name) && isEqualsTypeArray(methodTypes, types)) {
				method.setAccessible(true);
				return method;
			}
		}

		throw new Exception(StringUtil.format("The method {0} was not found with parameters {1}!", name, Arrays.asList(types)));
	}

	public String getPackagePath() {
		return this.packagePath;
	}

	public String getSubPackage() {
		return this.subPackage;
	}

	public Object getValue(Class<?> type, Object obj) throws Exception {
		return this.getField(type).get(obj);
	}

	public Object getValue(String name, Object obj) throws Exception {
		return this.getField(name).get(obj);
	}

	public Object invokeMethod(Class<?> type, Object obj, Object... args) throws Exception {
		return this.getMethod(type, toPrimitiveTypeArray(args)).invoke(obj, args);
	}

	public Object invokeMethod(String name, Object obj, Object... args) throws Exception {
		return this.getMethod(name, toPrimitiveTypeArray(args)).invoke(obj, args);
	}

	public Object newInstance(Object... args) throws Exception {
		return this.getConstructor(toPrimitiveTypeArray(args)).newInstance(args);
	}

	public void setValue(Object obj, FieldEntry entry) throws Exception {
		Field f = this.getField(entry.getKey());
		f.setAccessible(true);
		f.set(obj, entry.getValue());
	}

	public void setValues(Object obj, FieldEntry... entrys) throws Exception {
		for (FieldEntry entry : entrys)
			this.setValue(obj, entry);
	}

}