package com.mortimer.heart;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

final class WikiDpsResolver
{
	private static final String SHORTLINK_ENDPOINT = "https://tools.runescape.wiki/osrs-dps/shortlink?id=";
	private static final Pattern ID_PATTERN = Pattern.compile("(?:[?&]id=)([A-Za-z0-9_-]{3,100})", Pattern.CASE_INSENSITIVE);
	private final OkHttpClient httpClient;

	WikiDpsResolver(OkHttpClient httpClient)
	{
		// RuneLite must be able to exhaustively review every runtime destination.
		// Refuse response-provided redirects so this resolver can only contact the
		// single hardcoded OSRS Wiki endpoint below.
		this.httpClient = httpClient.newBuilder()
			.followRedirects(false)
			.followSslRedirects(false)
			.build();
	}

	Result resolve(String link, HeartTask expectedTask) throws IOException
	{
		String shareId = extractShareId(link);
		String endpoint = SHORTLINK_ENDPOINT
			+ URLEncoder.encode(shareId, StandardCharsets.UTF_8.name());
		Request request = new Request.Builder().url(endpoint).header("Accept", "application/json")
			.header("User-Agent", "Mortimer-Imbued-Heart-RuneLite/0.2").build();
		try (Response response = httpClient.newCall(request).execute())
		{
			if (!response.isSuccessful() || response.body() == null)
			{
				throw new IOException("OSRS Wiki returned " + response.code());
			}
			JsonObject envelope = new JsonParser().parse(response.body().string()).getAsJsonObject();
			JsonObject data = object(envelope, "data");
			JsonObject monster = object(data, "monster");
			String monsterName = string(monster, "name");
			if (!HeartData.textContainsTask(monsterName, expectedTask))
			{
				throw new IOException("Wiki target " + monsterName + " does not match " + expectedTask.getName());
			}
			JsonArray loadouts = data.getAsJsonArray("loadouts");
			int selected = integer(data, "selectedLoadout", 0);
			if (loadouts == null || loadouts.size() == 0 || selected < 0 || selected >= loadouts.size())
			{
				throw new IOException("Wiki link has no selected loadout");
			}
			JsonObject player = loadouts.get(selected).getAsJsonObject();
			double baseDps = calculateBasicDps(player, monster);
			String spell = stringOrEmpty(objectOrNull(player, "spell"), "name");
			JsonObject equipment = object(player, "equipment");
			String weapon = stringOrEmpty(objectOrNull(equipment, "weapon"), "name");
			double multiplier = spell.toLowerCase(Locale.ROOT).matches(".*(?:burst|barrage).*") ? 7.0
				: weapon.toLowerCase(Locale.ROOT).matches(".*venator bow.*charged.*") ? 2.0 : 1.0;
			return new Result(baseDps * multiplier, monsterName, multiplier);
		}
	}

	private static double calculateBasicDps(JsonObject player, JsonObject monster) throws IOException
	{
		JsonObject style = object(player, "style");
		String type = string(style, "type").toLowerCase(Locale.ROOT);
		JsonObject skills = object(player, "skills");
		JsonObject boosts = object(player, "boosts");
		JsonObject offensive = object(player, "offensive");
		JsonObject bonuses = object(player, "bonuses");
		JsonObject monsterSkills = object(monster, "skills");
		JsonObject monsterDefensive = object(monster, "defensive");
		int attackSpeed = Math.max(1, integer(player, "attackSpeed", 4));
		boolean onTask = booleanValue(objectOrNull(player, "buffs"), "onSlayerTask");
		String head = stringOrEmpty(objectOrNull(object(player, "equipment"), "head"), "name").toLowerCase(Locale.ROOT);
		boolean slayerHelmet = onTask && head.contains("slayer helmet");

		double attackLevel;
		double strengthLevel;
		double attackBonus;
		double damageBonus;
		double defenceBonus;
		double maxHit;
		if (type.contains("magic"))
		{
			attackLevel = number(skills, "magic") + number(boosts, "magic") + 9;
			attackBonus = number(offensive, "magic");
			defenceBonus = number(monsterDefensive, "magic");
			JsonObject spell = objectOrNull(player, "spell");
			maxHit = Math.max(1, numberAny(spell, 20, "maxHit", "max_hit", "max"));
			maxHit *= 1.0 + number(bonuses, "magic_str") / 100.0;
		}
		else if (type.contains("range") || type.equals("light") || type.equals("standard") || type.equals("heavy"))
		{
			attackLevel = number(skills, "ranged") + number(boosts, "ranged") + 8;
			strengthLevel = attackLevel;
			attackBonus = number(offensive, "ranged");
			damageBonus = number(bonuses, "ranged_str");
			defenceBonus = number(monsterDefensive, type);
			if (defenceBonus == 0)
			{
				defenceBonus = number(monsterDefensive, "standard");
			}
			maxHit = Math.floor(0.5 + strengthLevel * (damageBonus + 64) / 640.0);
		}
		else
		{
			attackLevel = number(skills, "atk") + number(boosts, "atk") + 8;
			strengthLevel = number(skills, "str") + number(boosts, "str") + 8;
			attackBonus = number(offensive, type);
			damageBonus = number(bonuses, "str");
			defenceBonus = number(monsterDefensive, type);
			maxHit = Math.floor(0.5 + strengthLevel * (damageBonus + 64) / 640.0);
		}

		double attackRoll = attackLevel * (attackBonus + 64.0);
		if (slayerHelmet)
		{
			attackRoll *= 7.0 / 6.0;
			maxHit *= 7.0 / 6.0;
		}
		double defenceRoll = (number(monsterSkills, "def") + 9.0) * (defenceBonus + 64.0);
		double accuracy = attackRoll > defenceRoll
			? 1.0 - (defenceRoll + 2.0) / (2.0 * (attackRoll + 1.0))
			: attackRoll / (2.0 * (defenceRoll + 1.0));
		double dps = Math.max(0, Math.min(1, accuracy)) * Math.max(1, Math.floor(maxHit)) / 2.0 / (attackSpeed * 0.6);
		if (!Double.isFinite(dps) || dps <= 0)
		{
			throw new IOException("Wiki loadout did not produce usable DPS");
		}
		return dps;
	}

	private static String extractShareId(String link) throws IOException
	{
		try
		{
			URI uri = URI.create(link.trim());
			String host = uri.getHost();
			if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null
				|| !(host.equalsIgnoreCase("dps.osrs.wiki") || host.equalsIgnoreCase("tools.runescape.wiki")))
			{
				throw new IOException("Use an OSRS Wiki DPS share link");
			}
			Matcher matcher = ID_PATTERN.matcher(link);
			if (!matcher.find())
			{
				throw new IOException("Wiki link has no share ID");
			}
			return matcher.group(1);
		}
		catch (IllegalArgumentException ex)
		{
			throw new IOException("Invalid Wiki DPS link", ex);
		}
	}

	private static JsonObject object(JsonObject parent, String key) throws IOException
	{
		JsonObject value = objectOrNull(parent, key);
		if (value == null)
		{
			throw new IOException("Wiki link is missing " + key);
		}
		return value;
	}

	private static JsonObject objectOrNull(JsonObject parent, String key)
	{
		if (parent == null)
		{
			return null;
		}
		JsonElement value = parent.get(key);
		return value != null && value.isJsonObject() ? value.getAsJsonObject() : null;
	}

	private static String string(JsonObject parent, String key) throws IOException
	{
		String value = stringOrEmpty(parent, key);
		if (value.isEmpty())
		{
			throw new IOException("Wiki link is missing " + key);
		}
		return value;
	}

	private static String stringOrEmpty(JsonObject parent, String key)
	{
		if (parent == null || !parent.has(key) || parent.get(key).isJsonNull())
		{
			return "";
		}
		return parent.get(key).getAsString();
	}

	private static double number(JsonObject parent, String key)
	{
		if (parent == null || !parent.has(key) || parent.get(key).isJsonNull())
		{
			return 0;
		}
		try
		{
			return parent.get(key).getAsDouble();
		}
		catch (RuntimeException ignored)
		{
			return 0;
		}
	}

	private static double numberAny(JsonObject parent, double fallback, String... keys)
	{
		for (String key : keys)
		{
			double value = number(parent, key);
			if (value > 0)
			{
				return value;
			}
		}
		return fallback;
	}

	private static int integer(JsonObject parent, String key, int fallback)
	{
		double value = number(parent, key);
		return value == 0 ? fallback : (int) Math.round(value);
	}

	private static boolean booleanValue(JsonObject parent, String key)
	{
		return parent != null && parent.has(key) && !parent.get(key).isJsonNull() && parent.get(key).getAsBoolean();
	}

	static final class Result
	{
		private final double effectiveDps;
		private final String monsterName;
		private final double multiplier;

		Result(double effectiveDps, String monsterName, double multiplier)
		{
			this.effectiveDps = effectiveDps;
			this.monsterName = monsterName;
			this.multiplier = multiplier;
		}

		double getEffectiveDps() { return effectiveDps; }
		String getMonsterName() { return monsterName; }
		double getMultiplier() { return multiplier; }
	}
}
