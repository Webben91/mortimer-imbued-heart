package com.mortimer.heart;

import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TaskActivityTrackerTest
{
	@Test
	public void firstTaskProgressExcludesInitialTravel()
	{
		TaskActivityTracker tracker = new TaskActivityTracker();
		tracker.update("Banshees", 50, "Banshee", false, false, 1_000L, 450, Bracelet.NONE);
		tracker.update("Banshees", 49, "Banshee", true, false, 61_000L, 450, Bracelet.NONE);
		tracker.update("Banshees", 49, "Banshee", true, false, 61_600L, 450, Bracelet.NONE);
		tracker.update("Banshees", 48, "Banshee", true, false, 62_200L, 450, Bracelet.NONE);

		TaskActivitySample sample = tracker.currentSample();
		assertEquals(1, sample.getKills());
		assertEquals(1_200L, sample.getActiveMillis());
	}

	@Test
	public void unrelatedCombatStopsAccumulatingImmediately()
	{
		TaskActivityTracker tracker = armedTracker();
		tracker.update("Banshees", 49, "Banshee", true, false, 2_600L, 450, Bracelet.NONE);
		long before = tracker.currentSample().getActiveMillis();
		tracker.update("Banshees", 49, "Banshee", false, true, 3_200L, 450, Bracelet.NONE);
		tracker.update("Banshees", 49, "Banshee", false, true, 3_800L, 450, Bracelet.NONE);
		tracker.update("Banshees", 49, "Banshee", false, false, 4_400L, 450, Bracelet.NONE);

		assertEquals(before, tracker.currentSample().getActiveMillis());
	}

	@Test
	public void longIdleGapIsNotAddedToActiveTime()
	{
		TaskActivityTracker tracker = armedTracker();
		tracker.update("Banshees", 49, "Banshee", true, false, 2_600L, 450, Bracelet.NONE);
		long before = tracker.currentSample().getActiveMillis();
		tracker.update("Banshees", 49, "Banshee", false, false, 120_000L, 450, Bracelet.NONE);

		assertEquals(before, tracker.currentSample().getActiveMillis());
	}

	@Test
	public void variantsProduceSeparateSamples()
	{
		TaskActivityTracker tracker = armedTracker();
		tracker.update("Banshees", 49, "Banshee", true, false, 2_600L, 450, Bracelet.NONE);
		tracker.update("Banshees", 48, "Banshee", true, false, 3_200L, 450, Bracelet.NONE);
		tracker.update("Banshees", 48, "Twisted banshee", true, false, 3_800L, 450, Bracelet.NONE);
		tracker.update("Banshees", 47, "Twisted banshee", true, false, 4_400L, 450, Bracelet.NONE);

		List<TaskActivitySample> samples = tracker.samples();
		assertEquals(2, samples.size());
		assertTrue(samples.stream().anyMatch(sample -> sample.getMonsterName().equals("Banshee")));
		assertTrue(samples.stream().anyMatch(sample -> sample.getMonsterName().equals("Twisted banshee")));
	}

	@Test
	public void inactivityWindowAdaptsToExpectedKillTime()
	{
		assertTrue(TaskActivityTracker.inactivityGraceMillis(120)
			> TaskActivityTracker.inactivityGraceMillis(1_000));
	}

	private static TaskActivityTracker armedTracker()
	{
		TaskActivityTracker tracker = new TaskActivityTracker();
		tracker.update("Banshees", 50, "Banshee", true, false, 1_000L, 450, Bracelet.NONE);
		tracker.update("Banshees", 49, "Banshee", true, false, 2_000L, 450, Bracelet.NONE);
		return tracker;
	}
}
