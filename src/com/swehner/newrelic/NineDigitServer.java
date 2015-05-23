package com.swehner.newrelic;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.swehner.newrelic.threadpool.ClosableExecutorService;

public class NineDigitServer {
	public static final int LISTENER_PORT = 4000;
	public static final int MAX_CONNECTIONS = 5;

	private static ClosableExecutorService threadPool = new ClosableExecutorService(MAX_CONNECTIONS);
	private static ExecutorService listenerThread = Executors.newSingleThreadExecutor();

	public static void main(String[] args) throws Exception {
		final ServerSocket listener = new ServerSocket(LISTENER_PORT);
		final UniqueNumberWriter writer = new UniqueNumberWriter("numbers.log");

		// need separate listener thread to be able to interrupt listener when application is terminated
		listenerThread.submit(new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						// blocks either on waiting for next connection or on available thread
						threadPool.submit(new NineDigitReader(listener.accept(), threadPool, writer));
						threadPool.waitForThreadAvailable();
					}
				} catch (Exception e) {
					System.out.println("Interrupted while waiting for new connections: " + e.getMessage());
				}
			}
		});

		while (!threadPool.isShutdown()) {
			System.out.println(writer.getStatus());
			Thread.sleep(1000);
		}
		listener.close();
		listenerThread.shutdownNow();
		writer.shutdownNow();
	}
}
