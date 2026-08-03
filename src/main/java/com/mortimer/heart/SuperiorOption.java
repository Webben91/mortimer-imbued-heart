package com.mortimer.heart;

import java.util.Locale;

final class SuperiorOption
{
	private final String monsterName;
	private final String name;
	private final double heartRate;
	private final double strategyKillsPerHour;

	SuperiorOption(String monsterName, String name, double heartRate)
	{
		this(monsterName, name, heartRate, 0.0);
	}

	SuperiorOption(String monsterName, String name, double heartRate, double strategyKillsPerHour)
	{
		this.monsterName = monsterName;
		this.name = name;
		this.heartRate = heartRate;
		this.strategyKillsPerHour = strategyKillsPerHour;
	}

	String getMonsterName()
	{
		return monsterName;
	}

	String getName()
	{
		return name;
	}

	double getHeartRate()
	{
		return heartRate;
	}

	double effectiveKillsPerHour(double taskKillsPerHour)
	{
		return strategyKillsPerHour > 0.0 ? strategyKillsPerHour : taskKillsPerHour;
	}

	boolean canDropHeart()
	{
		return heartRate > 0.0 && Double.isFinite(heartRate);
	}

	boolean matchesMonster(String candidate)
	{
		return normalize(monsterName).equals(normalize(candidate));
	}

	String taskDisplayName(HeartTask task)
	{
		if (task == null || task.getSuperiors().size() <= 1 || task.getSuperiors().get(0) == this)
		{
			return task == null ? monsterName : task.getName();
		}
		return pluralize(monsterName);
	}

	private static String normalize(String value)
	{
		return value == null ? "" : value.toLowerCase().replaceAll("[^a-z0-9]+", "");
	}

	private static String pluralize(String value)
	{
		String lower = value.toLowerCase(Locale.ROOT);
		if (lower.endsWith("y") && value.length() > 1)
		{
			char before = Character.toLowerCase(value.charAt(value.length() - 2));
			if ("aeiou".indexOf(before) < 0)
			{
				return value.substring(0, value.length() - 1) + "ies";
			}
		}
		return lower.endsWith("s") ? value : value + "s";
	}

	@Override
	public String toString()
	{
		return monsterName;
	}
}
