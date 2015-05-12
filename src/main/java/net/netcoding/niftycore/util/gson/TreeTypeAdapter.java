/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.netcoding.niftycore.util.gson;

import java.io.IOException;

import net.netcoding.niftycore.util.gson.internal.$Gson$Preconditions;
import net.netcoding.niftycore.util.gson.internal.Streams;
import net.netcoding.niftycore.util.gson.reflect.TypeToken;
import net.netcoding.niftycore.util.gson.stream.JsonReader;
import net.netcoding.niftycore.util.gson.stream.JsonWriter;

/**
 * Adapts a Gson 1.x tree-style adapter as a streaming TypeAdapter. Since the
 * tree adapter may be serialization-only or deserialization-only, this class
 * has a facility to lookup a delegate type adapter on demand.
 */
final class TreeTypeAdapter<T> extends TypeAdapter<T> {
	private static class SingleTypeFactory implements TypeAdapterFactory {
		private final JsonDeserializer<?> deserializer;
		private final TypeToken<?> exactType;
		private final Class<?> hierarchyType;
		private final boolean matchRawType;
		private final JsonSerializer<?> serializer;

		private SingleTypeFactory(Object typeAdapter, TypeToken<?> exactType,
				boolean matchRawType, Class<?> hierarchyType) {
			this.serializer = typeAdapter instanceof JsonSerializer ? (JsonSerializer<?>) typeAdapter
					: null;
			this.deserializer = typeAdapter instanceof JsonDeserializer ? (JsonDeserializer<?>) typeAdapter
					: null;
			$Gson$Preconditions.checkArgument(this.serializer != null
					|| this.deserializer != null);
			this.exactType = exactType;
			this.matchRawType = matchRawType;
			this.hierarchyType = hierarchyType;
		}

		@Override
		@SuppressWarnings("unchecked")
		// guarded by typeToken.equals() call
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			boolean matches = this.exactType != null ? this.exactType
					.equals(type)
					|| this.matchRawType
					&& this.exactType.getType() == type.getRawType()
					: this.hierarchyType.isAssignableFrom(type.getRawType());
					return matches ? new TreeTypeAdapter<T>(
							(JsonSerializer<T>) this.serializer,
							(JsonDeserializer<T>) this.deserializer, gson, type, this)
							: null;
		}
	}

	/**
	 * Returns a new factory that will match each type against {@code exactType}
	 * .
	 */
	public static TypeAdapterFactory newFactory(TypeToken<?> exactType,
			Object typeAdapter) {
		return new SingleTypeFactory(typeAdapter, exactType, false, null);
	}

	/**
	 * Returns a new factory that will match each type and its raw type against
	 * {@code exactType}.
	 */
	public static TypeAdapterFactory newFactoryWithMatchRawType(
			TypeToken<?> exactType, Object typeAdapter) {
		// only bother matching raw types if exact type is a raw type
		boolean matchRawType = exactType.getType() == exactType.getRawType();
		return new SingleTypeFactory(typeAdapter, exactType, matchRawType, null);
	}

	/**
	 * Returns a new factory that will match each type's raw type for
	 * assignability to {@code hierarchyType}.
	 */
	public static TypeAdapterFactory newTypeHierarchyFactory(
			Class<?> hierarchyType, Object typeAdapter) {
		return new SingleTypeFactory(typeAdapter, null, false, hierarchyType);
	}

	/**
	 * The delegate is lazily created because it may not be needed, and creating
	 * it may fail.
	 */
	private TypeAdapter<T> delegate;

	private final JsonDeserializer<T> deserializer;

	private final Gson gson;

	private final JsonSerializer<T> serializer;

	private final TypeAdapterFactory skipPast;

	private final TypeToken<T> typeToken;

	private TreeTypeAdapter(JsonSerializer<T> serializer,
			JsonDeserializer<T> deserializer, Gson gson,
			TypeToken<T> typeToken, TypeAdapterFactory skipPast) {
		this.serializer = serializer;
		this.deserializer = deserializer;
		this.gson = gson;
		this.typeToken = typeToken;
		this.skipPast = skipPast;
	}

	private TypeAdapter<T> delegate() {
		TypeAdapter<T> d = this.delegate;
		return d != null ? d : (this.delegate = this.gson.getDelegateAdapter(
				this.skipPast, this.typeToken));
	}

	@Override
	public T read(JsonReader in) throws IOException {
		if (this.deserializer == null)
			return this.delegate().read(in);
		JsonElement value = Streams.parse(in);
		if (value.isJsonNull())
			return null;
		return this.deserializer.deserialize(value, this.typeToken.getType(),
				this.gson.deserializationContext);
	}

	@Override
	public void write(JsonWriter out, T value) throws IOException {
		if (this.serializer == null) {
			this.delegate().write(out, value);
			return;
		}
		if (value == null) {
			out.nullValue();
			return;
		}
		JsonElement tree = this.serializer.serialize(value,
				this.typeToken.getType(), this.gson.serializationContext);
		Streams.write(tree, out);
	}
}
