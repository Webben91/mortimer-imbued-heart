package com.mortimer.heart;

final class MortimerDetectedOffer
{
	private final HeartTask task;
	private final int amount;
	private final double dropModifier;
	private final double xpModifier;
	private final String modifierText;

	MortimerDetectedOffer(HeartTask task, int amount, double dropModifier, String modifierText)
	{
		this(task, amount, dropModifier, 0.0, modifierText);
	}

	MortimerDetectedOffer(HeartTask task, int amount, double dropModifier, double xpModifier, String modifierText)
	{
		this.task = task;
		this.amount = amount;
		this.dropModifier = dropModifier;
		this.xpModifier = xpModifier;
		this.modifierText = modifierText;
	}

	HeartTask getTask() { return task; }
	int getAmount() { return amount; }
	double getDropModifier() { return dropModifier; }
	double getXpModifier() { return xpModifier; }
	String getModifierText() { return modifierText; }
}
