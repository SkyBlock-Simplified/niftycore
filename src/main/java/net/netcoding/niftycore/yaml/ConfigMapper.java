package net.netcoding.niftycore.yaml;

import net.netcoding.niftycore.util.ListUtil;
import net.netcoding.niftycore.util.StringUtil;
import net.netcoding.niftycore.yaml.exceptions.InvalidConfigurationException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ConfigMapper extends YamlMap {

	private final transient Yaml yaml;
	private final transient Map<String, ArrayList<String>> comments = new LinkedHashMap<>();
	private transient String[] header;
	private final transient NullRepresenter representer = new NullRepresenter();
	transient File configFile;
	transient ConfigSection root;

	protected ConfigMapper(File folder, String fileName, String... header) {
		if (StringUtil.isEmpty(fileName)) throw new IllegalArgumentException("Filename cannot be null!");
		this.configFile = new File(folder, fileName + (fileName.endsWith(".yml") ? "" : ".yml"));
		this.header = header;
		DumperOptions options = new DumperOptions();
		options.setIndent(2);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		this.representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		this.yaml = new Yaml(new CustomClassLoaderConstructor(ConfigMapper.class.getClassLoader()), this.representer, options);
	}

	public void addComment(String key, String value) {
		if (!this.comments.containsKey(key))
			this.comments.put(key, new ArrayList<String>());

		this.comments.get(key).add(value);
	}

	public void clearComments() {
		this.comments.clear();
	}

	private void convertMapsToSections(Map<?, ?> input, ConfigSection section) {
		if (input == null) return;

		for (Map.Entry<?, ?> entry : input.entrySet()) {
			String key = entry.getKey().toString();
			Object value = entry.getValue();

			if (value instanceof Map)
				this.convertMapsToSections((Map<?, ?>)value, section.create(key));
			else
				section.set(key, value, false);
		}
	}

	public final String getFullName() {
		return this.configFile.getName();
	}

	public final String getName() {
		return this.getFullName().replace(".yml", "");
	}

	public final File getParentDirectory() {
		return this.configFile.getAbsoluteFile().getParentFile();
	}

	protected void loadFromYaml() throws InvalidConfigurationException {
		this.root = new ConfigSection();

		try (InputStreamReader fileReader = new InputStreamReader(new FileInputStream(this.configFile), StandardCharsets.UTF_8)) {
			Object object = this.yaml.load(fileReader);

			if (object != null)
				convertMapsToSections((Map<?, ?>)object, this.root);
		} catch (IOException | ClassCastException | YAMLException ex) {
			throw new InvalidConfigurationException("Could not load YML", ex);
		}
	}

	protected void saveToYaml() throws InvalidConfigurationException {
		try (OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(this.configFile), StandardCharsets.UTF_8)) {
			if (ListUtil.notEmpty(this.header)) {
				for (String line : this.header)
					fileWriter.write("# " + line + "\n");

				fileWriter.write("\n");
			}

			Integer depth = 0;
			ArrayList<String> keyChain = new ArrayList<>();
			String yamlString = this.yaml.dump(root.getValues(true));
			StringBuilder writeLines = new StringBuilder();
			String[] yamlSplit = yamlString.split("\n");

			for (int y = 0; y < yamlSplit.length; y++) {
				String line = yamlSplit[y];
				int spaces = line.length() - line.replaceAll("^\\s+", "").length();

				if (line.startsWith(new String(new char[depth]).replace("\0", " "))) {
					keyChain.add(line.split(":")[0].trim());
					depth += 2;
				} else {
					if (line.startsWith(new String(new char[depth - 2]).replace("\0", " ")))
						keyChain.remove(keyChain.size() - 1);
					else {
						if (spaces == 0) {
							keyChain = new ArrayList<>();
							depth = 2;
						} else {
							ArrayList<String> temp = new ArrayList<>();
							depth = spaces;
							int index = 0;

							for (int i = 0; i < spaces; i += 2, index++)
								temp.add(keyChain.get(index));

							keyChain = temp;
							depth += 2;
						}
					}

					keyChain.add(line.split(":")[0].trim());
				}

				String search = (!keyChain.isEmpty() ? StringUtil.implode(".", keyChain) : "");
				if (this.comments.containsKey(search)) {
					for (String comment : comments.get(search)) {
						writeLines.append(new String(new char[depth - 2]).replace("\0", " "));
						writeLines.append("# ");
						writeLines.append(comment);
						writeLines.append("\n");
					}
				}

				writeLines.append(line);

				if (y < yamlSplit.length - 1) {
					String nextLine = yamlSplit[y + 1];
					int nextSpaces = nextLine.length() - nextLine.replaceAll("^\\s+", "").length();

					if ((spaces == 0 && nextSpaces == 0) || nextSpaces == 0)
						writeLines.append('\n');

					writeLines.append("\n");
				}
			}

			fileWriter.write(writeLines.toString());
		} catch (IOException e) {
			throw new InvalidConfigurationException("Could not save YML", e);
		}
	}

	private class NullRepresenter extends Representer {

		public NullRepresenter() {
			this.nullRepresenter = new RepresentNull();
		}

		private class RepresentNull implements Represent {

			public Node representData(Object data) {
				return representScalar(Tag.NULL, "");
			}

		}

	}

}