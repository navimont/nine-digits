package com.navimont.ninedigits;


import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.BitSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.navimont.ninedigits.threadpool.Shutdownable;

public class UniqueNumberWriter implements Shutdownable {
	private final String fileName;
	private AtomicInteger totalUnique = new AtomicInteger();
	private AtomicInteger deltaDuplicates = new AtomicInteger();
	private int lastUnique = 0;
	private BitSet nineDigitNumbers = new BitSet(999999999);

	private ExecutorService writingExecutor = Executors.newSingleThreadExecutor();
	private BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

	public UniqueNumberWriter(String fileName) {
		this.fileName = fileName;

		writingExecutor.submit(writingTask);
	}

	Runnable writingTask = new Runnable() {
		@Override
		public void run() {
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("numbers.log")));
				while (true) {
					String number = queue.take();
					if (isUnique(number)) {
						totalUnique.incrementAndGet();
						writer.write(number);
						writer.newLine();
					} else {
						deltaDuplicates.incrementAndGet();
					}
				}
			} catch (FileNotFoundException e) {
				System.out.println("Could not open " +fileName);
			} catch (InterruptedException e) {
				System.out.println("UniqueNumberWriter is shutting down");
			} catch (IOException e) {
				System.out.println("Error writing to file " + fileName);
			} finally {
				if (writer != null) try {
					writer.close();
				} catch (IOException e) {
					// nope
				}
			}
		}
	};

	private boolean isUnique(String number) {
		int index = Integer.parseInt(number);
		boolean seen = nineDigitNumbers.get(index);
		nineDigitNumbers.flip(index);
		return !seen;
	}

	public void write(String number) throws InterruptedException {
        // avoid any issues with concurrent access to the BitSet and the output file
        // by putting the input into a queue
		queue.put(number);
	}

	public String getStatus() {
		return "Received  " + (totalUnique.get() - lastUnique)
				+ " unique numbers, " + deltaDuplicates.getAndSet(0)
				+ " duplicates. Unique total: " + (lastUnique = totalUnique.get());
	}

	@Override
	public void shutdownNow() {
		writingExecutor.shutdownNow();
	}
}
