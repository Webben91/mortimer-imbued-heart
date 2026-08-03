package com.mortimer.heart;

import java.util.List;
import java.util.function.Function;

final class PreferenceRecommendationCalculator
{
	private PreferenceRecommendationCalculator()
	{
	}

	static int bestIndex(List<OfferState> offers, boolean eliteCombatAchievements, GrindPreference preference)
	{
		return bestIndex(offers, eliteCombatAchievements, preference, task -> TaskPreference.STANDARD);
	}

	static int bestIndex(List<OfferState> offers, boolean eliteCombatAchievements, GrindPreference preference,
		Function<HeartTask, TaskPreference> taskPreference)
	{
		if (offers.isEmpty())
		{
			return -1;
		}
		double bestXp = 0.0;
		double bestHeart = 0.0;
		for (OfferState offer : offers)
		{
			bestXp = Math.max(bestXp, SlayerExperienceData.experiencePerHour(offer));
			bestHeart = Math.max(bestHeart,
				HeartCalculator.calculate(offer, eliteCombatAchievements).getChancePerHour());
		}

		int bestIndex = 0;
		double bestScore = -1.0;
		double[] scores = new double[offers.size()];
		for (int index = 0; index < offers.size(); index++)
		{
			OfferState offer = offers.get(index);
			double xpScore = SlayerExperienceData.experiencePerHour(offer) / Math.max(1.0, bestXp);
			double heartScore = HeartCalculator.calculate(offer, eliteCombatAchievements).getChancePerHour()
				/ Math.max(1e-12, bestHeart);
			double score = preference == GrindPreference.SLAYER_XP
				? xpScore : (xpScore + heartScore) / 2.0;
			scores[index] = score;
			if (score > bestScore)
			{
				bestScore = score;
				bestIndex = index;
			}
		}
		int alwaysIndex = bestMatchingPreference(offers, scores, taskPreference, TaskPreference.ALWAYS);
		if (alwaysIndex >= 0)
		{
			return alwaysIndex;
		}
		int preferredIndex = bestMatchingPreference(offers, scores, taskPreference, TaskPreference.PREFER);
		if (preferredIndex >= 0 && scores[preferredIndex] >= bestScore * 0.90)
		{
			return preferredIndex;
		}
		return bestIndex;
	}

	private static int bestMatchingPreference(List<OfferState> offers, double[] scores,
		Function<HeartTask, TaskPreference> taskPreference, TaskPreference wanted)
	{
		int best = -1;
		for (int index = 0; index < offers.size(); index++)
		{
			if (taskPreference.apply(offers.get(index).getTask()) == wanted
				&& (best < 0 || scores[index] > scores[best]))
			{
				best = index;
			}
		}
		return best;
	}
}
