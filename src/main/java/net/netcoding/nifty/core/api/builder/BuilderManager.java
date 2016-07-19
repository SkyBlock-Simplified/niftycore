package net.netcoding.nifty.core.api.builder;

import com.google.common.base.Preconditions;
import net.netcoding.nifty.core.NiftyCore;
import net.netcoding.nifty.core.api.builder.exceptions.RegisteredBuilderException;
import net.netcoding.nifty.core.api.builder.exceptions.UnknownBuilderException;
import net.netcoding.nifty.core.api.service.exceptions.UnknownServiceException;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.core.util.concurrent.Concurrent;
import net.netcoding.nifty.core.util.concurrent.ConcurrentSet;

/**
 * Manager for builders that assist in constructing classes.
 * <p>
 * This should be used in an API, and only once across all projects.
 *
 * @param <P> Plugin class type used for identification purposes.
 */
@SuppressWarnings("unchecked")
public class BuilderManager<P> {

	private final transient ConcurrentSet<BuilderProvider> BUILDERS = Concurrent.newSet();

	/**
	 * Checks if the given service class has a registered builder.
	 *
	 * @param service Class type to check.
	 * @return True if class has a registered builder.
	 */
	public final boolean isRegistered(Class<?> service) {
		Preconditions.checkArgument(service != null, "Service cannot be NULL!");

		for (BuilderProvider provider : BUILDERS) {
			if (provider.getService().getName().equals(service.getName()))
				return true;
		}

		return false;
	}

	/**
	 * Gets the builder provider for the given service class.
	 *
	 * @param service Class type to get.
	 * @param <T> Type of service.
	 * @param <B> Type of builder class.
	 * @return Builder provider for the given class.
	 * @throws UnknownBuilderException When the given service class does not have a registered builder.
	 * @see #isRegistered(Class)
	 */
	public final <T, B extends BuilderCore<T>> BuilderProvider<P, T, B> getBuilderProvider(Class<T> service) throws UnknownBuilderException {
		if (this.isRegistered(service)) {
			for (BuilderProvider provider : BUILDERS) {
				if (provider.getService().getName().equals(service.getName()))
					return provider;
			}
		}

		for (BuilderProvider provider : BUILDERS) {
			if (provider.getService().isAssignableFrom(service)) {
				NiftyCore.getNiftyLogger().warning(StringUtil.format("Service ''{0}'' is superclass of ''{1}''!", service.getName(), provider.getService().getName()));
				return provider;
			}
		}

		throw new UnknownServiceException(service);
	}

	/**
	 * Gets a new builder instance for the given service class.
	 *
	 * @param service Class type to get.
	 * @param <T> Type of service.
	 * @param <B> Type of builder class.
	 * @return Builder instance for the given class.
	 * @throws UnknownBuilderException When the given service class does not have a registered builder.
	 * @see #isRegistered(Class)
	 */
	public final <T, B extends BuilderCore<T>> B createBuilder(Class<T> service) throws UnknownBuilderException {
		return (B)this.getBuilderProvider(service).createBuilder();
	}

	/**
	 * Registers a builder for the given service class.
	 *
	 * @param plugin Plugin class used for identification.
	 * @param service Service class.
	 * @param builder Builder class.
	 * @param <T> Type of service.
	 * @param <B> Type of builder class.
	 * @throws RegisteredBuilderException When the given service class already has a registered builder.
	 */
	public final <T, B extends BuilderCore<T>> void provide(P plugin, Class<T> service, Class<B> builder) throws RegisteredBuilderException {
		Preconditions.checkArgument(plugin != null, "Plugin cannot be NULL!");

		if (this.isRegistered(service))
			throw new RegisteredBuilderException(service);

		BUILDERS.add(new BuilderProvider<>(plugin, service, builder));
	}

}