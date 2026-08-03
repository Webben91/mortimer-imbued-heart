package com.mortimer.heart;

@FunctionalInterface
interface TaskKillsPerHourProvider
{
	double applyAsDouble(HeartTask task, SuperiorOption superior);
}
