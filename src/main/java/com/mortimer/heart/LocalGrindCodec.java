package com.mortimer.heart;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

final class LocalGrindCodec
{
	private LocalGrindCodec()
	{
	}

	static String encode(List<GrindRecord> records)
	{
		List<String> encoded = new ArrayList<>();
		for (GrindRecord record : records)
		{
			encoded.add(String.join("|",
				text(record.getTaskName()), text(record.getSuperiorName()),
				Integer.toString(record.getKills()),
				String.format(Locale.ENGLISH, "%.6f", record.getBaseHeartRate()),
				String.format(Locale.ENGLISH, "%.6f", record.getDropModifier()),
				String.format(Locale.ENGLISH, "%.6f", record.getSuperiorSpawnRate()),
				Integer.toString(record.getSuperiorRolls())));
		}
		return String.join(";", encoded);
	}

	static List<GrindRecord> decode(String value)
	{
		List<GrindRecord> records = new ArrayList<>();
		if (value == null || value.trim().isEmpty())
		{
			return records;
		}
		for (String encoded : value.split(";"))
		{
			try
			{
				String[] parts = encoded.split("\\|", -1);
				if (parts.length != 6 && parts.length != 7)
				{
					continue;
				}
				records.add(new GrindRecord(fromText(parts[0]), fromText(parts[1]),
					Integer.parseInt(parts[2]), Double.parseDouble(parts[3]),
					Double.parseDouble(parts[4]), Double.parseDouble(parts[5]),
					parts.length == 7 ? Integer.parseInt(parts[6]) : -1));
			}
			catch (IllegalArgumentException ignored)
			{
				// Ignore a damaged local entry while retaining all valid records.
			}
		}
		return records;
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
