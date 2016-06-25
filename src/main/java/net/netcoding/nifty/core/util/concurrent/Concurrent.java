package net.netcoding.nifty.core.util.concurrent;

import net.netcoding.nifty.core.util.concurrent.linked.ConcurrentLinkedList;
import net.netcoding.nifty.core.util.concurrent.linked.ConcurrentLinkedMap;
import net.netcoding.nifty.core.util.concurrent.linked.ConcurrentLinkedSet;

public final class Concurrent {

	public static <T> ConcurrentList<T> newList() {
		return new ConcurrentList<>();
	}

	public static <K, V> ConcurrentMap<K, V> newMap() {
		return new ConcurrentMap<>();
	}

	public static <T> ConcurrentSet<T> newSet() {
		return new ConcurrentSet<>();
	}

	public static <T> ConcurrentLinkedList<T> newLinkedList() {
		return new ConcurrentLinkedList<>();
	}

	public static <K, V> ConcurrentLinkedMap<K, V> newLinkedMap() {
		return new ConcurrentLinkedMap<>();
	}

	public static <T> ConcurrentLinkedSet<T> newLinkedSet() {
		return new ConcurrentLinkedSet<>();
	}

}