package net.netcoding.nifty.core.util.concurrent.linked;

import net.netcoding.nifty.core.util.concurrent.atomic.AtomicSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
public class ConcurrentLinkedSet<E> extends AtomicSet<E, LinkedHashSet<E>> {

	/**
	 * Create a new concurrent set.
	 */
	public ConcurrentLinkedSet() {
		super(new LinkedHashSet<>());
	}

	/**
	 * Create a new concurrent set and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentLinkedSet(E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Create a new concurrent set and fill it with the given collection.
	 */
	public ConcurrentLinkedSet(Collection<? extends E> collection) {
		super(new LinkedHashSet<>(collection));
	}

}