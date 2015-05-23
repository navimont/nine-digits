package com.navimont.ninedigits;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.navimont.ninedigits.threadpool.ClosableExecutorService;

public class NineDigitServer {
	public static final int LISTENER_PORT = 4000;
	public static final int MAX_CONNECTIONS = 5;
	public static final String OUTPUT_FILE = "numbers.log";

	private static final int SYSTEM_TASK_POLL_RATE = 10;

	private static ClosableExecutorService threadPool = new ClosableExecutorService(MAX_CONNECTIONS);
	private static ExecutorService listenerThread = Executors.newSingleThreadExecutor();

    /**
     * Create a thread pool and wait for connections on the port LISTENER_PORT.
     * Incoming connections are handled by NineDigitReader class.
     */
	public static void main(String[] args) throws Exception {
		final ServerSocket listener = new ServerSocket(LISTENER_PORT);
		final UniqueNumberWriter writer = new UniqueNumberWriter(OUTPUT_FILE);

		// need separate listener thread to be able to interrupt socket server listener when application is terminating
		listenerThread.submit(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("Waiting for connections on port " + LISTENER_PORT);
					while(true) {
						// blocks either on waiting for next connection or on available thread
						threadPool.submit(new NineDigitReader(listener.accept(), threadPool, writer));
						threadPool.waitForThreadAvailable();
					}
				} catch (Exception e) {
					System.out.println("Port listener interrupted: " + e.getMessage());
				}
			}
		});

        int millisSinceLastReport = 0;
		while (!threadPool.isShutdown()) {
			if (millisSinceLastReport > 10000) {
				System.out.println(writer.getStatus());
				millisSinceLastReport = 0;
			}

			millisSinceLastReport += SYSTEM_TASK_POLL_RATE;
			Thread.sleep(SYSTEM_TASK_POLL_RATE);
		}
		System.out.println("Server is shutting down.");
		listener.close();
		listenerThread.shutdownNow();
		writer.shutdownNow();
	}
}
