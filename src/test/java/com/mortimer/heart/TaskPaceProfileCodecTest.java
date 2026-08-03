package com.mortimer.heart;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TaskPaceProfileCodecTest
{
	@Test
	public void profilesRoundTripWithoutSavingDefaults()
	{
		Map<String, TaskPaceProfile> profiles = new LinkedHashMap<>();
		profiles.put("Araxytes", new TaskPaceProfile(612.5, 24, TaskPreference.PREFER));
		profiles.put("Banshees", TaskPaceProfile.DEFAULT);

		String encoded = TaskPaceProfileCodec.encode(profiles);
		Map<String, TaskPaceProfile> decoded = TaskPaceProfileCodec.decode(encoded);

		assertFalse(decoded.containsKey("Banshees"));
		assertEquals(612.5, decoded.get("Araxytes").getManualKillsPerHour(), 0.001);
		assertEquals(24, decoded.get("Araxytes").getTravelSeconds());
		assertEquals(TaskPreference.PREFER, decoded.get("Araxytes").getPreference());
	}

	@Test
	public void malformedRecordDoesNotDiscardValidProfiles()
	{
		Map<String, TaskPaceProfile> decoded = TaskPaceProfileCodec.decode(
			"bad;Smoke devils,800,30,ALWAYS;Unknown,12,4,STANDARD");

		assertEquals(1, decoded.size());
		assertEquals(TaskPreference.ALWAYS, decoded.get("Smoke devils").getPreference());
	}
}
