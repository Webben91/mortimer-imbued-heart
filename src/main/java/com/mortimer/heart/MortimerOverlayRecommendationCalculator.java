package com.mortimer.heart;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;

final class MortimerOverlayRecommendationCalculator
{
	private MortimerOverlayRecommendationCalculator()
	{
	}

	static MortimerOverlayRecommendation calculate(List<MortimerDetectedOffer> detectedOffers,
		boolean showMonsterVariants, GrindPreference preference, boolean eliteCombatAchievements,
		int slayerLevel, int slayerPoints, ToDoubleFunction<HeartTask> killsPerHour)
	{
		if (detectedOffers.isEmpty())
		{
			return null;
		}

		List<OfferState> offers = new ArrayList<>();
		List<Integer> sourceOfferIndexes = new ArrayList<>();
		for (int offerIndex = 0; offerIndex < detectedOffers.size(); offerIndex++)
		{
			MortimerDetectedOffer detected = detectedOffers.get(offerIndex);
			List<SuperiorOption> variants = showMonsterVariants
				? detected.getTask().getSuperiors()
				: detected.getTask().getSuperiors().subList(0, 1);
			for (SuperiorOption superior : variants)
			{
				offers.add(new OfferState(detected.getTask(), superior, detected.getAmount(),
					detected.getDropModifier(), detected.getXpModifier(),
					Math.max(1.0, killsPerHour.applyAsDouble(detected.getTask())), Bracelet.NONE));
				sourceOfferIndexes.add(offerIndex);
			}
		}

		if (preference != GrindPreference.IMBUED_HEART)
		{
			int best = PreferenceRecommendationCalculator.bestIndex(
				offers, eliteCombatAchievements, preference);
			if (best < 0)
			{
				return null;
			}
			return preference == GrindPreference.SLAYER_XP
				? new MortimerOverlayRecommendation(sourceOfferIndexes.get(best),
					MortimerOverlayRecommendation.Style.SLAYER_XP, "RECOMMENDED · BEST XP")
				: new MortimerOverlayRecommendation(sourceOfferIndexes.get(best),
					MortimerOverlayRecommendation.Style.BALANCED, "RECOMMENDED · BALANCED");
		}

		RoutingDecision decision = OptimalRoutingCalculator.calculate(offers, eliteCombatAchievements,
			slayerLevel, slayerPoints, detectedOffers.size(), killsPerHour);
		if (decision == null)
		{
			return null;
		}
		if (decision.getType() == RoutingDecision.Type.POINT_SKIP)
		{
			return new MortimerOverlayRecommendation(
				sourceOfferIndexes.get(decision.getFastestFallbackIndex()),
				MortimerOverlayRecommendation.Style.POINT_SKIP, "SKIP · FASTEST REROLL");
		}
		if (decision.getType() == RoutingDecision.Type.FAST_REROLL)
		{
			return new MortimerOverlayRecommendation(sourceOfferIndexes.get(decision.getPrimaryIndex()),
				MortimerOverlayRecommendation.Style.FAST_REROLL, "RECOMMENDED · FAST REROLL");
		}
		return new MortimerOverlayRecommendation(sourceOfferIndexes.get(decision.getPrimaryIndex()),
			MortimerOverlayRecommendation.Style.HEART, "RECOMMENDED · BEST HEART");
	}
}
