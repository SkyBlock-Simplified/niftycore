package net.netcoding.nifty.core.api.service;

import com.google.common.base.Preconditions;
import net.netcoding.nifty.core.NiftyCore;
import net.netcoding.nifty.core.api.service.exceptions.RegisteredServiceException;
import net.netcoding.nifty.core.api.service.exceptions.UnknownServiceException;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.core.util.concurrent.ConcurrentSet;
import sun.reflect.Reflection;

@SuppressWarnings("unchecked")
public class ServiceManager<P> {

	private final transient ConcurrentSet<ServiceProvider> SERVICES = new ConcurrentSet<>();

	public final boolean isRegistered(Class<?> service) {
		Preconditions.checkArgument(service != null, "Service cannot be NULL!");

		for (ServiceProvider provider : SERVICES) {
			if (provider.getService().getName().equals(service.getName()))
				return true;
		}

		return false;
	}

	public final <T> ServiceProvider<T, P> getServiceProvider(Class<T> service) throws UnknownServiceException {
		if (this.isRegistered(service)) {
			for (ServiceProvider provider : SERVICES) {
				if (provider.getService().getName().equals(service.getName()))
					return provider;
			}
		}

		for (ServiceProvider provider : SERVICES) {
			if (provider.getService().isAssignableFrom(service)) {
				NiftyCore.getLogger().warning(StringUtil.format("Service ''{0}'' is superclass of ''{1}''!", service.getName(), provider.getService().getName()));
				return provider;
			}
		}

		throw new UnknownServiceException(service);
	}

	public final <T> T getProvider(Class<T> service) throws UnknownServiceException {
		if (this.isRegistered(service))
			return this.getServiceProvider(service).getProvider();

		for (ServiceProvider provider : SERVICES) {
			if (provider.getService().isAssignableFrom(service)) {
				NiftyCore.getLogger().warning(StringUtil.format("Service ''{0}'' is superclass of ''{1}''!", service.getName(), provider.getService().getName()));
				return service.cast(provider.getProvider());
			}
		}

		throw new UnknownServiceException(service);
	}

	public final <T> void provide(P plugin, Class<T> service, T instance) throws RegisteredServiceException {
		Preconditions.checkArgument(plugin != null, "Plugin cannot be NULL!");
		Preconditions.checkArgument(instance != null, "Instance cannot be NULL!");

		if (this.isRegistered(service))
			throw new RegisteredServiceException(service);

		for (int i = 0; i < 10; i++)
			System.out.println("REF CALL: " + Reflection.getCallerClass(i).getName()); // TODO

		SERVICES.add(new ServiceProvider<>(plugin, service, instance));
	}

}