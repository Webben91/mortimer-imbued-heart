package com.mortimer.heart;

import java.util.List;

final class GrindSummary
{
	private final int tasks;
	private final int kills;
	private final double expectedHearts;
	private final double heartChance;

	private GrindSummary(int tasks, int kills, double expectedHearts, double heartChance)
	{
		this.tasks = tasks;
		this.kills = kills;
		this.expectedHearts = expectedHearts;
		this.heartChance = heartChance;
	}

	static GrindSummary from(List<GrindRecord> records)
	{
		int totalKills = 0;
		double expected = 0.0;
		double noHeart = 1.0;
		for (GrindRecord record : records)
		{
			totalKills += record.getKills();
			if (record.usesActualSuperiorRolls())
			{
				double denominator = record.getHeartPerSuperior();
				expected += record.getSuperiorRolls() / denominator;
				noHeart *= Math.pow(1.0 - 1.0 / denominator, record.getSuperiorRolls());
			}
			else
			{
				double denominator = record.getHeartPerKill();
				expected += record.getKills() / denominator;
				noHeart *= Math.pow(1.0 - 1.0 / denominator, record.getKills());
			}
		}
		return new GrindSummary(records.size(), totalKills, expected, 1.0 - noHeart);
	}

	int getTasks() { return tasks; }
	int getKills() { return kills; }
	double getExpectedHearts() { return expectedHearts; }
	double getHeartChance() { return heartChance; }
}
