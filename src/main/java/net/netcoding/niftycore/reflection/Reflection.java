package net.netcoding.niftycore.reflection;

import net.netcoding.niftycore.reflection.exceptions.ReflectionException;
import net.netcoding.niftycore.util.ListUtil;
import net.netcoding.niftycore.util.StringUtil;
import net.netcoding.niftycore.util.concurrent.ConcurrentMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Reflection {

	private static final transient ConcurrentMap<Class<?>, Class<?>> CORRESPONDING_TYPES = new ConcurrentMap<>();
	private static final transient ConcurrentMap<Class<?>, ConcurrentMap<Class<?>[], Constructor<?>>> CONSTRUCTOR_CACHE = new ConcurrentMap<>();
	private static final transient ConcurrentMap<String, Class<?>> CLASS_CACHE = new ConcurrentMap<>();
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

	public Reflection(Class<?> clazz) {
		this(clazz.getSimpleName(), clazz.getPackage().getName());
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

	public final String getClassName() {
		return this.className;
	}

	public final String getClassPath() {
		return this.getPackagePath() + (StringUtil.notEmpty(this.subPackage) ? "." + this.subPackage : "") + "." + this.getClassName();
	}

	public Class<?> getClazz() throws ReflectionException {
		try {
			if (!CLASS_CACHE.containsKey(this.getClassPath()))
				CLASS_CACHE.put(this.getClassPath(), Class.forName(this.getClassPath()));

			return CLASS_CACHE.get(this.getClassPath());
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	public Constructor<?> getConstructor(Class<?>... paramTypes) throws ReflectionException {
		Class<?>[] types = toPrimitiveTypeArray(paramTypes);

		if (CONSTRUCTOR_CACHE.containsKey(this.getClazz())) {
			ConcurrentMap<Class<?>[], Constructor<?>> constructors = CONSTRUCTOR_CACHE.get(this.getClazz());

			if (constructors.containsKey(types))
				return constructors.get(types);
		} else
			CONSTRUCTOR_CACHE.put(this.getClazz(), new ConcurrentMap<Class<?>[], Constructor<?>>());

		for (Constructor<?> constructor : this.getClazz().getDeclaredConstructors()) {
			Class<?>[] constructorTypes = toPrimitiveTypeArray(constructor.getParameterTypes());

			if (isEqualsTypeArray(constructorTypes, types)) {
				constructor.setAccessible(true);
				CONSTRUCTOR_CACHE.get(this.getClazz()).put(types, constructor);
				return constructor;
			}
		}

		if (this.getClazz().getSuperclass() != null)
			return this.getSuperReflection().getConstructor(paramTypes);

		throw new ReflectionException(StringUtil.format("The constructor {0} was not found!", Arrays.asList(types)));
	}

	public Field getField(Class<?> type) throws ReflectionException {
		for (Field field : this.getClazz().getDeclaredFields()) {
			if (field.getType().equals(type) || type.isAssignableFrom(field.getType())) {
				field.setAccessible(true);
				return field;
			}
		}

		if (this.getClazz().getSuperclass() != null)
			return this.getSuperReflection().getField(type);

		throw new ReflectionException(StringUtil.format("The field with type {0} was not found!", type));
	}

	public Field getField(String name) throws ReflectionException {
		for (Field field : this.getClazz().getDeclaredFields()) {
			if (field.getName().equals(name)) {
				field.setAccessible(true);
				return field;
			}
		}

		if (this.getClazz().getSuperclass() != null)
			return this.getSuperReflection().getField(name);

		throw new ReflectionException(StringUtil.format("The field {0} was not found!", name));
	}

	public Method getMethod(Class<?> type, Class<?>... paramTypes) throws ReflectionException {
		Class<?>[] types = toPrimitiveTypeArray(paramTypes);

		for (Method method : this.getClazz().getDeclaredMethods()) {
			Class<?>[] methodTypes = toPrimitiveTypeArray(method.getParameterTypes());

			if ((method.getReturnType().equals(type) || type.isAssignableFrom(method.getReturnType())) && isEqualsTypeArray(methodTypes, types)) {
				method.setAccessible(true);
				return method;
			}
		}

		if (this.getClazz().getSuperclass() != null)
			return this.getSuperReflection().getMethod(type, paramTypes);

		throw new ReflectionException(StringUtil.format("The method with return type {0} was not found with parameters {1}!", type, Arrays.asList(types)));
	}

	public Method getMethod(String name, Class<?>... paramTypes) throws ReflectionException {
		Class<?>[] types = toPrimitiveTypeArray(paramTypes);

		for (Method method : this.getClazz().getDeclaredMethods()) {
			Class<?>[] methodTypes = toPrimitiveTypeArray(method.getParameterTypes());

			if (method.getName().equals(name) && isEqualsTypeArray(methodTypes, types)) {
				method.setAccessible(true);
				return method;
			}
		}

		if (this.getClazz().getSuperclass() != null)
			return this.getSuperReflection().getMethod(name, paramTypes);

		throw new ReflectionException(StringUtil.format("The method {0} was not found with parameters {1}!", name, Arrays.asList(types)));
	}

	public final String getPackagePath() {
		return this.packagePath;
	}

	public final String getSubPackage() {
		return this.subPackage;
	}

	private Reflection getSuperReflection() throws ReflectionException {
		Class<?> superClass = this.getClazz().getSuperclass();
		return new Reflection(superClass.getSimpleName(), superClass.getPackage().toString());
	}

	public Object getValue(Class<?> type, Object obj) throws ReflectionException {
		Field field = this.getField(type);

		try {
			return field.get(obj);
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	public Object getValue(String name, Object obj) throws ReflectionException {
		Field field = this.getField(name);

		try {
			return field.get(obj);
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	public Object invokeMethod(Class<?> type, Object obj, Object... args) throws ReflectionException {
		try {
			return this.getMethod(type, toPrimitiveTypeArray(args)).invoke(obj, args);
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	public Object invokeMethod(String name, Object obj, Object... args) throws ReflectionException {
		try {
			return this.getMethod(name, toPrimitiveTypeArray(args)).invoke(obj, args);
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	public Object newInstance(Object... args) throws ReflectionException {
		try {
			return this.getConstructor(toPrimitiveTypeArray(args)).newInstance(args);
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	public void setValue(Object obj, FieldEntry entry) throws ReflectionException {
		Field f = this.getField(entry.getKey());

		try {
			f.set(obj, entry.getValue());
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	public void setValues(Object obj, FieldEntry... entrys) throws ReflectionException {
		for (FieldEntry entry : entrys)
			this.setValue(obj, entry);
	}

}