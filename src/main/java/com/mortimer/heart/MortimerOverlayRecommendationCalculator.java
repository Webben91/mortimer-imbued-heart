package com.mortimer.heart;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
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
		return calculate(detectedOffers, showMonsterVariants, preference, eliteCombatAchievements,
			slayerLevel, slayerPoints, Collections.emptySet(), false,
			(task, superior) -> superior.effectiveKillsPerHour(killsPerHour.applyAsDouble(task)),
			(task, amount) -> 0.0, task -> TaskPreference.STANDARD);
	}

	static MortimerOverlayRecommendation calculate(List<MortimerDetectedOffer> detectedOffers,
		boolean showMonsterVariants, GrindPreference preference, boolean eliteCombatAchievements,
		int slayerLevel, int slayerPoints, Set<String> blockedTasks, boolean slayerCape,
		ToDoubleFunction<HeartTask> killsPerHour)
	{
		return calculate(detectedOffers, showMonsterVariants, preference, eliteCombatAchievements,
			slayerLevel, slayerPoints, blockedTasks, slayerCape,
			(task, superior) -> superior.effectiveKillsPerHour(killsPerHour.applyAsDouble(task)),
			(task, amount) -> 0.0, task -> TaskPreference.STANDARD);
	}

	static MortimerOverlayRecommendation calculate(List<MortimerDetectedOffer> detectedOffers,
		boolean showMonsterVariants, GrindPreference preference, boolean eliteCombatAchievements,
		int slayerLevel, int slayerPoints, Set<String> blockedTasks, boolean slayerCape,
		TaskKillsPerHourProvider killsPerHour,
		ToDoubleBiFunction<HeartTask, Integer> overheadHours,
		Function<HeartTask, TaskPreference> taskPreference)
	{
		return calculate(detectedOffers, showMonsterVariants, preference, eliteCombatAchievements,
			slayerLevel, slayerPoints, blockedTasks, slayerCape, killsPerHour, overheadHours,
			taskPreference, (task, superior) -> false);
	}

	static MortimerOverlayRecommendation calculate(List<MortimerDetectedOffer> detectedOffers,
		boolean showMonsterVariants, GrindPreference preference, boolean eliteCombatAchievements,
		int slayerLevel, int slayerPoints, Set<String> blockedTasks, boolean slayerCape,
		TaskKillsPerHourProvider killsPerHour,
		ToDoubleBiFunction<HeartTask, Integer> overheadHours,
		Function<HeartTask, TaskPreference> taskPreference,
		BiPredicate<HeartTask, SuperiorOption> wildernessTask)
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
			List<SuperiorOption> variants = variantsFor(detected.getTask(), showMonsterVariants, wildernessTask);
			for (SuperiorOption superior : variants)
			{
				double effectiveKph = Math.max(1.0,
					killsPerHour.applyAsDouble(detected.getTask(), superior));
				offers.add(new OfferState(detected.getTask(), superior, detected.getAmount(),
					detected.getDropModifier(), detected.getXpModifier(),
					effectiveKph, overheadHours.applyAsDouble(detected.getTask(), detected.getAmount()),
					Bracelet.NONE, wildernessTask.test(detected.getTask(), superior)));
				sourceOfferIndexes.add(offerIndex);
			}
		}

		if (preference != GrindPreference.IMBUED_HEART)
		{
			int best = PreferenceRecommendationCalculator.bestIndex(
				offers, eliteCombatAchievements, preference, taskPreference);
			if (best < 0)
			{
				return null;
			}
			TaskPreference personal = taskPreference.apply(offers.get(best).getTask());
			if (personal != TaskPreference.STANDARD)
			{
				return new MortimerOverlayRecommendation(sourceOfferIndexes.get(best),
					preference == GrindPreference.SLAYER_XP
						? MortimerOverlayRecommendation.Style.SLAYER_XP : MortimerOverlayRecommendation.Style.BALANCED,
					personal == TaskPreference.ALWAYS ? "PERSONAL PICK · ALWAYS" : "PERSONAL PICK · PREFERRED");
			}
			return preference == GrindPreference.SLAYER_XP
				? new MortimerOverlayRecommendation(sourceOfferIndexes.get(best),
					MortimerOverlayRecommendation.Style.SLAYER_XP, "RECOMMENDED · BEST XP")
				: new MortimerOverlayRecommendation(sourceOfferIndexes.get(best),
					MortimerOverlayRecommendation.Style.BALANCED, "RECOMMENDED · BALANCED");
		}

		RoutingDecision decision = OptimalRoutingCalculator.calculate(offers, eliteCombatAchievements,
			slayerLevel, slayerPoints, detectedOffers.size(), blockedTasks, slayerCape, killsPerHour,
			overheadHours, taskPreference, wildernessTask);
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
			OfferState primary = offers.get(decision.getPrimaryIndex());
			String label = primary.getSuperior().canDropHeart()
				? "FAST REROLL · EXPEDITIOUS" : primary.getSuperior().getMonsterName().toUpperCase(Locale.ROOT) + " · EXPEDITIOUS";
			return new MortimerOverlayRecommendation(sourceOfferIndexes.get(decision.getPrimaryIndex()),
				MortimerOverlayRecommendation.Style.FAST_REROLL, label);
		}
		OfferState primary = offers.get(decision.getPrimaryIndex());
		TaskPreference personal = taskPreference.apply(primary.getTask());
		if (personal != TaskPreference.STANDARD)
		{
			return new MortimerOverlayRecommendation(sourceOfferIndexes.get(decision.getPrimaryIndex()),
				MortimerOverlayRecommendation.Style.HEART,
				personal == TaskPreference.ALWAYS ? "PERSONAL PICK · ALWAYS" : "PERSONAL PICK · PREFERRED");
		}
		String bracelet = decision.getBracelet() == Bracelet.NONE
			? "" : " · " + decision.getBracelet().toString().toUpperCase(Locale.ROOT);
		String heartLabel = primary.getTask().getSuperiors().size() > 1
			? primary.getSuperior().taskDisplayName(primary.getTask()).toUpperCase(Locale.ROOT) + bracelet
			: "BEST HEART" + bracelet;
		return new MortimerOverlayRecommendation(sourceOfferIndexes.get(decision.getPrimaryIndex()),
			MortimerOverlayRecommendation.Style.HEART, heartLabel);
	}

	private static List<SuperiorOption> variantsFor(HeartTask task, boolean showMonsterVariants,
		BiPredicate<HeartTask, SuperiorOption> wildernessTask)
	{
		if (showMonsterVariants)
		{
			return task.getSuperiors();
		}
		for (SuperiorOption superior : task.getSuperiors())
		{
			if (wildernessTask.test(task, superior))
			{
				return Collections.singletonList(superior);
			}
		}
		return task.getSuperiors().subList(0, 1);
	}
}
