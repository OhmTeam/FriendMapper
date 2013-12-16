package com.ohmteam.friendmapper.util;

/**
 * Represents a callback function, used as a return value for some asynchronous
 * calculation that returns a result of type <code>T</code>.
 * 
 * Since the calculation may result in either a successful result or an
 * exception, two methods are specified: <code>onSuccess</code> and
 * <code>onFailure</code>. Each should be implemented.
 * 
 * @author Dylan
 * 
 * @param <T>
 *            The type of a successful result.
 */
public interface ResultCallback<T> {
	public void onSuccess(T result);

	public void onFailure(Throwable cause);
}
