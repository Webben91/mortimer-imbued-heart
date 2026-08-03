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
		String key = LearnedPaceCodec.key("Jellies", "Warped Jelly");
		samples.put(key, new LearnedPace(100, 900_000L).combine(50, 450_000L));
		String chilledKey = LearnedPaceCodec.key("Jellies", "Chilled Jelly");
		samples.put(chilledKey, new LearnedPace(80, 800_000L));

		Map<String, LearnedPace> decodedSamples = LearnedPaceCodec.decode(LearnedPaceCodec.encode(samples));
		LearnedPace decoded = decodedSamples.get(key);

		assertEquals(150, decoded.getKills());
		assertEquals(1_350_000L, decoded.getElapsedMillis());
		assertEquals(400.0, decoded.killsPerHour(), 0.001);
		assertEquals(360.0, decodedSamples.get(chilledKey).killsPerHour(), 0.001);
	}
}
