package com.mortimer.heart;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BlockedTaskCodecTest
{
	@Test
	public void detectedBlocksRoundTripUsingCanonicalNames()
	{
		Set<String> expected = new LinkedHashSet<>(Arrays.asList("Turoth", "Kurask"));
		assertEquals(expected, BlockedTaskCodec.decode(BlockedTaskCodec.encode(expected)));
	}
}
