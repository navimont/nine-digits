package com.navimont.ninedigits.threadpool;

public abstract class CloseableAndRunnable implements Runnable {
	// not using Closeable interface because I'm not interested in the exception thrown
	public abstract void close();
}
