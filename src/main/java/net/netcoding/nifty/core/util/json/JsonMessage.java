package net.netcoding.nifty.core.util.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import net.netcoding.nifty.core.api.color.ChatColor;
import net.netcoding.nifty.core.util.ListUtil;
import net.netcoding.nifty.core.util.RegexUtil;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.core.util.concurrent.Concurrent;
import net.netcoding.nifty.core.util.concurrent.ConcurrentList;
import net.netcoding.nifty.core.util.concurrent.ConcurrentMap;
import net.netcoding.nifty.core.util.json.events.ClickEvent;
import net.netcoding.nifty.core.util.json.events.HoverEvent;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;

@SuppressWarnings("unchecked")
public class JsonMessage<T extends JsonMessage<T>> implements JsonRepresentedObject, Cloneable, Iterable<MessagePart> {

	protected final ConcurrentList<MessagePart> messageParts = Concurrent.newList();
	protected String jsonString = null;
	protected boolean dirty = false;

	/**
	 * Creates a JSON message without text.
	 */
	public JsonMessage() {
		this((TextualComponent)null);
	}

	/**
	 * Creates a JSON message with text.
	 *
	 * @param firstPartText The existing text in the message.
	 */
	public JsonMessage(String firstPartText) {
		this(TextualComponent.rawText(firstPartText));
	}

	/**
	 * Creates a JSON message with component.
	 *
	 * @param firstPartText The existing text component in the message.
	 */
	public JsonMessage(TextualComponent firstPartText) {
		this.messageParts.add(new MessagePart(firstPartText));
	}

	public JsonMessage(JsonMessage<T> firstPartText) {
		this.messageParts.addAll(firstPartText.messageParts);
	}

	/**
	 * Appends all parts of a legacy message.
	 *
	 * @param message The legacy text to append.
	 * @return This builder instance.
	 */
	public T legacy(String message) {
		List<JsonMessage> converted = fromLegacyText(message);
		converted.forEach(json -> this.messageParts.addAll(json.messageParts));
		this.dirty = true;
		return (T)this;
	}

	/**
	 * Converts the old formatting system that used
	 * {@link ChatColor#COLOR_CHAR} into the new json based
	 * system.
	 *
	 * @param message The text to convert.
	 * @return The components needed to print the message to the client
	 */
	public static List<JsonMessage> fromLegacyText(String message) {
		ConcurrentList<JsonMessage> components = Concurrent.newList();
		StringBuilder builder = new StringBuilder();
		JsonMessage component = new JsonMessage();
		Matcher matcher = RegexUtil.URL_PATTERN.matcher(message);

		for (int i = 0; i < message.length(); i++) {
			char c = message.charAt(i);

			if (c == ChatColor.COLOR_CHAR) {
				c = message.charAt(++i);

				if (c >= 'A' && c <= 'Z')
					c += 32;

				ChatColor format = ChatColor.getByChar(c);
				if (format == null)
					continue;

				if (builder.length() > 0) {
					JsonMessage old = component;
					component = new JsonMessage(old);
					old.text(builder.toString());
					builder = new StringBuilder();
					components.add(old);
				}

				if (format.isFormat())
					component.style(format);
				else {
					component = new JsonMessage();
					component.color(format);
				}

				continue;
			}

			int pos = message.indexOf(' ', i);
			if (pos == -1)
				pos = message.length();

			if (matcher.region(i, pos).find()) { // Web link handling
				if (builder.length() > 0) {
					JsonMessage old = component;
					component = new JsonMessage(old);
					old.text(builder.toString());
					builder = new StringBuilder();
					components.add(old);
				}

				JsonMessage old = component;
				component = new JsonMessage(old);
				String urlString = message.substring( i, pos );
				component.text(urlString);
				component.onClick(ClickEvent.Type.LINK, (urlString.startsWith( "http" ) ? urlString : "http://" + urlString));
				components.add(component);
				i += pos - i - 1;
				component = old;
				continue;
			}

			builder.append(c);
		}

		if (builder.length() > 0) {
			component.text(builder.toString());
			components.add(component);
		}

		// The client will crash if the array is empty
		if (components.isEmpty())
			components.add(new JsonMessage());

		return components;
	}

	@Override
	public Iterator<MessagePart> iterator() {
		return this.messageParts.iterator();
	}

	@Override
	public T clone() throws CloneNotSupportedException{
		T message = (T)super.clone();

		for (MessagePart part : this.messageParts)
			message.messageParts.add(part.clone());

		message.dirty = false;
		message.jsonString = null;
		return message;
	}

	/**
	 * Sets the text of the current editing component to a value.
	 *
	 * @param text The new text of the current editing component.
	 * @return This builder instance.
	 */
	public T text(String text) {
		MessagePart latest = this.latest();
		latest.text = TextualComponent.rawText(text);
		dirty = true;
		return (T)this;
	}

	/**
	 * Sets the text of the current editing component to a value.
	 *
	 * @param text The new text of the current editing component.
	 * @return This builder instance.
	 */
	public T text(TextualComponent text) {
		MessagePart latest = this.latest();
		latest.text = text;
		dirty = true;
		return (T)this;
	}

	/**
	 * Sets the color of the current editing component to a value.
	 *
	 * @param color The new color of the current editing component.
	 * @return This builder instance.
	 * @exception IllegalArgumentException If the specified {@code ChatColor} enumeration value is not a color (but a format value).
	 */
	public T color(ChatColor color) {
		if (!color.isColor())
			throw new IllegalArgumentException(color.name() + " is not a color");

		this.latest().color = color;
		dirty = true;
		return (T)this;
	}

	/**
	 * Sets the stylization of the current editing component.
	 *
	 * @param styles The array of styles to apply to the editing component.
	 * @return This builder instance.
	 * @exception IllegalArgumentException If any of the enumeration values in the array do not represent formatters.
	 */
	public T style(ChatColor... styles) {
		for (final ChatColor style : styles) {
			if (!style.isFormat())
				throw new IllegalArgumentException(style.name() + " is not a style");
		}

		this.latest().styles.addAll(Arrays.asList(styles));
		dirty = true;
		return (T)this;
	}

	/**
	 * Set the behavior of the current editing component to instruct the client
	 * to open a file on the client side filesystem when the currently edited
	 * part of the {@code JsonMessage} is clicked.
	 *
	 * @param path The path of the file on the client filesystem.
	 * @return This builder instance.
	 */
	public T file(String path) {
		onClick(ClickEvent.Type.FILE, path);
		return (T)this;
	}

	/**
	 * Set the behavior of the current editing component to instruct the
	 * client to open a webpage in the client's web browser when the currently
	 * edited part of the {@code JsonMessage} is clicked.
	 *
	 * @param url The URL of the page to open when the link is clicked.
	 * @return This builder instance.
	 */
	public T link(String url) {
		onClick(ClickEvent.Type.LINK, url);
		return (T)this;
	}

	/**
	 * Set the behavior of the current editing component to instruct the client to
	 * replace the chat input box content with the specified string when the currently
	 * edited part of the {@code JsonMessage} is clicked.
	 * <p>
	 * The client will not immediately send the command to the server to be executed
	 * unless the client player submits the command/chat message, usually with the enter key.
	 *
	 * @param command The text to display in the chat bar of the client.
	 * @return This builder instance.
	 */
	public T suggest(String command) {
		onClick(ClickEvent.Type.SUGGEST, command);
		return (T)this;
	}

	/**
	 * Set the behavior of the current editing component to instruct the client
	 * to append the chat input box content with the specified string when the
	 * currently edited part of the {@code JsonMessage} is SHIFT-CLICKED.
	 * <p>
	 * The client will not immediately send the command to the server to be executed unless
	 * the client player submits the command/chat message, usually with the enter key.
	 *
	 * @param command The text to append to the chat bar of the client.
	 * @return This builder instance.
	 */
	public T insert(String command) {
		this.latest().insertionData = command;
		dirty = true;
		return (T)this;
	}

	/**
	 * Set the behavior of the current editing component to instruct the
	 * client to send thespecified string to the server as a chat message
	 * when the currently edited part of the {@code JsonMessage} is clicked.
	 * <p>
	 * The client <b>will</b> immediately send the command to the
	 * server to be executed when the editing component is clicked.
	 *
	 * @param command The text to display in the chat bar of the client.
	 * @return This builder instance.
	 */
	public T command(String command) {
		onClick(ClickEvent.Type.COMMAND, command);
		return (T)this;
	}

	/**
	 * Set the behavior of the current editing component to display raw text when the client hovers over the text.
	 * <p>Tooltips do not inherit display characteristics, such as color and styles, from the message component on which they are applied.</p>
	 * @param text The text, which supports newlines, which will be displayed to the client upon hovering.
	 * @return This builder instance.
	 */
	public T tooltip(String text) {
		onHover(HoverEvent.Type.TEXT, new JsonString(text));
		return (T)this;
	}

	/**
	 * Set the behavior of the current editing component to
	 * display raw text when the client hovers over the text.
	 * <p>
	 * Tooltips do not inherit display characteristics, such as color
	 * and styles, from the message component on which they are applied.
	 *
	 * @param lines The lines of text which will be displayed to the client upon hovering.
	 *              The iteration order of this object will be the order in which the lines of the tooltip are created.
	 * @return This builder instance.
	 */
	public T tooltip(Iterable<String> lines) {
		tooltip(ListUtil.toArray(lines, String.class));
		return (T)this;
	}

	/**
	 * Set the behavior of the current editing component to
	 * display raw text when the client hovers over the text.
	 * <p>
	 * Tooltips do not inherit display characteristics, such as color
	 * and styles, from the message component on which they are applied.
	 *
	 * @param lines The lines of text which will be displayed to the client upon hovering.
	 * @return This builder instance.
	 */
	public T tooltip(final String... lines) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < lines.length; i++) {
			builder.append(lines[i]);

			if (i != lines.length - 1)
				builder.append('\n');
		}

		tooltip(builder.toString());
		return (T)this;
	}

	/**
	 * Set the behavior of the current editing component to display
	 * formatted text when the client hovers over the text.
	 * <p>
	 * Tooltips do not inherit display characteristics, such as color
	 * and styles, from the message component on which they are applied.
	 *
	 * @param text The formatted text which will be displayed to the client upon hovering.
	 * @return This builder instance.
	 */
	public T formattedTooltip(T text) {
		for (MessagePart component : text.messageParts) {
			if (component.clickEvent != null)
				throw new IllegalArgumentException("The tooltip text cannot have click data.");

			if (component.hoverEvent != null)
				throw new IllegalArgumentException("The tooltip text cannot have a tooltip.");
		}

		onHover(HoverEvent.Type.TEXT, text);
		return (T)this;
	}

	/**
	 * Set the behavior of the current editing component to display the
	 * specified lines of formatted text when the client hovers over the text.
	 * <p>
	 * Tooltips do not inherit display characteristics, such as color and
	 * styles, from the message component on which they are applied.
	 *
	 * @param lines The lines of formatted text which will be displayed to the client upon hovering.
	 * @return This builder instance.
	 */
	public T formattedTooltip(JsonMessage... lines) {
		if (lines.length < 1) {
			onHover(null, null); // Clear tooltip
			return (T)this;
		}

		JsonMessage<T> result = new JsonMessage();
		result.messageParts.clear(); // Remove default text component (destabilizes the object)

		for (int i = 0; i < lines.length; i++) {
			try {
				JsonMessage<T> message = lines[i];

				for (MessagePart component : message) {
					if (component.clickEvent != null)
						throw new IllegalArgumentException("The tooltip text cannot have click data.");

					if (component.hoverEvent != null)
						throw new IllegalArgumentException("The tooltip text cannot have a tooltip.");

					if (component.hasText())
						result.messageParts.add(component.clone());
				}

				if (i != lines.length - 1)
					result.messageParts.add(new MessagePart(TextualComponent.rawText("\n")));
			} catch (CloneNotSupportedException cnsex) {
				return (T)this;
			}
		}

		return formattedTooltip(result.messageParts.isEmpty() ? null : result); // Throws NPE if size is 0, intended
	}

	/**
	 * Set the behavior of the current editing component to display the
	 * specified lines of formatted text when the client hovers over the text.
	 * <p>
	 * Tooltips do not inherit display characteristics, such as color
	 * and styles, from the message component on which they are applied.
	 *
	 * @param lines The lines of text which will be displayed to the client upon hovering.
	 *              The iteration order of this object will be the order in which the lines of the tooltip are created.
	 * @return This builder instance.
	 */
	public T formattedTooltip(Iterable<JsonMessage> lines) {
		return formattedTooltip(ListUtil.toArray(lines, JsonMessage.class));
	}

	/**
	 * If the text is a translatable key, and it has replaceable values, this
	 * function can be used to set the replacements that will be used in the message.
	 *
	 * @param replacements The replacements, in order, that will be used in the language-specific message.
	 * @return This builder instance.
	 */
	public T translationReplacements(String... replacements) {
		for (String str : replacements)
			this.latest().translationReplacements.add(new JsonString(str));

		dirty = true;
		return (T)this;
	}

	/**
	 * If the text is a translatable key, and it has replaceable values, this
	 * function can be used to set the replacements that will be used in the message.
	 *
	 * @param replacements The replacements, in order, that will be used in the language-specific message.
	 * @return This builder instance.
	 */
	public T translationReplacements(JsonMessage... replacements) {
		Collections.addAll(this.latest().translationReplacements, replacements);
		dirty = true;
		return (T)this;
	}

	/**
	 * If the text is a translatable key, and it has replaceable values, this
	 * function can be used to set the replacements that will be used in the message.
	 *
	 * @param replacements The replacements, in order, that will be used in the language-specific message.
	 * @return This builder instance.
	 */
	public T translationReplacements(Iterable<JsonMessage> replacements) {
		return translationReplacements(ListUtil.toArray(replacements, JsonMessage.class));
	}

	/**
	 * Terminate construction of the current editing component,
	 * and begin construction of a new message component.
	 * <p>
	 * After a successful call to this method, all setter methods will refer
	 * to a new message component, created as a result of the call to this method.
	 *
	 * @return This builder instance.
	 */
	public T then() {
		return then((TextualComponent)null);
	}

	/**
	 * Terminate construction of the current editing component,
	 * and begin construction of a new message component.
	 * <p>
	 * After a successful call to this method, all setter methods will refer to
	 * a new message component, created as a result of the call to this method.
	 *
	 * @param text The text which will populate the new message component.
	 * @return This builder instance.
	 */
	public T then(final String text) {
		return then(TextualComponent.rawText(text));
	}

	/**
	 * Terminate construction of the current editing component,
	 * and begin construction of a new message component.
	 * <p>
	 * After a successful call to this method, all setter methods will refer to
	 * a new message component, created as a result of the call to this method.
	 *
	 * @param text The text which will populate the new message component.
	 * @return This builder instance.
	 */
	public T then(final TextualComponent text) {
		if (!this.latest().hasText())
			throw new IllegalStateException("Previous message part has no text");

		messageParts.add(new MessagePart(text));
		dirty = true;
		return (T)this;
	}

	@Override
	public void writeJson(JsonWriter writer) throws IOException {
		if (messageParts.size() == 1) {
			this.latest().writeJson(writer);
		} else {
			writer.beginObject().name("text").value("").name("extra").beginArray();

			for (final MessagePart part : this)
				part.writeJson(writer);

			writer.endArray().endObject();
		}
	}

	/**
	 * Serialize this fancy message, converting it into syntactically-valid JSON using a {@link JsonWriter}.
	 * This JSON should be compatible with vanilla formatter commands such as {@code /tellraw}.
	 *
	 * @return The JSON string representing this object.
	 */
	public String toJSONString() {
		if (!dirty && StringUtil.notEmpty(this.jsonString))
			return this.jsonString;

		StringWriter string = new StringWriter();
		JsonWriter writer = new JsonWriter(string);

		try {
			this.writeJson(writer);
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("invalid message");
		}

		dirty = false;
		return this.jsonString = string.toString();
	}

	/**
	 * Convert this message to a human-readable string with limited formatting.
	 * This method is used to send this message to clients without JSON formatting support.
	 * <p>
	 * Serialization of this message by using this message will include (in this order for each message part):
	 * <ol>
	 * <li>The color of each message part.</li>
	 * <li>The applicable stylizations for each message part.</li>
	 * <li>The core text of the message part.</li>
	 * </ol>
	 * The primary omissions are tooltips and clickable actions. Consequently, this method should be used only as a last resort.
	 * </p>
	 * <p>
	 * Color and formatting can be removed from the returned string by using {@link ChatColor#stripColor(String)}.</p>
	 * @return A human-readable string representing limited formatting in addition to the core text of this message.
	 */
	public String toLegacyText() {
		StringBuilder result = new StringBuilder();

		for (MessagePart part : this) {
			result.append(part.color == null ? "" : part.color);
			part.styles.forEach(result::append);
			result.append(part.text);
		}

		return result.toString();
	}

	protected final MessagePart latest() {
		return messageParts.get(messageParts.size() - 1);
	}

	protected final void onClick(ClickEvent.Type type, String data) {
		MessagePart latest = this.latest();
		latest.clickEvent = new ClickEvent(type, data);
		dirty = true;
	}

	protected final void onHover(HoverEvent.Type type, JsonRepresentedObject data) {
		MessagePart latest = this.latest();
		latest.hoverEvent = new HoverEvent(type, data);
		dirty = true;
	}

	public Map<String, Object> serialize() {
		ConcurrentMap<String, Object> map = Concurrent.newMap();
		map.put("messageParts", this.messageParts);
		return map;
	}

	/**
	 * Deserializes a JSON-represented message from a mapping of key-value pairs.
	 * This is called by the Bukkit serialization API.
	 * It is not intended for direct public API consumption.
	 * @param serialized The key-value mapping which represents a fancy message.
	 */
	@SuppressWarnings("unchecked")
	public static JsonMessage deserialize(Map<String, Object> serialized) {
		JsonMessage message = new JsonMessage();
		message.messageParts.addAll((List<MessagePart>)serialized.get("messageParts"));

		if (serialized.containsKey("JSON"))
			message.jsonString = serialized.get("JSON").toString();
		else
			message.dirty = true;

		return message;
	}

	/**
	 * Deserializes a fancy message from its JSON representation.
	 * This JSON representation is of the format of that returned by
	 * {@link #toJSONString()}, and is compatible with vanilla inputs.
	 *
	 * @param json The JSON string which represents a fancy message.
	 * @return A {@code JsonMessage} representing the parameterized JSON message.
	 */
	public static JsonMessage deserialize(String json) {
		JsonObject serialized = new JsonParser().parse(json).getAsJsonObject();
		JsonArray extra = serialized.getAsJsonArray("extra"); // Get the extra component
		JsonMessage returnVal = new JsonMessage();
		returnVal.messageParts.clear();

		for (JsonElement mPrt : extra) {
			MessagePart component = new MessagePart();
			JsonObject messagePart = mPrt.getAsJsonObject();

			for (Map.Entry<String, JsonElement> entry : messagePart.entrySet()) {
				if (TextualComponent.isTextKey(entry.getKey())) {
					ConcurrentMap<String, Object> serializedMapForm = Concurrent.newMap(); // Must be object due to Bukkit serializer API compliance
					serializedMapForm.put("key", entry.getKey());

					if (entry.getValue().isJsonPrimitive())
						serializedMapForm.put("value", entry.getValue().getAsString());
					else {
						// Composite object, but we assume each element is a string
						for (Map.Entry<String, JsonElement> compositeNestedElement : entry.getValue().getAsJsonObject().entrySet())
							serializedMapForm.put("value." + compositeNestedElement.getKey(), compositeNestedElement.getValue().getAsString());
					}

					component.text = TextualComponent.deserialize(serializedMapForm);
				} else if (MessagePart.STYLES_TO_NAMES.inverse().containsKey(entry.getKey())) {
					if (entry.getValue().getAsBoolean())
						component.styles.add(MessagePart.STYLES_TO_NAMES.inverse().get(entry.getKey()));
				} else if (entry.getKey().equals("color"))
					component.color = ChatColor.valueOf(entry.getValue().getAsString().toUpperCase());
				else if (entry.getKey().equals("clickEvent")) {
					JsonObject object = entry.getValue().getAsJsonObject();
					component.clickEvent = new ClickEvent(ClickEvent.Type.getFromAction(object.get("action").getAsString()), object.get("value").getAsString());
				} else if (entry.getKey().equals("hoverEvent")) {
					JsonObject object = entry.getValue().getAsJsonObject();
					String hoverAction = object.get("action").getAsString();
					JsonRepresentedObject jsonRep;

					if (object.get("value").isJsonPrimitive())
						jsonRep = new JsonString(object.get("value").getAsString());
					else
						jsonRep = deserialize(object.get("value").toString() /* This should properly serialize the JSON object as a JSON string */);

					component.hoverEvent = new HoverEvent(HoverEvent.Type.getFromAction(hoverAction), jsonRep);
				} else if (entry.getKey().equals("insertion")) {
					component.insertionData = entry.getValue().getAsString();
				} else if (entry.getKey().equals("with")) {
					for (JsonElement object : entry.getValue().getAsJsonArray()) {
						if (object.isJsonPrimitive())
							component.translationReplacements.add(new JsonString(object.getAsString()));
						else
							component.translationReplacements.add(deserialize(object.toString()));
					}
				}
			}

			returnVal.messageParts.add(component);
		}

		return returnVal;
	}

}