package com.ohmteam.friendmapper.data.clustering;

import java.util.Comparator;
import java.util.Set;

import com.ohmteam.friendmapper.util.HasLocation;

/**
 * Comparator for Clusterees that says one Clusteree is "less than" another Clusteree if it has more
 * neighbors according to a given Neighborhood. This is used for sorting a list of Clusterees so
 * that the ones with more neighbors appear earlier in the list.
 * 
 * @author Dylan
 * 
 * @param <T> The type of items in the Clusterees, and the Neighborhood. It must have a
 *        `getLocation` method.
 */
public class ClustereeComparatorByNeighbors<T extends HasLocation> implements Comparator<Clusteree<T>> {
	private final Neighborhood<T> neighborhood;

	public ClustereeComparatorByNeighbors(Neighborhood<T> neighborhood) {
		this.neighborhood = neighborhood;
	}

	public int compare(Clusteree<T> lhs, Clusteree<T> rhs) {
		Set<Clusteree<T>> leftNeighbors = neighborhood.getNeighbors(lhs);
		Set<Clusteree<T>> rightNeighbors = neighborhood.getNeighbors(rhs);

		int leftSize = 0;
		int rightSize = 0;
		if (leftNeighbors != null) {
			leftSize = leftNeighbors.size();
		}
		if (rightNeighbors != null) {
			rightSize = rightNeighbors.size();
		}

		return rightSize - leftSize;
	}
}
