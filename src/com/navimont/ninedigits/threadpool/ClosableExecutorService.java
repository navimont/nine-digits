package com.navimont.ninedigits.threadpool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ClosableExecutorService implements Shutdownable {
	private final ExecutorService executorService;
	private int maxThreads;
	private Map<CloseableAndRunnable,Future> submittedTasks = new HashMap<CloseableAndRunnable,Future>();

	public ClosableExecutorService(int maxThreads) {
		this.maxThreads = maxThreads;
		this.executorService = Executors.newFixedThreadPool(maxThreads);
	}

	@Override
	public void shutdownNow() {
		for (CloseableAndRunnable task : submittedTasks.keySet()) {
			task.close();
		}
		executorService.shutdownNow();
	}

	public boolean isShutdown() {
		return executorService.isShutdown();
	}

	public boolean awaitTermination() throws InterruptedException {
		return executorService.awaitTermination(1, TimeUnit.SECONDS);
	}

	public void submit(CloseableAndRunnable socketReaderTask) {
		submittedTasks.put(socketReaderTask, executorService.submit(socketReaderTask));
        System.out.println("clients connected: "+submittedTasks.size());
    }

	/**
	 * if all threads are occupied, this method will block until a client disconnects
	 */
	public void waitForThreadAvailable() throws ExecutionException, InterruptedException {
		while (submittedTasks.size() >= maxThreads) {
			Thread.sleep(10);
			purgeFinishedTasks();
		}
	}

	private void purgeFinishedTasks() throws InterruptedException, ExecutionException {
		Iterator<Map.Entry<CloseableAndRunnable,Future>> submittedTasksIterator = submittedTasks.entrySet().iterator();
		while (submittedTasksIterator.hasNext()) {
			Future taskFuture = submittedTasksIterator.next().getValue();
			if (taskFuture.isDone()) {
				submittedTasksIterator.remove();
			}
		}
	}
}
