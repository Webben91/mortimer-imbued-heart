package com.mortimer.heart;

final class GrindRecord
{
	private final String taskName;
	private final String superiorName;
	private final int kills;
	private final double baseHeartRate;
	private final double dropModifier;
	private final double superiorSpawnRate;
	private final int superiorRolls;

	GrindRecord(String taskName, String superiorName, int kills, double baseHeartRate,
		double dropModifier, double superiorSpawnRate)
	{
		this(taskName, superiorName, kills, baseHeartRate, dropModifier, superiorSpawnRate, -1);
	}

	GrindRecord(String taskName, String superiorName, int kills, double baseHeartRate,
		double dropModifier, double superiorSpawnRate, int superiorRolls)
	{
		this.taskName = taskName;
		this.superiorName = superiorName;
		this.kills = kills;
		this.baseHeartRate = baseHeartRate;
		this.dropModifier = dropModifier;
		this.superiorSpawnRate = superiorSpawnRate;
		this.superiorRolls = superiorRolls;
	}

	String getTaskName() { return taskName; }
	String getSuperiorName() { return superiorName; }
	int getKills() { return kills; }
	double getBaseHeartRate() { return baseHeartRate; }
	double getDropModifier() { return dropModifier; }
	double getSuperiorSpawnRate() { return superiorSpawnRate; }
	int getSuperiorRolls() { return superiorRolls; }
	boolean usesActualSuperiorRolls() { return superiorRolls >= 0; }

	double getHeartPerSuperior()
	{
		return baseHeartRate / (1.0 + Math.max(0.0, dropModifier) / 100.0);
	}

	double getHeartPerKill()
	{
		return superiorSpawnRate * getHeartPerSuperior();
	}
}
