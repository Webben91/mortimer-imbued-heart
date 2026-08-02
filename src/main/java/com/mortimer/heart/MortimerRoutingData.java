package com.mortimer.heart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class MortimerRoutingData
{
	private static final Map<String, Profile> PROFILES;

	static
	{
		Map<String, Profile> profiles = new LinkedHashMap<>();
		add(profiles, "Crawling hands", 10, 5, -30, -15, 50, 100);
		add(profiles, "Cave crawlers", 10, 10, -30, -15, 50, 100);
		add(profiles, "Banshees", 10, 15, -30, -15, 50, 100);
		add(profiles, "Rockslugs", 10, 20, -30, -15, 50, 100);
		add(profiles, "Cockatrice", 10, 25, -30, -15, 50, 100);
		add(profiles, "Pyrefiends", 10, 30, -30, -15, 50, 100);
		add(profiles, "Basilisks", 10, 40, 50, 100, 200, 300);
		add(profiles, "Infernal mages", 10, 45, -30, -15, 50, 100);
		add(profiles, "Bloodvelds", 8, 50, 50, 100, 100, 150);
		add(profiles, "Gryphons", 10, 51, 50, 100, 100, 150);
		add(profiles, "Jellies", 10, 52, 50, 100, 100, 150);
		add(profiles, "Custodian stalkers", 8, 54, 50, 100, 100, 150);
		add(profiles, "Turoth", 10, 55, 30, 80, 200, 300);
		add(profiles, "Warped creatures", 10, 56, 50, 100, 200, 300);
		add(profiles, "Cave horrors", 10, 58, 30, 80, 200, 300);
		add(profiles, "Aberrant spectres", 10, 60, 30, 80, 200, 300);
		add(profiles, "Wyrms", 10, 62, 50, 100, 200, 300);
		add(profiles, "Dust devils", 8, 65, 50, 150, 100, 150);
		add(profiles, "Kurask", 10, 70, 30, 80, 200, 300);
		add(profiles, "Venators", 10, 74, 50, 100, 200, 300);
		add(profiles, "Gargoyles", 10, 75, 50, 100, 200, 300);
		add(profiles, "Aquanites", 10, 78, 50, 100, 200, 300);
		add(profiles, "Nechryael", 8, 80, 50, 100, 70, 120);
		add(profiles, "Drakes", 10, 84, 30, 80, 75, 150);
		add(profiles, "Abyssal demons", 8, 85, 50, 100, 30, 60);
		add(profiles, "Dark beasts", 10, 90, 30, 80, 200, 300);
		add(profiles, "Araxytes", 8, 92, 50, 100, 10, 25);
		add(profiles, "Smoke devils", 8, 93, 50, 100, 10, 25);
		add(profiles, "Hydras", 10, 95, 50, 100, 75, 150);
		PROFILES = Collections.unmodifiableMap(profiles);
	}

	private MortimerRoutingData()
	{
	}

	static List<Profile> eligibleProfiles(int slayerLevel)
	{
		int effectiveLevel = slayerLevel <= 0 ? 99 : slayerLevel;
		List<Profile> eligible = new ArrayList<>();
		for (Profile profile : PROFILES.values())
		{
			if (profile.requiredSlayer <= effectiveLevel && profile.task != null)
			{
				eligible.add(profile);
			}
		}
		return eligible;
	}

	private static void add(Map<String, Profile> profiles, String taskName, int weight, int requiredSlayer,
		int quantityMin, int quantityMax, int superiorMin, int superiorMax)
	{
		profiles.put(taskName, new Profile(HeartData.findTask(taskName), weight, requiredSlayer,
			quantityMin, quantityMax, superiorMin, superiorMax));
	}

	static final class Profile
	{
		private final HeartTask task;
		private final int weight;
		private final int requiredSlayer;
		private final int quantityMin;
		private final int quantityMax;
		private final int superiorMin;
		private final int superiorMax;

		private Profile(HeartTask task, int weight, int requiredSlayer, int quantityMin, int quantityMax,
			int superiorMin, int superiorMax)
		{
			this.task = task;
			this.weight = weight;
			this.requiredSlayer = requiredSlayer;
			this.quantityMin = quantityMin;
			this.quantityMax = quantityMax;
			this.superiorMin = superiorMin;
			this.superiorMax = superiorMax;
		}

		HeartTask getTask() { return task; }
		int getWeight() { return weight; }
		int getQuantityMin() { return quantityMin; }
		int getQuantityMax() { return quantityMax; }
		int getSuperiorMin() { return superiorMin; }
		int getSuperiorMax() { return superiorMax; }
	}
}
