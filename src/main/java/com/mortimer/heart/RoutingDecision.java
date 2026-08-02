package com.mortimer.heart;

final class RoutingDecision
{
	enum Type
	{
		HUNT,
		FAST_REROLL,
		POINT_SKIP
	}

	private final Type type;
	private final int primaryIndex;
	private final int bestHeartIndex;
	private final int fastestFallbackIndex;
	private final Bracelet bracelet;
	private final double expectedHours;
	private final double freshOfferHours;
	private final double probabilityBetterNext;

	RoutingDecision(Type type, int primaryIndex, int bestHeartIndex, int fastestFallbackIndex,
		Bracelet bracelet, double expectedHours, double freshOfferHours, double probabilityBetterNext)
	{
		this.type = type;
		this.primaryIndex = primaryIndex;
		this.bestHeartIndex = bestHeartIndex;
		this.fastestFallbackIndex = fastestFallbackIndex;
		this.bracelet = bracelet;
		this.expectedHours = expectedHours;
		this.freshOfferHours = freshOfferHours;
		this.probabilityBetterNext = probabilityBetterNext;
	}

	Type getType() { return type; }
	int getPrimaryIndex() { return primaryIndex; }
	int getBestHeartIndex() { return bestHeartIndex; }
	int getFastestFallbackIndex() { return fastestFallbackIndex; }
	Bracelet getBracelet() { return bracelet; }
	double getExpectedHours() { return expectedHours; }
	double getFreshOfferHours() { return freshOfferHours; }
	double getProbabilityBetterNext() { return probabilityBetterNext; }
}
