package com.mortimer.heart;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LocalGrindTest
{
	@Test
	public void localRecordsRoundTripWithoutLosingCalculatorInputs()
	{
		List<GrindRecord> original = Arrays.asList(
			new GrindRecord("Dust devils", "Choke devil", 153, 680.0, 105.0, 150.0));
		List<GrindRecord> decoded = LocalGrindCodec.decode(LocalGrindCodec.encode(original));
		assertEquals(1, decoded.size());
		assertEquals("Dust devils", decoded.get(0).getTaskName());
		assertEquals(153, decoded.get(0).getKills());
		assertEquals(105.0, decoded.get(0).getDropModifier(), 0.0001);
	}

	@Test
	public void overallChanceAccumulatesAcrossRecordedTasks()
	{
		GrindRecord first = new GrindRecord("Dust devils", "Choke devil", 150, 680.0, 100.0, 150.0);
		GrindSummary one = GrindSummary.from(Arrays.asList(first));
		GrindSummary two = GrindSummary.from(Arrays.asList(first, first));
		assertEquals(1, one.getTasks());
		assertEquals(300, two.getKills());
		assertTrue(two.getHeartChance() > one.getHeartChance());
		assertEquals(one.getExpectedHearts() * 2.0, two.getExpectedHearts(), 0.000001);
	}

	@Test
	public void activeMortimerTaskSurvivesAClientRestart()
	{
		ActiveMortimerTask original = new ActiveMortimerTask("Araxytes", "Dreadborn Araxyte", 164,
			224.0, 205.0, 150.0, 3);
		ActiveMortimerTask decoded = ActiveMortimerTaskCodec.decode(ActiveMortimerTaskCodec.encode(original));
		assertEquals("Araxytes", decoded.getTaskName());
		assertEquals(164, decoded.getAssignedAmount());
		assertEquals(205.0, decoded.getDropModifier(), 0.0001);
		assertEquals(3, decoded.getSuperiorRolls());
	}

	@Test
	public void completedAutomaticTasksUseActualSuperiorRolls()
	{
		GrindRecord noSuperiors = new GrindRecord("Dust devils", "Choke devil", 150,
			680.0, 100.0, 150.0, 0);
		GrindRecord twoSuperiors = new GrindRecord("Dust devils", "Choke devil", 150,
			680.0, 100.0, 150.0, 2);
		assertEquals(0.0, GrindSummary.from(Arrays.asList(noSuperiors)).getHeartChance(), 0.0);
		assertTrue(GrindSummary.from(Arrays.asList(twoSuperiors)).getHeartChance() > 0.0);
	}

	@Test
	public void basiliskAssignmentKeepsBothHeartVariants()
	{
		HeartTask basilisks = HeartData.findTask("Basilisk");
		assertEquals(2, basilisks.getSuperiors().size());
		assertEquals("Monstrous basilisk", basilisks.getSuperiors().get(0).getName());
		assertEquals(1024.0, basilisks.getSuperiors().get(0).getHeartRate(), 0.0);
		assertTrue(basilisks.getSuperiors().get(1).matchesMonster("Basilisk Knight"));
		assertEquals(760.0, basilisks.getSuperiors().get(1).getHeartRate(), 0.0);
	}
}
