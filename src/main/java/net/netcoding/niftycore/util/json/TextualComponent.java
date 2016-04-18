package net.netcoding.niftycore.util.json;

import com.google.common.collect.ImmutableMap;
import com.google.gson.stream.JsonWriter;
import net.netcoding.niftycore.util.StringUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a textual component of a message part.
 * This can be used to not only represent string literals in a JSON message,
 * but also to represent localized strings and other text values.
 * <p>Different instances of this class can be created with static constructor methods.</p>
 */
public abstract class TextualComponent implements Cloneable {

	protected String key;

	/**
	 * Clones a textual component instance.
	 * The returned object should not reference this textual component instance, but should maintain the same key and value.
	 */
	@Override
	public abstract TextualComponent clone();

	/**
	 * Gets the json key.
	 *
	 * @return The JSON key used to represent text components of this type.
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * Gets a readable json string.
	 *
	 * @return A readable String
	 */
	public abstract String getReadableString();

	@Override
	public String toString() {
		return getReadableString();
	}

	/**
	 * Writes the text data represented by this textual component to the specified JSON writer object.
	 * A new object within the writer is not started.
	 *
	 * @param writer The object to which to write the JSON data.
	 * @throws IOException If an error occurs while writing to the stream.
	 */
	public abstract void writeJson(JsonWriter writer) throws IOException;

	static TextualComponent deserialize(Map<String, Object> map){
		if (map.containsKey("key") && map.size() == 2 && map.containsKey("value"))
			return ArbitraryTextTypeComponent.deserialize(map);
		else if (map.size() >= 2 && map.containsKey("key") && !map.containsKey("value"))
			return ComplexTextTypeComponent.deserialize(map);

		return null;
	}

	static boolean isTextKey(String key) {
		return Arrays.asList("translate", "text", "score", "selector").contains(key);
	}

	static boolean isTranslatableText(TextualComponent component) {
		return component instanceof ComplexTextTypeComponent && "translate".equals(component.getKey());
	}

	/**
	 * Internal class used to represent all types of text components.
	 * Exception validating done is on keys and values.
	 */
	private static final class ArbitraryTextTypeComponent extends TextualComponent {

		private String value;

		public ArbitraryTextTypeComponent(String key, String value) {
			this.setKey(key);
			this.setValue(value);
		}

		@Override
		public TextualComponent clone() {
			return new ArbitraryTextTypeComponent(getKey(), getValue());
		}

		@Override
		public String getReadableString() {
			return this.getValue();
		}

		public String getValue() {
			return this.value;
		}

		public Map<String, Object> serialize() {
			return new HashMap<String, Object>() {{
				put("key", getKey());
				put("value", getValue());
			}};
		}

		public void setKey(String key) {
			if (StringUtil.isEmpty(key))
				throw new IllegalArgumentException("The key cannot be empty!");

			this.key = key;
		}

		public void setValue(String value) {
			if (StringUtil.isEmpty(value))
				throw new IllegalArgumentException("The value cannot be empty!");

			this.value = value;
		}

		@Override
		public void writeJson(JsonWriter writer) throws IOException {
			writer.name(getKey()).value(getValue());
		}

		public static ArbitraryTextTypeComponent deserialize(Map<String, Object> map) {
			return new ArbitraryTextTypeComponent(map.get("key").toString(), map.get("value").toString());
		}

	}

	/**
	 * Internal class used to represent a text component with a nested JSON value.
	 * Exception validating done is on keys and values.
	 */
	private static final class ComplexTextTypeComponent extends TextualComponent {

		private Map<String, String> values;

		public ComplexTextTypeComponent(String key, Map<String, String> values) {
			this.setKey(key);
			this.setValue(values);
		}

		@Override
		public TextualComponent clone() {
			return new ComplexTextTypeComponent(getKey(), getValues());
		}

		@Override
		public String getReadableString() {
			return getKey();
		}

		public Map<String, String> getValues() {
			return this.values;
		}

		public Map<String, Object> serialize() {
			return new java.util.HashMap<String, Object>() {{
				put("key", getKey());

				for (Map.Entry<String, String> valEntry : getValues().entrySet()) {
					put("value." + valEntry.getKey(), valEntry.getValue());
				}
			}};
		}

		public void setKey(String key) {
			if (StringUtil.isEmpty(key))
				throw new IllegalArgumentException("The key cannot be empty!");

			this.key = key;
		}

		public void setValue(Map<String, String> value) {
			if (value == null)
				throw new IllegalArgumentException("The value cannot be empty!");

			this.values = value;
		}

		@Override
		public void writeJson(JsonWriter writer) throws IOException {
			writer.name(this.getKey());
			writer.beginObject();

			for (Map.Entry<String, String> entry : this.getValues().entrySet())
				writer.name(entry.getKey()).value(entry.getValue());

			writer.endObject();
		}

		public static ComplexTextTypeComponent deserialize(Map<String, Object> map) {
			String key = null;
			Map<String, String> value = new HashMap<>();

			for (Map.Entry<String, Object> entry : map.entrySet()) {
				if (entry.getKey().equals("key"))
					key = (String) entry.getValue();
				else if (entry.getKey().startsWith("value."))
					value.put(entry.getKey().substring(6), entry.getValue().toString());
			}

			return new ComplexTextTypeComponent(key, value);
		}

	}

	/**
	 * Create a textual component representing a string literal.
	 * This is the default type of textual component when a single string literal is given to a method.
	 *
	 * @param textValue The text which will be represented.
	 * @return The text component representing the specified literal text.
	 */
	public static TextualComponent rawText(String textValue){
		return new ArbitraryTextTypeComponent("text", textValue);
	}


	/**
	 * Create a textual component representing a localized string.
	 * The client will see this text component as their localized version of the specified string <em>key</em>, which can be overridden by a resource pack.
	 * <p>
	 * If the specified translation key is not present on the client resource pack, the translation key will be displayed as a string literal to the client.
	 * </p>
	 *
	 * @param translateKey The string key which maps to localized text.
	 * @return The text component representing the specified localized text.
	 */
	public static TextualComponent localizedText(String translateKey){
		return new ArbitraryTextTypeComponent("translate", translateKey);
	}

	private static void throwUnsupportedSnapshot(){
		throw new UnsupportedOperationException("This feature is only supported in snapshot releases.");
	}

	/**
	 * Create a textual component representing a scoreboard value.
	 * The client will see their own score for the specified objective as the text represented by this component.
	 * <p>
	 * <b>This method is currently guaranteed to throw an {@code UnsupportedOperationException} as it is only supported on snapshot clients.</b>
	 * </p>
	 *
	 * @param scoreboardObjective The name of the objective for which to display the score.
	 * @return The text component representing the specified scoreboard score (for the viewing player), or {@code null} if an error occurs during JSON serialization.
	 */
	public static TextualComponent objectiveScore(String scoreboardObjective){
		return objectiveScore("*", scoreboardObjective);
	}

	/**
	 * Create a textual component representing a scoreboard value.
	 * The client will see the score of the specified player for the specified objective as the text represented by this component.
	 * <p>
	 * <b>This method is currently guaranteed to throw an {@code UnsupportedOperationException} as it is only supported on snapshot clients.</b>
	 * </p>
	 *
	 * @param playerName The name of the player whos score will be shown. If this string represents the single-character sequence "*", the viewing player's score will be displayed.
	 * Standard minecraft selectors (@a, @p, etc) are <em>not</em> supported.
	 * @param scoreboardObjective The name of the objective for which to display the score.
	 * @return The text component representing the specified scoreboard score for the specified player, or {@code null} if an error occurs during JSON serialization.
	 */
	public static TextualComponent objectiveScore(String playerName, String scoreboardObjective){
		throwUnsupportedSnapshot(); // Remove this line when the feature is released to non-snapshot versions, in addition to updating ALL THE OVERLOADS documentation accordingly

		return new ComplexTextTypeComponent("score", ImmutableMap.<String, String>builder()
				.put("name", playerName)
				.put("objective", scoreboardObjective)
				.build());
	}

	/**
	 * Create a textual component representing a player name, retrievable by using a standard minecraft selector.
	 * The client will see the players or entities captured by the specified selector as the text represented by this component.
	 * <p>
	 * <b>This method is currently guaranteed to throw an {@code UnsupportedOperationException} as it is only supported on snapshot clients.</b>
	 * </p>
	 *
	 * @param selector The minecraft player or entity selector which will capture the entities whose string representations will be displayed in the place of this text component.
	 * @return The text component representing the name of the entities captured by the selector.
	 */
	public static TextualComponent selector(String selector){
		throwUnsupportedSnapshot(); // Remove this line when the feature is released to non-snapshot versions, in addition to updating ALL THE OVERLOADS documentation accordingly

		return new ArbitraryTextTypeComponent("selector", selector);
	}
}