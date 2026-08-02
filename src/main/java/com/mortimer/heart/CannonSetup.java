package com.mortimer.heart;

public enum CannonSetup
{
	OFF("Off", 0.0),
	SINGLE_STEEL("Single combat · Steel", 1.01),
	SINGLE_GRANITE("Single combat · Granite", 1.18),
	MULTI_STEEL("Multicombat · Steel", 4.05),
	MULTI_GRANITE("Multicombat · Granite", 4.73);

	private final String label;
	private final double dps;

	CannonSetup(String label, double dps)
	{
		this.label = label;
		this.dps = dps;
	}

	double getDps()
	{
		return dps;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
