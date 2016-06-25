package net.netcoding.nifty.core.util.misc;

import net.netcoding.nifty.core.util.concurrent.linked.ConcurrentLinkedMap;

import java.util.Map;
import java.util.Set;

public class MaxSizeLinkedMap<K, V> extends ConcurrentLinkedMap<K, V> {

	private final int maxSize;

	public MaxSizeLinkedMap() {
		this(Integer.MAX_VALUE);
	}

	public MaxSizeLinkedMap(int maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return super.entrySet();
	}

	public boolean removeEldestEntry() {
		return this.removeEldestEntry(null);
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return this.size() > this.maxSize;
	}

}