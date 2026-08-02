package com.mortimer.heart;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PreferenceRecommendationCalculatorTest
{
	@Test
	public void xpPreferenceChoosesHighestSlayerExperienceRate()
	{
		List<OfferState> offers = Arrays.asList(
			offer("Smoke devils", 100, 0),
			offer("Venators", 100, 0));

		assertEquals(1, PreferenceRecommendationCalculator.bestIndex(
			offers, true, GrindPreference.SLAYER_XP));
	}

	@Test
	public void xpMortifierChangesRecommendation()
	{
		List<OfferState> offers = Arrays.asList(
			offer("Smoke devils", 100, 150),
			offer("Venators", 100, 0));

		assertEquals(0, PreferenceRecommendationCalculator.bestIndex(
			offers, true, GrindPreference.SLAYER_XP));
		assertEquals(46_250.0, SlayerExperienceData.experiencePerHour(offers.get(0)), 0.001);
	}

	@Test
	public void balancedPreferenceRewardsBothHeartAndExperience()
	{
		List<OfferState> offers = Arrays.asList(
			offer("Smoke devils", 100, 0),
			offer("Venators", 100, 0));

		assertEquals(0, PreferenceRecommendationCalculator.bestIndex(
			offers, true, GrindPreference.BALANCED));
	}

	private static OfferState offer(String taskName, double kph, double xpModifier)
	{
		HeartTask task = HeartData.findTask(taskName);
		return new OfferState(task, task.getSuperiors().get(0), task.getPlanningAmount(), 0,
			xpModifier, kph, Bracelet.NONE);
	}
}
