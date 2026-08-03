package com.mortimer.heart;

final class OfferState
{
	private final HeartTask task;
	private final SuperiorOption superior;
	private final int amount;
	private final double dropModifier;
	private final double xpModifier;
	private final double killsPerHour;
	private final double overheadHours;
	private final Bracelet bracelet;

	OfferState(HeartTask task, SuperiorOption superior, int amount, double dropModifier, double killsPerHour, Bracelet bracelet)
	{
		this(task, superior, amount, dropModifier, 0.0, killsPerHour, 0.0, bracelet);
	}

	OfferState(HeartTask task, SuperiorOption superior, int amount, double dropModifier, double xpModifier,
		double killsPerHour, Bracelet bracelet)
	{
		this(task, superior, amount, dropModifier, xpModifier, killsPerHour, 0.0, bracelet);
	}

	OfferState(HeartTask task, SuperiorOption superior, int amount, double dropModifier, double xpModifier,
		double killsPerHour, double overheadHours, Bracelet bracelet)
	{
		this.task = task;
		this.superior = superior;
		this.amount = amount;
		this.dropModifier = dropModifier;
		this.xpModifier = xpModifier;
		this.killsPerHour = killsPerHour;
		this.overheadHours = Math.max(0.0, overheadHours);
		this.bracelet = bracelet;
	}

	HeartTask getTask()
	{
		return task;
	}

	SuperiorOption getSuperior()
	{
		return superior;
	}

	int getAmount()
	{
		return amount;
	}

	double getDropModifier()
	{
		return dropModifier;
	}

	double getXpModifier()
	{
		return xpModifier;
	}

	double getKillsPerHour()
	{
		return killsPerHour;
	}

	double getOverheadHours()
	{
		return overheadHours;
	}

	Bracelet getBracelet()
	{
		return bracelet;
	}
}
