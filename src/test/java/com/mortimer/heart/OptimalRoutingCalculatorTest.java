package com.mortimer.heart;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class OptimalRoutingCalculatorTest
{
	@Test
	public void shortBadTaskUsesFastRerollInsteadOfPoints()
	{
		RoutingDecision decision = calculate(100,
			offer("Rockslugs", 35, 500, 0),
			offer("Crawling hands", 35, 500, 0),
			offer("Banshees", 35, 450, 0));

		assertEquals(RoutingDecision.Type.FAST_REROLL, decision.getType());
	}

	@Test
	public void slowBadTasksCanUsePointSkip()
	{
		RoutingDecision decision = calculate(100,
			offer("Rockslugs", 100, 20, 0),
			offer("Crawling hands", 100, 20, 0),
			offer("Banshees", 100, 20, 0));

		assertEquals(RoutingDecision.Type.POINT_SKIP, decision.getType());
	}

	@Test
	public void slowBadTasksWithoutPointsKeepTaskFallback()
	{
		RoutingDecision decision = calculate(0,
			offer("Rockslugs", 100, 20, 0),
			offer("Crawling hands", 100, 20, 0),
			offer("Banshees", 100, 20, 0));

		assertNotEquals(RoutingDecision.Type.POINT_SKIP, decision.getType());
	}

	@Test
	public void offerInsideTargetWindowIsHunted()
	{
		RoutingDecision decision = calculate(500,
			offer("Hydras", 175, 120, 150),
			offer("Rockslugs", 50, 500, 0),
			offer("Crawling hands", 50, 500, 0));

		assertEquals(RoutingDecision.Type.HUNT, decision.getType());
		assertEquals("Hydras", decisionOfferName(decision,
			offer("Hydras", 175, 120, 150),
			offer("Rockslugs", 50, 500, 0),
			offer("Crawling hands", 50, 500, 0)));
	}

	@Test
	public void expectedExitTimeAccountsForEarlyHeart()
	{
		OfferState offer = offer("Crawling hands", 50, 500, 0);
		HeartResult result = HeartCalculator.calculate(offer, true);
		OptimalRoutingCalculator.TaskOutcome outcome = OptimalRoutingCalculator.outcome(offer, true);

		assertTrue(outcome.getHoursBeforeExit() > 0.0);
		assertTrue(outcome.getHoursBeforeExit() < result.getTaskHours());
		assertEquals(1.0 - result.getTaskChance(), outcome.getFailureChance(), 1e-12);
	}

	@Test
	public void slayerCapeRepeatLowersCostForStrongHeartTask()
	{
		OfferState offer = offer("Smoke devils", 100, 800, 0);
		OptimalRoutingCalculator.TaskOutcome outcome = OptimalRoutingCalculator.outcome(offer, true);

		assertTrue(outcome.costWithContinuation(100.0, true)
			< outcome.costWithContinuation(100.0, false));
	}

	@Test
	public void detectedBlocksAreRemovedFromFuturePool()
	{
		List<MortimerRoutingData.Profile> profiles = MortimerRoutingData.eligibleProfiles(99,
			new java.util.LinkedHashSet<>(Arrays.asList("Turoth", "Kurask")));

		assertTrue(profiles.stream().noneMatch(profile -> profile.getTask().getName().equals("Turoth")));
		assertTrue(profiles.stream().noneMatch(profile -> profile.getTask().getName().equals("Kurask")));
	}

	@Test
	public void alwaysTakePreferenceOverridesAPointSkip()
	{
		List<OfferState> offers = Arrays.asList(
			offer("Rockslugs", 100, 20, 0),
			offer("Crawling hands", 100, 20, 0),
			offer("Banshees", 100, 20, 0));

		RoutingDecision decision = OptimalRoutingCalculator.calculate(offers, true, 99, 500, 3,
			Collections.emptySet(), false,
			(task, superior) -> superior.effectiveKillsPerHour(task.getDefaultKph()),
			(task, amount) -> 0.0,
			task -> task.getName().equals("Banshees") ? TaskPreference.ALWAYS : TaskPreference.STANDARD);

		assertEquals(RoutingDecision.Type.HUNT, decision.getType());
		assertEquals("Banshees", offers.get(decision.getPrimaryIndex()).getTask().getName());
	}

	@Test
	public void earlyExitStillPaysFixedAssignmentOverhead()
	{
		HeartTask smoke = HeartData.findTask("Smoke devils");
		OfferState offer = new OfferState(smoke, smoke.getSuperiors().get(0), 100, 100,
			0, 800, 0.25, Bracelet.NONE);

		assertTrue(OptimalRoutingCalculator.outcome(offer, true).getHoursBeforeExit() >= 0.25);
	}

	private static RoutingDecision calculate(int points, OfferState... offers)
	{
		return OptimalRoutingCalculator.calculate(Arrays.asList(offers), true, 99, points, 3,
			task -> task.getDefaultKph());
	}

	private static OfferState offer(String taskName, int amount, double kph, double modifier)
	{
		HeartTask task = HeartData.findTask(taskName);
		return new OfferState(task, task.getSuperiors().get(0), amount, modifier, kph, Bracelet.NONE);
	}

	private static String decisionOfferName(RoutingDecision decision, OfferState... offers)
	{
		List<OfferState> list = Arrays.asList(offers);
		return list.get(decision.getPrimaryIndex()).getTask().getName();
	}
}
