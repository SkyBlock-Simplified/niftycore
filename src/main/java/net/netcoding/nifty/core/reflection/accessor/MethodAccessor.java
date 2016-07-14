package net.netcoding.nifty.core.reflection.accessor;

import net.netcoding.nifty.core.reflection.Reflection;
import net.netcoding.nifty.core.reflection.exceptions.ReflectionException;

import java.lang.reflect.Method;

/**
 * Grants simpler access to method invoking.
 */
public final class MethodAccessor extends ReflectionAccessor<Method> {

	private final Method method;

	public MethodAccessor(Reflection reflection, Method method) {
		super(reflection);
		this.method = method;
	}

	@Override
	protected Method getHandle() {
		return this.method;
	}

	/**
	 * Gets the method associated with this accessor.
	 *
	 * @return The method.
	 */
	public Method getMethod() {
		return this.getHandle();
	}

	/**
	 * Gets the value of an invoked method with matching {@link #getClazz() class type}.
	 * <p>
	 * This is the same as calling {@link #invoke(Object, Object...) invoke(null, args)}.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param args The arguments with matching types to pass to the method.
	 * @return The invoked method value with matching return type.
	 * @throws ReflectionException When the static method is passed invalid arguments.
	 */
	public Object invoke(Object... args) throws ReflectionException {
		return this.invoke(null, args);
	}

	/**
	 * Gets the value of an invoked method with matching {@link #getClazz() class type}.
	 * <p>
	 * Super classes are automatically checked.
	 *
	 * @param obj Instance of the current class object, null if static field.
	 * @param args The arguments with matching types to pass to the method.
	 * @return The invoked method value with matching return type.
	 * @throws ReflectionException When the method is passed invalid arguments.
	 */
	public Object invoke(Object obj, Object... args) throws ReflectionException {
		try {
			return this.getMethod().invoke(obj, args);
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

}