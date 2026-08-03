package com.mortimer.heart;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MortimerRecommendationOverlayTest
{
	@Test
	public void rendersSparklyRecommendationCard()
	{
		HeartTask task = HeartData.findTask("Smoke devils");
		MortimerDetectedOffer offer = new MortimerDetectedOffer(task, 100, 100, "test");
		MortimerRecommendationOverlay overlay = new MortimerRecommendationOverlay();
		overlay.show(Collections.singletonList(
			new MortimerOfferPlacement(offer, new Rectangle(25, 30, 330, 115))),
			new MortimerOverlayRecommendation(0, MortimerOverlayRecommendation.Style.HEART,
				"RECOMMENDED · BEST HEART"));

		BufferedImage image = new BufferedImage(400, 180, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try
		{
			overlay.render(graphics);
		}
		finally
		{
			graphics.dispose();
		}
		int paintedPixels = 0;
		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = 0; x < image.getWidth(); x++)
			{
				if ((image.getRGB(x, y) >>> 24) != 0)
				{
					paintedPixels++;
				}
			}
		}
		assertTrue(paintedPixels > 3_000);
	}
}
