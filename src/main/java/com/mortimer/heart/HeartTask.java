package com.mortimer.heart;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class HeartTask
{
	private final String name;
	private final int amountMin;
	private final int amountMax;
	private final int defaultKph;
	private final List<SuperiorOption> superiors;

	HeartTask(String name, int amountMin, int amountMax, int defaultKph, SuperiorOption... superiors)
	{
		this.name = name;
		this.amountMin = amountMin;
		this.amountMax = amountMax;
		this.defaultKph = defaultKph;
		this.superiors = Collections.unmodifiableList(Arrays.asList(superiors));
	}

	String getName()
	{
		return name;
	}

	int getAmountMin()
	{
		return amountMin;
	}

	int getAmountMax()
	{
		return amountMax;
	}

	int getPlanningAmount()
	{
		return (amountMin + amountMax) / 2;
	}

	int getDefaultKph()
	{
		return defaultKph;
	}

	List<SuperiorOption> getSuperiors()
	{
		return superiors;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
