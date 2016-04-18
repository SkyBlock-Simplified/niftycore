package net.netcoding.niftycore.yaml;

import java.io.File;

/**
 * PLEASE USE {@link YamlConfig} INSTEAD OF THIS CLASS NOW
 */
@Deprecated
public class Config extends YamlConfig {

	public Config(File folder, String fileName, String... header) {
		this(folder, fileName, false, header);
	}

	public Config(File folder, String fileName, boolean skipFailedConversion, String... header) {
		super(folder, fileName, skipFailedConversion, header);
	}

}