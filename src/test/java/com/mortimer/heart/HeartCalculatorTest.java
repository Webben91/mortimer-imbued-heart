package com.mortimer.heart;

import java.util.Arrays;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HeartCalculatorTest
{
	@Test
	public void oneHundredPercentModifierDoublesHeartChanceMultiplier()
	{
		HeartTask smokeDevils = HeartData.findTask("Smoke devils");
		OfferState base = new OfferState(smokeDevils, smokeDevils.getSuperiors().get(0), 100, 0, 460, Bracelet.NONE);
		OfferState doubled = new OfferState(smokeDevils, smokeDevils.getSuperiors().get(0), 100, 100, 460, Bracelet.NONE);
		HeartResult baseResult = HeartCalculator.calculate(base, true);
		HeartResult doubledResult = HeartCalculator.calculate(doubled, true);
		assertEquals(baseResult.getHeartPerKill() / 2.0, doubledResult.getHeartPerKill(), 0.00001);
	}

	@Test
	public void eliteCombatAchievementsUseOneInOneHundredFiftySuperiors()
	{
		HeartTask hydras = HeartData.findTask("Hydras");
		OfferState offer = new OfferState(hydras, hydras.getSuperiors().get(0), 175, 0, 120, Bracelet.NONE);
		HeartResult elite = HeartCalculator.calculate(offer, true);
		HeartResult normal = HeartCalculator.calculate(offer, false);
		assertEquals(150.0 * 160.0, elite.getHeartPerKill(), 0.00001);
		assertEquals(200.0 * 160.0, normal.getHeartPerKill(), 0.00001);
	}

	@Test
	public void combinedChanceIsGreaterThanEitherOfferAlone()
	{
		HeartTask smoke = HeartData.findTask("Smoke devils");
		HeartTask araxytes = HeartData.findTask("Araxytes");
		HeartResult first = HeartCalculator.calculate(new OfferState(smoke, smoke.getSuperiors().get(0), 100, 100, 460, Bracelet.NONE), true);
		HeartResult second = HeartCalculator.calculate(new OfferState(araxytes, araxytes.getSuperiors().get(0), 150, 100, 550, Bracelet.NONE), true);
		double combined = HeartCalculator.combinedChance(Arrays.asList(first, second));
		assertTrue(combined > first.getTaskChance());
		assertTrue(combined > second.getTaskChance());
	}

	@Test
	public void assignmentOverheadReducesHourlyEfficiencyWithoutChangingTaskChance()
	{
		HeartTask smoke = HeartData.findTask("Smoke devils");
		OfferState noTravel = new OfferState(smoke, smoke.getSuperiors().get(0), 100, 100,
			0, 460, 0, Bracelet.NONE);
		OfferState withTravel = new OfferState(smoke, smoke.getSuperiors().get(0), 100, 100,
			0, 460, 10.0 / 60.0, Bracelet.NONE);

		HeartResult base = HeartCalculator.calculate(noTravel, true);
		HeartResult delayed = HeartCalculator.calculate(withTravel, true);

		assertEquals(base.getTaskChance(), delayed.getTaskChance(), 0.0000001);
		assertTrue(delayed.getTaskHours() > base.getTaskHours());
		assertTrue(delayed.getChancePerHour() < base.getChancePerHour());
		assertTrue(delayed.getHoursOnRate() > base.getHoursOnRate());
		assertTrue(SlayerExperienceData.experiencePerHour(withTravel)
			< SlayerExperienceData.experiencePerHour(noTravel));
	}
}
