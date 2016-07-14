package net.netcoding.nifty.core.util.concurrent.linked;

import net.netcoding.nifty.core.util.concurrent.atomic.AtomicList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
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
public class ConcurrentLinkedList<E> extends AtomicList<E, LinkedList<E>> {

	/**
	 * Create a new concurrent list.
	 */
	public ConcurrentLinkedList() {
		super(new LinkedList<>());
	}

	/**
	 * Create a new concurrent list and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentLinkedList(E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Create a new concurrent list and fill it with the given collection.
	 */
	public ConcurrentLinkedList(Collection<? extends E> collection) {
		super(new LinkedList<>(collection));
	}

}