package com.mortimer.heart;

final class HeartResult
{
	private final OfferState offer;
	private final double actualKills;
	private final double heartPerSuperior;
	private final double heartPerKill;
	private final double taskChance;
	private final double chancePerHour;
	private final double expectedSuperiors;
	private final double taskHours;
	private final double hoursOnRate;

	HeartResult(OfferState offer, double actualKills, double heartPerSuperior, double heartPerKill,
		double taskChance, double chancePerHour, double expectedSuperiors, double taskHours, double hoursOnRate)
	{
		this.offer = offer;
		this.actualKills = actualKills;
		this.heartPerSuperior = heartPerSuperior;
		this.heartPerKill = heartPerKill;
		this.taskChance = taskChance;
		this.chancePerHour = chancePerHour;
		this.expectedSuperiors = expectedSuperiors;
		this.taskHours = taskHours;
		this.hoursOnRate = hoursOnRate;
	}

	OfferState getOffer() { return offer; }
	double getActualKills() { return actualKills; }
	double getHeartPerSuperior() { return heartPerSuperior; }
	double getHeartPerKill() { return heartPerKill; }
	double getTaskChance() { return taskChance; }
	double getChancePerHour() { return chancePerHour; }
	double getExpectedSuperiors() { return expectedSuperiors; }
	double getTaskHours() { return taskHours; }
	double getHoursOnRate() { return hoursOnRate; }
}
