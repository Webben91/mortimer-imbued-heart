package com.mortimer.heart;

import java.awt.Rectangle;

final class MortimerRepeatChoice
{
	private final Rectangle acceptBounds;
	private final Rectangle declineBounds;

	MortimerRepeatChoice(Rectangle acceptBounds, Rectangle declineBounds)
	{
		this.acceptBounds = new Rectangle(acceptBounds);
		this.declineBounds = new Rectangle(declineBounds);
	}

	Rectangle recommendedBounds(boolean accept)
	{
		return new Rectangle(accept ? acceptBounds : declineBounds);
	}
}
