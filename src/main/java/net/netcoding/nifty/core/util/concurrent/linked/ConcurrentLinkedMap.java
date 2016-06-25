package net.netcoding.nifty.core.util.concurrent.linked;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
public class ConcurrentLinkedMap<K, V> extends LinkedHashMap<K, V> implements Map<K, V> {

	private final AtomicReference<LinkedHashMap<K, V>> ref;

	/**
	 * Create a new concurrent map.
	 */
	public ConcurrentLinkedMap() {

		this.ref = new AtomicReference<>(new LinkedHashMap<K, V>());
	}

	/**
	 * Create a new concurrent map and fill it with the given map.
	 */
	public ConcurrentLinkedMap(Map<? extends K, ? extends V> map) {
		this.ref = new AtomicReference<>(new LinkedHashMap<>(map));
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
			LinkedHashMap<K, V> current = this.ref.get();
			LinkedHashMap<K, V> modified = new LinkedHashMap<>(current);
			modified.put(key, value);

			if (this.ref.compareAndSet(current, modified))
				return value;
		}
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	@Override
	public V remove(Object key) {
		while (true) {
			LinkedHashMap<K, V> current = this.ref.get();

			if (!current.containsKey(key))
				return null;

			LinkedHashMap<K, V> modified = new LinkedHashMap<>(current);
			V value = modified.remove(key);

			if (this.ref.compareAndSet(current, modified))
				return value;
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		while (true) {
			LinkedHashMap<K, V> current = this.ref.get();
			LinkedHashMap<K, V> modified = new LinkedHashMap<>(current);

			for (Entry<? extends K, ? extends V> entry : m.entrySet())
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