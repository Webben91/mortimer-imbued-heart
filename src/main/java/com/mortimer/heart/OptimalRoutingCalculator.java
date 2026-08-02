package com.mortimer.heart;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.ToDoubleFunction;

final class OptimalRoutingCalculator
{
	static final double TARGET_HEART_HOURS = 120.0;
	static final double POINT_SKIP_MIN_FASTEST_TASK_HOURS = 1.0;
	static final double POINT_SKIP_HOURS = 30.0 / 3600.0;
	private static final int SAMPLE_COUNT = 6000;
	private static final long RANDOM_SEED = 0x4d4f5254494d4552L;

	private OptimalRoutingCalculator()
	{
	}

	static RoutingDecision calculate(List<OfferState> currentOffers, boolean eliteCombatAchievements,
		int slayerLevel, int slayerPoints, int futureChoiceCount, ToDoubleFunction<HeartTask> killsPerHour)
	{
		if (currentOffers.isEmpty())
		{
			return null;
		}

		List<HeartResult> baseResults = new ArrayList<>();
		for (OfferState offer : currentOffers)
		{
			baseResults.add(HeartCalculator.calculate(withBracelet(offer, Bracelet.NONE), eliteCombatAchievements));
		}
		int bestHeartIndex = indexOfMinimum(baseResults, HeartResult::getHoursOnRate);
		int fastestFallbackIndex = indexOfMinimum(baseResults, result ->
			HeartCalculator.calculate(withBracelet(result.getOffer(), Bracelet.EXPEDITIOUS),
				eliteCombatAchievements).getTaskHours());
		double currentBestOnRate = baseResults.get(bestHeartIndex).getHoursOnRate();
		boolean allOutsideTarget = baseResults.stream()
			.allMatch(result -> result.getHoursOnRate() > TARGET_HEART_HOURS);
		double fastestExpeditiousHours = HeartCalculator.calculate(
			withBracelet(currentOffers.get(fastestFallbackIndex), Bracelet.EXPEDITIOUS),
			eliteCombatAchievements).getTaskHours();

		List<FutureSet> samples = sampleFutureSets(eliteCombatAchievements, slayerLevel,
			Math.max(2, Math.min(3, futureChoiceCount)), killsPerHour);
		double noPointFuture = solveFutureValue(samples, Double.POSITIVE_INFINITY);
		boolean pointSkipAvailable = slayerPoints >= 100;
		double pointSkipCost = POINT_SKIP_HOURS + noPointFuture;
		double retainedPointFuture = pointSkipAvailable ? solveFutureValue(samples, pointSkipCost) : noPointFuture;

		int primaryIndex = 0;
		Bracelet primaryBracelet = Bracelet.NONE;
		double bestTaskCost = Double.POSITIVE_INFINITY;
		for (int index = 0; index < currentOffers.size(); index++)
		{
			for (Bracelet bracelet : Bracelet.values())
			{
				TaskOutcome outcome = outcome(withBracelet(currentOffers.get(index), bracelet), eliteCombatAchievements);
				double cost = outcome.hoursBeforeExit + outcome.failureChance * retainedPointFuture;
				if (cost < bestTaskCost)
				{
					bestTaskCost = cost;
					primaryIndex = index;
					primaryBracelet = bracelet;
				}
			}
		}

		double probabilityBetter = samples.stream()
			.filter(sample -> sample.bestOnRateHours < currentBestOnRate).count() / (double) samples.size();
		RoutingDecision.Type type = RoutingDecision.Type.HUNT;
		double expectedHours = bestTaskCost;
		if (allOutsideTarget && pointSkipAvailable
			&& fastestExpeditiousHours >= POINT_SKIP_MIN_FASTEST_TASK_HOURS
			&& pointSkipCost < bestTaskCost)
		{
			type = RoutingDecision.Type.POINT_SKIP;
			expectedHours = pointSkipCost;
		}
		else if (allOutsideTarget && primaryBracelet == Bracelet.EXPEDITIOUS)
		{
			type = RoutingDecision.Type.FAST_REROLL;
		}
		else
		{
			primaryIndex = bestHeartIndex;
			primaryBracelet = Bracelet.NONE;
		}

		return new RoutingDecision(type, primaryIndex, bestHeartIndex, fastestFallbackIndex,
			primaryBracelet, expectedHours, noPointFuture, probabilityBetter);
	}

	private static List<FutureSet> sampleFutureSets(boolean eliteCombatAchievements, int slayerLevel,
		int choiceCount, ToDoubleFunction<HeartTask> killsPerHour)
	{
		List<MortimerRoutingData.Profile> eligible = MortimerRoutingData.eligibleProfiles(slayerLevel);
		if (eligible.size() < choiceCount)
		{
			return new ArrayList<>();
		}
		Random random = new Random(RANDOM_SEED + slayerLevel * 31L + choiceCount);
		List<FutureSet> samples = new ArrayList<>(SAMPLE_COUNT);
		for (int sampleIndex = 0; sampleIndex < SAMPLE_COUNT; sampleIndex++)
		{
			List<MortimerRoutingData.Profile> pool = new ArrayList<>(eligible);
			List<TaskOutcome> outcomes = new ArrayList<>(choiceCount * Bracelet.values().length);
			double bestOnRate = Double.POSITIVE_INFINITY;
			for (int choice = 0; choice < choiceCount; choice++)
			{
				MortimerRoutingData.Profile profile = removeWeighted(random, pool);
				HeartTask task = profile.getTask();
				int amount = between(random, task.getAmountMin(), task.getAmountMax(), 1);
				double modifier = 0.0;
				int modifierType = random.nextInt(5);
				if (modifierType == 1)
				{
					amount = Math.max(1, amount + between(random, profile.getQuantityMin(), profile.getQuantityMax(), 5));
				}
				else if (modifierType == 3)
				{
					modifier = between(random, profile.getSuperiorMin(), profile.getSuperiorMax(), 5);
				}
				SuperiorOption superior = task.getSuperiors().stream()
					.min(Comparator.comparingDouble(SuperiorOption::getHeartRate)).orElse(task.getSuperiors().get(0));
				double kph = Math.max(1.0, killsPerHour.applyAsDouble(task));
				OfferState base = new OfferState(task, superior, amount, modifier, kph, Bracelet.NONE);
				bestOnRate = Math.min(bestOnRate,
					HeartCalculator.calculate(base, eliteCombatAchievements).getHoursOnRate());
				for (Bracelet bracelet : Bracelet.values())
				{
					outcomes.add(outcome(withBracelet(base, bracelet), eliteCombatAchievements));
				}
			}
			samples.add(new FutureSet(outcomes, bestOnRate));
		}
		return samples;
	}

	private static double solveFutureValue(List<FutureSet> samples, double skipCost)
	{
		if (samples.isEmpty())
		{
			return 0.0;
		}
		double low = 0.0;
		double high = 1000.0;
		while (futureValueAt(samples, high, skipCost) > high && high < 1_000_000.0)
		{
			high *= 2.0;
		}
		for (int iteration = 0; iteration < 70; iteration++)
		{
			double middle = (low + high) / 2.0;
			if (futureValueAt(samples, middle, skipCost) > middle)
			{
				low = middle;
			}
			else
			{
				high = middle;
			}
		}
		return (low + high) / 2.0;
	}

	private static double futureValueAt(List<FutureSet> samples, double continuationHours, double skipCost)
	{
		double total = 0.0;
		for (FutureSet sample : samples)
		{
			double best = skipCost;
			for (TaskOutcome candidate : sample.outcomes)
			{
				best = Math.min(best, candidate.hoursBeforeExit + candidate.failureChance * continuationHours);
			}
			total += best;
		}
		return total / samples.size();
	}

	static TaskOutcome outcome(OfferState offer, boolean eliteCombatAchievements)
	{
		HeartResult result = HeartCalculator.calculate(offer, eliteCombatAchievements);
		double failureChance = 1.0 - result.getTaskChance();
		double successPerKill = 1.0 / result.getHeartPerKill();
		double expectedKillsBeforeExit = (1.0 - failureChance) / successPerKill;
		return new TaskOutcome(expectedKillsBeforeExit / Math.max(1.0, offer.getKillsPerHour()), failureChance);
	}

	private static OfferState withBracelet(OfferState offer, Bracelet bracelet)
	{
		return new OfferState(offer.getTask(), offer.getSuperior(), offer.getAmount(), offer.getDropModifier(),
			offer.getKillsPerHour(), bracelet);
	}

	private static MortimerRoutingData.Profile removeWeighted(Random random, List<MortimerRoutingData.Profile> pool)
	{
		int totalWeight = pool.stream().mapToInt(MortimerRoutingData.Profile::getWeight).sum();
		int roll = random.nextInt(totalWeight);
		for (int index = 0; index < pool.size(); index++)
		{
			roll -= pool.get(index).getWeight();
			if (roll < 0)
			{
				return pool.remove(index);
			}
		}
		return pool.remove(pool.size() - 1);
	}

	private static int between(Random random, int minimum, int maximum, int step)
	{
		int steps = Math.max(0, (maximum - minimum) / step);
		return minimum + random.nextInt(steps + 1) * step;
	}

	private static int indexOfMinimum(List<HeartResult> values, ToDoubleFunction<HeartResult> metric)
	{
		int best = 0;
		double bestValue = Double.POSITIVE_INFINITY;
		for (int index = 0; index < values.size(); index++)
		{
			double value = metric.applyAsDouble(values.get(index));
			if (value < bestValue)
			{
				best = index;
				bestValue = value;
			}
		}
		return best;
	}

	static final class TaskOutcome
	{
		private final double hoursBeforeExit;
		private final double failureChance;

		private TaskOutcome(double hoursBeforeExit, double failureChance)
		{
			this.hoursBeforeExit = hoursBeforeExit;
			this.failureChance = failureChance;
		}

		double getHoursBeforeExit() { return hoursBeforeExit; }
		double getFailureChance() { return failureChance; }
	}

	private static final class FutureSet
	{
		private final List<TaskOutcome> outcomes;
		private final double bestOnRateHours;

		private FutureSet(List<TaskOutcome> outcomes, double bestOnRateHours)
		{
			this.outcomes = outcomes;
			this.bestOnRateHours = bestOnRateHours;
		}
	}
}
