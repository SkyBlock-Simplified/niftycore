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

import static net.netcoding.niftycore.util.gson.internal.bind.JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

import net.netcoding.niftycore.util.gson.FieldNamingStrategy;
import net.netcoding.niftycore.util.gson.Gson;
import net.netcoding.niftycore.util.gson.JsonSyntaxException;
import net.netcoding.niftycore.util.gson.TypeAdapter;
import net.netcoding.niftycore.util.gson.TypeAdapterFactory;
import net.netcoding.niftycore.util.gson.annotations.JsonAdapter;
import net.netcoding.niftycore.util.gson.annotations.SerializedName;
import net.netcoding.niftycore.util.gson.internal.$Gson$Types;
import net.netcoding.niftycore.util.gson.internal.ConstructorConstructor;
import net.netcoding.niftycore.util.gson.internal.Excluder;
import net.netcoding.niftycore.util.gson.internal.ObjectConstructor;
import net.netcoding.niftycore.util.gson.internal.Primitives;
import net.netcoding.niftycore.util.gson.reflect.TypeToken;
import net.netcoding.niftycore.util.gson.stream.JsonReader;
import net.netcoding.niftycore.util.gson.stream.JsonToken;
import net.netcoding.niftycore.util.gson.stream.JsonWriter;

/**
 * Type adapter that reflects over the fields and methods of a class.
 */
public final class ReflectiveTypeAdapterFactory implements TypeAdapterFactory {
	public static final class Adapter<T> extends TypeAdapter<T> {
		private final Map<String, BoundField> boundFields;
		private final ObjectConstructor<T> constructor;

		private Adapter(ObjectConstructor<T> constructor,
				Map<String, BoundField> boundFields) {
			this.constructor = constructor;
			this.boundFields = boundFields;
		}

		@Override
		public T read(JsonReader in) throws IOException {
			if (in.peek() == JsonToken.NULL) {
				in.nextNull();
				return null;
			}

			T instance = this.constructor.construct();

			try {
				in.beginObject();
				while (in.hasNext()) {
					String name = in.nextName();
					BoundField field = this.boundFields.get(name);
					if (field == null || !field.deserialized)
						in.skipValue();
					else
						field.read(in, instance);
				}
			} catch (IllegalStateException e) {
				throw new JsonSyntaxException(e);
			} catch (IllegalAccessException e) {
				throw new AssertionError(e);
			}
			in.endObject();
			return instance;
		}

		@Override
		public void write(JsonWriter out, T value) throws IOException {
			if (value == null) {
				out.nullValue();
				return;
			}

			out.beginObject();
			try {
				for (BoundField boundField : this.boundFields.values())
					if (boundField.writeField(value)) {
						out.name(boundField.name);
						boundField.write(out, value);
					}
			} catch (IllegalAccessException e) {
				throw new AssertionError();
			}
			out.endObject();
		}
	}

	static abstract class BoundField {
		final boolean deserialized;
		final String name;
		final boolean serialized;

		protected BoundField(String name, boolean serialized,
				boolean deserialized) {
			this.name = name;
			this.serialized = serialized;
			this.deserialized = deserialized;
		}

		abstract void read(JsonReader reader, Object value) throws IOException,
		IllegalAccessException;

		abstract void write(JsonWriter writer, Object value)
				throws IOException, IllegalAccessException;

		abstract boolean writeField(Object value) throws IOException,
		IllegalAccessException;
	}

	static boolean excludeField(Field f, boolean serialize, Excluder excluder) {
		return !excluder.excludeClass(f.getType(), serialize)
				&& !excluder.excludeField(f, serialize);
	}

	static String getFieldName(FieldNamingStrategy fieldNamingPolicy, Field f) {
		SerializedName serializedName = f.getAnnotation(SerializedName.class);
		return serializedName == null ? fieldNamingPolicy.translateName(f)
				: serializedName.value();
	}

	private final ConstructorConstructor constructorConstructor;

	private final Excluder excluder;

	private final FieldNamingStrategy fieldNamingPolicy;

	public ReflectiveTypeAdapterFactory(
			ConstructorConstructor constructorConstructor,
			FieldNamingStrategy fieldNamingPolicy, Excluder excluder) {
		this.constructorConstructor = constructorConstructor;
		this.fieldNamingPolicy = fieldNamingPolicy;
		this.excluder = excluder;
	}

	@Override
	public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {
		Class<? super T> raw = type.getRawType();

		if (!Object.class.isAssignableFrom(raw))
			return null; // it's a primitive!

		ObjectConstructor<T> constructor = this.constructorConstructor
				.get(type);
		return new Adapter<T>(constructor, this.getBoundFields(gson, type, raw));
	}

	private ReflectiveTypeAdapterFactory.BoundField createBoundField(
			final Gson context, final Field field, final String name,
			final TypeToken<?> fieldType, boolean serialize, boolean deserialize) {
		final boolean isPrimitive = Primitives.isPrimitive(fieldType
				.getRawType());
		// special casing primitives here saves ~5% on Android...
		return new ReflectiveTypeAdapterFactory.BoundField(name, serialize,
				deserialize) {
			final TypeAdapter<?> typeAdapter = ReflectiveTypeAdapterFactory.this
					.getFieldAdapter(context, field, fieldType);

			@Override
			void read(JsonReader reader, Object value) throws IOException,
			IllegalAccessException {
				Object fieldValue = this.typeAdapter.read(reader);
				if (fieldValue != null || !isPrimitive)
					field.set(value, fieldValue);
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			// the type adapter and field type always agree
			@Override
			void write(JsonWriter writer, Object value) throws IOException,
			IllegalAccessException {
				Object fieldValue = field.get(value);
				TypeAdapter t = new TypeAdapterRuntimeTypeWrapper(context,
						this.typeAdapter, fieldType.getType());
				t.write(writer, fieldValue);
			}

			@Override
			public boolean writeField(Object value) throws IOException,
			IllegalAccessException {
				if (!this.serialized)
					return false;
				Object fieldValue = field.get(value);
				return fieldValue != value; // avoid recursion for example for
				// Throwable.cause
			}
		};
	}

	public boolean excludeField(Field f, boolean serialize) {
		return excludeField(f, serialize, this.excluder);
	}

	private Map<String, BoundField> getBoundFields(Gson context,
			TypeToken<?> type, Class<?> raw) {
		Map<String, BoundField> result = new LinkedHashMap<String, BoundField>();
		if (raw.isInterface())
			return result;

		Type declaredType = type.getType();
		while (raw != Object.class) {
			Field[] fields = raw.getDeclaredFields();
			for (Field field : fields) {
				boolean serialize = this.excludeField(field, true);
				boolean deserialize = this.excludeField(field, false);
				if (!serialize && !deserialize)
					continue;
				field.setAccessible(true);
				Type fieldType = $Gson$Types.resolve(type.getType(), raw,
						field.getGenericType());
				BoundField boundField = this.createBoundField(context, field,
						this.getFieldName(field), TypeToken.get(fieldType),
						serialize, deserialize);
				BoundField previous = result.put(boundField.name, boundField);
				if (previous != null)
					throw new IllegalArgumentException(declaredType
							+ " declares multiple JSON fields named "
							+ previous.name);
			}
			type = TypeToken.get($Gson$Types.resolve(type.getType(), raw,
					raw.getGenericSuperclass()));
			raw = type.getRawType();
		}
		return result;
	}

	private TypeAdapter<?> getFieldAdapter(Gson gson, Field field,
			TypeToken<?> fieldType) {
		JsonAdapter annotation = field.getAnnotation(JsonAdapter.class);
		if (annotation != null) {
			TypeAdapter<?> adapter = getTypeAdapter(
					this.constructorConstructor, gson, fieldType, annotation);
			if (adapter != null)
				return adapter;
		}
		return gson.getAdapter(fieldType);
	}

	private String getFieldName(Field f) {
		return getFieldName(this.fieldNamingPolicy, f);
	}
}
