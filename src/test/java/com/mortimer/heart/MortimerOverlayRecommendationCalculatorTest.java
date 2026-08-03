package com.mortimer.heart;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MortimerOverlayRecommendationCalculatorTest
{
	@Test
	public void heartPreferenceUsesPurpleBestHeartRecommendation()
	{
		MortimerOverlayRecommendation recommendation = calculate(GrindPreference.IMBUED_HEART, 500,
			task -> 120.0,
			offer("Hydras", 175, 150, 0),
			offer("Rockslugs", 50, 0, 0),
			offer("Crawling hands", 50, 0, 0));

		assertEquals(0, recommendation.getOfferIndex());
		assertEquals(MortimerOverlayRecommendation.Style.HEART, recommendation.getStyle());
		assertEquals("BEST HEART · SLAUGHTER", recommendation.getLabel());
	}

	@Test
	public void xpPreferenceUsesGreenBestXpRecommendation()
	{
		MortimerOverlayRecommendation recommendation = calculate(GrindPreference.SLAYER_XP, 0,
			task -> 100.0,
			offer("Smoke devils", 100, 0, 0),
			offer("Venators", 100, 0, 0));

		assertEquals(1, recommendation.getOfferIndex());
		assertEquals(MortimerOverlayRecommendation.Style.SLAYER_XP, recommendation.getStyle());
		assertEquals("RECOMMENDED · BEST XP", recommendation.getLabel());
	}

	@Test
	public void quickBadOffersUseBlueFastRerollRecommendation()
	{
		MortimerOverlayRecommendation recommendation = calculate(GrindPreference.IMBUED_HEART, 100,
			task -> task.getName().equals("Banshees") ? 450.0 : 500.0,
			offer("Rockslugs", 35, 0, 0),
			offer("Crawling hands", 35, 0, 0),
			offer("Banshees", 35, 0, 0));

		assertEquals(MortimerOverlayRecommendation.Style.FAST_REROLL, recommendation.getStyle());
		assertEquals("FAST REROLL · EXPEDITIOUS", recommendation.getLabel());
	}

	@Test
	public void pointSkipUsesRedBorderOnFastestFallback()
	{
		MortimerOverlayRecommendation recommendation = calculate(GrindPreference.IMBUED_HEART, 100,
			task -> 20.0,
			offer("Rockslugs", 100, 0, 0),
			offer("Crawling hands", 100, 0, 0),
			offer("Banshees", 100, 0, 0));

		assertEquals(0, recommendation.getOfferIndex());
		assertEquals(MortimerOverlayRecommendation.Style.POINT_SKIP, recommendation.getStyle());
		assertEquals("SKIP · FASTEST REROLL", recommendation.getLabel());
	}

	private static MortimerOverlayRecommendation calculate(GrindPreference preference, int points,
		java.util.function.ToDoubleFunction<HeartTask> killsPerHour, MortimerDetectedOffer... offers)
	{
		List<MortimerDetectedOffer> detected = Arrays.asList(offers);
		return MortimerOverlayRecommendationCalculator.calculate(
			detected, false, preference, true, 99, points, killsPerHour);
	}

	private static MortimerDetectedOffer offer(String taskName, int amount, double heartModifier,
		double xpModifier)
	{
		HeartTask task = HeartData.findTask(taskName);
		return new MortimerDetectedOffer(task, amount, heartModifier, xpModifier, "test modifier");
	}
}
