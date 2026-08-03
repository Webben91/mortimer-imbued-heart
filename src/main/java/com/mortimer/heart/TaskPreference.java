package com.mortimer.heart;

public enum TaskPreference
{
	STANDARD("Standard"),
	PREFER("Prefer when competitive"),
	ALWAYS("Always take");

	private final String label;

	TaskPreference(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
