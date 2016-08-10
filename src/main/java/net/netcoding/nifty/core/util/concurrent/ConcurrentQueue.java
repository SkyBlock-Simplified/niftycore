package net.netcoding.nifty.core.util.concurrent;

import net.netcoding.nifty.core.util.concurrent.atomic.AtomicQueue;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A concurrent queue that allows for simultaneously fast reading, iteration and
 * modification utilizing {@link AtomicReference}.
 * <p>
 * The AtomicReference changes the methods that modify the queue by replacing the
 * entire queue on each modification. This allows for maintaining the original
 * speed of {@link Queue#contains(Object)} and makes it cross-thread-safe.
 *
 * @param <E> type of elements
 */
public class ConcurrentQueue<E> extends AtomicQueue<E> {

	/**
	 * Create a new concurrent queue.
	 */
	public ConcurrentQueue() {
		super(new LinkedList<>());
	}

	/**
	 * Create a new concurrent queue and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentQueue(E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Create a new concurrent queue and fill it with the given collection.
	 */
	public ConcurrentQueue(Collection<? extends E> collection) {
		super(new LinkedList<>(collection));
	}

}