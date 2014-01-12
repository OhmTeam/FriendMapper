package com.ohmteam.friendmapper.data.clustering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.graphics.Point;

import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.ohmteam.friendmapper.util.GridMap;
import com.ohmteam.friendmapper.util.HasLocation;
import com.ohmteam.friendmapper.util.Tuple;

public class Clusterizer<T extends HasLocation> {

	private final int radius;
	private final int radiusSquared;
	private final Projection projection;

	private final Map<Clusteree<T>, Point> screenLocationCache = new HashMap<Clusteree<T>, Point>();

	public Clusterizer(int radius, Projection projection) {
		this.radius = radius;
		this.projection = projection;
		this.radiusSquared = radius * radius;
	}

	public Map<LatLng, List<T>> findClusters(List<T> inputs) {

		// Create a list of citizens to pass into updateClusters
		List<Clusteree<T>> citizens = new ArrayList<Clusteree<T>>(inputs.size());
		for (T input : inputs) {
			Clusteree<T> c = new Clusteree<T>(input, input.getLocation());
			citizens.add(c);
		}

		// run the clustering algorithm on the citizens
		updateClusters(citizens);

		// assemble the clusters by location
		Map<LatLng, List<T>> clusters = new HashMap<LatLng, List<T>>();
		for (Clusteree<T> citizen : citizens) {
			LatLng clusterLoc = citizen.getClusterLocation();
			List<T> clusterMembers = clusters.get(clusterLoc);
			if (clusterMembers == null) {
				clusterMembers = new LinkedList<T>();
				clusters.put(clusterLoc, clusterMembers);
			}
			clusterMembers.add(citizen.getSelf());
		}

		return clusters;
	}

	public List<LocationChange> updateClusters(List<Clusteree<T>> citizens) {

		// build a spatial grid to assist the neighbor checks
		GridMap<Clusteree<T>> grid = new GridMap<Clusteree<T>>(radius);
		for (Clusteree<T> c : citizens) {
			grid.add(pointOf(c), c);
		}

		// build a neighborhood of citizen adjacencies
		Neighborhood<T> neighborhood = new Neighborhood<T>();
		for (Clusteree<T> c : citizens) {
			addAdjacencies(c, grid, neighborhood);
		}

		// Set up a list of citizens yet to be processed,
		// and a set of citizens that have been put in clusters
		LinkedList<Clusteree<T>> workingList = new LinkedList<Clusteree<T>>(citizens);
		Set<Clusteree<T>> finishedSet = new HashSet<Clusteree<T>>();

		// sort citizens by their number of neighbors
		Comparator<Clusteree<T>> citizenCompare = new ClustereeComparatorByNeighbors<T>(neighborhood);
		Collections.sort(workingList, citizenCompare);

		// Set up a list to put location changes in, to be returned
		List<LocationChange> locationChanges = new LinkedList<LocationChange>();

		// add citizens to clusters
		while (!workingList.isEmpty()) {
			// Take the first citizen off the front of the list.
			// It is the one with the most neighbors (or tied for most)
			Clusteree<T> citizen = workingList.removeFirst();

			// If the citizen was already put in a cluster, just ignore it.
			if (finishedSet.contains(citizen))
				continue;

			// Resolve a cluster that contains the `citizen`.
			Set<Clusteree<T>> cNeighbors = neighborhood.getNeighbors(citizen);
			WorkingCluster cluster = new WorkingCluster(citizen, cNeighbors);
			cluster.resolve(grid, finishedSet, 10);

			// Put all members of the cluster in the finished set,
			// and assign them to the cluster
			for (Clusteree<T> member : cluster.getMembers()) {
				finishedSet.add(member);

				// set the citizen's marker location to where the cluster is.
				// if it changed, add that change to the `locationChanges` list
				LocationChange change = member.setClusterLocation(cluster.getCenter());
				if (change != null) {
					locationChanges.add(change);
				}
			}
		}

		return locationChanges;
	}

	private Point pointOf(Clusteree<T> citizen) {
		Point screenLoc = screenLocationCache.get(citizen);
		if (screenLoc == null) {
			LatLng realLoc = citizen.getRealLocation();
			screenLoc = projection.toScreenLocation(realLoc);
			screenLocationCache.put(citizen, screenLoc);
		}
		return screenLoc;
	}

	private int distSquared(Point a, Point b) {
		int dx = b.x - a.x;
		int dy = b.y - a.y;
		return (dx * dx) + (dy * dy);
	}

	private void addAdjacencies(Clusteree<T> citizen, GridMap<Clusteree<T>> grid, Neighborhood<T> neighborhood) {
		Point cPoint = pointOf(citizen);

		// Look up potential neighbors from tiles at or
		// next to the citizen's tile in the grid
		List<Tuple<Point, Clusteree<T>>> tileNeighbors = grid.getNearbyEntries(cPoint, 1);
		for (Tuple<Point, Clusteree<T>> tup : tileNeighbors) {
			Clusteree<T> c2 = tup._2;

			// ignore c2 if it has already been identified as a neighbor
			if (neighborhood.checkAdjacency(citizen, c2)) {
				continue;
			}

			// check that c2 is close enough
			Point c2Point = pointOf(c2);
			if (distSquared(cPoint, c2Point) <= radiusSquared) {
				neighborhood.addAdjacency(citizen, c2);
			}
		}
	}

	private class WorkingCluster {
		// the starting citizen for the cluster
		private final Clusteree<T> origin;

		// current values
		private LatLng center;
		private Set<Clusteree<T>> members = new HashSet<Clusteree<T>>();
		private int sumOfSquares = Integer.MAX_VALUE;

		// values set by the updater function,
		// used to see if the update was better than the current
		private Set<Clusteree<T>> newMembers = new HashSet<Clusteree<T>>();
		private int newSumOfSquares;

		public WorkingCluster(Clusteree<T> origin, Collection<Clusteree<T>> startingNeighbors) {
			this.origin = origin;
			this.center = origin.getRealLocation(); // pointOf(origin);
			members.add(origin);
			if (startingNeighbors != null) {
				members.addAll(startingNeighbors);
			}
		}

		public LatLng getCenter() {
			return center;
		}

		public Set<Clusteree<T>> getMembers() {
			return members;
		}

		/**
		 * @return True if the values stored in `newXXX` are "better" than the current values of
		 *         "XXX".
		 */
		private boolean isNewBetter() {
			// instantly reject if the new set doesn't have the origin
			if (!newMembers.contains(origin)) {
				return false;
			}

			// favor more members
			if (newMembers.size() > members.size()) {
				return true;
			}

			// favor a lower sum of squared distances to the members
			if (newSumOfSquares < sumOfSquares) {
				return true;
			}

			// failing those, favor the current members
			return false;
		}

		public boolean resolve(GridMap<Clusteree<T>> grid, Set<Clusteree<T>> ignoredSet, int recursionCountdown) {

			// if we reached the recursion limit, return immediately
			if (recursionCountdown <= 0)
				return false;

			// pick a new Center based on the average of the current members
			LatLng newCenter = calculateCentroid();

			// stop recursion if the center didn't move
			if (newCenter.equals(center))
				return true;

			// figure out the new members and sum of squared distances, based on
			// the new center
			calculateNewValues(newCenter, grid, ignoredSet);

			if (isNewBetter()) {
				// improve the cluster by using the new values
				center = newCenter;
				members = newMembers;
				sumOfSquares = newSumOfSquares;

				// then recurse to try improving further
				return resolve(grid, ignoredSet, recursionCountdown - 1);
			} else {
				// keep old values and don't try any further
				return true;
			}
		}

		/**
		 * @return The Point that is the average of all of the current members' screen locations
		 */
		public LatLng calculateCentroid() {
			int numPoints = members.size();
			if (numPoints == 0) {
				return center;
			}
			double totalLat = 0;
			double totalLng = 0;
			for (Clusteree<T> member : members) {
				LatLng p = member.getRealLocation();
				totalLat += p.latitude;
				totalLng += p.longitude;
			}
			return new LatLng(totalLat / numPoints, totalLng / numPoints);
		}

		/**
		 * Find all citizens that are near enough to the `newCenter` yet not in the `ignoredSet`.
		 * The `newMembers` set will be repopulated with those citizens. The `newSumOfSquares` will
		 * be set to the total of the distance-squared between the member points and the newCenter
		 * point.
		 * 
		 * @param newCenter The new center that determines the new members
		 * @param grid Used to facilitate nearby-neighbor lookup
		 * @param ignoredSet A set of citizens that should never be included in the members
		 */
		private void calculateNewValues(LatLng newCenter, GridMap<Clusteree<T>> grid, Set<Clusteree<T>> ignoredSet) {
			newMembers.clear();
			newSumOfSquares = 0;

			// interact with the `newCenter` as a Point
			Point queryPoint = projection.toScreenLocation(newCenter);

			// get a list of potential neighbors from the grid
			List<Tuple<Point, Clusteree<T>>> tileNeighbors = grid.getNearbyEntries(queryPoint, 1);

			// narrow down the list with distance checks
			for (Tuple<Point, Clusteree<T>> tup : tileNeighbors) {
				Clusteree<T> c = tup._2;

				// ignore c if it is in the ignoredSet
				if (ignoredSet.contains(c)) {
					continue;
				}

				// check that c is close enough
				int distSquared = distSquared(queryPoint, pointOf(c));
				if (distSquared <= radiusSquared) {
					newMembers.add(c);
					newSumOfSquares += distSquared;
				}
			}
		}
	}

}
