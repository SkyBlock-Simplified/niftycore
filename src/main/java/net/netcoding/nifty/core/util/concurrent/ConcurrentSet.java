package net.netcoding.nifty.core.util.concurrent;

import net.netcoding.nifty.core.util.concurrent.atomic.AtomicSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A concurrent set that allows for simultaneously fast reading, iteration and
 * modification utilizing {@link AtomicReference}.
 * <p>
 * The AtomicReference changes the methods that modify the set by replacing the
 * entire set each modification. This allows for maintaining the original speed
 * of {@link HashSet#contains(Object)} and makes it cross-thread-safe.
 *
 * @param <E> type of elements
 */
public class ConcurrentSet<E> extends AtomicSet<E, HashSet<E>> {

	/**
	 * Create a new concurrent set.
	 */
	public ConcurrentSet() {
		super(new HashSet<>());
	}

	/**
	 * Create a new concurrent set and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentSet(E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Create a new concurrent set and fill it with the given collection.
	 */
	public ConcurrentSet(Collection<? extends E> collection) {
		super(new HashSet<>(collection));
	}

}