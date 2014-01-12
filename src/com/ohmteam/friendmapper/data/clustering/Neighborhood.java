package com.ohmteam.friendmapper.data.clustering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ohmteam.friendmapper.util.HasLocation;

/**
 * Represents a graph in which objects of type T are the nodes, and there are edges between
 * neighbors.
 * 
 * @author Dylan
 * 
 * @param <T>
 */
public class Neighborhood<T extends HasLocation> {

	private final Map<Clusteree<T>, Set<Clusteree<T>>> neighborsMap = new HashMap<Clusteree<T>, Set<Clusteree<T>>>();

	// adds a->b and b->a to the neighborsMap
	public void addAdjacency(Clusteree<T> a, Clusteree<T> b) {
		if (!a.equals(b)) {
			addDirectedAdjacency(a, b);
			addDirectedAdjacency(b, a);
		}
	}

	public boolean checkAdjacency(Clusteree<T> a, Clusteree<T> b) {
		Set<Clusteree<T>> neighbors = neighborsMap.get(a);
		if (neighbors == null) {
			return false;
		} else {
			return neighbors.contains(b);
		}
	}

	private void addDirectedAdjacency(Clusteree<T> from, Clusteree<T> to) {
		Set<Clusteree<T>> neighbors = neighborsMap.get(from);
		if (neighbors == null) {
			neighbors = new HashSet<Clusteree<T>>();
			neighborsMap.put(from, neighbors);
		}
		neighbors.add(to);
	}

	// may return null
	public Set<Clusteree<T>> getNeighbors(Clusteree<T> c) {
		return neighborsMap.get(c);
	}

}
