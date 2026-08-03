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
			if (fields.length != 3)
			{
				continue;
			}
			try
			{
				HeartTask task = HeartData.findTask(fields[0]);
				if (task != null)
				{
					paces.put(task.getName(), new LearnedPace(Integer.parseInt(fields[1]), Long.parseLong(fields[2])));
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
			if (encoded.length() > 0)
			{
				encoded.append(';');
			}
			encoded.append(entry.getKey()).append(',').append(pace.getKills()).append(',')
				.append(pace.getElapsedMillis());
		}
		return encoded.toString();
	}
}
