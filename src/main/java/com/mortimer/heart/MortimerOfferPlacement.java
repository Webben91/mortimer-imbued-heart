package com.mortimer.heart;

import java.awt.Rectangle;

final class MortimerOfferPlacement
{
	private final MortimerDetectedOffer offer;
	private final Rectangle bounds;

	MortimerOfferPlacement(MortimerDetectedOffer offer, Rectangle bounds)
	{
		this.offer = offer;
		this.bounds = new Rectangle(bounds);
	}

	MortimerDetectedOffer getOffer()
	{
		return offer;
	}

	Rectangle getBounds()
	{
		return new Rectangle(bounds);
	}
}
