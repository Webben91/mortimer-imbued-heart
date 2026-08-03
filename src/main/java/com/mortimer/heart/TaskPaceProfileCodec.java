package com.mortimer.heart;

import java.util.LinkedHashMap;
import java.util.Map;

final class TaskPaceProfileCodec
{
	private TaskPaceProfileCodec()
	{
	}

	static Map<String, TaskPaceProfile> decode(String encoded)
	{
		Map<String, TaskPaceProfile> profiles = new LinkedHashMap<>();
		if (encoded == null || encoded.trim().isEmpty())
		{
			return profiles;
		}
		for (String record : encoded.split(";"))
		{
			String[] fields = record.split(",", -1);
			if (fields.length != 4)
			{
				continue;
			}
			try
			{
				HeartTask task = HeartData.findTask(fields[0]);
				if (task == null)
				{
					continue;
				}
				double kph = Double.parseDouble(fields[1]);
				int travel = Integer.parseInt(fields[2]);
				TaskPreference preference = TaskPreference.valueOf(fields[3]);
				profiles.put(task.getName(), new TaskPaceProfile(kph, travel, preference));
			}
			catch (RuntimeException ignored)
			{
				// Ignore one malformed profile without discarding the others.
			}
		}
		return profiles;
	}

	static String encode(Map<String, TaskPaceProfile> profiles)
	{
		StringBuilder encoded = new StringBuilder();
		for (Map.Entry<String, TaskPaceProfile> entry : profiles.entrySet())
		{
			TaskPaceProfile profile = entry.getValue();
			if (profile == null || profile.isDefault() || HeartData.findTask(entry.getKey()) == null)
			{
				continue;
			}
			if (encoded.length() > 0)
			{
				encoded.append(';');
			}
			encoded.append(entry.getKey()).append(',')
				.append(profile.getManualKillsPerHour()).append(',')
				.append(profile.getTravelSeconds()).append(',')
				.append(profile.getPreference().name());
		}
		return encoded.toString();
	}
}
