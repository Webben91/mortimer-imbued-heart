package com.mortimer.heart;

import java.util.LinkedHashMap;
import java.util.Map;

final class LearnedPaceCodec
{
	private LearnedPaceCodec()
	{
	}

	static Map<String, LearnedPace> decode(String encoded)
	{
		Map<String, LearnedPace> paces = new LinkedHashMap<>();
		if (encoded == null || encoded.trim().isEmpty())
		{
			return paces;
		}
		for (String record : encoded.split(";"))
		{
			String[] fields = record.split(",", -1);
			if (fields.length != 3 && fields.length != 4)
			{
				continue;
			}
			try
			{
				HeartTask task = HeartData.findTask(fields[0]);
				if (task != null)
				{
					String monsterName = fields.length == 4 ? fields[1] : "";
					int killsIndex = fields.length == 4 ? 2 : 1;
					int timeIndex = fields.length == 4 ? 3 : 2;
					paces.put(key(task.getName(), monsterName),
						new LearnedPace(Integer.parseInt(fields[killsIndex]), Long.parseLong(fields[timeIndex])));
				}
			}
			catch (RuntimeException ignored)
			{
				// Ignore malformed samples.
			}
		}
		return paces;
	}

	static String encode(Map<String, LearnedPace> paces)
	{
		StringBuilder encoded = new StringBuilder();
		for (Map.Entry<String, LearnedPace> entry : paces.entrySet())
		{
			LearnedPace pace = entry.getValue();
			if (pace == null || pace.getKills() < 1 || pace.getElapsedMillis() < 1L)
			{
				continue;
			}
			String[] keyParts = splitKey(entry.getKey());
			if (HeartData.findTask(keyParts[0]) == null)
			{
				continue;
			}
			if (encoded.length() > 0)
			{
				encoded.append(';');
			}
			encoded.append(keyParts[0]).append(',').append(keyParts[1]).append(',')
				.append(pace.getKills()).append(',').append(pace.getElapsedMillis());
		}
		return encoded.toString();
	}

	static String key(String taskName, String monsterName)
	{
		return taskName + "|" + (monsterName == null ? "" : monsterName);
	}

	private static String[] splitKey(String key)
	{
		int separator = key.indexOf('|');
		return separator < 0 ? new String[]{key, ""}
			: new String[]{key.substring(0, separator), key.substring(separator + 1)};
	}
}
