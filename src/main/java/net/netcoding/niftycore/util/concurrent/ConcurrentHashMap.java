package net.netcoding.niftycore.util.concurrent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
public class ConcurrentHashMap<K, V> implements Map<K, V> {

	private final AtomicReference<Map<K, V>> ref;

	/**
	 * Create a new concurrent map.
	 */
	public ConcurrentHashMap() {
		this.ref = new AtomicReference<Map<K, V>>(new HashMap<K, V>());
	}

	/**
	 * Create a new concurrent map and fill it with the given map.
	 */
	public ConcurrentHashMap(Map<? extends K, ? extends V> map) {
		this.ref = new AtomicReference<Map<K, V>>(new HashMap<>(map));
	}

	@Override
	public void clear() {
		this.ref.get().clear();
	}

	@Override
	public Set<K> keySet() {
		return this.ref.get().keySet();
	}

	@Override
	public Collection<V> values() {
		return this.ref.get().values();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return this.ref.get().entrySet();
	}

	@Override
	public boolean isEmpty() {
		return this.ref.get().isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return this.ref.get().containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return this.ref.get().containsValue(value);
	}

	@Override
	public V get(Object key) {
		return this.ref.get().get(key);
	}

	@Override
	public V put(K key, V value) {
		while (true) {
			Map<K, V> current = this.ref.get();

			if (current.containsKey(key))
				return current.get(key);

			Map<K, V> modified = new HashMap<>(current);
			modified.put(key, value);

			if (this.ref.compareAndSet(current, modified))
				return value;
		}
	}

	@Override
	public V remove(Object key) {
		while (true) {
			Map<K, V> current = this.ref.get();

			if (!current.containsKey(key))
				return null;

			Map<K, V> modified = new HashMap<>(current);
			V value = modified.remove(key);

			if (this.ref.compareAndSet(current, modified))
				return value;
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		while (true) {
			Map<K, V> current = this.ref.get();
			Map<K, V> modified = new HashMap<>(current);

			for (Map.Entry<? extends K, ? extends V> entry : m.entrySet())
				modified.put(entry.getKey(), entry.getValue());

			if (this.ref.compareAndSet(current, modified))
				return;
		}
	}

	@Override
	public int size() {
		return this.ref.get().size();
	}

}