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
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.netcoding.niftycore.util.gson.JsonArray;
import net.netcoding.niftycore.util.gson.JsonElement;
import net.netcoding.niftycore.util.gson.JsonNull;
import net.netcoding.niftycore.util.gson.JsonObject;
import net.netcoding.niftycore.util.gson.JsonPrimitive;
import net.netcoding.niftycore.util.gson.stream.JsonReader;
import net.netcoding.niftycore.util.gson.stream.JsonToken;

/**
 * This reader walks the elements of a JsonElement as if it was coming from a
 * character stream.
 *
 * @author Jesse Wilson
 */
public final class JsonTreeReader extends JsonReader {
	private static final Object SENTINEL_CLOSED = new Object();
	private static final Reader UNREADABLE_READER = new Reader() {
		@Override
		public void close() throws IOException {
			throw new AssertionError();
		}

		@Override
		public int read(char[] buffer, int offset, int count)
				throws IOException {
			throw new AssertionError();
		}
	};

	private final List<Object> stack = new ArrayList<Object>();

	public JsonTreeReader(JsonElement element) {
		super(UNREADABLE_READER);
		this.stack.add(element);
	}

	@Override
	public void beginArray() throws IOException {
		this.expect(JsonToken.BEGIN_ARRAY);
		JsonArray array = (JsonArray) this.peekStack();
		this.stack.add(array.iterator());
	}

	@Override
	public void beginObject() throws IOException {
		this.expect(JsonToken.BEGIN_OBJECT);
		JsonObject object = (JsonObject) this.peekStack();
		this.stack.add(object.entrySet().iterator());
	}

	@Override
	public void close() throws IOException {
		this.stack.clear();
		this.stack.add(SENTINEL_CLOSED);
	}

	@Override
	public void endArray() throws IOException {
		this.expect(JsonToken.END_ARRAY);
		this.popStack(); // empty iterator
		this.popStack(); // array
	}

	@Override
	public void endObject() throws IOException {
		this.expect(JsonToken.END_OBJECT);
		this.popStack(); // empty iterator
		this.popStack(); // object
	}

	private void expect(JsonToken expected) throws IOException {
		if (this.peek() != expected)
			throw new IllegalStateException("Expected " + expected
					+ " but was " + this.peek());
	}

	@Override
	public boolean hasNext() throws IOException {
		JsonToken token = this.peek();
		return token != JsonToken.END_OBJECT && token != JsonToken.END_ARRAY;
	}

	@Override
	public boolean nextBoolean() throws IOException {
		this.expect(JsonToken.BOOLEAN);
		return ((JsonPrimitive) this.popStack()).getAsBoolean();
	}

	@Override
	public double nextDouble() throws IOException {
		JsonToken token = this.peek();
		if (token != JsonToken.NUMBER && token != JsonToken.STRING)
			throw new IllegalStateException("Expected " + JsonToken.NUMBER
					+ " but was " + token);
		double result = ((JsonPrimitive) this.peekStack()).getAsDouble();
		if (!this.isLenient()
				&& (Double.isNaN(result) || Double.isInfinite(result)))
			throw new NumberFormatException("JSON forbids NaN and infinities: "
					+ result);
		this.popStack();
		return result;
	}

	@Override
	public int nextInt() throws IOException {
		JsonToken token = this.peek();
		if (token != JsonToken.NUMBER && token != JsonToken.STRING)
			throw new IllegalStateException("Expected " + JsonToken.NUMBER
					+ " but was " + token);
		int result = ((JsonPrimitive) this.peekStack()).getAsInt();
		this.popStack();
		return result;
	}

	@Override
	public long nextLong() throws IOException {
		JsonToken token = this.peek();
		if (token != JsonToken.NUMBER && token != JsonToken.STRING)
			throw new IllegalStateException("Expected " + JsonToken.NUMBER
					+ " but was " + token);
		long result = ((JsonPrimitive) this.peekStack()).getAsLong();
		this.popStack();
		return result;
	}

	@Override
	public String nextName() throws IOException {
		this.expect(JsonToken.NAME);
		Iterator<?> i = (Iterator<?>) this.peekStack();
		Map.Entry<?, ?> entry = (Map.Entry<?, ?>) i.next();
		this.stack.add(entry.getValue());
		return (String) entry.getKey();
	}

	@Override
	public void nextNull() throws IOException {
		this.expect(JsonToken.NULL);
		this.popStack();
	}

	@Override
	public String nextString() throws IOException {
		JsonToken token = this.peek();
		if (token != JsonToken.STRING && token != JsonToken.NUMBER)
			throw new IllegalStateException("Expected " + JsonToken.STRING
					+ " but was " + token);
		return ((JsonPrimitive) this.popStack()).getAsString();
	}

	@Override
	public JsonToken peek() throws IOException {
		if (this.stack.isEmpty())
			return JsonToken.END_DOCUMENT;

		Object o = this.peekStack();
		if (o instanceof Iterator) {
			boolean isObject = this.stack.get(this.stack.size() - 2) instanceof JsonObject;
			Iterator<?> iterator = (Iterator<?>) o;
			if (iterator.hasNext()) {
				if (isObject)
					return JsonToken.NAME;

				this.stack.add(iterator.next());
				return this.peek();
			}

			return isObject ? JsonToken.END_OBJECT : JsonToken.END_ARRAY;
		} else if (o instanceof JsonObject)
			return JsonToken.BEGIN_OBJECT;
		else if (o instanceof JsonArray)
			return JsonToken.BEGIN_ARRAY;
		else if (o instanceof JsonPrimitive) {
			JsonPrimitive primitive = (JsonPrimitive) o;
			if (primitive.isString())
				return JsonToken.STRING;
			else if (primitive.isBoolean())
				return JsonToken.BOOLEAN;
			else if (primitive.isNumber())
				return JsonToken.NUMBER;
			else
				throw new AssertionError();
		} else if (o instanceof JsonNull)
			return JsonToken.NULL;
		else if (o == SENTINEL_CLOSED)
			throw new IllegalStateException("JsonReader is closed");
		else
			throw new AssertionError();
	}

	private Object peekStack() {
		return this.stack.get(this.stack.size() - 1);
	}

	private Object popStack() {
		return this.stack.remove(this.stack.size() - 1);
	}

	public void promoteNameToValue() throws IOException {
		this.expect(JsonToken.NAME);
		Iterator<?> i = (Iterator<?>) this.peekStack();
		Map.Entry<?, ?> entry = (Map.Entry<?, ?>) i.next();
		this.stack.add(entry.getValue());
		this.stack.add(new JsonPrimitive((String) entry.getKey()));
	}

	@Override
	public void skipValue() throws IOException {
		if (this.peek() == JsonToken.NAME)
			this.nextName();
		else
			this.popStack();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
