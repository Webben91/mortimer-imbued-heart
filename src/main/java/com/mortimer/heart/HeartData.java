package com.mortimer.heart;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class HeartData
{
	private HeartData()
	{
	}

	private static SuperiorOption superior(String monsterName, String superiorName, double rate)
	{
		return new SuperiorOption(monsterName, superiorName, rate);
	}

	private static SuperiorOption strategy(String monsterName, String strategyName, double killsPerHour)
	{
		return new SuperiorOption(monsterName, strategyName, 0.0, killsPerHour);
	}

	static final List<HeartTask> TASKS = Collections.unmodifiableList(Arrays.asList(
		new HeartTask("Aberrant spectres", 80, 120, 300, superior("Aberrant spectre", "Abhorrent spectre", 760), superior("Deviant spectre", "Repugnant spectre", 760)),
		new HeartTask("Abyssal demons", 120, 180, 350, superior("Abyssal demon", "Greater abyssal demon", 352)),
		new HeartTask("Aquanites", 40, 60, 150, superior("Aquanite", "Elder aquanite", 472)),
		new HeartTask("Araxytes", 120, 180, 550, superior("Araxyte", "Dreadborn Araxyte", 224)),
		new HeartTask("Banshees", 35, 50, 450, superior("Banshee", "Screaming banshee", 1288), superior("Twisted banshee", "Screaming twisted banshee", 1288)),
		new HeartTask("Basilisks", 40, 60, 350, superior("Basilisk", "Monstrous basilisk", 1024), superior("Basilisk Knight", "Basilisk Sentinel", 760)),
		new HeartTask("Bloodvelds", 120, 180, 350, superior("Bloodveld", "Insatiable Bloodveld", 896), superior("Mutated Bloodveld", "Insatiable mutated Bloodveld", 896)),
		new HeartTask("Cave crawlers", 35, 50, 550, superior("Cave crawler", "Chasm Crawler", 1336)),
		new HeartTask("Cave horrors", 80, 120, 350, superior("Cave horror", "Cave abomination", 784)),
		new HeartTask("Cockatrice", 35, 50, 450, superior("Cockatrice", "Cockathrice", 1192)),
		new HeartTask("Crawling hands", 35, 50, 700, superior("Crawling hand", "Crushing hand", 1376)),
		new HeartTask("Custodian stalkers", 80, 120, 300, superior("Custodian stalker", "Ancient Custodian", 504)),
		new HeartTask("Dark beasts", 40, 60, 200, superior("Dark beast", "Night beast", 256)),
		new HeartTask("Drakes", 40, 60, 140, superior("Drake", "Guardian Drake", 368)),
		new HeartTask("Dust devils", 120, 180, 550, superior("Dust devil", "Choke devil", 680)),
		new HeartTask("Gargoyles", 120, 180, 260, superior("Gargoyle", "Marble gargoyle", 520)),
		new HeartTask("Gryphons", 80, 120, 500, superior("Gryphon", "Dire gryphon", 888)),
		new HeartTask("Hydras", 150, 200, 120, superior("Hydra", "Colossal Hydra", 160)),
		new HeartTask("Infernal mages", 35, 50, 350, superior("Infernal mage", "Malevolent Mage", 960)),
		new HeartTask("Jellies", 80, 120, 450, superior("Jelly", "Vitreous Jelly", 872), superior("Warped Jelly", "Vitreous Warped Jelly", 872), superior("Chilled Jelly", "Vitreous Chilled Jelly", 872)),
		new HeartTask("Kurask", 40, 60, 280, superior("Kurask", "King kurask", 600)),
		new HeartTask("Nechryael", 150, 200, 490, superior("Nechryael", "Nechryarch (Normal)", 440), superior("Greater Nechryael", "Nechryarch (Greater)", 440)),
		new HeartTask("Pyrefiends", 35, 50, 450, superior("Pyrefiend", "Flaming pyrelord", 1144), superior("Pyrelord", "Infernal pyrelord", 1144)),
		new HeartTask("Rockslugs", 35, 50, 500, superior("Rockslug", "Giant rockslug", 1240)),
		new HeartTask("Smoke devils", 80, 120, 460, superior("Smoke devil", "Nuclear smoke devil", 200)),
		new HeartTask("Turoth", 80, 120, 350, superior("Turoth", "Spiked Turoth", 832)),
		new HeartTask("Venators", 120, 180, 110, superior("Venator", "Blood-starved venator", 536)),
		new HeartTask("Warped creatures", 80, 120, 350, superior("Warped Terrorbird", "Mutated Terrorbird", 816), superior("Warped Tortoise", "Mutated Tortoise", 816)),
		new HeartTask("Wyrms", 80, 120, 180, superior("Wyrm", "Shadow Wyrm", 728), superior("Magma Wyrm", "Magma strykewyrm", 728),
			strategy("Wyrmling", "Fast reroll — no superior", 920))
	));

	static HeartTask findTask(String text)
	{
		String normalized = normalize(text);
		for (HeartTask task : TASKS)
		{
			String taskName = normalize(task.getName());
			if (normalized.equals(taskName) || normalized.equals(singular(taskName)))
			{
				return task;
			}
		}
		return null;
	}

	static boolean textContainsTask(String text, HeartTask task)
	{
		String normalized = " " + normalize(text) + " ";
		String plural = " " + normalize(task.getName()) + " ";
		String singular = " " + singular(normalize(task.getName())) + " ";
		return normalized.contains(plural) || normalized.contains(singular);
	}

	private static String singular(String value)
	{
		if (value.endsWith("ies"))
		{
			return value.substring(0, value.length() - 3) + "y";
		}
		if (value.endsWith("s"))
		{
			return value.substring(0, value.length() - 1);
		}
		return value;
	}

	private static String normalize(String value)
	{
		return value == null ? "" : value.toLowerCase(Locale.ROOT)
			.replace('\u00a0', ' ')
			.replaceAll("<[^>]*>", " ")
			.replaceAll("[^a-z0-9]+", " ")
			.trim();
	}
}
