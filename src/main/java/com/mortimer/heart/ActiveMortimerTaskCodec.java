package com.mortimer.heart;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

final class ActiveMortimerTaskCodec
{
	private ActiveMortimerTaskCodec()
	{
	}

	static String encode(ActiveMortimerTask task)
	{
		if (task == null)
		{
			return "";
		}
		return String.join("|", text(task.getTaskName()), text(task.getSuperiorName()),
			Integer.toString(task.getAssignedAmount()),
			String.format(Locale.ENGLISH, "%.6f", task.getBaseHeartRate()),
			String.format(Locale.ENGLISH, "%.6f", task.getDropModifier()),
			String.format(Locale.ENGLISH, "%.6f", task.getSuperiorSpawnRate()),
			Integer.toString(task.getSuperiorRolls()));
	}

	static ActiveMortimerTask decode(String value)
	{
		if (value == null || value.trim().isEmpty())
		{
			return null;
		}
		try
		{
			String[] parts = value.split("\\|", -1);
			if (parts.length != 6 && parts.length != 7)
			{
				return null;
			}
			return new ActiveMortimerTask(fromText(parts[0]), fromText(parts[1]), Integer.parseInt(parts[2]),
				Double.parseDouble(parts[3]), Double.parseDouble(parts[4]), Double.parseDouble(parts[5]),
				parts.length == 7 ? Integer.parseInt(parts[6]) : 0);
		}
		catch (RuntimeException ignored)
		{
			return null;
		}
	}

	private static String text(String value)
	{
		return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	private static String fromText(String value)
	{
		return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
	}
}
