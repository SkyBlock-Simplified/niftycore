package net.netcoding.niftycore.util.json;

import com.google.gson.stream.JsonWriter;
import net.netcoding.niftycore.util.StringUtil;

import java.io.IOException;

public class JsonString implements JsonRepresentedObject {

	private final String value;

	public JsonString(CharSequence value) {
		this.value = StringUtil.isEmpty(value) ? null : value.toString();
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return this.getValue();
	}

	@Override
	public void writeJson(JsonWriter writer) throws IOException {
		writer.value(this.getValue());
	}

}