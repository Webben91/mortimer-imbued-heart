package com.mortimer.heart;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.util.Text;

final class MortimerWidgetReader
{
	private static final Pattern AMOUNT_PATTERN = Pattern.compile("amount\\s*:?\\s*(\\d+)\\s*(?:to|[-–])\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern PERCENT_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)\\s*%");

	List<MortimerDetectedOffer> read(Client client)
	{
		Map<Integer, List<WidgetLine>> interfaceLines = new LinkedHashMap<>();
		Set<Widget> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		for (Widget root : client.getWidgetRoots())
		{
			if (root == null)
			{
				continue;
			}
			int groupId = root.getId() >>> 16;
			List<WidgetLine> lines = interfaceLines.computeIfAbsent(groupId, ignored -> new ArrayList<>());
			collect(root, lines, visited);
		}

		List<WidgetLine> lines = interfaceLines.values().stream()
			.filter(MortimerWidgetReader::isMortimerChoiceInterface)
			.findFirst()
			.orElse(Collections.emptyList());
		if (lines.isEmpty())
		{
			return Collections.emptyList();
		}
		lines.sort(Comparator.comparingInt(WidgetLine::getY).thenComparingInt(WidgetLine::getX));

		List<TaskLine> taskLines = new ArrayList<>();
		for (WidgetLine line : lines)
		{
			for (HeartTask task : HeartData.TASKS)
			{
				if (HeartData.textContainsTask(line.getText(), task))
				{
					boolean duplicate = taskLines.stream().anyMatch(existing -> existing.task == task && Math.abs(existing.y - line.y) < 8);
					if (!duplicate)
					{
						taskLines.add(new TaskLine(task, line.y));
					}
					break;
				}
			}
		}
		taskLines.sort(Comparator.comparingInt(value -> value.y));

		List<MortimerDetectedOffer> offers = new ArrayList<>();
		for (int index = 0; index < taskLines.size() && offers.size() < 3; index++)
		{
			TaskLine taskLine = taskLines.get(index);
			int endY = index + 1 < taskLines.size() ? taskLines.get(index + 1).y - 4 : taskLine.y + 145;
			int amount = taskLine.task.getPlanningAmount();
			double modifier = 0;
			double xpModifier = 0;
			String modifierText = "No Heart or XP modifier detected";
			for (WidgetLine line : lines)
			{
				if (line.y < taskLine.y - 8 || line.y >= endY)
				{
					continue;
				}
				Matcher amountMatcher = AMOUNT_PATTERN.matcher(line.text);
				if (amountMatcher.find())
				{
					int minimum = Integer.parseInt(amountMatcher.group(1));
					int maximum = Integer.parseInt(amountMatcher.group(2));
					amount = (minimum + maximum) / 2;
				}
				String lineLower = line.text.toLowerCase(Locale.ROOT);
				if (lineLower.contains("superior") && lineLower.contains("unique"))
				{
					Matcher percentage = PERCENT_PATTERN.matcher(line.text);
					if (percentage.find())
					{
						modifier = Math.max(0, Double.parseDouble(percentage.group(1).replace("+", "")));
						modifierText = line.text;
					}
				}
				else if (lineLower.contains("slayer experience") || lineLower.matches(".*\\bxp\\b.*"))
				{
					Matcher percentage = PERCENT_PATTERN.matcher(line.text);
					if (percentage.find())
					{
						xpModifier = Math.max(0, Double.parseDouble(percentage.group(1).replace("+", "")));
						modifierText = line.text;
					}
				}
			}
			offers.add(new MortimerDetectedOffer(taskLine.task, amount, modifier, xpModifier, modifierText));
		}
		return offers;
	}

	private static boolean isMortimerChoiceInterface(List<WidgetLine> lines)
	{
		for (WidgetLine line : lines)
		{
			String normalized = line.getText().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
			if ("slayer task choice".equals(normalized))
			{
				return true;
			}
		}
		return false;
	}

	private void collect(Widget widget, List<WidgetLine> lines, Set<Widget> visited)
	{
		if (widget == null || !visited.add(widget) || widget.isHidden())
		{
			return;
		}
		String raw = widget.getText();
		if (raw != null && !raw.trim().isEmpty())
		{
			String clean = Text.removeTags(raw.replaceAll("(?i)<br\\s*/?>", " ")).replace('\u00a0', ' ').trim();
			if (!clean.isEmpty())
			{
				Rectangle bounds = widget.getBounds();
				lines.add(new WidgetLine(clean, bounds == null ? 0 : bounds.x, bounds == null ? 0 : bounds.y));
			}
		}
		collectChildren(widget.getStaticChildren(), lines, visited);
		collectChildren(widget.getDynamicChildren(), lines, visited);
		collectChildren(widget.getNestedChildren(), lines, visited);
	}

	private void collectChildren(Widget[] children, List<WidgetLine> lines, Set<Widget> visited)
	{
		if (children == null)
		{
			return;
		}
		for (Widget child : children)
		{
			collect(child, lines, visited);
		}
	}

	private static final class WidgetLine
	{
		private final String text;
		private final int x;
		private final int y;

		private WidgetLine(String text, int x, int y)
		{
			this.text = text;
			this.x = x;
			this.y = y;
		}

		String getText() { return text; }
		int getX() { return x; }
		int getY() { return y; }
	}

	private static final class TaskLine
	{
		private final HeartTask task;
		private final int y;

		private TaskLine(HeartTask task, int y)
		{
			this.task = task;
			this.y = y;
		}
	}
}
