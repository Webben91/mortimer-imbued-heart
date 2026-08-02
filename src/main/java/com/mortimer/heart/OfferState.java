package com.mortimer.heart;

final class OfferState
{
	private final HeartTask task;
	private final SuperiorOption superior;
	private final int amount;
	private final double dropModifier;
	private final double killsPerHour;
	private final Bracelet bracelet;

	OfferState(HeartTask task, SuperiorOption superior, int amount, double dropModifier, double killsPerHour, Bracelet bracelet)
	{
		this.task = task;
		this.superior = superior;
		this.amount = amount;
		this.dropModifier = dropModifier;
		this.killsPerHour = killsPerHour;
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

	double getKillsPerHour()
	{
		return killsPerHour;
	}

	Bracelet getBracelet()
	{
		return bracelet;
	}
}
