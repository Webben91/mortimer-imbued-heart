package com.mortimer.heart;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WildernessTaskSettingsTest
{
	private final MortimerHeartConfig enabled = new MortimerHeartConfig()
	{
		@Override
		public boolean abyssalDemonsWilderness()
		{
			return true;
		}

		@Override
		public boolean dustDevilsWilderness()
		{
			return true;
		}

		@Override
		public boolean jelliesWilderness()
		{
			return true;
		}

		@Override
		public boolean nechryaelWilderness()
		{
			return true;
		}
	};

	@Test
	public void enablesTheFourWildernessSuperiorVariants()
	{
		assertEnabled("Abyssal demons", 0);
		assertEnabled("Dust devils", 0);
		assertEnabled("Jellies", 0);
		assertEnabled("Nechryael", 1);
	}

	@Test
	public void doesNotApplyBonusToNonWildernessVariants()
	{
		assertDisabled("Jellies", 1);
		assertDisabled("Jellies", 2);
		assertDisabled("Nechryael", 0);
		assertDisabled("Smoke devils", 0);
	}

	private void assertEnabled(String taskName, int superiorIndex)
	{
		HeartTask task = HeartData.findTask(taskName);
		assertTrue(WildernessTaskSettings.isEnabled(enabled, task,
			task.getSuperiors().get(superiorIndex)));
	}

	private void assertDisabled(String taskName, int superiorIndex)
	{
		HeartTask task = HeartData.findTask(taskName);
		assertFalse(WildernessTaskSettings.isEnabled(enabled, task,
			task.getSuperiors().get(superiorIndex)));
	}
}
