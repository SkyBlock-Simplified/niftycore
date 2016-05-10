package net.netcoding.niftycore.util.concurrent;

import java.util.AbstractMap;
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
public class ConcurrentMap<K, V> extends AbstractMap<K, V> implements Map<K, V> {

	private final AtomicReference<HashMap<K, V>> ref;

	/**
	 * Create a new concurrent map.
	 */
	public ConcurrentMap() {
		this.ref = new AtomicReference<>(new HashMap<K, V>());
	}

	/**
	 * Create a new concurrent map and fill it with the given map.
	 */
	public ConcurrentMap(Map<? extends K, ? extends V> map) {
		this.ref = new AtomicReference<>(new HashMap<>(map));
	}

	@Override
	public void clear() {
		this.ref.get().clear();
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
	public Set<Entry<K, V>> entrySet() {
		return this.ref.get().entrySet();
	}

	@Override
	public V get(Object key) {
		return this.ref.get().get(key);
	}

	@Override
	public boolean isEmpty() {
		return this.ref.get().isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return this.ref.get().keySet();
	}

	@Override
	public V put(K key, V value) {
		while (true) {
			HashMap<K, V> current = this.ref.get();
			HashMap<K, V> modified = new HashMap<>(current);
			modified.put(key, value);

			if (this.ref.compareAndSet(current, modified))
				return value;
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		while (true) {
			HashMap<K, V> current = this.ref.get();
			HashMap<K, V> modified = new HashMap<>(current);

			for (Map.Entry<? extends K, ? extends V> entry : m.entrySet())
				modified.put(entry.getKey(), entry.getValue());

			if (this.ref.compareAndSet(current, modified))
				return;
		}
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	@Override
	public V remove(Object key) {
		while (true) {
			HashMap<K, V> current = this.ref.get();

			if (!current.containsKey(key))
				return null;

			HashMap<K, V> modified = new HashMap<>(current);
			V value = modified.remove(key);

			if (this.ref.compareAndSet(current, modified))
				return value;
		}
	}

	@Override
	public int size() {
		return this.ref.get().size();
	}

	@Override
	public Collection<V> values() {
		return this.ref.get().values();
	}

}