package com.mortimer.heart;

public enum ManualBlockedTask
{
	NONE("None", null),
	ABERRANT_SPECTRES("Aberrant spectres", "Aberrant spectres"),
	ABYSSAL_DEMONS("Abyssal demons", "Abyssal demons"),
	AQUANITES("Aquanites", "Aquanites"),
	ARAXYTES("Araxytes", "Araxytes"),
	BANSHEES("Banshees", "Banshees"),
	BASILISKS("Basilisks", "Basilisks"),
	BLOODVELDS("Bloodvelds", "Bloodvelds"),
	CAVE_CRAWLERS("Cave crawlers", "Cave crawlers"),
	CAVE_HORRORS("Cave horrors", "Cave horrors"),
	COCKATRICE("Cockatrice", "Cockatrice"),
	CRAWLING_HANDS("Crawling hands", "Crawling hands"),
	CUSTODIAN_STALKERS("Custodian stalkers", "Custodian stalkers"),
	DARK_BEASTS("Dark beasts", "Dark beasts"),
	DRAKES("Drakes", "Drakes"),
	DUST_DEVILS("Dust devils", "Dust devils"),
	GARGOYLES("Gargoyles", "Gargoyles"),
	GRYPHONS("Gryphons", "Gryphons"),
	HYDRAS("Hydras", "Hydras"),
	INFERNAL_MAGES("Infernal mages", "Infernal mages"),
	JELLIES("Jellies", "Jellies"),
	KURASK("Kurask", "Kurask"),
	NECHRYAEL("Nechryael", "Nechryael"),
	PYREFIENDS("Pyrefiends", "Pyrefiends"),
	ROCKSLUGS("Rockslugs", "Rockslugs"),
	SMOKE_DEVILS("Smoke devils", "Smoke devils"),
	TUROTH("Turoth", "Turoth"),
	VENATORS("Venators", "Venators"),
	WARPED_CREATURES("Warped creatures", "Warped creatures"),
	WYRMS("Wyrms", "Wyrms");

	private final String label;
	private final String taskName;

	ManualBlockedTask(String label, String taskName)
	{
		this.label = label;
		this.taskName = taskName;
	}

	String getTaskName()
	{
		return taskName;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
