package com.mortimer.heart;

final class WildernessTaskSettings
{
	private WildernessTaskSettings()
	{
	}

	static boolean isEnabled(MortimerHeartConfig config, HeartTask task, SuperiorOption superior)
	{
		if (config == null || task == null || superior == null)
		{
			return false;
		}

		switch (task.getName())
		{
			case "Abyssal demons":
				return config.abyssalDemonsWilderness();
			case "Dust devils":
				return config.dustDevilsWilderness();
			case "Jellies":
				return config.jelliesWilderness() && "Jelly".equals(superior.getMonsterName());
			case "Nechryael":
				return config.nechryaelWilderness()
					&& "Greater Nechryael".equals(superior.getMonsterName());
			default:
				return false;
		}
	}
}
