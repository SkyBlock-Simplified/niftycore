package net.netcoding.niftycore.util.concurrent.linked;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A concurrent set that allows for simultaneously fast reading, iteration and
 * modification utilizing {@link AtomicReference}.
 * <p>
 * The AtomicReference changes the methods that modify the set by replacing the
 * entire set each modification. This allows for maintaining the original speed
 * of {@link HashSet#contains(Object)} and makes it cross-thread-safe.
 *
 * @param <T> type of elements
 */
public class ConcurrentLinkedSet<T> extends AbstractSet<T> implements Set<T> {

	private final AtomicReference<LinkedHashSet<T>> ref;

	/**
	 * Create a new concurrent set.
	 */
	public ConcurrentLinkedSet() {
		this.ref = new AtomicReference<>(new LinkedHashSet<T>());
	}

	/**
	 * Create a new concurrent set and fill it with the given collection.
	 */
	public ConcurrentLinkedSet(Collection<? extends T> collection) {
		this.ref = new AtomicReference<>(new LinkedHashSet<>(collection));
	}

	@Override
	public boolean add(T item) {
		while (true) {
			LinkedHashSet<T> current = this.ref.get();

			if (current.contains(item))
				return false;

			LinkedHashSet<T> modified = new LinkedHashSet<>(current);
			modified.add(item);

			if (this.ref.compareAndSet(current, modified))
				return true;
		}
	}

	@Override
	public boolean addAll(Collection<? extends T> collection) {
		while (true) {
			LinkedHashSet<T> current = this.ref.get();
			LinkedHashSet<T> modified = new LinkedHashSet<>(current);
			modified.addAll(collection);

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
	public boolean isEmpty() {
		return this.ref.get().isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return this.ref.get().iterator();
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	@Override
	public boolean remove(Object item) {
		while (true) {
			LinkedHashSet<T> current = this.ref.get();

			if (!current.contains(item))
				return false;

			LinkedHashSet<T> modified = new LinkedHashSet<>(current);
			modified.remove(item);

			if (this.ref.compareAndSet(current, modified))
				return true;
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		while (true) {
			LinkedHashSet<T> current = this.ref.get();
			LinkedHashSet<T> modified = new LinkedHashSet<>(current);
			boolean result = modified.removeAll(c);
			super.toArray();

			if (this.ref.compareAndSet(current, modified))
				return result;
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return this.ref.get().retainAll(c);
	}

	@Override
	public int size() {
		return this.ref.get().size();
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