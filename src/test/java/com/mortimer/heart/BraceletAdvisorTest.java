package com.mortimer.heart;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class BraceletAdvisorTest
{
	@Test
	public void strongHeartTasksRecommendSlaughter()
	{
		assertEquals(Bracelet.SLAUGHTER, BraceletAdvisor.recommend(offer("Araxytes", 550, 0), true));
		assertEquals(Bracelet.SLAUGHTER, BraceletAdvisor.recommend(offer("Smoke devils", 460, 0), true));
		assertEquals(Bracelet.SLAUGHTER, BraceletAdvisor.recommend(offer("Dust devils", 550, 150), true));
	}

	@Test
	public void weakHeartTaskRecommendsExpeditious()
	{
		assertEquals(Bracelet.EXPEDITIOUS, BraceletAdvisor.recommend(offer("Dust devils", 550, 0), true));
	}

	@Test
	public void wyrmlingStrategyIsFastButHasNoHeartChance()
	{
		HeartTask wyrms = HeartData.findTask("Wyrms");
		SuperiorOption wyrmling = wyrms.getSuperiors().get(2);
		OfferState offer = new OfferState(wyrms, wyrmling, 100, 300, 920, Bracelet.NONE);

		assertFalse(wyrmling.canDropHeart());
		assertEquals(0.0, HeartCalculator.calculate(offer, true).getTaskChance(), 0.0);
		assertEquals(Bracelet.EXPEDITIOUS, BraceletAdvisor.recommend(offer, true));
		assertEquals(HeartCalculator.calculate(offer, true).getTaskHours(),
			OptimalRoutingCalculator.outcome(offer, true).getHoursBeforeExit(), 1e-12);
	}

	private static OfferState offer(String taskName, double kph, double modifier)
	{
		HeartTask task = HeartData.findTask(taskName);
		return new OfferState(task, task.getSuperiors().get(0), task.getPlanningAmount(), modifier,
			kph, Bracelet.NONE);
	}
}
