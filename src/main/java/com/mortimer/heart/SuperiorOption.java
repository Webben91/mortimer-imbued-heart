package com.mortimer.heart;

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

	private static String normalize(String value)
	{
		return value == null ? "" : value.toLowerCase().replaceAll("[^a-z0-9]+", "");
	}

	@Override
	public String toString()
	{
		return monsterName;
	}
}
