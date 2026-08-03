package com.mortimer.heart;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class TaskActivityTracker
{
	private static final long MINIMUM_GRACE_MILLIS = 10_000L;
	private static final long MAXIMUM_GRACE_MILLIS = 45_000L;
	private static final long MAXIMUM_TICK_MILLIS = 2_000L;

	private final Map<String, MutableSample> samples = new LinkedHashMap<>();
	private String taskName = "";
	private String currentMonsterName = "";
	private int lastRemaining = -1;
	private long lastTickAt;
	private long lastRelevantActivityAt;
	private boolean progressStarted;

	void update(String newTaskName, int remaining, String monsterName, boolean relevantCombat,
		boolean unrelatedCombat, long now, double expectedKillsPerHour, Bracelet bracelet)
	{
		if (newTaskName == null || newTaskName.isEmpty() || remaining < 0)
		{
			return;
		}
		if (!newTaskName.equals(taskName))
		{
			reset();
			taskName = newTaskName;
		}
		if (monsterName != null && !monsterName.isEmpty())
		{
			currentMonsterName = monsterName;
		}
		if (lastTickAt <= 0L)
		{
			lastTickAt = now;
			lastRemaining = remaining;
			return;
		}

		long tickMillis = Math.max(0L, Math.min(MAXIMUM_TICK_MILLIS, now - lastTickAt));
		long graceMillis = inactivityGraceMillis(expectedKillsPerHour);
		boolean recentlyActive = lastRelevantActivityAt > 0L
			&& now - lastRelevantActivityAt <= graceMillis;
		if (progressStarted && !unrelatedCombat && (relevantCombat || recentlyActive)
			&& !currentMonsterName.isEmpty())
		{
			sample(currentMonsterName).activeMillis += tickMillis;
		}

		int progress = lastRemaining < 0 ? 0 : Math.max(0, lastRemaining - remaining);
		if (unrelatedCombat)
		{
			lastRelevantActivityAt = 0L;
		}
		else if (relevantCombat || progress > 0)
		{
			lastRelevantActivityAt = now;
		}
		if (progress > 0)
		{
			if (progressStarted && !currentMonsterName.isEmpty())
			{
				Bracelet activeBracelet = bracelet == null ? Bracelet.NONE : bracelet;
				sample(currentMonsterName).kills += activeBracelet.adjustKills(progress);
			}
			else
			{
				// The first decrement arms the tracker, excluding initial banking and travel.
				progressStarted = true;
			}
		}
		lastRemaining = remaining;
		lastTickAt = now;
	}

	TaskActivitySample currentSample()
	{
		MutableSample sample = samples.get(currentMonsterName);
		return sample == null ? new TaskActivitySample(currentMonsterName, 0, 0L)
			: sample.snapshot(currentMonsterName);
	}

	List<TaskActivitySample> samples()
	{
		List<TaskActivitySample> result = new ArrayList<>();
		for (Map.Entry<String, MutableSample> entry : samples.entrySet())
		{
			TaskActivitySample sample = entry.getValue().snapshot(entry.getKey());
			if (sample.getKills() > 0 && sample.getActiveMillis() > 0L)
			{
				result.add(sample);
			}
		}
		return result;
	}

	void reset()
	{
		samples.clear();
		taskName = "";
		currentMonsterName = "";
		lastRemaining = -1;
		lastTickAt = 0L;
		lastRelevantActivityAt = 0L;
		progressStarted = false;
	}

	static long inactivityGraceMillis(double expectedKillsPerHour)
	{
		double secondsPerKill = 3600.0 / Math.max(1.0, expectedKillsPerHour);
		long adaptive = Math.round((secondsPerKill * 1.5 + 5.0) * 1000.0);
		return Math.max(MINIMUM_GRACE_MILLIS, Math.min(MAXIMUM_GRACE_MILLIS, adaptive));
	}

	private MutableSample sample(String monsterName)
	{
		return samples.computeIfAbsent(monsterName, ignored -> new MutableSample());
	}

	private static final class MutableSample
	{
		private double kills;
		private long activeMillis;

		private TaskActivitySample snapshot(String monsterName)
		{
			return new TaskActivitySample(monsterName, (int) Math.round(kills), activeMillis);
		}
	}
}
