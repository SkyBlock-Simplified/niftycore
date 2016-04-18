package net.netcoding.niftycore.util.json;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.stream.JsonWriter;
import net.netcoding.niftycore.minecraft.ChatColor;
import net.netcoding.niftycore.util.StringUtil;
import net.netcoding.niftycore.util.json.event.ClickEvent;
import net.netcoding.niftycore.util.json.event.HoverEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class MessagePart implements Cloneable {

	static final BiMap<ChatColor, String> STYLES_TO_NAMES;
	List<ChatColor> styles = new ArrayList<>();
	ChatColor color = ChatColor.WHITE;
	ClickEvent clickEvent = null;
	HoverEvent hoverEvent = null;
	TextualComponent text = null;
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
		this.text = text;
	}

	@Override
	public MessagePart clone() throws CloneNotSupportedException {
		MessagePart message = (MessagePart)super.clone();

		for (ChatColor style : this.styles)
			message.styles.add(style);

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

		for (JsonRepresentedObject replacement : this.translationReplacements)
			message.translationReplacements.add(replacement);

		return message;
	}

	public boolean hasText() {
		return this.text != null;
	}

	public void writeJson(JsonWriter writer) throws IOException {
		writer.beginObject();
		text.writeJson(writer);
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