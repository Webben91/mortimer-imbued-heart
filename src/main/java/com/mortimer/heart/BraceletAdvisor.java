package com.mortimer.heart;

final class BraceletAdvisor
{
	private BraceletAdvisor()
	{
	}

	static Bracelet recommend(OfferState offer, boolean eliteCombatAchievements)
	{
		HeartResult result = HeartCalculator.calculate(withBracelet(offer, Bracelet.NONE), eliteCombatAchievements);
		return result.getHoursOnRate() <= OptimalRoutingCalculator.TARGET_HEART_HOURS
			? Bracelet.SLAUGHTER : Bracelet.EXPEDITIOUS;
	}

	private static OfferState withBracelet(OfferState offer, Bracelet bracelet)
	{
		return new OfferState(offer.getTask(), offer.getSuperior(), offer.getAmount(), offer.getDropModifier(),
			offer.getXpModifier(), offer.getKillsPerHour(), offer.getOverheadHours(), bracelet,
			offer.isWilderness());
	}
}
