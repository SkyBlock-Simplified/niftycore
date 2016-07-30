package net.netcoding.nifty.core.util.misc;

import net.netcoding.nifty.core.util.concurrent.Concurrent;
import net.netcoding.nifty.core.util.concurrent.ConcurrentMap;
import net.netcoding.nifty.core.util.concurrent.ConcurrentSet;

import java.util.*;

public class DirectedGraph<T> implements Iterable<T> {

	// key is a Node, value is a set of Nodes connected by outgoing edges from the key
	private final ConcurrentMap<T, ConcurrentSet<T>> graph = Concurrent.newMap();

	public boolean addNode(T node) {
		if (this.graph.containsKey(node))
			return false;

		this.graph.put(node, Concurrent.newSet());
		return true;
	}

	public void addNodes(Collection<T> nodes) {
		nodes.forEach(this::addNode);
	}

	public void addEdge(T src, T dest) {
		validateSourceAndDestinationNodes(src, dest);

		// Add the edge by adding the dest node into the outgoing edges
		this.graph.get(src).add(dest);
	}

	public void removeEdge(T src, T dest) {
		validateSourceAndDestinationNodes(src, dest);

		this.graph.get(src).remove(dest);
	}

	public boolean edgeExists(T src, T dest) {
		validateSourceAndDestinationNodes(src, dest);

		return this.graph.get(src).contains(dest);
	}

	public Set<T> edgesFrom(T node) {
		// Check that the node exists.
		Set<T> edges = graph.get(node);
		if (edges == null)
			throw new NoSuchElementException("Source node does not exist.");

		return Collections.unmodifiableSet(edges);
	}

	@Override
	public Iterator<T> iterator() {
		return this.graph.keySet().iterator();
	}

	public int size() {
		return this.graph.size();
	}

	public boolean isEmpty() {
		return this.graph.isEmpty();
	}

	private void validateSourceAndDestinationNodes(T src, T dest) {
		// Confirm both endpoints exist
		if (!this.graph.containsKey(src) || !this.graph.containsKey(dest))
			throw new NoSuchElementException("Both nodes must be in the graph.");
	}

}