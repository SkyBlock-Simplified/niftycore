package net.netcoding.nifty.core.util.concurrent.atomic;

import net.netcoding.nifty.core.reflection.exceptions.ReflectionException;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AtomicSet<E, T extends AbstractSet<E>> extends AbstractSet<E> implements Set<E> {

	private final AtomicReference<T> ref;

	protected AtomicSet(T type) {
		this.ref = new AtomicReference<>(type);
	}

	@Override
	public final boolean add(E element) {
		while (true) {
			T current = this.ref.get();

			if (current.contains(element))
				return false;

			T modified = this.newSet(current);
			boolean result = modified.add(element);

			if (this.ref.compareAndSet(current, modified))
				return result;
		}
	}

	@Override
	public final boolean addAll(Collection<? extends E> collection) {
		while (true) {
			T current = this.ref.get();
			T modified = this.newSet(current);
			boolean result = modified.addAll(collection);

			if (this.ref.compareAndSet(current, modified))
				return result;
		}
	}

	@Override
	public final void clear() {
		this.ref.get().clear();
	}

	@Override
	public final boolean contains(Object element) {
		return ref.get().contains(element);
	}

	@Override
	public final boolean containsAll(Collection<?> collection) {
		return this.ref.get().containsAll(collection);
	}

	@Override
	public final boolean isEmpty() {
		return this.ref.get().isEmpty();
	}

	@Override
	public final Iterator<E> iterator() {
		return this.ref.get().iterator();
	}

	@SuppressWarnings("unchecked")
	private T newSet(T current) {
		try {
			Set<E> set = current.getClass().newInstance();
			set.addAll(current);
			return (T)set;
		} catch (Exception ex) {
			throw new ReflectionException("Unable to create new set instance of " + current.getClass().getSimpleName() + "!"); // Cannot use StringUtil!
		}
	}

	@Override
	public final boolean remove(Object item) {
		while (true) {
			T current = this.ref.get();

			if (!current.contains(item))
				return false;

			T modified = this.newSet(current);
			boolean result = modified.remove(item);

			if (this.ref.compareAndSet(current, modified))
				return result;
		}
	}

	@Override
	public final boolean removeAll(Collection<?> collection) {
		while (true) {
			T current = this.ref.get();
			T modified = this.newSet(current);
			boolean result = modified.removeAll(collection);

			if (this.ref.compareAndSet(current, modified))
				return result;
		}
	}

	@Override
	public final boolean retainAll(Collection<?> collection) {
		while (true) {
			T current = this.ref.get();
			T modified = this.newSet(current);
			boolean result = modified.retainAll(collection);

			if (this.ref.compareAndSet(current, modified))
				return result;
		}
	}

	@Override
	public final int size() {
		return this.ref.get().size();
	}

	@Override
	public final Object[] toArray() {
		return this.ref.get().toArray();
	}

	@Override
	public final <U> U[] toArray(U[] array) {
		return this.ref.get().toArray(array);
	}

}