package com.swehner.newrelic;


import static org.junit.Assert.*;

import java.util.BitSet;

import org.junit.Test;

public class BitSetTests {
	@Test
	public void testBitSet() {
		BitSet fours = new BitSet(4444);

		fours.set(2222);
		assertTrue(fours.get(2222));
		assertFalse(fours.get(2223));
	}
}
