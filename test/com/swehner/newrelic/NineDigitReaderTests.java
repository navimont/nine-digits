package com.swehner.newrelic;


import static org.junit.Assert.*;

import org.junit.Test;

public class NineDigitReaderTests {
	@Test
	public void testNineDigits() {
		assertTrue(NineDigitReader.isNineDigitNumber("123456789"));
		assertTrue(NineDigitReader.isNineDigitNumber("023456789"));

		assertFalse(NineDigitReader.isNineDigitNumber("123"));
		assertFalse(NineDigitReader.isNineDigitNumber("0123456789"));
		assertFalse(NineDigitReader.isNineDigitNumber("ABCDEFGHJ"));
	}
}
