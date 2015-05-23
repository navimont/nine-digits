package com.swehner.newrelic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.regex.Pattern;

import com.swehner.newrelic.threadpool.CloseableAndRunnable;
import com.swehner.newrelic.threadpool.Shutdownable;

public class NineDigitReader extends CloseableAndRunnable {
	private UniqueNumberWriter uniqueNumberWriter;
	private Shutdownable application;
	private Socket socket;

	public NineDigitReader(Socket socket, Shutdownable application, UniqueNumberWriter uniqueNumberWriter) {
		this.uniqueNumberWriter = uniqueNumberWriter;
		this.application = application;
		this.socket = socket;
	}

	@Override
	public void run() {
		String line = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while((line = reader.readLine()) != null) {
				if (!isNineDigitNumber(line)) {
					break;
				}
				uniqueNumberWriter.write(line);
			}
		} catch (IOException e) {
			System.out.println("NineDigitReader: " + e.getMessage());
		} catch (InterruptedException e) {
			System.out.println("NineDigitReader is shutting down");
		} finally {
			close();
		}

        if ("terminate".equals(line)) {
			// shut down the complete application
			application.shutdownNow();
		}
	}

	static Pattern nineDigitsPattern = Pattern.compile("\\d{9}");

	/**
	 * @param line input string to be tested
	 * @return if input consists of exactly nine decimal digits
	 */
	protected static boolean isNineDigitNumber(String line) {
		return nineDigitsPattern.matcher(line).matches();
	}

	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			// do nothing
		}
	}
}
