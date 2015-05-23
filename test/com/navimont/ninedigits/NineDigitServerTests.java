package com.navimont.ninedigits;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Ignore;
import org.junit.Test;

public class NineDigitServerTests {
	@Test
	public void testFiveClients() throws IOException, InterruptedException {
		startServer();
		ExecutorService executor = Executors.newCachedThreadPool();
		for (int i=0; i< 5; i++) {
			executor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						Socket socket = new Socket("localhost", NineDigitServer.LISTENER_PORT);
						OutputStream stream = socket.getOutputStream();
						BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
						Random random = new Random();
						int counter = 0;
						while(counter < 2000000/5) {
							counter++;
							writer.write(String.format("%09d\n", Math.abs(random.nextInt()) % 1000000000));
						}
						writer.close();
						socket.close();
					} catch (IOException e) {
						System.out.println(e.getMessage());
					}
				}
			});
		}
		Thread.sleep(1000000);
	}

	@Ignore // used for manual testing
	@Test
	public void testOneClientContinuous() throws IOException {
		startServer();
		Socket socket = new Socket("localhost", NineDigitServer.LISTENER_PORT);
		OutputStream stream = socket.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
		Random random = new Random();
		while(true) {
			writer.write(String.format("%09d\n", Math.abs(random.nextInt()) % 1000000000));
		}
	}

	private void startServer() {
		Executors.newSingleThreadExecutor().submit(new Runnable() {
			@Override
			public void run() {
				NineDigitServer server = new NineDigitServer();
				try {
					server.main(new String[]{});
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		});
	}
}
