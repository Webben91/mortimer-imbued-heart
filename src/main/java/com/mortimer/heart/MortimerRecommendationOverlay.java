package com.mortimer.heart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

final class MortimerRecommendationOverlay extends Overlay
{
	private static final Color HEART = new Color(177, 91, 255);
	private static final Color SLAYER_XP = new Color(73, 207, 104);
	private static final Color BALANCED = new Color(242, 190, 60);
	private static final Color FAST_REROLL = new Color(69, 155, 255);
	private static final Color POINT_SKIP = new Color(230, 64, 64);
	private static final Color HEART_FILL = new Color(177, 91, 255, 28);
	private static final Color SLAYER_XP_FILL = new Color(73, 207, 104, 28);
	private static final Color BALANCED_FILL = new Color(242, 190, 60, 28);
	private static final Color FAST_REROLL_FILL = new Color(69, 155, 255, 28);
	private static final Color POINT_SKIP_FILL = new Color(230, 64, 64, 28);
	private static final Color SHADOW = new Color(0, 0, 0, 155);
	private static final Color LABEL_BACKGROUND = new Color(12, 12, 12, 225);
	private static final Font LABEL_FONT = FontManager.getRunescapeBoldFont().deriveFont(14f);
	private static final Color[] WHITE_SPARKLES = sparkleColors();
	private static final Stroke SHADOW_STROKE = new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private static final Stroke BORDER_STROKE = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private static final Stroke LABEL_STROKE = new BasicStroke(2f);
	private static final Stroke SPARKLE_THIN = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private static final Stroke SPARKLE_THICK = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private static final int ARC = 12;
	private static final int SPARKLE_COUNT = 18;

	private volatile Highlight highlight;

	MortimerRecommendationOverlay()
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(Overlay.PRIORITY_HIGH);
	}

	void show(List<MortimerOfferPlacement> placements, MortimerOverlayRecommendation recommendation)
	{
		if (recommendation == null || recommendation.getOfferIndex() < 0
			|| recommendation.getOfferIndex() >= placements.size())
		{
			clear();
			return;
		}
		show(placements.get(recommendation.getOfferIndex()).getBounds(),
			recommendation.getStyle(), recommendation.getLabel());
	}

	void show(Rectangle bounds, MortimerOverlayRecommendation.Style style, String label)
	{
		highlight = bounds != null && bounds.width > 0 && bounds.height > 0
			? new Highlight(bounds, style, label) : null;
	}

	void clear()
	{
		highlight = null;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Highlight current = highlight;
		if (current == null)
		{
			return null;
		}

		Stroke oldStroke = graphics.getStroke();
		Paint oldPaint = graphics.getPaint();
		Composite oldComposite = graphics.getComposite();
		Font oldFont = graphics.getFont();
		Object oldAntialiasing = graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		try
		{
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			drawHighlight(graphics, current);
		}
		finally
		{
			graphics.setStroke(oldStroke);
			graphics.setPaint(oldPaint);
			graphics.setComposite(oldComposite);
			graphics.setFont(oldFont);
			if (oldAntialiasing != null)
			{
				graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialiasing);
			}
		}
		return null;
	}

	private static void drawHighlight(Graphics2D graphics, Highlight highlight)
	{
		Rectangle row = highlight.bounds;
		Color color = color(highlight.style);
		RoundRectangle2D border = new RoundRectangle2D.Double(
			row.x - 2, row.y - 2, row.width + 4, row.height + 4, ARC, ARC);

		graphics.setColor(fillColor(highlight.style));
		graphics.fill(border);
		graphics.setStroke(SHADOW_STROKE);
		graphics.setColor(SHADOW);
		graphics.draw(border);
		graphics.setStroke(BORDER_STROKE);
		graphics.setColor(color);
		graphics.draw(border);

		drawSparkles(graphics, row, color);
		drawLabel(graphics, row, color, highlight.label);
	}

	private static void drawSparkles(Graphics2D graphics, Rectangle row, Color color)
	{
		double perimeter = Math.max(1.0, 2.0 * (row.width + row.height));
		double phase = (System.currentTimeMillis() % 4200L) / 4200.0 * perimeter;
		for (int index = 0; index < SPARKLE_COUNT; index++)
		{
			double distance = (phase + perimeter * index / SPARKLE_COUNT) % perimeter;
			long point = perimeterPoint(row, distance);
			int pointX = (int) (point >> 32);
			int pointY = (int) point;
			double pulse = 0.5 + 0.5 * Math.sin(index * 1.91 + phase / 22.0);
			int radius = 1 + (int) Math.round(pulse * 2.0);
			int brightness = Math.min(WHITE_SPARKLES.length - 1, (int) Math.round(pulse * (WHITE_SPARKLES.length - 1)));
			graphics.setStroke(index % 3 == 0 ? SPARKLE_THICK : SPARKLE_THIN);
			graphics.setColor(WHITE_SPARKLES[brightness]);
			graphics.drawLine(pointX - radius, pointY, pointX + radius, pointY);
			graphics.drawLine(pointX, pointY - radius, pointX, pointY + radius);
			if (radius >= 3)
			{
				graphics.setColor(color);
				graphics.drawLine(pointX - 2, pointY - 2, pointX + 2, pointY + 2);
				graphics.drawLine(pointX - 2, pointY + 2, pointX + 2, pointY - 2);
			}
		}
	}

	private static long perimeterPoint(Rectangle row, double distance)
	{
		double top = row.width;
		double right = top + row.height;
		double bottom = right + row.width;
		if (distance < top)
		{
			return point(row.x + (int) Math.round(distance), row.y);
		}
		if (distance < right)
		{
			return point(row.x + row.width, row.y + (int) Math.round(distance - top));
		}
		if (distance < bottom)
		{
			return point(row.x + row.width - (int) Math.round(distance - right), row.y + row.height);
		}
		return point(row.x, row.y + row.height - (int) Math.round(distance - bottom));
	}

	private static long point(int x, int y)
	{
		return (long) x << 32 | y & 0xffffffffL;
	}

	private static void drawLabel(Graphics2D graphics, Rectangle row, Color color, String text)
	{
		graphics.setFont(LABEL_FONT);
		FontMetrics metrics = graphics.getFontMetrics();
		int labelWidth = metrics.stringWidth(text) + 18;
		int labelHeight = metrics.getHeight() + 6;
		int labelX = Math.max(row.x + 6, row.x + row.width - labelWidth - 8);
		int labelY = row.y + 7;

		graphics.setColor(LABEL_BACKGROUND);
		graphics.fillRoundRect(labelX, labelY, labelWidth, labelHeight, 9, 9);
		graphics.setStroke(LABEL_STROKE);
		graphics.setColor(color);
		graphics.drawRoundRect(labelX, labelY, labelWidth, labelHeight, 9, 9);
		graphics.setColor(Color.WHITE);
		graphics.drawString(text, labelX + 9, labelY + 3 + metrics.getAscent());
	}

	private static Color color(MortimerOverlayRecommendation.Style style)
	{
		switch (style)
		{
			case HEART:
				return HEART;
			case SLAYER_XP:
				return SLAYER_XP;
			case BALANCED:
				return BALANCED;
			case FAST_REROLL:
				return FAST_REROLL;
			case POINT_SKIP:
			default:
				return POINT_SKIP;
		}
	}

	private static Color fillColor(MortimerOverlayRecommendation.Style style)
	{
		switch (style)
		{
			case HEART:
				return HEART_FILL;
			case SLAYER_XP:
				return SLAYER_XP_FILL;
			case BALANCED:
				return BALANCED_FILL;
			case FAST_REROLL:
				return FAST_REROLL_FILL;
			case POINT_SKIP:
			default:
				return POINT_SKIP_FILL;
		}
	}

	private static Color[] sparkleColors()
	{
		Color[] colors = new Color[8];
		for (int index = 0; index < colors.length; index++)
		{
			colors[index] = new Color(255, 255, 255, 120 + index * 19);
		}
		return colors;
	}

	private static final class Highlight
	{
		private final Rectangle bounds;
		private final MortimerOverlayRecommendation.Style style;
		private final String label;

		private Highlight(Rectangle bounds, MortimerOverlayRecommendation.Style style, String label)
		{
			this.bounds = new Rectangle(bounds);
			this.style = style;
			this.label = label;
		}
	}
}
