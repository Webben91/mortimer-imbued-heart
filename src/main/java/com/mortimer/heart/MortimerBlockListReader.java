package com.mortimer.heart;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.util.Text;

final class MortimerBlockListReader
{
	Result read(Client client)
	{
		List<Entry> entries = new ArrayList<>();
		Set<Widget> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		for (Widget root : client.getWidgetRoots())
		{
			collect(root, entries, visited);
		}
		String allText = entries.stream().map(entry -> entry.text).reduce("", (left, right) -> left + " " + right);
		long taskCount = HeartData.TASKS.stream().filter(task -> HeartData.textContainsTask(allText, task)).count();
		String lower = allText.toLowerCase(Locale.ROOT);
		if (taskCount < 2 || (!lower.contains("block list") && !lower.contains("blocked task")
			&& !(lower.contains("mortimer") && lower.contains("block"))))
		{
			return null;
		}

		Set<String> blocked = new LinkedHashSet<>();
		Set<String> unblocked = new LinkedHashSet<>();
		for (HeartTask task : HeartData.TASKS)
		{
			for (Entry taskEntry : entries)
			{
				if (!HeartData.textContainsTask(taskEntry.text, task))
				{
					continue;
				}
				boolean markedBlocked = false;
				boolean markedAvailable = false;
				for (Entry nearby : entries)
				{
					if (!sameRow(taskEntry.bounds, nearby.bounds))
					{
						continue;
					}
					String marker = (nearby.text + " " + nearby.actions).toLowerCase(Locale.ROOT);
					markedBlocked |= marker.contains("unblock") || marker.contains("remove block")
						|| marker.contains("already blocked") || isBlockedRed(nearby.textColor);
					markedAvailable |= !marker.contains("unblock") && (marker.contains("block task")
						|| marker.matches(".*\\bblock\\b.*"));
				}
				if (markedBlocked)
				{
					blocked.add(task.getName());
				}
				else if (markedAvailable)
				{
					unblocked.add(task.getName());
				}
				break;
			}
		}
		return blocked.isEmpty() && unblocked.isEmpty() ? null : new Result(blocked, unblocked);
	}

	private static boolean sameRow(Rectangle left, Rectangle right)
	{
		if (left == null || right == null)
		{
			return false;
		}
		int leftMiddle = left.y + left.height / 2;
		int rightMiddle = right.y + right.height / 2;
		return Math.abs(leftMiddle - rightMiddle) <= 18;
	}

	static boolean isBlockedRed(int color)
	{
		int red = color >> 16 & 0xff;
		int green = color >> 8 & 0xff;
		int blue = color & 0xff;
		return red >= 180 && green <= 80 && blue <= 80;
	}

	private static void collect(Widget widget, List<Entry> entries, Set<Widget> visited)
	{
		if (widget == null || widget.isHidden() || !visited.add(widget))
		{
			return;
		}
		String raw = widget.getText();
		String text = raw == null ? "" : Text.removeTags(raw.replaceAll("(?i)<br\\s*/?>", " "))
			.replace('\u00a0', ' ').trim();
		StringBuilder actions = new StringBuilder();
		if (widget.getActions() != null)
		{
			for (String action : widget.getActions())
			{
				if (action != null)
				{
					actions.append(' ').append(Text.removeTags(action));
				}
			}
		}
		if (!text.isEmpty() || actions.length() > 0)
		{
			entries.add(new Entry(text, actions.toString(), widget.getTextColor(), widget.getBounds()));
		}
		collect(widget.getStaticChildren(), entries, visited);
		collect(widget.getDynamicChildren(), entries, visited);
		collect(widget.getNestedChildren(), entries, visited);
	}

	private static void collect(Widget[] children, List<Entry> entries, Set<Widget> visited)
	{
		if (children != null)
		{
			for (Widget child : children)
			{
				collect(child, entries, visited);
			}
		}
	}

	static final class Result
	{
		private final Set<String> blocked;
		private final Set<String> unblocked;

		private Result(Set<String> blocked, Set<String> unblocked)
		{
			this.blocked = blocked;
			this.unblocked = unblocked;
		}

		Set<String> getBlocked() { return blocked; }
		Set<String> getUnblocked() { return unblocked; }
	}

	private static final class Entry
	{
		private final String text;
		private final String actions;
		private final int textColor;
		private final Rectangle bounds;

		private Entry(String text, String actions, int textColor, Rectangle bounds)
		{
			this.text = text;
			this.actions = actions;
			this.textColor = textColor;
			this.bounds = bounds == null ? new Rectangle() : new Rectangle(bounds);
		}
	}
}
