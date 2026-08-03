package com.mortimer.heart;

import java.util.HashMap;
import java.util.Map;

final class SlayerExperienceData
{
	private static final Map<String, Double> XP_PER_KILL = new HashMap<>();

	static
	{
		add("Aberrant spectre", 90);
		add("Deviant spectre", 194.5);
		add("Abyssal demon", 150);
		add("Aquanite", 180);
		add("Araxyte", 60);
		add("Banshee", 22);
		add("Twisted banshee", 100);
		add("Basilisk", 75);
		add("Basilisk Knight", 300);
		add("Bloodveld", 120);
		add("Mutated Bloodveld", 170);
		add("Cave crawler", 22);
		add("Cave horror", 55);
		add("Cockatrice", 37);
		add("Crawling hand", 12);
		add("Custodian stalker", 250);
		add("Dark beast", 225.4);
		add("Drake", 230.6);
		add("Dust devil", 105);
		add("Gargoyle", 105);
		add("Gryphon", 110);
		add("Hydra", 322.5);
		add("Infernal mage", 60);
		add("Jelly", 75);
		add("Warped Jelly", 140);
		add("Chilled Jelly", 55);
		add("Kurask", 97);
		add("Nechryael", 105);
		add("Greater Nechryael", 210);
		add("Pyrefiend", 45);
		add("Pyrelord", 80);
		add("Rockslug", 27);
		add("Smoke devil", 185);
		add("Turoth", 76);
		add("Venator", 405);
		add("Warped Terrorbird", 150);
		add("Warped Tortoise", 200);
		add("Wyrm", 123);
		add("Magma Wyrm", 97);
	}

	private SlayerExperienceData()
	{
	}

	static double experiencePerHour(OfferState offer)
	{
		double experience = XP_PER_KILL.getOrDefault(offer.getSuperior().getMonsterName(), 0.0);
		double modifier = 1.0 + Math.max(0.0, offer.getXpModifier()) / 100.0;
		double actualKills = Math.max(1.0, offer.getBracelet().adjustKills(offer.getAmount()));
		double taskHours = actualKills / Math.max(1.0, offer.getKillsPerHour()) + offer.getOverheadHours();
		return experience * actualKills / Math.max(1e-12, taskHours) * modifier;
	}

	static double experiencePerKill(String monsterName)
	{
		return XP_PER_KILL.getOrDefault(monsterName, 0.0);
	}

	private static void add(String monsterName, double experience)
	{
		XP_PER_KILL.put(monsterName, experience);
	}
}
