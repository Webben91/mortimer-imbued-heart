package com.mortimer.heart;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.runelite.client.config.ConfigManager;

final class TaskPerformanceService
{
	private static final double DOWNTIME_SECONDS = 2.0;
	private static final int MINIMUM_LEARNED_KILLS = 10;
	private static final long MINIMUM_LEARNED_MILLIS = 180_000L;
	private static final Map<String, Integer> HITPOINTS = new HashMap<>();
	private static final Map<String, String> KEYS = new HashMap<>();

	static
	{
		add("Aberrant spectres", 90, "aberrantSpectres");
		add("Abyssal demons", 150, "abyssalDemons");
		add("Aquanites", 180, "aquanites");
		add("Araxytes", 60, "araxytes");
		add("Banshees", 22, "banshees");
		add("Basilisks", 75, "basilisks");
		add("Bloodvelds", 120, "bloodvelds");
		add("Cave crawlers", 22, "caveCrawlers");
		add("Cave horrors", 55, "caveHorrors");
		add("Cockatrice", 37, "cockatrice");
		add("Crawling hands", 15, "crawlingHands");
		add("Custodian stalkers", 250, "custodianStalkers");
		add("Dark beasts", 220, "darkBeasts");
		add("Drakes", 225, "drakes");
		add("Dust devils", 105, "dustDevils");
		add("Gargoyles", 105, "gargoyles");
		add("Gryphons", 110, "gryphons");
		add("Hydras", 300, "hydras");
		add("Infernal mages", 60, "infernalMages");
		add("Jellies", 75, "jellies");
		add("Kurask", 97, "kurask");
		add("Nechryael", 105, "nechryael");
		add("Pyrefiends", 45, "pyrefiends");
		add("Rockslugs", 27, "rockslugs");
		add("Smoke devils", 185, "smokeDevils");
		add("Turoth", 76, "turoth");
		add("Venators", 345, "venators");
		add("Warped creatures", 150, "warpedCreatures");
		add("Wyrms", 120, "wyrms");
	}

	private final ConfigManager configManager;

	TaskPerformanceService(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	double killsPerHour(HeartTask task)
	{
		TaskPaceProfile profile = profile(task);
		if (profile.getManualKillsPerHour() > 0.0)
		{
			return profile.getManualKillsPerHour();
		}
		LearnedPace learned = learnedPace(task);
		if (paceMode() == PaceMode.LEARNED_WHEN_AVAILABLE && validLearnedPace(learned))
		{
			return learned.killsPerHour();
		}
		String key = key(task);
		String raw = value(key + "Dps").trim();
		double baseDps = parseNumber(raw);
		if (baseDps <= 0 && isWikiLink(raw))
		{
			String source = value(key + "ResolvedSource");
			if (raw.equals(source))
			{
				baseDps = parseNumber(value(key + "ResolvedDps"));
			}
		}

		CannonSetup cannon = configManager.getConfiguration(MortimerHeartConfig.GROUP,
			key + "Cannon", CannonSetup.class);
		double cannonDps = cannon == null ? 0.0 : cannon.getDps();
		if (baseDps <= 0 && cannonDps <= 0)
		{
			return task.getDefaultKph();
		}
		if (baseDps <= 0)
		{
			baseDps = baselineDps(task);
		}
		return Math.max(1.0, calculateKph(task, baseDps + cannonDps));
	}

	double expectedDps(HeartTask task)
	{
		return dpsFromKph(task, killsPerHour(task));
	}

	double measuredDps(HeartTask task, double kills, long elapsedMillis)
	{
		if (kills < 1 || elapsedMillis < 30_000L)
		{
			return 0.0;
		}
		double kph = kills * 3_600_000.0 / elapsedMillis;
		return dpsFromKph(task, kph);
	}

	String paceLabel(HeartTask task)
	{
		TaskPaceProfile profile = profile(task);
		if (profile.getManualKillsPerHour() > 0.0)
		{
			return "Manual KPH";
		}
		LearnedPace learned = learnedPace(task);
		if (paceMode() == PaceMode.LEARNED_WHEN_AVAILABLE && validLearnedPace(learned))
		{
			return "Learned pace · " + learned.getKills() + " kills";
		}
		String key = key(task);
		String raw = value(key + "Dps").trim();
		CannonSetup cannon = configManager.getConfiguration(MortimerHeartConfig.GROUP,
			key + "Cannon", CannonSetup.class);
		String source;
		if (parseNumber(raw) > 0)
		{
			source = "Manual DPS";
		}
		else if (isWikiLink(raw) && raw.equals(value(key + "ResolvedSource")))
		{
			source = "Wiki DPS";
		}
		else if (isWikiLink(raw))
		{
			source = "Wiki DPS resolving";
		}
		else
		{
			source = "Planning pace";
		}
		if (cannon != null && cannon != CannonSetup.OFF)
		{
			source += " + cannon";
		}
		return source;
	}

	double overheadHours(HeartTask task)
	{
		LearnedPace learned = learnedPace(task);
		if (profile(task).getManualKillsPerHour() <= 0.0
			&& paceMode() == PaceMode.LEARNED_WHEN_AVAILABLE && validLearnedPace(learned))
		{
			return 0.0;
		}
		int preparation = Math.max(0, integerValue("preparationSeconds"));
		return (preparation + profile(task).getTravelSeconds()) / 3600.0;
	}

	TaskPreference preference(HeartTask task)
	{
		return profile(task).getPreference();
	}

	TaskPaceProfile profile(HeartTask task)
	{
		return TaskPaceProfileCodec.decode(value("personalPaceData"))
			.getOrDefault(task.getName(), TaskPaceProfile.DEFAULT);
	}

	LearnedPace learnedPace(HeartTask task)
	{
		return LearnedPaceCodec.decode(value("learnedPaceData")).get(task.getName());
	}

	String recordLearnedPace(HeartTask task, int kills, long elapsedMillis)
	{
		if (task == null || kills < MINIMUM_LEARNED_KILLS || elapsedMillis < MINIMUM_LEARNED_MILLIS)
		{
			return value("learnedPaceData");
		}
		double sampleKph = kills * 3_600_000.0 / elapsedMillis;
		if (!Double.isFinite(sampleKph) || sampleKph < 10.0 || sampleKph > 5_000.0)
		{
			return value("learnedPaceData");
		}
		Map<String, LearnedPace> learned = LearnedPaceCodec.decode(value("learnedPaceData"));
		LearnedPace previous = learned.get(task.getName());
		learned.put(task.getName(), previous == null
			? new LearnedPace(kills, elapsedMillis) : previous.combine(kills, elapsedMillis));
		String encoded = LearnedPaceCodec.encode(learned);
		configManager.setConfiguration(MortimerHeartConfig.GROUP, "learnedPaceData", encoded);
		return encoded;
	}

	String wikiLink(HeartTask task)
	{
		String raw = value(key(task) + "Dps").trim();
		return isWikiLink(raw) ? raw : "";
	}

	void saveResolved(HeartTask task, String source, double effectiveDps)
	{
		String key = key(task);
		configManager.setConfiguration(MortimerHeartConfig.GROUP, key + "ResolvedSource", source);
		configManager.setConfiguration(MortimerHeartConfig.GROUP, key + "ResolvedDps",
			String.format(Locale.ENGLISH, "%.6f", effectiveDps));
	}

	private double baselineDps(HeartTask task)
	{
		double secondsPerKill = 3600.0 / task.getDefaultKph();
		return hitpoints(task) / Math.max(0.25, secondsPerKill - DOWNTIME_SECONDS);
	}

	private double calculateKph(HeartTask task, double dps)
	{
		return 3600.0 / (hitpoints(task) / Math.max(0.01, dps) + DOWNTIME_SECONDS);
	}

	private double dpsFromKph(HeartTask task, double kph)
	{
		double combatSeconds = 3600.0 / Math.max(1.0, kph) - DOWNTIME_SECONDS;
		return combatSeconds <= 0.05 ? 0.0 : hitpoints(task) / combatSeconds;
	}

	private String value(String key)
	{
		String result = configManager.getConfiguration(MortimerHeartConfig.GROUP, key);
		return result == null ? "" : result;
	}

	private int integerValue(String key)
	{
		try
		{
			return Integer.parseInt(value(key));
		}
		catch (RuntimeException ignored)
		{
			return 0;
		}
	}

	private PaceMode paceMode()
	{
		PaceMode mode = configManager.getConfiguration(MortimerHeartConfig.GROUP, "paceMode", PaceMode.class);
		return mode == null ? PaceMode.PLANNING : mode;
	}

	private static boolean validLearnedPace(LearnedPace pace)
	{
		return pace != null && pace.getKills() >= MINIMUM_LEARNED_KILLS
			&& pace.getElapsedMillis() >= MINIMUM_LEARNED_MILLIS
			&& pace.killsPerHour() >= 10.0 && pace.killsPerHour() <= 5_000.0;
	}

	private static int hitpoints(HeartTask task)
	{
		return HITPOINTS.getOrDefault(task.getName(), 100);
	}

	static String key(HeartTask task)
	{
		return KEYS.getOrDefault(task.getName(), task.getName().replaceAll("[^A-Za-z0-9]", ""));
	}

	static boolean isWikiLink(String value)
	{
		return value != null && value.matches("(?i)^https://(?:dps\\.osrs\\.wiki|tools\\.runescape\\.wiki)(?:/osrs-dps)?/?.*[?&]id=[A-Za-z0-9_-]{3,100}.*$");
	}

	private static double parseNumber(String value)
	{
		try
		{
			double parsed = Double.parseDouble(value);
			return Double.isFinite(parsed) && parsed > 0 && parsed <= 1000 ? parsed : 0.0;
		}
		catch (RuntimeException ignored)
		{
			return 0.0;
		}
	}

	private static void add(String task, int hitpoints, String key)
	{
		HITPOINTS.put(task, hitpoints);
		KEYS.put(task, key);
	}
}
