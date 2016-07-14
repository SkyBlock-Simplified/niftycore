package net.netcoding.nifty.core.util.concurrent;

import net.netcoding.nifty.core.util.concurrent.atomic.AtomicList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A concurrent list that allows for simultaneously fast reading, iteration and
 * modification utilizing {@link AtomicReference}.
 * <p>
 * The AtomicReference changes the methods that modify the list by replacing the
 * entire list on each modification. This allows for maintaining the original
 * speed of {@link ArrayList#contains(Object)} and makes it cross-thread-safe.
 *
 * @param <E> type of elements
 */
public class ConcurrentList<E> extends AtomicList<E, ArrayList<E>> {

	/**
	 * Create a new concurrent list.
	 */
	public ConcurrentList() {
		super(new ArrayList<>());
	}

	/**
	 * Create a new concurrent list and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentList(E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Create a new concurrent list and fill it with the given collection.
	 */
	public ConcurrentList(Collection<? extends E> collection) {
		super(new ArrayList<>(collection));
	}

	@Override
	public ConcurrentList<E> subList(int start, int end) {
		return Concurrent.newList(super.subList(0, 5));
	}

}