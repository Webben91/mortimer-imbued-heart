package com.mortimer.heart;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class MortimerHeartPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(MortimerHeartPlugin.class);
		RuneLite.main(args);
	}
}
