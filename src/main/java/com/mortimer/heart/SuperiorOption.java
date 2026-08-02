package com.mortimer.heart;

final class SuperiorOption
{
	private final String monsterName;
	private final String name;
	private final double heartRate;

	SuperiorOption(String monsterName, String name, double heartRate)
	{
		this.monsterName = monsterName;
		this.name = name;
		this.heartRate = heartRate;
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
