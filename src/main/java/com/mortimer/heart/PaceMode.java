package com.mortimer.heart;

public enum PaceMode
{
	PLANNING("Planning/manual pace"),
	LEARNED_WHEN_AVAILABLE("Learned pace when available");

	private final String label;

	PaceMode(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
