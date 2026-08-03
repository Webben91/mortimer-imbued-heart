package com.mortimer.heart;

final class LearnedPace
{
	private final int kills;
	private final long elapsedMillis;

	LearnedPace(int kills, long elapsedMillis)
	{
		this.kills = Math.max(0, kills);
		this.elapsedMillis = Math.max(0L, elapsedMillis);
	}

	int getKills() { return kills; }
	long getElapsedMillis() { return elapsedMillis; }

	double killsPerHour()
	{
		return kills < 1 || elapsedMillis < 1L ? 0.0 : kills * 3_600_000.0 / elapsedMillis;
	}

	LearnedPace combine(int additionalKills, long additionalElapsedMillis)
	{
		return new LearnedPace(kills + Math.max(0, additionalKills),
			elapsedMillis + Math.max(0L, additionalElapsedMillis));
	}
}
