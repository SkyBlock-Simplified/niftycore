package net.netcoding.nifty.core.util.concurrent.linked;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A concurrent list that allows for simultaneously fast reading, iteration and
 * modification utilizing {@link AtomicReference}.
 * <p>
 * The AtomicReference changes the methods that modify the list by replacing the
 * entire list on each modification. This allows for maintaining the original
 * speed of {@link ArrayList#contains(Object)} and makes it cross-thread-safe.
 *
 * @param <T> type of elements
 */
public class ConcurrentLinkedList<T> extends AbstractList<T> implements List<T> {

	private final AtomicReference<LinkedList<T>> ref;

	/**
	 * Create a new concurrent list.
	 */
	public ConcurrentLinkedList() {
		this.ref = new AtomicReference<>(new LinkedList<T>());
	}

	/**
	 * Create a new concurrent list and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentLinkedList(T... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Create a new concurrent list and fill it with the given collection.
	 */
	public ConcurrentLinkedList(Collection<? extends T> collection) {
		this.ref = new AtomicReference<>(new LinkedList<>(collection));
	}

	@Override
	public void add(int index, T item) {
		while (true) {
			LinkedList<T> current = this.ref.get();
			LinkedList<T> modified = new LinkedList<>(current);
			modified.add(index, item);

			if (this.ref.compareAndSet(current, modified))
				return;
		}
	}

	@Override
	public boolean add(T item) {
		while (true) {
			LinkedList<T> current = this.ref.get();
			LinkedList<T> modified = new LinkedList<>(current);
			modified.add(item);

			if (this.ref.compareAndSet(current, modified))
				return true;
		}
	}

	@Override
	public boolean addAll(Collection<? extends T> collection) {
		while (true) {
			LinkedList<T> current = this.ref.get();
			LinkedList<T> modified = new LinkedList<>(current);
			modified.addAll(collection);

			if (this.ref.compareAndSet(current, modified))
				return true;
		}
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> collection) {
		while (true) {
			LinkedList<T> current = this.ref.get();
			LinkedList<T> modified = new LinkedList<>(current);
			modified.addAll(index, collection);

			if (this.ref.compareAndSet(current, modified))
				return true;
		}
	}

	@Override
	public void clear() {
		this.ref.get().clear();
	}

	@Override
	public boolean contains(Object item) {
		return ref.get().contains(item);
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		return this.ref.get().containsAll(collection);
	}

	@Override
	public T get(int index) {
		return this.ref.get().get(index);
	}

	@Override
	public int indexOf(Object item) {
		return this.ref.get().indexOf(item);
	}

	@Override
	public boolean isEmpty() {
		return this.ref.get().isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return this.ref.get().iterator();
	}

	@Override
	public int lastIndexOf(Object item) {
		return this.ref.get().lastIndexOf(item);
	}

	@Override
	public ListIterator<T> listIterator() {
		return this.ref.get().listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return this.ref.get().listIterator(index);
	}

	@Override
	public T remove(int index) {
		while (true) {
			LinkedList<T> current = this.ref.get();

			if (index >= current.size())
				return null;

			LinkedList<T> modified = new LinkedList<>(current);
			T item = modified.remove(index);

			if (this.ref.compareAndSet(current, modified))
				return item;
		}
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	@Override
	public boolean remove(Object item) {
		while (true) {
			LinkedList<T> current = this.ref.get();

			if (!current.contains(item))
				return false;

			LinkedList<T> modified = new LinkedList<>(current);
			boolean result = modified.remove(item);

			if (this.ref.compareAndSet(current, modified))
				return result;
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		while (true) {
			LinkedList<T> current = this.ref.get();
			LinkedList<T> modified = new LinkedList<>(current);
			boolean result = modified.removeAll(c);

			if (this.ref.compareAndSet(current, modified))
				return result;
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return this.ref.get().retainAll(c);
	}

	@Override
	public T set(int index, T item) {
		while (true) {
			LinkedList<T> current = this.ref.get();
			LinkedList<T> modified = new LinkedList<>(current);
			modified.set(index, item);

			if (this.ref.compareAndSet(current, modified))
				return item;
		}
	}

	@Override
	public int size() {
		return this.ref.get().size();
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return this.ref.get().subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return this.ref.get().toArray();
	}

	@SuppressWarnings("SuspiciousToArrayCall")
	@Override
	public <U> U[] toArray(U[] array) {
		return this.ref.get().toArray(array);
	}

}