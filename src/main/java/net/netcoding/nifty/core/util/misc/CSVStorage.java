package net.netcoding.nifty.core.util.misc;

import com.google.common.base.Preconditions;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.core.util.concurrent.ConcurrentList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;

public abstract class CSVStorage {

	private final File file;

	public CSVStorage(File folder, String fileName) {
		Preconditions.checkArgument(StringUtil.notEmpty(fileName), "Filename cannot be NULL!");
		this.file = new File(folder, fileName + (fileName.endsWith(".csv") ? "" : ".csv"));
		this.reload();
	}

	protected final ClassLoader getClassLoader() {
		return this.getClass().getClassLoader();
	}

	public final List<String> getLines() throws IOException {
		try (InputStreamReader inputStream = (this.getLocalFile().exists() ? new FileReader(this.getLocalFile()) : new InputStreamReader(this.getResource()))) {
			try (BufferedReader reader = new BufferedReader(inputStream)) {
				ConcurrentList<String> lines = new ConcurrentList<>();

				do {
					String line = reader.readLine();
					if (StringUtil.isEmpty(line)) break;
					lines.add(line);
				} while (true);

				return Collections.unmodifiableList(lines);
			}
		}
	}

	protected final InputStream getResource() {
		URL url = this.getClassLoader().getResource(this.file.getName());

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

	protected final File getLocalFile() {
		return this.file;
	}

	public abstract void reload();

	public final void save() {
		this.save(false);
	}

	public final void save(boolean replace) {
		try (InputStream inputStream = this.getResource()) {
			File parent = this.file.getAbsoluteFile().getParentFile();

			if (!parent.exists()) {
				if (!parent.mkdirs())
					throw new IllegalStateException(StringUtil.format("Unable to create parent directories for ''{0}''!", this.file));
			}

			try (FileOutputStream outputStream = new FileOutputStream(this.file)) {
				byte[] buffer = new byte[1024];
				int length;

				while ((length = inputStream.read(buffer)) > 0)
					outputStream.write(buffer, 0, length);
			}
		} catch (Exception ignore) {
			throw new IllegalStateException(StringUtil.format("Unable to save resource ''{0}'' to ''{1}''!", this.file.getName(), this.file));
		}
	}

}