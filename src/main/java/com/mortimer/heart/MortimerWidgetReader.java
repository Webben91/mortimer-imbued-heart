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
		List<MortimerDetectedOffer> offers = new ArrayList<>();
		for (MortimerOfferPlacement placement : readScreen(client))
		{
			offers.add(placement.getOffer());
		}
		return offers;
	}

	List<MortimerOfferPlacement> readScreen(Client client)
	{
		Map<Integer, InterfaceData> interfaces = new LinkedHashMap<>();
		Set<Widget> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		for (Widget root : client.getWidgetRoots())
		{
			if (root == null)
			{
				continue;
			}
			int groupId = root.getId() >>> 16;
			InterfaceData data = interfaces.computeIfAbsent(groupId, ignored -> new InterfaceData());
			collect(root, data, visited);
		}

		InterfaceData selected = interfaces.values().stream()
			.filter(data -> isMortimerChoiceInterface(data.lines))
			.findFirst()
			.orElse(null);
		if (selected == null || selected.lines.isEmpty())
		{
			return Collections.emptyList();
		}
		List<WidgetLine> lines = new ArrayList<>(selected.lines);
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
						taskLines.add(new TaskLine(task, line.x, line.y));
					}
					break;
				}
			}
		}
		taskLines.sort(Comparator.comparingInt(value -> value.y));

		Rectangle interfaceBounds = union(selected.bounds);
		List<MortimerOfferPlacement> offers = new ArrayList<>();
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
			MortimerDetectedOffer offer = new MortimerDetectedOffer(
				taskLine.task, amount, modifier, xpModifier, modifierText);
			Rectangle rowBounds = findRowBounds(selected.bounds, interfaceBounds, taskLines, index);
			offers.add(new MortimerOfferPlacement(offer, rowBounds));
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

	private void collect(Widget widget, InterfaceData data, Set<Widget> visited)
	{
		if (widget == null || !visited.add(widget) || widget.isHidden())
		{
			return;
		}
		Rectangle bounds = widget.getBounds();
		if (bounds != null && bounds.width > 0 && bounds.height > 0)
		{
			data.bounds.add(new Rectangle(bounds));
		}
		String raw = widget.getText();
		if (raw != null && !raw.trim().isEmpty())
		{
			String clean = Text.removeTags(raw.replaceAll("(?i)<br\\s*/?>", " ")).replace('\u00a0', ' ').trim();
			if (!clean.isEmpty())
			{
				data.lines.add(new WidgetLine(clean, bounds == null ? 0 : bounds.x, bounds == null ? 0 : bounds.y));
			}
		}
		collectChildren(widget.getStaticChildren(), data, visited);
		collectChildren(widget.getDynamicChildren(), data, visited);
		collectChildren(widget.getNestedChildren(), data, visited);
	}

	private void collectChildren(Widget[] children, InterfaceData data, Set<Widget> visited)
	{
		if (children == null)
		{
			return;
		}
		for (Widget child : children)
		{
			collect(child, data, visited);
		}
	}

	private static Rectangle findRowBounds(List<Rectangle> widgetBounds, Rectangle interfaceBounds,
		List<TaskLine> taskLines, int index)
	{
		TaskLine task = taskLines.get(index);
		Rectangle container = widgetBounds.stream()
			.filter(bounds -> bounds.width >= Math.max(220, interfaceBounds.width * 0.6))
			.filter(bounds -> bounds.height >= 65 && bounds.height <= 190)
			.filter(bounds -> bounds.x <= task.x && bounds.x + bounds.width >= task.x + 20)
			.filter(bounds -> bounds.y <= task.y && bounds.y + bounds.height >= task.y + 20)
			.min(Comparator.comparingLong(bounds -> (long) bounds.width * bounds.height))
			.orElse(null);
		if (container != null)
		{
			return new Rectangle(container);
		}

		int horizontalInset = Math.max(5, interfaceBounds.width / 100);
		int top = Math.max(interfaceBounds.y, task.y - 18);
		int averageGap = 140;
		if (taskLines.size() > 1)
		{
			int total = 0;
			for (int taskIndex = 1; taskIndex < taskLines.size(); taskIndex++)
			{
				total += taskLines.get(taskIndex).y - taskLines.get(taskIndex - 1).y;
			}
			averageGap = Math.max(80, total / (taskLines.size() - 1));
		}
		int bottom = index + 1 < taskLines.size()
			? taskLines.get(index + 1).y - 18 : top + averageGap;
		int interfaceBottom = interfaceBounds.y + interfaceBounds.height;
		if (interfaceBottom - top >= 70)
		{
			bottom = Math.min(bottom, interfaceBottom - 4);
		}
		return new Rectangle(interfaceBounds.x + horizontalInset, top,
			Math.max(1, interfaceBounds.width - horizontalInset * 2), Math.max(65, bottom - top));
	}

	private static Rectangle union(List<Rectangle> bounds)
	{
		if (bounds.isEmpty())
		{
			return new Rectangle();
		}
		Rectangle union = new Rectangle(bounds.get(0));
		for (int index = 1; index < bounds.size(); index++)
		{
			union.add(bounds.get(index));
		}
		return union;
	}

	private static final class InterfaceData
	{
		private final List<WidgetLine> lines = new ArrayList<>();
		private final List<Rectangle> bounds = new ArrayList<>();
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
		private final int x;
		private final int y;

		private TaskLine(HeartTask task, int x, int y)
		{
			this.task = task;
			this.x = x;
			this.y = y;
		}
	}
}
