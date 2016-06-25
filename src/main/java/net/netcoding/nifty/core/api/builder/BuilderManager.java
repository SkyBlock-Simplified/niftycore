package net.netcoding.nifty.core.api.builder;

import com.google.common.base.Preconditions;
import net.netcoding.nifty.core.NiftyCore;
import net.netcoding.nifty.core.api.builder.exceptions.RegisteredBuilderException;
import net.netcoding.nifty.core.api.builder.exceptions.UnknownBuilderException;
import net.netcoding.nifty.core.api.service.exceptions.UnknownServiceException;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.core.util.concurrent.ConcurrentSet;

@SuppressWarnings("unchecked")
public class BuilderManager<P> {

	private final transient ConcurrentSet<BuilderProvider> BUILDERS = new ConcurrentSet<>();

	public final boolean isRegistered(Class<?> service) {
		Preconditions.checkArgument(service != null, "Service cannot be NULL!");

		for (BuilderProvider provider : BUILDERS) {
			if (provider.getBuilder().getName().equals(service.getName()))
				return true;
		}

		return false;
	}

	public final <T, B extends BuilderCore<T>> BuilderProvider<P, T, B> getBuilderProvider(Class<T> service) throws UnknownBuilderException {
		if (this.isRegistered(service)) {
			for (BuilderProvider provider : BUILDERS) {
				if (provider.getBuilder().getName().equals(service.getName()))
					return provider;
			}
		}

		for (BuilderProvider provider : BUILDERS) {
			if (provider.getService().isAssignableFrom(service)) {
				NiftyCore.getLogger().warning(StringUtil.format("Service ''{0}'' is superclass of ''{1}''!", service.getName(), provider.getBuilder().getName()));
				return provider;
			}
		}

		throw new UnknownServiceException(service);
	}

	public final <T, B extends BuilderCore<T>> B createBuilder(Class<T> service) throws UnknownBuilderException {
		if (this.isRegistered(service))
			return (B)this.getBuilderProvider(service).createBuilder();

		for (BuilderProvider provider : BUILDERS) {
			if (provider.getService().isAssignableFrom(service)) {
				NiftyCore.getLogger().warning(StringUtil.format("Service ''{0}'' is superclass of ''{1}''!", service.getName(), provider.getBuilder().getName()));
				return (B)provider.createBuilder();
			}
		}

		throw new UnknownBuilderException(service);
	}

	public final <T, B extends BuilderCore<T>> void provide(P plugin, Class<T> service, Class<B> builder) throws RegisteredBuilderException {
		Preconditions.checkArgument(plugin != null, "Plugin cannot be NULL!");

		if (this.isRegistered(service))
			throw new RegisteredBuilderException(service);

		BUILDERS.add(new BuilderProvider<>(plugin, service, builder));
	}

}