package com.mortimer.heart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

final class BraceletReminderOverlay extends Overlay
{
	private static final Color GOLD = new Color(242, 190, 60);
	private static final Color DARK = new Color(12, 12, 12, 220);
	private volatile Bracelet recommended = Bracelet.NONE;

	BraceletReminderOverlay()
	{
		setPosition(OverlayPosition.TOP_LEFT);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(Overlay.PRIORITY_HIGH);
	}

	void show(Bracelet bracelet)
	{
		recommended = bracelet == null ? Bracelet.NONE : bracelet;
	}

	void clear()
	{
		recommended = Bracelet.NONE;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Bracelet bracelet = recommended;
		if (bracelet == Bracelet.NONE)
		{
			return null;
		}
		boolean bright = System.currentTimeMillis() / 450L % 2L == 0L;
		String title = "BRACELET REMINDER";
		String message = "Equip " + bracelet + " bracelet";
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setFont(FontManager.getRunescapeBoldFont());
		FontMetrics titleMetrics = graphics.getFontMetrics();
		graphics.setFont(FontManager.getRunescapeSmallFont());
		FontMetrics messageMetrics = graphics.getFontMetrics();
		int width = Math.max(titleMetrics.stringWidth(title), messageMetrics.stringWidth(message)) + 24;
		int height = titleMetrics.getHeight() + messageMetrics.getHeight() + 18;
		graphics.setColor(DARK);
		graphics.fillRoundRect(0, 0, width, height, 10, 10);
		graphics.setColor(bright ? Color.WHITE : GOLD);
		graphics.drawRoundRect(1, 1, width - 2, height - 2, 10, 10);
		graphics.setFont(FontManager.getRunescapeBoldFont());
		graphics.drawString(title, 12, 8 + titleMetrics.getAscent());
		graphics.setFont(FontManager.getRunescapeSmallFont());
		graphics.setColor(Color.WHITE);
		graphics.drawString(message, 12, 10 + titleMetrics.getHeight() + messageMetrics.getAscent());
		return new Dimension(width, height);
	}
}
