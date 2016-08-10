package net.netcoding.nifty.core.util.concurrent.atomic;

import net.netcoding.nifty.core.util.concurrent.Concurrent;
import net.netcoding.nifty.core.util.concurrent.linked.ConcurrentLinkedList;

import java.util.*;

public abstract class AtomicQueue<E> extends AbstractQueue<E> implements Queue<E> {

	protected final ConcurrentLinkedList<E> storage;

	protected AtomicQueue(Collection<? extends E> collection) {
		this.storage = Concurrent.newLinkedList(collection);
	}

	@Override
	public final boolean add(E element) {
		return super.add(element);
	}

	@Override
	public final boolean addAll(Collection<? extends E> collection) {
		return this.storage.addAll(collection);
	}

	@Override
	public final void clear() {
		super.clear();
	}

	@Override
	public final boolean contains(Object obj) {
		return this.storage.contains(obj);
	}

	@Override
	public final boolean containsAll(Collection<?> collection) {
		return this.storage.containsAll(collection);
	}

	@Override
	public final E element() {
		return super.element();
	}

	@Override
	public final boolean isEmpty() {
		return this.storage.isEmpty();
	}

	@Override
	public final Iterator<E> iterator() {
		return this.storage.iterator();
	}

	@Override
	public final boolean offer(E element) {
		return this.storage.add(element);
	}

	@Override
	public final E peek() {
		return this.isEmpty() ? null : this.storage.get(0);
	}

	@Override
	public final E poll() {
		return this.isEmpty() ? null : this.storage.remove(0);
	}

	@Override
	public final E remove() {
		return super.remove();
	}

	@Override
	public final boolean remove(Object obj) {
		return super.remove(obj);
	}

	@Override
	public final boolean removeAll(Collection<?> collection) {
		return this.storage.removeAll(collection);
	}

	@Override
	public final boolean retainAll(Collection<?> collection) {
		return this.storage.retainAll(collection);
	}

	@Override
	public final int size() {
		return this.storage.size();
	}

	@Override
	public final Object[] toArray() {
		return this.storage.toArray();
	}

	@SuppressWarnings("SuspiciousToArrayCall")
	@Override
	public final <T> T[] toArray(T[] array) {
		return this.storage.toArray(array);
	}

}