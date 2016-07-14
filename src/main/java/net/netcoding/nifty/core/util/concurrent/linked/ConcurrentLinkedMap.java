package net.netcoding.nifty.core.util.concurrent.linked;

import net.netcoding.nifty.core.util.concurrent.atomic.AtomicMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A concurrent map that allows for simultaneously fast reading, iteration and
 * modification utilizing {@link AtomicReference}.
 * <p>
 * The AtomicReference changes the methods that modify the map by replacing the
 * entire map each modification. This allows for maintaining the original speed
 * of {@link HashMap#containsKey(Object)} and {@link HashMap#containsValue(Object)} and makes it cross-thread-safe.
 *
 * @param <K> type of keys
 * @param <V> type of values
 */
public class ConcurrentLinkedMap<K, V> extends AtomicMap<K, V, ConcurrentLinkedMap.MaxSizeLinkedMap<K, V>> {

	/**
	 * Create a new concurrent map.
	 */
	public ConcurrentLinkedMap() {
		super(new MaxSizeLinkedMap<>());
	}

	/**
	 * Create a new concurrent map.
	 *
	 * @param maxSize The maximum number of entries allowed in the map.
	 */
	public ConcurrentLinkedMap(int maxSize) {
		super(new MaxSizeLinkedMap<>(maxSize));
	}

	/**
	 * Create a new concurrent map and fill it with the given map.
	 *
	 * @param map Map to fill the new map with.
	 */
	public ConcurrentLinkedMap(Map<? extends K, ? extends V> map) {
		super(new MaxSizeLinkedMap<>(map));
	}

	/**
	 * Create a new concurrent map and fill it with the given map.
	 *
	 * @param map Map to fill the new map with.
	 * @param maxSize The maximum number of entries allowed in the map.
	 */
	public ConcurrentLinkedMap(Map<? extends K, ? extends V> map, int maxSize) {
		super(new MaxSizeLinkedMap<>(map, maxSize));
	}

	protected static final class MaxSizeLinkedMap<K, V> extends LinkedHashMap<K, V> {

		private final int maxSize;

		public MaxSizeLinkedMap() {
			this(-1);
		}

		public MaxSizeLinkedMap(int maxSize) {
			this.maxSize = -1;
		}

		public MaxSizeLinkedMap(Map<? extends K, ? extends V> map) {
			this(map, -1);
		}

		public MaxSizeLinkedMap(Map<? extends K, ? extends V> map, int maxSize) {
			super(map);
			this.maxSize = -1;
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			return this.maxSize != -1 && this.size() > this.maxSize;
		}

	}

}