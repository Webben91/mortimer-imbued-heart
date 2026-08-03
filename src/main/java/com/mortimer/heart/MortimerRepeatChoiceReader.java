package com.mortimer.heart;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.util.Text;

final class MortimerRepeatChoiceReader
{
	MortimerRepeatChoice read(Client client)
	{
		List<Line> lines = new ArrayList<>();
		Set<Widget> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		for (Widget root : client.getWidgetRoots())
		{
			collect(root, lines, visited);
		}
		String joined = lines.stream().map(line -> line.text).reduce("", (left, right) -> left + " " + right)
			.toLowerCase(Locale.ROOT);
		boolean repeatPrompt = joined.contains("slayer cape") || joined.contains("same task")
			|| joined.contains("repeat the task") || joined.contains("retain the task")
			|| joined.contains("keep the task");
		if (!repeatPrompt)
		{
			return null;
		}
		Line accept = null;
		Line decline = null;
		for (Line line : lines)
		{
			String value = line.text.toLowerCase(Locale.ROOT).trim();
			if (accept == null && value.matches("^(yes|accept|repeat|keep)([,.! ]|$).*") )
			{
				accept = line;
			}
			if (decline == null && value.matches("^(no|decline)([,.! ]|$).*") )
			{
				decline = line;
			}
		}
		return accept == null || decline == null ? null : new MortimerRepeatChoice(accept.bounds, decline.bounds);
	}

	private static void collect(Widget widget, List<Line> lines, Set<Widget> visited)
	{
		if (widget == null || widget.isHidden() || !visited.add(widget))
		{
			return;
		}
		String raw = widget.getText();
		if (raw != null && !raw.trim().isEmpty())
		{
			String clean = Text.removeTags(raw.replaceAll("(?i)<br\\s*/?>", " "))
				.replace('\u00a0', ' ').trim();
			Rectangle bounds = widget.getBounds();
			if (!clean.isEmpty() && bounds != null && bounds.width > 0 && bounds.height > 0)
			{
				lines.add(new Line(clean, bounds));
			}
		}
		collect(widget.getStaticChildren(), lines, visited);
		collect(widget.getDynamicChildren(), lines, visited);
		collect(widget.getNestedChildren(), lines, visited);
	}

	private static void collect(Widget[] children, List<Line> lines, Set<Widget> visited)
	{
		if (children != null)
		{
			for (Widget child : children)
			{
				collect(child, lines, visited);
			}
		}
	}

	private static final class Line
	{
		private final String text;
		private final Rectangle bounds;

		private Line(String text, Rectangle bounds)
		{
			this.text = text;
			this.bounds = new Rectangle(bounds);
		}
	}
}
