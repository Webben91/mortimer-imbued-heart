package com.mortimer.heart;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LearnedPaceCodecTest
{
	@Test
	public void learnedSamplesRoundTripAndCombine()
	{
		Map<String, LearnedPace> samples = new LinkedHashMap<>();
		samples.put("Jellies", new LearnedPace(100, 900_000L).combine(50, 450_000L));

		LearnedPace decoded = LearnedPaceCodec.decode(LearnedPaceCodec.encode(samples)).get("Jellies");

		assertEquals(150, decoded.getKills());
		assertEquals(1_350_000L, decoded.getElapsedMillis());
		assertEquals(400.0, decoded.killsPerHour(), 0.001);
	}
}
