package net.netcoding.nifty.core.api.plugin;

import java.io.File;

public final class PluginDescription {

	private final String name;
	private final File file;
	private final File dataFolder;
	private final String version;

	public PluginDescription(String name, File file, File dataFolder, String version) {
		this.name = name;
		this.file = file;
		this.dataFolder = dataFolder;
		this.version = version;
	}

	public File getDataFolder() {
		return this.dataFolder;
	}

	public File getFile() {
		return this.file;
	}

	public String getName() {
		return this.name;
	}

	public String getVersion() {
		return this.version;
	}

}