package com.mortimer.heart;

import java.util.List;

final class PreferenceRecommendationCalculator
{
	private PreferenceRecommendationCalculator()
	{
	}

	static int bestIndex(List<OfferState> offers, boolean eliteCombatAchievements, GrindPreference preference)
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
		for (int index = 0; index < offers.size(); index++)
		{
			OfferState offer = offers.get(index);
			double xpScore = SlayerExperienceData.experiencePerHour(offer) / Math.max(1.0, bestXp);
			double heartScore = HeartCalculator.calculate(offer, eliteCombatAchievements).getChancePerHour()
				/ Math.max(1e-12, bestHeart);
			double score = preference == GrindPreference.SLAYER_XP
				? xpScore : (xpScore + heartScore) / 2.0;
			if (score > bestScore)
			{
				bestScore = score;
				bestIndex = index;
			}
		}
		return bestIndex;
	}
}
