package com.mortimer.heart;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SuperiorOptionTest
{
	@Test
	public void duplicateAssignmentRoutesUseDistinctNames()
	{
		HeartTask banshees = HeartData.findTask("Banshees");

		assertEquals("Banshees", banshees.getSuperiors().get(0).taskDisplayName(banshees));
		assertEquals("Twisted banshees", banshees.getSuperiors().get(1).taskDisplayName(banshees));
	}

	@Test
	public void variantPluralisationHandlesConsonantY()
	{
		HeartTask jellies = HeartData.findTask("Jellies");

		assertEquals("Warped Jellies", jellies.getSuperiors().get(1).taskDisplayName(jellies));
	}
}
