package com.mortimer.heart;

public enum GrindPreference
{
	IMBUED_HEART("Imbued Heart chance"),
	SLAYER_XP("Slayer XP per hour"),
	BALANCED("Balanced");

	private final String label;

	GrindPreference(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
