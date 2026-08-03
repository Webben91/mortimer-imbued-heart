package com.mortimer.heart;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(MortimerHeartConfig.GROUP)
public interface MortimerHeartConfig extends Config
{
	String GROUP = "mortimerHeart";

	@ConfigItem(
		keyName = "showMonsterVariants",
		name = "Show monster variants",
		description = "Show separate comparison choices when one Slayer assignment can be completed using different monsters",
		position = 0
	)
	default boolean showMonsterVariants()
	{
		return true;
	}

	@ConfigItem(
		keyName = "preferredGrind",
		name = "Preferred grind",
		description = "Choose whether task recommendations prioritise Imbued Heart chance, Slayer XP per hour, or an equal balance of both",
		position = 1
	)
	default GrindPreference preferredGrind()
	{
		return GrindPreference.IMBUED_HEART;
	}

	@ConfigItem(
		keyName = "braceletReminder",
		name = "Bracelet reminder",
		description = "Flash an in-game reminder when the active task's recommended Slayer bracelet is not equipped",
		position = 2
	)
	default boolean braceletReminder()
	{
		return true;
	}

	@ConfigItem(
		keyName = "manualBlockedTaskOne",
		name = "Manual block 1",
		description = "Add a task to routing calculations when RuneLite cannot read your in-game block list",
		position = 3
	)
	default ManualBlockedTask manualBlockedTaskOne()
	{
		return ManualBlockedTask.NONE;
	}

	@ConfigItem(
		keyName = "manualBlockedTaskTwo",
		name = "Manual block 2",
		description = "Add a second task to routing calculations when RuneLite cannot read your in-game block list",
		position = 4
	)
	default ManualBlockedTask manualBlockedTaskTwo()
	{
		return ManualBlockedTask.NONE;
	}

	@ConfigSection(
		name = "Aberrant spectres",
		description = "DPS and cannon settings for Aberrant spectres",
		position = 10,
		closedByDefault = true
	)
	String aberrantSpectresSection = "aberrantSpectresSection";

	@ConfigItem(
		keyName = "aberrantSpectresDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = aberrantSpectresSection,
		position = 0
	)
	default String aberrantSpectresDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "aberrantSpectresCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = aberrantSpectresSection,
		position = 1
	)
	default CannonSetup aberrantSpectresCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Abyssal demons",
		description = "DPS and cannon settings for Abyssal demons",
		position = 11,
		closedByDefault = true
	)
	String abyssalDemonsSection = "abyssalDemonsSection";

	@ConfigItem(
		keyName = "abyssalDemonsDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = abyssalDemonsSection,
		position = 0
	)
	default String abyssalDemonsDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "abyssalDemonsCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = abyssalDemonsSection,
		position = 1
	)
	default CannonSetup abyssalDemonsCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Aquanites",
		description = "DPS and cannon settings for Aquanites",
		position = 12,
		closedByDefault = true
	)
	String aquanitesSection = "aquanitesSection";

	@ConfigItem(
		keyName = "aquanitesDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = aquanitesSection,
		position = 0
	)
	default String aquanitesDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "aquanitesCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = aquanitesSection,
		position = 1
	)
	default CannonSetup aquanitesCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Araxytes",
		description = "DPS and cannon settings for Araxytes",
		position = 13,
		closedByDefault = true
	)
	String araxytesSection = "araxytesSection";

	@ConfigItem(
		keyName = "araxytesDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = araxytesSection,
		position = 0
	)
	default String araxytesDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "araxytesCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = araxytesSection,
		position = 1
	)
	default CannonSetup araxytesCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Banshees",
		description = "DPS and cannon settings for Banshees",
		position = 14,
		closedByDefault = true
	)
	String bansheesSection = "bansheesSection";

	@ConfigItem(
		keyName = "bansheesDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = bansheesSection,
		position = 0
	)
	default String bansheesDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "bansheesCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = bansheesSection,
		position = 1
	)
	default CannonSetup bansheesCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Basilisks",
		description = "DPS and cannon settings for Basilisks",
		position = 15,
		closedByDefault = true
	)
	String basilisksSection = "basilisksSection";

	@ConfigItem(
		keyName = "basilisksDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = basilisksSection,
		position = 0
	)
	default String basilisksDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "basilisksCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = basilisksSection,
		position = 1
	)
	default CannonSetup basilisksCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Bloodvelds",
		description = "DPS and cannon settings for Bloodvelds",
		position = 16,
		closedByDefault = true
	)
	String bloodveldsSection = "bloodveldsSection";

	@ConfigItem(
		keyName = "bloodveldsDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = bloodveldsSection,
		position = 0
	)
	default String bloodveldsDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "bloodveldsCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = bloodveldsSection,
		position = 1
	)
	default CannonSetup bloodveldsCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Cave crawlers",
		description = "DPS and cannon settings for Cave crawlers",
		position = 17,
		closedByDefault = true
	)
	String caveCrawlersSection = "caveCrawlersSection";

	@ConfigItem(
		keyName = "caveCrawlersDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = caveCrawlersSection,
		position = 0
	)
	default String caveCrawlersDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "caveCrawlersCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = caveCrawlersSection,
		position = 1
	)
	default CannonSetup caveCrawlersCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Cave horrors",
		description = "DPS and cannon settings for Cave horrors",
		position = 18,
		closedByDefault = true
	)
	String caveHorrorsSection = "caveHorrorsSection";

	@ConfigItem(
		keyName = "caveHorrorsDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = caveHorrorsSection,
		position = 0
	)
	default String caveHorrorsDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "caveHorrorsCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = caveHorrorsSection,
		position = 1
	)
	default CannonSetup caveHorrorsCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Cockatrice",
		description = "DPS and cannon settings for Cockatrice",
		position = 19,
		closedByDefault = true
	)
	String cockatriceSection = "cockatriceSection";

	@ConfigItem(
		keyName = "cockatriceDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = cockatriceSection,
		position = 0
	)
	default String cockatriceDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "cockatriceCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = cockatriceSection,
		position = 1
	)
	default CannonSetup cockatriceCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Crawling hands",
		description = "DPS and cannon settings for Crawling hands",
		position = 20,
		closedByDefault = true
	)
	String crawlingHandsSection = "crawlingHandsSection";

	@ConfigItem(
		keyName = "crawlingHandsDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = crawlingHandsSection,
		position = 0
	)
	default String crawlingHandsDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "crawlingHandsCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = crawlingHandsSection,
		position = 1
	)
	default CannonSetup crawlingHandsCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Custodian stalkers",
		description = "DPS and cannon settings for Custodian stalkers",
		position = 21,
		closedByDefault = true
	)
	String custodianStalkersSection = "custodianStalkersSection";

	@ConfigItem(
		keyName = "custodianStalkersDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = custodianStalkersSection,
		position = 0
	)
	default String custodianStalkersDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "custodianStalkersCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = custodianStalkersSection,
		position = 1
	)
	default CannonSetup custodianStalkersCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Dark beasts",
		description = "DPS and cannon settings for Dark beasts",
		position = 22,
		closedByDefault = true
	)
	String darkBeastsSection = "darkBeastsSection";

	@ConfigItem(
		keyName = "darkBeastsDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = darkBeastsSection,
		position = 0
	)
	default String darkBeastsDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "darkBeastsCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = darkBeastsSection,
		position = 1
	)
	default CannonSetup darkBeastsCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Drakes",
		description = "DPS and cannon settings for Drakes",
		position = 23,
		closedByDefault = true
	)
	String drakesSection = "drakesSection";

	@ConfigItem(
		keyName = "drakesDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = drakesSection,
		position = 0
	)
	default String drakesDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "drakesCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = drakesSection,
		position = 1
	)
	default CannonSetup drakesCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Dust devils",
		description = "DPS and cannon settings for Dust devils",
		position = 24,
		closedByDefault = true
	)
	String dustDevilsSection = "dustDevilsSection";

	@ConfigItem(
		keyName = "dustDevilsDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = dustDevilsSection,
		position = 0
	)
	default String dustDevilsDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "dustDevilsCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = dustDevilsSection,
		position = 1
	)
	default CannonSetup dustDevilsCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Gargoyles",
		description = "DPS and cannon settings for Gargoyles",
		position = 25,
		closedByDefault = true
	)
	String gargoylesSection = "gargoylesSection";

	@ConfigItem(
		keyName = "gargoylesDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = gargoylesSection,
		position = 0
	)
	default String gargoylesDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "gargoylesCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = gargoylesSection,
		position = 1
	)
	default CannonSetup gargoylesCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Gryphons",
		description = "DPS and cannon settings for Gryphons",
		position = 26,
		closedByDefault = true
	)
	String gryphonsSection = "gryphonsSection";

	@ConfigItem(
		keyName = "gryphonsDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = gryphonsSection,
		position = 0
	)
	default String gryphonsDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "gryphonsCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = gryphonsSection,
		position = 1
	)
	default CannonSetup gryphonsCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Hydras",
		description = "DPS and cannon settings for Hydras",
		position = 27,
		closedByDefault = true
	)
	String hydrasSection = "hydrasSection";

	@ConfigItem(
		keyName = "hydrasDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = hydrasSection,
		position = 0
	)
	default String hydrasDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "hydrasCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = hydrasSection,
		position = 1
	)
	default CannonSetup hydrasCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Infernal mages",
		description = "DPS and cannon settings for Infernal mages",
		position = 28,
		closedByDefault = true
	)
	String infernalMagesSection = "infernalMagesSection";

	@ConfigItem(
		keyName = "infernalMagesDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = infernalMagesSection,
		position = 0
	)
	default String infernalMagesDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "infernalMagesCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = infernalMagesSection,
		position = 1
	)
	default CannonSetup infernalMagesCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Jellies",
		description = "DPS and cannon settings for Jellies",
		position = 29,
		closedByDefault = true
	)
	String jelliesSection = "jelliesSection";

	@ConfigItem(
		keyName = "jelliesDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = jelliesSection,
		position = 0
	)
	default String jelliesDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "jelliesCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = jelliesSection,
		position = 1
	)
	default CannonSetup jelliesCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Kurask",
		description = "DPS and cannon settings for Kurask",
		position = 30,
		closedByDefault = true
	)
	String kuraskSection = "kuraskSection";

	@ConfigItem(
		keyName = "kuraskDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = kuraskSection,
		position = 0
	)
	default String kuraskDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "kuraskCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = kuraskSection,
		position = 1
	)
	default CannonSetup kuraskCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Nechryael",
		description = "DPS and cannon settings for Nechryael",
		position = 31,
		closedByDefault = true
	)
	String nechryaelSection = "nechryaelSection";

	@ConfigItem(
		keyName = "nechryaelDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = nechryaelSection,
		position = 0
	)
	default String nechryaelDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "nechryaelCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = nechryaelSection,
		position = 1
	)
	default CannonSetup nechryaelCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Pyrefiends",
		description = "DPS and cannon settings for Pyrefiends",
		position = 32,
		closedByDefault = true
	)
	String pyrefiendsSection = "pyrefiendsSection";

	@ConfigItem(
		keyName = "pyrefiendsDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = pyrefiendsSection,
		position = 0
	)
	default String pyrefiendsDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "pyrefiendsCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = pyrefiendsSection,
		position = 1
	)
	default CannonSetup pyrefiendsCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Rockslugs",
		description = "DPS and cannon settings for Rockslugs",
		position = 33,
		closedByDefault = true
	)
	String rockslugsSection = "rockslugsSection";

	@ConfigItem(
		keyName = "rockslugsDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = rockslugsSection,
		position = 0
	)
	default String rockslugsDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "rockslugsCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = rockslugsSection,
		position = 1
	)
	default CannonSetup rockslugsCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Smoke devils",
		description = "DPS and cannon settings for Smoke devils",
		position = 34,
		closedByDefault = true
	)
	String smokeDevilsSection = "smokeDevilsSection";

	@ConfigItem(
		keyName = "smokeDevilsDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = smokeDevilsSection,
		position = 0
	)
	default String smokeDevilsDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "smokeDevilsCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = smokeDevilsSection,
		position = 1
	)
	default CannonSetup smokeDevilsCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Turoth",
		description = "DPS and cannon settings for Turoth",
		position = 35,
		closedByDefault = true
	)
	String turothSection = "turothSection";

	@ConfigItem(
		keyName = "turothDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = turothSection,
		position = 0
	)
	default String turothDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "turothCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = turothSection,
		position = 1
	)
	default CannonSetup turothCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Venators",
		description = "DPS and cannon settings for Venators",
		position = 36,
		closedByDefault = true
	)
	String venatorsSection = "venatorsSection";

	@ConfigItem(
		keyName = "venatorsDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = venatorsSection,
		position = 0
	)
	default String venatorsDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "venatorsCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = venatorsSection,
		position = 1
	)
	default CannonSetup venatorsCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Warped creatures",
		description = "DPS and cannon settings for Warped creatures",
		position = 37,
		closedByDefault = true
	)
	String warpedCreaturesSection = "warpedCreaturesSection";

	@ConfigItem(
		keyName = "warpedCreaturesDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = warpedCreaturesSection,
		position = 0
	)
	default String warpedCreaturesDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "warpedCreaturesCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = warpedCreaturesSection,
		position = 1
	)
	default CannonSetup warpedCreaturesCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigSection(
		name = "Wyrms",
		description = "DPS and cannon settings for Wyrms",
		position = 38,
		closedByDefault = true
	)
	String wyrmsSection = "wyrmsSection";

	@ConfigItem(
		keyName = "wyrmsDps",
		name = "DPS override",
		description = "Enter a manual DPS number or paste an OSRS Wiki DPS share link",
		section = wyrmsSection,
		position = 0
	)
	default String wyrmsDps()
	{
		return "";
	}

	@ConfigItem(
		keyName = "wyrmsCannon",
		name = "Cannon",
		description = "Add the selected cannon contribution to this monster's DPS",
		section = wyrmsSection,
		position = 1
	)
	default CannonSetup wyrmsCannon()
	{
		return CannonSetup.OFF;
	}

	@ConfigItem(
		keyName = "localGrindData",
		name = "Local grind data",
		description = "Completed Mortimer tasks saved only in RuneLite's local configuration",
		hidden = true
	)
	default String localGrindData()
	{
		return "";
	}

	@ConfigItem(
		keyName = "activeTaskData",
		name = "Active task data",
		description = "The active Mortimer task saved locally so it survives a client restart",
		hidden = true
	)
	default String activeTaskData()
	{
		return "";
	}

	@ConfigItem(
		keyName = "blockedTasksData",
		name = "Detected Mortimer blocks",
		description = "Mortimer's blocked tasks detected from the in-game block list",
		hidden = true
	)
	default String blockedTasksData()
	{
		return "";
	}
}
