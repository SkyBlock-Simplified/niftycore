package net.netcoding.niftycore.reflection;

import net.netcoding.niftycore.reflection.exceptions.ReflectionException;
import net.netcoding.niftycore.util.StringUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Annotations {

	private static Constructor<?> AnnotationInvocationHandler_constructor;
	private static Constructor<?> AnnotationData_constructor;
	private static Method Class_annotationData;
	private static Field Class_classRedefinedCount;
	private static Field AnnotationData_annotations;
	private static Field AnnotationData_declaredAnotations;
	private static Method Atomic_casAnnotationData;
	private static Class<?> Atomic_class;

	static {
		// Prototype Runtime Annotations

		try {
			Class<?> AnnotationInvocationHandler_class = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
			AnnotationInvocationHandler_constructor = AnnotationInvocationHandler_class.getDeclaredConstructor(Class.class, Map.class);
			Class_classRedefinedCount = Class.class.getDeclaredField("classRedefinedCount");

			if (Reflection.JAVA_VERSION >= 1.8) {
				Atomic_class = Class.forName("java.lang.Class$Atomic");
				Class<?> AnnotationData_class = Class.forName("java.lang.Class$AnnotationData");

				AnnotationData_constructor = AnnotationData_class.getDeclaredConstructor(Map.class, Map.class, int.class);
				AnnotationData_constructor.setAccessible(true);

				Class_annotationData = Class.class.getDeclaredMethod("annotationData");
				Class_annotationData.setAccessible(true);

				AnnotationData_annotations = AnnotationData_class.getDeclaredField("annotations");
				AnnotationData_declaredAnotations = AnnotationData_class.getDeclaredField("declaredAnnotations");

				Atomic_casAnnotationData = Atomic_class.getDeclaredMethod("casAnnotationData", Class.class, AnnotationData_class, AnnotationData_class);
				Atomic_casAnnotationData.setAccessible(true);
			} else if (Reflection.JAVA_VERSION >= 1.7) {
				AnnotationData_annotations = Class.class.getDeclaredField("annotations");
				AnnotationData_declaredAnotations = Class.class.getDeclaredField("declaredAnnotations");
			}

			AnnotationInvocationHandler_constructor.setAccessible(true);
			Class_classRedefinedCount.setAccessible(true);
			AnnotationData_declaredAnotations.setAccessible(true);
			AnnotationData_annotations.setAccessible(true);
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	private static <T extends Annotation> T annotationForMap(final Class<T> annotationClass, final Map<String, Object> valuesMap) {
		return (T)AccessController.doPrivileged(new PrivilegedAction<Annotation>() {
			public Annotation run() {
				try {
					InvocationHandler handler = (InvocationHandler)AnnotationInvocationHandler_constructor.newInstance(annotationClass, new HashMap<>(valuesMap));
					return (Annotation) Proxy.newProxyInstance(annotationClass.getClassLoader(), new Class[] { annotationClass }, handler);
				} catch (Exception ex) {
					throw new ReflectionException(ex);
				}
			}
		});
	}

	/**
	 * 1.8+
	 */
	private static <T extends Annotation> Object createAnnotationData(Object annotationData, Class<T> annotationClass, T annotation, int classRedefinedCount) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Map<Class<? extends Annotation>, Annotation> annotations = (Map<Class<? extends Annotation>, Annotation>)AnnotationData_annotations.get(annotationData);
		Map<Class<? extends Annotation>, Annotation> declaredAnnotations = (Map<Class<? extends Annotation>, Annotation>)AnnotationData_declaredAnotations.get(annotationData);
		Map<Class<? extends Annotation>, Annotation> newAnnotations;
		Map<Class<? extends Annotation>, Annotation> newDeclaredAnnotations = new LinkedHashMap<>(annotations);
		newDeclaredAnnotations.put(annotationClass, annotation);

		if (declaredAnnotations.equals(annotations))
			newAnnotations = newDeclaredAnnotations;
		else {
			newAnnotations = new LinkedHashMap<>(annotations);
			newAnnotations.put(annotationClass, annotation);
		}

		return AnnotationData_constructor.newInstance(newAnnotations, newDeclaredAnnotations, classRedefinedCount);
	}

	public static <T extends Annotation> void putAnnotation(Class<?> clazz, Class<T> annotationClass, Map<String, Object> valuesMap) throws ReflectionException {
		putAnnotation(clazz, annotationClass, annotationForMap(annotationClass, valuesMap));
	}

	public static <T extends Annotation> void putAnnotation(Class<?> clazz, Class<T> annotationClass, T annotation) throws ReflectionException {
		try {
			if (Reflection.JAVA_VERSION >= 1.8) {
				while (true) { // retry loop
					int classRedefinedCount = Class_classRedefinedCount.getInt(clazz);
					Object annotationData = Class_annotationData.invoke(clazz); // 1.7+
					// null or stale annotationData -> optimistically create new instance
					Object newAnnotationData = createAnnotationData(annotationData, annotationClass, annotation, classRedefinedCount); // 1.7+

					// try to install it
					if ((boolean) Atomic_casAnnotationData.invoke(Atomic_class, clazz, annotationData, newAnnotationData)) // 1.8+
						break; // successfully installed new AnnotationData
				}
			} else if (Reflection.JAVA_VERSION >= 1.7) {
				long declaredAnnotationsOffset = Reflection.getUnsafe().objectFieldOffset(AnnotationData_declaredAnotations);
				long annotationsOffset = Reflection.getUnsafe().objectFieldOffset(AnnotationData_annotations);
				Map<Class<? extends Annotation>, Annotation> declaredAnnotations = getDeclaredAnnotations(Class.class);
				Map<Class<? extends Annotation>, Annotation> annotations = getAnnotations(Class.class);
				Map<Class<? extends Annotation>, Annotation> newDeclaredAnnotations = getNewDeclaredAnnotations(annotations, annotationClass, annotation);
				Map<Class<? extends Annotation>, Annotation> newAnnotations = getNewAnnotations(annotations, declaredAnnotations, annotationClass, annotation);

				while (true) {
					boolean replacedAnnotations = Reflection.getUnsafe().compareAndSwapObject(clazz, annotationsOffset, annotations, newAnnotations);
					boolean replacedDeclaredAnnotations = Reflection.getUnsafe().compareAndSwapObject(clazz, declaredAnnotationsOffset, declaredAnnotations, newDeclaredAnnotations);

					if (replacedAnnotations && replacedDeclaredAnnotations)
						break;
				}
			} else
				throw new ReflectionException(StringUtil.format("Runtime annotation modification does not support: {0}!", Reflection.JAVA_VERSION));
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	private static Map<Class<? extends Annotation>, Annotation> getAnnotations(Object obj) throws IllegalAccessException, IllegalArgumentException {
		return (Map<Class<? extends Annotation>, Annotation>)AnnotationData_annotations.get(obj);
	}

	private static Map<Class<? extends Annotation>, Annotation> getDeclaredAnnotations(Object obj) throws IllegalAccessException, IllegalArgumentException {
		return (Map<Class<? extends Annotation>, Annotation>)AnnotationData_declaredAnotations.get(obj);
	}

	private static <T extends Annotation> Map<Class<? extends Annotation>, Annotation> getNewAnnotations(Map<Class<? extends Annotation>, Annotation> annotations, Map<Class<? extends Annotation>, Annotation> declaredAnnotations, Class<T> annotationClass, T annotation) {
		Map<Class<? extends Annotation>, Annotation> newAnnotations;
		Map<Class<? extends Annotation>, Annotation> newDeclaredAnnotations = getNewDeclaredAnnotations(annotations, annotationClass, annotation);

		if (declaredAnnotations.equals(annotations))
			newAnnotations = newDeclaredAnnotations;
		else {
			newAnnotations = new LinkedHashMap<>(annotations);
			newAnnotations.put(annotationClass, annotation);
		}

		return newAnnotations;
	}

	private static <T extends Annotation> Map<Class<? extends Annotation>, Annotation> getNewDeclaredAnnotations(Map<Class<? extends Annotation>, Annotation> annotations, Class<T> annotationClass, T annotation) {
		Map<Class<? extends Annotation>, Annotation> newDeclaredAnnotations = new LinkedHashMap<>(annotations);
		newDeclaredAnnotations.put(annotationClass, annotation);
		return newDeclaredAnnotations;
	}

}