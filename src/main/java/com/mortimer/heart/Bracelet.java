package com.mortimer.heart;

enum Bracelet
{
	NONE("No bracelet", 1.0),
	EXPEDITIOUS("Expeditious", 1.0 / 1.25),
	SLAUGHTER("Slaughter", 1.0 / 0.75);

	private final String label;
	private final double killMultiplier;

	Bracelet(String label, double killMultiplier)
	{
		this.label = label;
		this.killMultiplier = killMultiplier;
	}

	double adjustKills(int assignedAmount)
	{
		return assignedAmount * killMultiplier;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
