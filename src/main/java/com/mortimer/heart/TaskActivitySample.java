package com.mortimer.heart;

final class TaskActivitySample
{
	private final String monsterName;
	private final int kills;
	private final long activeMillis;

	TaskActivitySample(String monsterName, int kills, long activeMillis)
	{
		this.monsterName = monsterName;
		this.kills = Math.max(0, kills);
		this.activeMillis = Math.max(0L, activeMillis);
	}

	String getMonsterName() { return monsterName; }
	int getKills() { return kills; }
	long getActiveMillis() { return activeMillis; }
}
