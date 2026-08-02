package com.mortimer.heart;

final class MortimerDetectedOffer
{
	private final HeartTask task;
	private final int amount;
	private final double dropModifier;
	private final String modifierText;

	MortimerDetectedOffer(HeartTask task, int amount, double dropModifier, String modifierText)
	{
		this.task = task;
		this.amount = amount;
		this.dropModifier = dropModifier;
		this.modifierText = modifierText;
	}

	HeartTask getTask() { return task; }
	int getAmount() { return amount; }
	double getDropModifier() { return dropModifier; }
	String getModifierText() { return modifierText; }
}
