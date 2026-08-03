package com.mortimer.heart;

final class TaskPaceProfile
{
	static final TaskPaceProfile DEFAULT = new TaskPaceProfile(0.0, 0, TaskPreference.STANDARD);

	private final double manualKillsPerHour;
	private final int travelSeconds;
	private final TaskPreference preference;

	TaskPaceProfile(double manualKillsPerHour, int travelSeconds, TaskPreference preference)
	{
		this.manualKillsPerHour = Math.max(0.0, manualKillsPerHour);
		this.travelSeconds = Math.max(0, travelSeconds);
		this.preference = preference == null ? TaskPreference.STANDARD : preference;
	}

	double getManualKillsPerHour() { return manualKillsPerHour; }
	int getTravelSeconds() { return travelSeconds; }
	TaskPreference getPreference() { return preference; }

	boolean isDefault()
	{
		return manualKillsPerHour <= 0.0 && travelSeconds == 0 && preference == TaskPreference.STANDARD;
	}
}
