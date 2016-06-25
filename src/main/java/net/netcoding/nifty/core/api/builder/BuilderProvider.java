package net.netcoding.nifty.core.api.builder;

import net.netcoding.nifty.core.reflection.Reflection;

public final class BuilderProvider<P, T, B extends BuilderCore<T>> {

	private final P plugin;
	private final Class<T> service;
	private final Class<B> builder;

	BuilderProvider(P plugin, Class<T> service, Class<B> builder) {
		this.plugin = plugin;
		this.service = service;
		this.builder = builder;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		else if (obj == this)
			return true;
		else {
			BuilderProvider provider = (BuilderProvider)obj;
			return provider.getBuilder().equals(this.getBuilder());
		}
	}

	@SuppressWarnings("unchecked")
	public B createBuilder() {
		return (B)new Reflection(this.getBuilder()).newInstance();
	}

	public Class<B> getBuilder() {
		return this.builder;
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