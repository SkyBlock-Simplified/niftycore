package net.netcoding.nifty.core.api.service;

import com.google.common.base.Preconditions;
import net.netcoding.nifty.core.NiftyCore;
import net.netcoding.nifty.core.api.service.exceptions.RegisteredServiceException;
import net.netcoding.nifty.core.api.service.exceptions.UnknownServiceException;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.core.util.concurrent.Concurrent;
import net.netcoding.nifty.core.util.concurrent.ConcurrentSet;

/**
 * Manager for containing services that assist in instance access.
 * <p>
 * This should be used in an API, and only once across all projects.
 *
 * @param <P> Plugin class type used for identification purposes.
 */
@SuppressWarnings("unchecked")
public class ServiceManager<P> {

	private final transient ConcurrentSet<ServiceProvider> SERVICES = Concurrent.newSet();

	/**
	 * Checks if the given service class has a registered instance.
	 *
	 * @param service Class type to check.
	 * @return True if class has a registered instance.
	 */
	public final boolean isRegistered(Class<?> service) {
		Preconditions.checkArgument(service != null, "Service cannot be NULL!");

		for (ServiceProvider provider : SERVICES) {
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
	 * @return Builder provider for the given class.
	 * @throws UnknownServiceException When the given service class does not have a registered instance.
	 * @see #isRegistered(Class)
	 */
	public final <T> ServiceProvider<T, P> getServiceProvider(Class<T> service) throws UnknownServiceException {
		if (this.isRegistered(service)) {
			for (ServiceProvider provider : SERVICES) {
				if (provider.getService().getName().equals(service.getName()))
					return provider;
			}
		}

		for (ServiceProvider provider : SERVICES) {
			if (provider.getService().isAssignableFrom(service)) {
				NiftyCore.getNiftyLogger().warning(StringUtil.format("Service ''{0}'' is superclass of ''{1}''!", service.getName(), provider.getService().getName()));
				return provider;
			}
		}

		throw new UnknownServiceException(service);
	}

	/**
	 * Gets the instance for the given service class.
	 *
	 * @param service Class type to get.
	 * @param <T> Type of service.
	 * @return Builder instance for the given class.
	 * @throws UnknownServiceException When the given service class does not have a registered instance.
	 * @see #isRegistered(Class)
	 */
	public final <T> T getProvider(Class<T> service) throws UnknownServiceException {
		if (this.isRegistered(service))
			return this.getServiceProvider(service).getProvider();

		for (ServiceProvider provider : SERVICES) {
			if (provider.getService().isAssignableFrom(service)) {
				NiftyCore.getNiftyLogger().warning(StringUtil.format("Service ''{0}'' is superclass of ''{1}''!", service.getName(), provider.getService().getName()));
				return service.cast(provider.getProvider());
			}
		}

		throw new UnknownServiceException(service);
	}

	/**
	 * Registers an instance for the given service class.
	 *
	 * @param plugin Plugin class used for identification.
	 * @param service Service class.
	 * @param instance Instance of service.
	 * @param <T> Type of service.
	 * @throws RegisteredServiceException When the given class already has a registered service.
	 */
	public final <T> void provide(P plugin, Class<T> service, T instance) throws RegisteredServiceException {
		Preconditions.checkArgument(plugin != null, "Plugin cannot be NULL!");
		Preconditions.checkArgument(instance != null, "Instance cannot be NULL!");

		if (this.isRegistered(service))
			throw new RegisteredServiceException(service);

		SERVICES.add(new ServiceProvider<>(plugin, service, instance));
	}

}