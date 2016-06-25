package net.netcoding.nifty.core.api.plugin;

import com.google.common.base.Preconditions;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.core.api.logger.BroadcasttLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public interface Plugin<T extends BroadcasttLogger> extends PluginHelper<T> {

	default String getName() {
		return this.getPluginDescription().getName();
	}

	default File getDataFolder() {
		return this.getPluginDescription().getDataFolder();
	}

	default InputStream getResource(String resourcePath) {
		Preconditions.checkArgument(StringUtil.notEmpty(resourcePath), "Resource path cannot be NULL");
		URL url = this.getClass().getClassLoader().getResource(resourcePath);

		if (url != null) {
			try {
				URLConnection connection = url.openConnection();
				connection.setUseCaches(false);

				try (InputStream inputStream = connection.getInputStream()) {
					return inputStream;
				}
			} catch (IOException ignore) { }
		}

		throw new IllegalArgumentException(StringUtil.format("No resource with name ''{0}'' found!"));
	}

	default void saveResource(String resourcePath, boolean replace) {
		File output = new File(this.getDataFolder(), resourcePath);

		try (InputStream inputStream = this.getResource(resourcePath)) {
			if (!this.getDataFolder().exists()) {
				if (!this.getDataFolder().mkdirs())
					throw new IllegalStateException(StringUtil.format("Unable to create parent directories for ''{0}''!", output));
			}

			try (FileOutputStream outputStream = new FileOutputStream(output)) {
				byte[] buffer = new byte[1024];
				int length;

				while ((length = inputStream.read(buffer)) > 0)
					outputStream.write(buffer, 0, length);
			}
		} catch (Exception ignore) {
			throw new IllegalStateException(StringUtil.format("Unable to save resource ''{0}'' to ''{1}''!", resourcePath, output));
		}
	}

	/*private long enable = System.currentTimeMillis();

	public final void startLoggingTime() {
		this.enable = System.currentTimeMillis();
	}

	public final void showRunningTime() {
		this.showRunningTime(TimeUnit.MILLISECONDS);
	}

	public final void showRunningTime(TimeUnit time) {
		this.getLog().console("Running time: {0} {1}", time.convert((System.currentTimeMillis() - this.enable), TimeUnit.MILLISECONDS), time.name().toLowerCase());
	}*/

}