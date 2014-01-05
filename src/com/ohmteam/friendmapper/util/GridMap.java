package com.ohmteam.friendmapper.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.graphics.Point;

public class GridMap<T> {
	private final int tileWidth;
	private final Map<Point, List<Tuple<Point, T>>> grid = new HashMap<Point, List<Tuple<Point, T>>>();

	public GridMap(int tileWidth) {
		this.tileWidth = tileWidth;
	}

	public Point roundToGrid(Point point) {
		int px = point.x - (point.x % tileWidth);
		int py = point.y - (point.y % tileWidth);
		return new Point(px, py);
	}

	public Point roundToGridCenter(Point point) {
		Point corner = roundToGrid(point);
		int d = tileWidth / 2;
		return new Point(corner.x + d, corner.y + d);
	}

	/**
	 * Adds the given value to the appropriate grid tile, based on the given
	 * location.
	 * 
	 * @param location
	 * @param value
	 * @return A Point representing the grid tile that the value was added in.
	 */
	public Point add(Point location, T value) {
		Point key = roundToGrid(location);
		Tuple<Point, T> entry = new Tuple<Point, T>(location, value);
		List<Tuple<Point, T>> values = grid.get(key);
		if (values == null) {
			values = new ArrayList<Tuple<Point, T>>();
			grid.put(key, values);
		}
		values.add(entry);
		return key;
	}

	/**
	 * @return A set representing all of the grid tiles that have values stored
	 *         in them.
	 */
	public Set<Point> getFilledTiles() {
		return grid.keySet();
	}

	/**
	 * Looks up all values (and their corresponding points) that have been
	 * stored near the given location. The given gridDistance determines how
	 * many grid tiles are considered "nearby". A gridDistance of 0 will include
	 * only the tile where the location falls. A gridDistance of 1 will include
	 * all direct neighbors (including diagonals) of the center tile.
	 * 
	 * @param location
	 * @param gridDistance
	 * @return
	 */
	public List<Tuple<Point, T>> getNearbyEntries(Point location, int gridDistance) {
		Point center = roundToGrid(location);
		List<Tuple<Point, T>> results = new LinkedList<Tuple<Point, T>>();
		for (int dx = -gridDistance; dx <= gridDistance; dx++) {
			for (int dy = -gridDistance; dy <= gridDistance; dy++) {
				int x = center.x + (dx * tileWidth);
				int y = center.y + (dy * tileWidth);
				Point key = new Point(x, y);
				loadGridEntries(key, results);
			}
		}
		return results;
	}

	protected void loadGridEntries(Point key, List<Tuple<Point, T>> output) {
		List<Tuple<Point, T>> values = grid.get(key);
		if (values != null) {
			for (Tuple<Point, T> value : values) {
				output.add(value);
			}
		}
	}
}
