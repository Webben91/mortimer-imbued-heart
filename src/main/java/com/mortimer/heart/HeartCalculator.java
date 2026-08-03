package com.mortimer.heart;

import java.util.List;

final class HeartCalculator
{
	private HeartCalculator()
	{
	}

	static HeartResult calculate(OfferState offer, boolean eliteCombatAchievements)
	{
		double spawnRate = eliteCombatAchievements ? 150.0 : 200.0;
		double modifierMultiplier = 1.0 + Math.max(0.0, offer.getDropModifier()) / 100.0;
		double heartPerSuperior = offer.getSuperior().canDropHeart()
			? offer.getSuperior().getHeartRate() / modifierMultiplier : Double.POSITIVE_INFINITY;
		double heartPerKill = spawnRate * heartPerSuperior;
		double actualKills = Math.max(1.0, offer.getBracelet().adjustKills(offer.getAmount()));
		double killsPerHour = Math.max(1.0, offer.getKillsPerHour());
		double taskChance = atLeastOneChance(actualKills, heartPerKill);
		double chancePerHour = atLeastOneChance(killsPerHour, heartPerKill);
		double expectedSuperiors = actualKills / spawnRate;
		double taskHours = actualKills / killsPerHour;
		double hoursOnRate = Double.isFinite(heartPerKill)
			? heartPerKill / killsPerHour : Double.POSITIVE_INFINITY;
		return new HeartResult(offer, actualKills, heartPerSuperior, heartPerKill, taskChance,
			chancePerHour, expectedSuperiors, taskHours, hoursOnRate);
	}

	static double combinedChance(List<HeartResult> results)
	{
		double noHeartChance = 1.0;
		for (HeartResult result : results)
		{
			noHeartChance *= 1.0 - result.getTaskChance();
		}
		return 1.0 - noHeartChance;
	}

	private static double atLeastOneChance(double rolls, double denominator)
	{
		if (rolls <= 0 || denominator <= 1)
		{
			return 0.0;
		}
		return 1.0 - Math.pow(1.0 - 1.0 / denominator, rolls);
	}
}
