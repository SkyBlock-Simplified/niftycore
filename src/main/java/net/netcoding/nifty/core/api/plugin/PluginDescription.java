package net.netcoding.nifty.core.api.plugin;

import java.io.File;

public final class PluginDescription {

	private final String name;
	private final File file;
	private final File dataFolder;

	public PluginDescription(String name, File file, File dataFolder) {
		this.name = name;
		this.file = file;
		this.dataFolder = dataFolder;
	}

	public final String getName() {
		return this.name;
	}

	public final File getFile() {
		return this.file;
	}

	public final File getDataFolder() {
		return this.dataFolder;
	}

}