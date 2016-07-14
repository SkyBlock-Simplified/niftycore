package net.netcoding.nifty.core.util.json;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.stream.JsonWriter;
import net.netcoding.nifty.core.api.color.ChatColor;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.core.util.json.events.ClickEvent;
import net.netcoding.nifty.core.util.json.events.HoverEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MessagePart implements Cloneable {

	static final BiMap<ChatColor, String> STYLES_TO_NAMES;
	List<ChatColor> styles = new ArrayList<>();
	ChatColor color = null;
	ClickEvent clickEvent = null;
	HoverEvent hoverEvent = null;
	TextualComponent text = TextualComponent.rawText("");
	String insertionData = null;
	List<JsonRepresentedObject> translationReplacements = new ArrayList<>();

	static {
		ImmutableBiMap.Builder<ChatColor, String> builder = ImmutableBiMap.builder();

		for (ChatColor color : ChatColor.values()) {
			if (!color.isFormat())
				continue;

			builder.put(color, color.getJsonName());
		}

		STYLES_TO_NAMES = builder.build();
	}

	MessagePart() { }

	MessagePart(TextualComponent text) {
		this.text = text == null ? TextualComponent.rawText("") : text;
	}

	@Override
	public MessagePart clone() throws CloneNotSupportedException {
		MessagePart message = (MessagePart)super.clone();
		message.text = this.text.clone();
		message.styles.addAll(this.styles);

		if (this.clickEvent != null)
			message.clickEvent = new ClickEvent(ClickEvent.Type.getFromAction(this.clickEvent.getName()), this.clickEvent.getValue());

		if (this.hoverEvent != null) {
			HoverEvent.Type type = HoverEvent.Type.getFromAction(this.hoverEvent.getName());
			JsonRepresentedObject jsonRep;

			if (this.hoverEvent.getValue() instanceof JsonString)
				jsonRep = new JsonString(((JsonString)this.hoverEvent.getValue()).getValue());
			else
				jsonRep = ((JsonMessage)this.hoverEvent.getValue()).clone();

			message.hoverEvent = new HoverEvent(type, jsonRep);
		}

		message.translationReplacements.addAll(this.translationReplacements);
		return message;
	}

	public boolean hasText() {
		return this.text != null;
	}

	public Map<String, Object> serialize() {
		HashMap<String, Object> map = new HashMap<>();
		map.put("text", this.text);
		map.put("styles", this.styles);
		map.put("color", this.color.getCode());
		map.put("hoverEvent", this.hoverEvent);
		map.put("clickEvent", this.clickEvent);
		map.put("insertion", this.insertionData);
		map.put("translationReplacements",  this.translationReplacements);
		return map;
	}

	@SuppressWarnings("unchecked")
	public static MessagePart deserialize(Map<String, Object> serialized){
		MessagePart part = new MessagePart((TextualComponent)serialized.get("text"));
		part.styles = (List<ChatColor>)serialized.get("styles");
		part.color = ChatColor.getByChar((char)serialized.get("color"));
		part.hoverEvent = (HoverEvent)serialized.get("hoverEvent");
		part.clickEvent = (ClickEvent)serialized.get("clickEvent");
		part.insertionData = (String)serialized.get("insertion");
		part.translationReplacements = (ArrayList<JsonRepresentedObject>)serialized.get("translationReplacements");
		return part;
	}

	public void writeJson(JsonWriter writer) throws IOException {
		writer.beginObject();
		text.writeJson(writer);

		if (color != null)
			writer.name("color").value(this.color.name().toLowerCase());

		for (ChatColor style : this.styles)
			writer.name(STYLES_TO_NAMES.get(style)).value(true);

		if (this.clickEvent != null) {
			writer.name("clickEvent").beginObject();
			writer.name("action").value(this.clickEvent.getName());
			writer.name("value").value(this.clickEvent.getValue());
			writer.endObject();
		}

		if (this.hoverEvent != null) {
			writer.name("clickEvent").beginObject();
			writer.name("action").value(this.hoverEvent.getName());
			writer.name("value");
			this.hoverEvent.getValue().writeJson(writer);
			writer.endObject();
		}

		if (StringUtil.notEmpty(this.insertionData))
			writer.name("insertion").value(this.insertionData);

		if (!this.translationReplacements.isEmpty() && TextualComponent.isTranslatableText(this.text)) {
			writer.name("with").beginArray();

			for (JsonRepresentedObject replacement : this.translationReplacements)
				replacement.writeJson(writer);

			writer.endArray();
		}

		writer.endObject();
	}

}