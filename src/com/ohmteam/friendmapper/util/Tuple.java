package com.ohmteam.friendmapper.util;

/**
 * Simple holder class for two values. Just like Tuple2[A,B] in scala.
 * 
 * @author Dylan
 * 
 * @param <A>
 * @param <B>
 */
public class Tuple<A, B> {
	public final A _1;
	public final B _2;

	public Tuple(A a, B b) {
		this._1 = a;
		this._2 = b;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Tuple) {
			Tuple<?, ?> that = (Tuple<?, ?>) o;
			return this._1.equals(that._1) && this._2.equals(that._2);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return 31 * _1.hashCode() + _2.hashCode();
	}
}
