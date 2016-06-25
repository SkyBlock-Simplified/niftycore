package net.netcoding.nifty.core.api.service;

public final class ServiceProvider<T, P> {

	private final P plugin;
	private final Class<T> service;
	private final T provider;

	ServiceProvider(P plugin, Class<T> service, T provider) {
		this.plugin = plugin;
		this.service = service;
		this.provider = provider;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		else if (obj == this)
			return true;
		else {
			ServiceProvider provider = (ServiceProvider)obj;
			return provider.getService().equals(this.getService());
		}
	}

	public T getProvider() {
		return this.provider;
	}

	public P getPlugin() {
		return this.plugin;
	}

	public Class<T> getService() {
		return this.service;
	}

	@Override
	public int hashCode() {
		return this.getService().hashCode();
	}

}