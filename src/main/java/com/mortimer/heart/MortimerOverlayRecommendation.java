package com.mortimer.heart;

final class MortimerOverlayRecommendation
{
	enum Style
	{
		HEART,
		SLAYER_XP,
		BALANCED,
		FAST_REROLL,
		POINT_SKIP
	}

	private final int offerIndex;
	private final Style style;
	private final String label;

	MortimerOverlayRecommendation(int offerIndex, Style style, String label)
	{
		this.offerIndex = offerIndex;
		this.style = style;
		this.label = label;
	}

	int getOfferIndex()
	{
		return offerIndex;
	}

	Style getStyle()
	{
		return style;
	}

	String getLabel()
	{
		return label;
	}
}
