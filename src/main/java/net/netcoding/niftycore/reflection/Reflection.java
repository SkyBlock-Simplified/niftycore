package net.netcoding.niftycore.reflection;

import com.google.common.primitives.Primitives;
import net.netcoding.niftycore.reflection.exceptions.ReflectionException;
import net.netcoding.niftycore.util.ListUtil;
import net.netcoding.niftycore.util.StringUtil;
import net.netcoding.niftycore.util.concurrent.ConcurrentMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;

@SuppressWarnings("AccessOfSystemProperties")
public class Reflection {

	private static final double JAVA_VERSION = Double.parseDouble(System.getProperty("java.specification.version"));
	private static final transient ConcurrentMap<Class<?>, ConcurrentMap<Class<?>[], Constructor<?>>> CONSTRUCTOR_CACHE = new ConcurrentMap<>();
	private static final transient ConcurrentMap<String, Class<?>> CLASS_CACHE = new ConcurrentMap<>();
	private final String className;
	private final String subPackage;
	private final String packagePath;

	public Reflection(Class<?> clazz) {
		this.className = clazz.getSimpleName();

		if (clazz.getPackage() != null) {
			this.subPackage = "";
			this.packagePath = clazz.getPackage().getName();
		} else {
			this.subPackage = "";
			this.packagePath = clazz.getName().replaceAll(StringUtil.format("\\.{0}$", this.className), "");
		}
	}

	public Reflection(String className, String packagePath) {
		this(className, "", packagePath);
	}

	public Reflection(String className, String subPackage, String packagePath) {
		this.className = className;
		this.subPackage = StringUtil.stripNull(subPackage).replaceAll("\\.$", "").replaceAll("^\\.", "");
		this.packagePath = packagePath;
	}

	public final String getClazzName() {
		return this.className;
	}

	public final String getClazzPath() {
		return StringUtil.format("{0}.{1}", this.getPackagePath(), this.getClazzName());
	}

	public Class<?> getClazz() throws ReflectionException {
		try {
			if (!CLASS_CACHE.containsKey(this.getClazzPath()))
				CLASS_CACHE.put(this.getClazzPath(), Class.forName(this.getClazzPath()));

			return CLASS_CACHE.get(this.getClazzPath());
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	public final URL getClazzLocation() throws ReflectionException {
		ProtectionDomain domain = this.getClazz().getProtectionDomain();

		if (domain != null) {
			CodeSource source = domain.getCodeSource();

			if (source != null)
				return source.getLocation();
		}

		return null;
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
		Class<?> utype = (type.isPrimitive() ? Primitives.wrap(type) : Primitives.unwrap(type));

		for (Field field : this.getClazz().getDeclaredFields()) {
			if (field.getType().equals(type) || type.isAssignableFrom(field.getType()) || field.getType().equals(utype) || utype.isAssignableFrom(field.getType())) {
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

	public static double getJavaVersion() {
		return JAVA_VERSION;
	}

	public Method getMethod(Class<?> type, Class<?>... paramTypes) throws ReflectionException {
		Class<?> utype = (type.isPrimitive() ? Primitives.wrap(type) : Primitives.unwrap(type));
		Class<?>[] types = toPrimitiveTypeArray(paramTypes);

		for (Method method : this.getClazz().getDeclaredMethods()) {
			Class<?>[] methodTypes = toPrimitiveTypeArray(method.getParameterTypes());
			Class<?> returnType = method.getReturnType();

			if ((returnType.equals(type) || type.isAssignableFrom(returnType) || returnType.equals(utype) || utype.isAssignableFrom(returnType)) && isEqualsTypeArray(methodTypes, types)) {
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
		return this.packagePath + (StringUtil.notEmpty(this.subPackage) ? "." + this.subPackage : "");
	}

	public final String getSubPackage() {
		return this.subPackage;
	}

	private Reflection getSuperReflection() throws ReflectionException {
		Class<?> superClass = this.getClazz().getSuperclass();
		String className = superClass.getSimpleName();
		String packageName = (superClass.getPackage() != null ? superClass.getPackage().getName() : superClass.getName().replaceAll(StringUtil.format("\\.{0}$", className), ""));
		return new Reflection(className, packageName);
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

	private static boolean isEqualsTypeArray(Class<?>[] a, Class<?>[] o) {
		if (a.length != o.length) return false;

		for (int i = 0; i < a.length; i++) {
			if (o[i] != null && !a[i].equals(o[i]) && !a[i].isAssignableFrom(o[i]))
				return false;
		}

		return true;
	}

	public Object invokeMethod(Class<?> type, Object obj, Object... args) throws ReflectionException {
		try {
			return this.getMethod(type, toPrimitiveTypeArray(args)).invoke(obj, args);
		} catch (ReflectionException rex) {
			throw rex;
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	public Object invokeMethod(String name, Object obj, Object... args) throws ReflectionException {
		try {
			return this.getMethod(name, toPrimitiveTypeArray(args)).invoke(obj, args);
		} catch (ReflectionException rex) {
			throw rex;
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	public Object newInstance(Object... args) throws ReflectionException {
		try {
			return this.getConstructor(toPrimitiveTypeArray(args)).newInstance(args);
		} catch (ReflectionException rex) {
			throw rex;
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	public void setValue(String name, Object obj, Object value) throws ReflectionException {
		Field f = this.getField(name);

		try {
			f.set(obj, value);
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	public void setValue(Class<?> clazz, Object obj, Object value) throws ReflectionException {
		Field f = this.getField(clazz);

		try {
			f.set(obj, value);
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	public static Class<?>[] toPrimitiveTypeArray(Class<?>[] classes) {
		Class<?>[] types = new Class<?>[ListUtil.notEmpty(classes) ? classes.length : 0];

		for (int i = 0; i < types.length; i++)
			types[i] = Primitives.unwrap(classes[i]);

		return types;
	}

	public static Class<?>[] toPrimitiveTypeArray(Object[] objects) {
		Class<?>[] types = new Class<?>[ListUtil.notEmpty(objects) ? objects.length : 0];

		for (int i = 0; i < types.length; i++)
			types[i] = Primitives.unwrap(objects[i] != null ? objects[i].getClass() : null);

		return types;
	}

}