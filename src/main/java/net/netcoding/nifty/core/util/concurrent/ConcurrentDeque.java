package net.netcoding.nifty.core.util.concurrent;

import net.netcoding.nifty.core.util.concurrent.atomic.AtomicDeque;

import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A concurrent queue that allows for simultaneously fast reading, iteration and
 * modification utilizing {@link AtomicReference}.
 * <p>
 * The AtomicReference changes the methods that modify the queue by replacing the
 * entire queue on each modification. This allows for maintaining the original
 * speed of {@link Deque#contains(Object)} and makes it cross-thread-safe.
 *
 * @param <E> type of elements
 */
public class ConcurrentDeque<E> extends AtomicDeque<E> {

	/**
	 * Create a new concurrent deque.
	 */
	public ConcurrentDeque() {
		super(new LinkedList<>());
	}

	/**
	 * Create a new concurrent deque and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentDeque(E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Create a new concurrent deque and fill it with the given collection.
	 */
	public ConcurrentDeque(Collection<? extends E> collection) {
		super(new LinkedList<>(collection));
	}

}