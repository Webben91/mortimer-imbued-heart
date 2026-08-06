package com.mortimer.heart;

final class ActiveMortimerTask
{
	private final String taskName;
	private final String superiorName;
	private final int assignedAmount;
	private final double baseHeartRate;
	private final double dropModifier;
	private final double superiorSpawnRate;
	private final int superiorRolls;

	ActiveMortimerTask(String taskName, String superiorName, int assignedAmount, double baseHeartRate,
		double dropModifier, double superiorSpawnRate)
	{
		this(taskName, superiorName, assignedAmount, baseHeartRate, dropModifier, superiorSpawnRate, 0);
	}

	ActiveMortimerTask(String taskName, String superiorName, int assignedAmount, double baseHeartRate,
		double dropModifier, double superiorSpawnRate, int superiorRolls)
	{
		this.taskName = taskName;
		this.superiorName = superiorName;
		this.assignedAmount = assignedAmount;
		this.baseHeartRate = baseHeartRate;
		this.dropModifier = dropModifier;
		this.superiorSpawnRate = superiorSpawnRate;
		this.superiorRolls = Math.max(0, superiorRolls);
	}

	String getTaskName() { return taskName; }
	String getSuperiorName() { return superiorName; }
	int getAssignedAmount() { return assignedAmount; }
	double getBaseHeartRate() { return baseHeartRate; }
	double getDropModifier() { return dropModifier; }
	double getSuperiorSpawnRate() { return superiorSpawnRate; }
	int getSuperiorRolls() { return superiorRolls; }

	ActiveMortimerTask withSuperiorRoll()
	{
		return new ActiveMortimerTask(taskName, superiorName, assignedAmount, baseHeartRate,
			dropModifier, superiorSpawnRate, superiorRolls + 1);
	}

	ActiveMortimerTask withSuperior(SuperiorOption superior)
	{
		return new ActiveMortimerTask(taskName, superior.getName(), assignedAmount, superior.getHeartRate(),
			dropModifier, superiorSpawnRate, superiorRolls);
	}

	ActiveMortimerTask withSuperior(SuperiorOption superior, double newSuperiorSpawnRate)
	{
		return new ActiveMortimerTask(taskName, superior.getName(), assignedAmount, superior.getHeartRate(),
			dropModifier, newSuperiorSpawnRate, superiorRolls);
	}

	ActiveMortimerTask withSuperiorSpawnRate(double newSuperiorSpawnRate)
	{
		return new ActiveMortimerTask(taskName, superiorName, assignedAmount, baseHeartRate,
			dropModifier, newSuperiorSpawnRate, superiorRolls);
	}

	ActiveMortimerTask withAssignedAmount(int amount)
	{
		return new ActiveMortimerTask(taskName, superiorName, Math.max(1, amount), baseHeartRate,
			dropModifier, superiorSpawnRate, superiorRolls);
	}

	ActiveMortimerTask withDropModifier(double modifier)
	{
		return new ActiveMortimerTask(taskName, superiorName, assignedAmount, baseHeartRate,
			Math.max(0.0, modifier), superiorSpawnRate, superiorRolls);
	}

	GrindRecord toRecord()
	{
		return new GrindRecord(taskName, superiorName, assignedAmount, baseHeartRate, dropModifier,
			superiorSpawnRate, superiorRolls);
	}
}
