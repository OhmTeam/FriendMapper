package com.ohmteam.friendmapper.util;

import java.util.concurrent.ThreadFactory;

/**
 * Thread Factory implementation that returns Daemon Threads. (A daemon thread
 * does not prevent the JVM from shutting down when other threads have ended.)
 * This class is intended to be used with a ThreadPool that executes background
 * tasks.
 * 
 * @author Dylan
 */
public class DaemonThreadFactory implements ThreadFactory {
	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r);
		t.setDaemon(true);
		return t;
	}
}
