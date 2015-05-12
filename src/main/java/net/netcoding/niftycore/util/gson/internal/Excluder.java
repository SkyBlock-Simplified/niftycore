/*
 * Copyright (C) 2008 Google Inc.
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

package net.netcoding.niftycore.util.gson.internal;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.netcoding.niftycore.util.gson.ExclusionStrategy;
import net.netcoding.niftycore.util.gson.FieldAttributes;
import net.netcoding.niftycore.util.gson.Gson;
import net.netcoding.niftycore.util.gson.TypeAdapter;
import net.netcoding.niftycore.util.gson.TypeAdapterFactory;
import net.netcoding.niftycore.util.gson.annotations.Expose;
import net.netcoding.niftycore.util.gson.annotations.Since;
import net.netcoding.niftycore.util.gson.annotations.Until;
import net.netcoding.niftycore.util.gson.reflect.TypeToken;
import net.netcoding.niftycore.util.gson.stream.JsonReader;
import net.netcoding.niftycore.util.gson.stream.JsonWriter;

/**
 * This class selects which fields and types to omit. It is configurable,
 * supporting version attributes {@link Since} and {@link Until}, modifiers,
 * synthetic fields, anonymous and local classes, inner classes, and fields with
 * the {@link Expose} annotation.
 *
 * <p>
 * This class is a type adapter factory; types that are excluded will be adapted
 * to null. It may delegate to another type adapter if only one direction is
 * excluded.
 *
 * @author Joel Leitch
 * @author Jesse Wilson
 */
public final class Excluder implements TypeAdapterFactory, Cloneable {
	public static final Excluder DEFAULT = new Excluder();
	private static final double IGNORE_VERSIONS = -1.0d;

	private List<ExclusionStrategy> deserializationStrategies = Collections
			.emptyList();
	private int modifiers = Modifier.TRANSIENT | Modifier.STATIC;
	private boolean requireExpose;
	private List<ExclusionStrategy> serializationStrategies = Collections
			.emptyList();
	private boolean serializeInnerClasses = true;
	private double version = IGNORE_VERSIONS;

	@Override
	protected Excluder clone() {
		try {
			return (Excluder) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError();
		}
	}

	@Override
	public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
		Class<?> rawType = type.getRawType();
		final boolean skipSerialize = this.excludeClass(rawType, true);
		final boolean skipDeserialize = this.excludeClass(rawType, false);

		if (!skipSerialize && !skipDeserialize)
			return null;

		return new TypeAdapter<T>() {
			/**
			 * The delegate is lazily created because it may not be needed, and
			 * creating it may fail.
			 */
			private TypeAdapter<T> delegate;

			private TypeAdapter<T> delegate() {
				TypeAdapter<T> d = this.delegate;
				return d != null ? d : (this.delegate = gson
						.getDelegateAdapter(Excluder.this, type));
			}

			@Override
			public T read(JsonReader in) throws IOException {
				if (skipDeserialize) {
					in.skipValue();
					return null;
				}
				return this.delegate().read(in);
			}

			@Override
			public void write(JsonWriter out, T value) throws IOException {
				if (skipSerialize) {
					out.nullValue();
					return;
				}
				this.delegate().write(out, value);
			}
		};
	}

	public Excluder disableInnerClassSerialization() {
		Excluder result = this.clone();
		result.serializeInnerClasses = false;
		return result;
	}

	public boolean excludeClass(Class<?> clazz, boolean serialize) {
		if (this.version != Excluder.IGNORE_VERSIONS
				&& !this.isValidVersion(clazz.getAnnotation(Since.class),
						clazz.getAnnotation(Until.class)))
			return true;

		if (!this.serializeInnerClasses && this.isInnerClass(clazz))
			return true;

		if (this.isAnonymousOrLocal(clazz))
			return true;

		List<ExclusionStrategy> list = serialize ? this.serializationStrategies
				: this.deserializationStrategies;
		for (ExclusionStrategy exclusionStrategy : list)
			if (exclusionStrategy.shouldSkipClass(clazz))
				return true;

		return false;
	}

	public boolean excludeField(Field field, boolean serialize) {
		if ((this.modifiers & field.getModifiers()) != 0)
			return true;

		if (this.version != Excluder.IGNORE_VERSIONS
				&& !this.isValidVersion(field.getAnnotation(Since.class),
						field.getAnnotation(Until.class)))
			return true;

		if (field.isSynthetic())
			return true;

		if (this.requireExpose) {
			Expose annotation = field.getAnnotation(Expose.class);
			if (annotation == null
					|| (serialize ? !annotation.serialize() : !annotation
							.deserialize()))
				return true;
		}

		if (!this.serializeInnerClasses && this.isInnerClass(field.getType()))
			return true;

		if (this.isAnonymousOrLocal(field.getType()))
			return true;

		List<ExclusionStrategy> list = serialize ? this.serializationStrategies
				: this.deserializationStrategies;
		if (!list.isEmpty()) {
			FieldAttributes fieldAttributes = new FieldAttributes(field);
			for (ExclusionStrategy exclusionStrategy : list)
				if (exclusionStrategy.shouldSkipField(fieldAttributes))
					return true;
		}

		return false;
	}

	public Excluder excludeFieldsWithoutExposeAnnotation() {
		Excluder result = this.clone();
		result.requireExpose = true;
		return result;
	}

	private boolean isAnonymousOrLocal(Class<?> clazz) {
		return !Enum.class.isAssignableFrom(clazz)
				&& (clazz.isAnonymousClass() || clazz.isLocalClass());
	}

	private boolean isInnerClass(Class<?> clazz) {
		return clazz.isMemberClass() && !this.isStatic(clazz);
	}

	private boolean isStatic(Class<?> clazz) {
		return (clazz.getModifiers() & Modifier.STATIC) != 0;
	}

	private boolean isValidSince(Since annotation) {
		if (annotation != null) {
			double annotationVersion = annotation.value();
			if (annotationVersion > this.version)
				return false;
		}
		return true;
	}

	private boolean isValidUntil(Until annotation) {
		if (annotation != null) {
			double annotationVersion = annotation.value();
			if (annotationVersion <= this.version)
				return false;
		}
		return true;
	}

	private boolean isValidVersion(Since since, Until until) {
		return this.isValidSince(since) && this.isValidUntil(until);
	}

	public Excluder withExclusionStrategy(ExclusionStrategy exclusionStrategy,
			boolean serialization, boolean deserialization) {
		Excluder result = this.clone();
		if (serialization) {
			result.serializationStrategies = new ArrayList<ExclusionStrategy>(
					this.serializationStrategies);
			result.serializationStrategies.add(exclusionStrategy);
		}
		if (deserialization) {
			result.deserializationStrategies = new ArrayList<ExclusionStrategy>(
					this.deserializationStrategies);
			result.deserializationStrategies.add(exclusionStrategy);
		}
		return result;
	}

	public Excluder withModifiers(int... modifiers) {
		Excluder result = this.clone();
		result.modifiers = 0;
		for (int modifier : modifiers)
			result.modifiers |= modifier;
		return result;
	}

	public Excluder withVersion(double ignoreVersionsAfter) {
		Excluder result = this.clone();
		result.version = ignoreVersionsAfter;
		return result;
	}
}
