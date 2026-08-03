package com.mortimer.heart;

import java.util.LinkedHashSet;
import java.util.Set;

final class BlockedTaskCodec
{
	private BlockedTaskCodec()
	{
	}

	static String encode(Set<String> tasks)
	{
		return String.join("|", tasks);
	}

	static Set<String> decode(String encoded)
	{
		Set<String> tasks = new LinkedHashSet<>();
		if (encoded == null || encoded.trim().isEmpty())
		{
			return tasks;
		}
		for (String value : encoded.split("\\|"))
		{
			HeartTask task = HeartData.findTask(value);
			if (task != null)
			{
				tasks.add(task.getName());
			}
		}
		return tasks;
	}
}
