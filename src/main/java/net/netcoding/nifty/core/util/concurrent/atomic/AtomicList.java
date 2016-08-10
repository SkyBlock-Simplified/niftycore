package net.netcoding.nifty.core.util.concurrent.atomic;

import net.netcoding.nifty.core.reflection.exceptions.ReflectionException;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AtomicList<E, T extends AbstractList<E>> extends AbstractList<E> implements List<E> {

	private final AtomicReference<T> ref;

	protected AtomicList(T type) {
		this.ref = new AtomicReference<>(type);
	}

	@Override
	public final void add(int index, E element) {
		while (true) {
			T current = this.ref.get();
			T modified = this.newList(current);
			modified.add(index, element);

			if (this.ref.compareAndSet(current, modified))
				return;
		}
	}

	@Override
	public final boolean add(E element) {
		while (true) {
			T current = this.ref.get();
			T modified = this.newList(current);
			boolean result = modified.add(element);

			if (this.ref.compareAndSet(current, modified))
				return result;
		}
	}

	@Override
	public final boolean addAll(Collection<? extends E> collection) {
		while (true) {
			T current = this.ref.get();
			T modified = this.newList(current);
			boolean result = modified.addAll(collection);

			if (this.ref.compareAndSet(current, modified))
				return result;
		}
	}

	@Override
	public final boolean addAll(int index, Collection<? extends E> collection) {
		while (true) {
			T current = this.ref.get();
			T modified = this.newList(current);
			boolean result = modified.addAll(index, collection);

			if (this.ref.compareAndSet(current, modified))
				return result;
		}
	}

	@Override
	public final void clear() {
		this.ref.get().clear();
	}

	@Override
	public final boolean contains(Object item) {
		return ref.get().contains(item);
	}

	@Override
	public final boolean containsAll(Collection<?> collection) {
		return this.ref.get().containsAll(collection);
	}

	@Override
	public final E get(int index) {
		return this.ref.get().get(index);
	}

	@Override
	public final int indexOf(Object item) {
		return this.ref.get().indexOf(item);
	}

	@Override
	public final boolean isEmpty() {
		return this.ref.get().isEmpty();
	}

	@Override
	public final Iterator<E> iterator() {
		return this.ref.get().iterator();
	}

	@Override
	public final int lastIndexOf(Object item) {
		return this.ref.get().lastIndexOf(item);
	}

	@Override
	public final ListIterator<E> listIterator() {
		return this.ref.get().listIterator();
	}

	@Override
	public final ListIterator<E> listIterator(int index) {
		return this.ref.get().listIterator(index);
	}

	@SuppressWarnings("unchecked")
	private T newList(T current) {
		try {
			List<E> list = current.getClass().newInstance();
			list.addAll(current);
			return (T)list;
		} catch (Exception ex) {
			throw new ReflectionException("Unable to create new list instance of " + current.getClass().getSimpleName() + "!"); // Cannot use StringUtil!
		}
	}

	@Override
	public final E remove(int index) {
		while (true) {
			T current = this.ref.get();

			if (index >= current.size())
				return null;

			T modified = this.newList(current);
			E old = modified.remove(index);

			if (this.ref.compareAndSet(current, modified))
				return old;
		}
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	@Override
	public final boolean remove(Object element) {
		while (true) {
			T current = this.ref.get();

			if (!current.contains(element))
				return false;

			T modified = this.newList(current);
			boolean result = modified.remove(element);

			if (this.ref.compareAndSet(current, modified))
				return result;
		}
	}

	@Override
	public final boolean removeAll(Collection<?> collection) {
		while (true) {
			T current = this.ref.get();
			T modified = this.newList(current);
			boolean result = modified.removeAll(collection);

			if (this.ref.compareAndSet(current, modified))
				return result;
		}
	}

	@Override
	public final boolean retainAll(Collection<?> collection) {
		while (true) {
			T current = this.ref.get();
			T modified = this.newList(current);
			boolean result = modified.retainAll(collection);

			if (this.ref.compareAndSet(current, modified))
				return result;
		}
	}

	@Override
	public final E set(int index, E element) {
		while (true) {
			T current = this.ref.get();
			T modified = this.newList(current);
			E old = modified.set(index, element);

			if (this.ref.compareAndSet(current, modified))
				return old;
		}
	}

	@Override
	public final int size() {
		return this.ref.get().size();
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return this.ref.get().subList(fromIndex, toIndex);
	}

	@Override
	public final Object[] toArray() {
		return this.ref.get().toArray();
	}

	@SuppressWarnings("SuspiciousToArrayCall")
	@Override
	public final <U> U[] toArray(U[] array) {
		return this.ref.get().toArray(array);
	}

}