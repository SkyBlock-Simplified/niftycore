package net.netcoding.nifty.core.util.concurrent;

import net.netcoding.nifty.core.util.concurrent.atomic.AtomicMap;

import java.util.HashMap;
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
public class ConcurrentMap<K, V> extends AtomicMap<K, V, HashMap<K, V>> {

	/**
	 * Create a new concurrent map.
	 */
	public ConcurrentMap() {
		super(new HashMap<>());
	}

	/**
	 * Create a new concurrent map and fill it with the given map.
	 */
	public ConcurrentMap(Map<? extends K, ? extends V> map) {
		super(new HashMap<>(map));
	}

}