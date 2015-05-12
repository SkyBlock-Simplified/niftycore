/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.netcoding.niftycore.util.gson.internal.bind;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import net.netcoding.niftycore.util.gson.JsonArray;
import net.netcoding.niftycore.util.gson.JsonElement;
import net.netcoding.niftycore.util.gson.JsonNull;
import net.netcoding.niftycore.util.gson.JsonObject;
import net.netcoding.niftycore.util.gson.JsonPrimitive;
import net.netcoding.niftycore.util.gson.stream.JsonWriter;

/**
 * This writer creates a JsonElement.
 */
public final class JsonTreeWriter extends JsonWriter {
	/**
	 * Added to the top of the stack when this writer is closed to cause
	 * following ops to fail.
	 */
	private static final JsonPrimitive SENTINEL_CLOSED = new JsonPrimitive(
			"closed");
	private static final Writer UNWRITABLE_WRITER = new Writer() {
		@Override
		public void close() throws IOException {
			throw new AssertionError();
		}

		@Override
		public void flush() throws IOException {
			throw new AssertionError();
		}

		@Override
		public void write(char[] buffer, int offset, int counter) {
			throw new AssertionError();
		}
	};

	/**
	 * The name for the next JSON object value. If non-null, the top of the
	 * stack is a JsonObject.
	 */
	private String pendingName;

	/** the JSON element constructed by this writer. */
	private JsonElement product = JsonNull.INSTANCE; // TODO: is this really
	// what we want?;

	/**
	 * The JsonElements and JsonArrays under modification, outermost to
	 * innermost.
	 */
	private final List<JsonElement> stack = new ArrayList<JsonElement>();

	public JsonTreeWriter() {
		super(UNWRITABLE_WRITER);
	}

	@Override
	public JsonWriter beginArray() throws IOException {
		JsonArray array = new JsonArray();
		this.put(array);
		this.stack.add(array);
		return this;
	}

	@Override
	public JsonWriter beginObject() throws IOException {
		JsonObject object = new JsonObject();
		this.put(object);
		this.stack.add(object);
		return this;
	}

	@Override
	public void close() throws IOException {
		if (!this.stack.isEmpty())
			throw new IOException("Incomplete document");
		this.stack.add(SENTINEL_CLOSED);
	}

	@Override
	public JsonWriter endArray() throws IOException {
		if (this.stack.isEmpty() || this.pendingName != null)
			throw new IllegalStateException();
		JsonElement element = this.peek();
		if (element instanceof JsonArray) {
			this.stack.remove(this.stack.size() - 1);
			return this;
		}
		throw new IllegalStateException();
	}

	@Override
	public JsonWriter endObject() throws IOException {
		if (this.stack.isEmpty() || this.pendingName != null)
			throw new IllegalStateException();
		JsonElement element = this.peek();
		if (element instanceof JsonObject) {
			this.stack.remove(this.stack.size() - 1);
			return this;
		}
		throw new IllegalStateException();
	}

	@Override
	public void flush() throws IOException {
	}

	/**
	 * Returns the top level object produced by this writer.
	 */
	public JsonElement get() {
		if (!this.stack.isEmpty())
			throw new IllegalStateException(
					"Expected one JSON element but was " + this.stack);
		return this.product;
	}

	@Override
	public JsonWriter name(String name) throws IOException {
		if (this.stack.isEmpty() || this.pendingName != null)
			throw new IllegalStateException();
		JsonElement element = this.peek();
		if (element instanceof JsonObject) {
			this.pendingName = name;
			return this;
		}
		throw new IllegalStateException();
	}

	@Override
	public JsonWriter nullValue() throws IOException {
		this.put(JsonNull.INSTANCE);
		return this;
	}

	private JsonElement peek() {
		return this.stack.get(this.stack.size() - 1);
	}

	private void put(JsonElement value) {
		if (this.pendingName != null) {
			if (!value.isJsonNull() || this.getSerializeNulls()) {
				JsonObject object = (JsonObject) this.peek();
				object.add(this.pendingName, value);
			}
			this.pendingName = null;
		} else if (this.stack.isEmpty())
			this.product = value;
		else {
			JsonElement element = this.peek();
			if (element instanceof JsonArray)
				((JsonArray) element).add(value);
			else
				throw new IllegalStateException();
		}
	}

	@Override
	public JsonWriter value(boolean value) throws IOException {
		this.put(new JsonPrimitive(value));
		return this;
	}

	@Override
	public JsonWriter value(double value) throws IOException {
		if (!this.isLenient()
				&& (Double.isNaN(value) || Double.isInfinite(value)))
			throw new IllegalArgumentException(
					"JSON forbids NaN and infinities: " + value);
		this.put(new JsonPrimitive(value));
		return this;
	}

	@Override
	public JsonWriter value(long value) throws IOException {
		this.put(new JsonPrimitive(value));
		return this;
	}

	@Override
	public JsonWriter value(Number value) throws IOException {
		if (value == null)
			return this.nullValue();

		if (!this.isLenient()) {
			double d = value.doubleValue();
			if (Double.isNaN(d) || Double.isInfinite(d))
				throw new IllegalArgumentException(
						"JSON forbids NaN and infinities: " + value);
		}

		this.put(new JsonPrimitive(value));
		return this;
	}

	@Override
	public JsonWriter value(String value) throws IOException {
		if (value == null)
			return this.nullValue();
		this.put(new JsonPrimitive(value));
		return this;
	}
}
