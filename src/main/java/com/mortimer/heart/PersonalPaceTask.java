package com.mortimer.heart;

public enum PersonalPaceTask
{
	ABERRANT_SPECTRES("Aberrant spectres"),
	ABYSSAL_DEMONS("Abyssal demons"),
	AQUANITES("Aquanites"),
	ARAXYTES("Araxytes"),
	BANSHEES("Banshees"),
	BASILISKS("Basilisks"),
	BLOODVELDS("Bloodvelds"),
	CAVE_CRAWLERS("Cave crawlers"),
	CAVE_HORRORS("Cave horrors"),
	COCKATRICE("Cockatrice"),
	CRAWLING_HANDS("Crawling hands"),
	CUSTODIAN_STALKERS("Custodian stalkers"),
	DARK_BEASTS("Dark beasts"),
	DRAKES("Drakes"),
	DUST_DEVILS("Dust devils"),
	GARGOYLES("Gargoyles"),
	GRYPHONS("Gryphons"),
	HYDRAS("Hydras"),
	INFERNAL_MAGES("Infernal mages"),
	JELLIES("Jellies"),
	KURASK("Kurask"),
	NECHRYAEL("Nechryael"),
	PYREFIENDS("Pyrefiends"),
	ROCKSLUGS("Rockslugs"),
	SMOKE_DEVILS("Smoke devils"),
	TUROTH("Turoth"),
	VENATORS("Venators"),
	WARPED_CREATURES("Warped creatures"),
	WYRMS("Wyrms");

	private final String taskName;

	PersonalPaceTask(String taskName)
	{
		this.taskName = taskName;
	}

	String getTaskName()
	{
		return taskName;
	}

	@Override
	public String toString()
	{
		return taskName;
	}
}
