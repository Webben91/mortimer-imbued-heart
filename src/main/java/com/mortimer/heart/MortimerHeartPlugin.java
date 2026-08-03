package com.mortimer.heart;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.slayer.SlayerPlugin;
import net.runelite.client.plugins.slayer.SlayerPluginService;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import okhttp3.OkHttpClient;

@Slf4j
@PluginDependency(SlayerPlugin.class)
@PluginDescriptor(
	name = "Mortimer Slayer",
	description = "Compare Mortimer Slayer offers for Imbued Heart chance, Slayer XP, or a balance of both",
	tags = {"slayer", "mortimer", "imbued heart", "experience", "drop rate", "calculator"}
)
public class MortimerHeartPlugin extends Plugin
{
	private static final String SLAYER_GROUP = "slayer";
	private static final Pattern ASSIGNMENT_STATUS = Pattern.compile(
		"you(?:'re| are) assigned to (?:kill |slay )?(.+?)(?:[;,.:]\\s*|\\s+only\\s+)(?:only\\s+)?([0-9,]+) more to go");

	@Inject private Client client;
	@Inject private ClientThread clientThread;
	@Inject private ClientToolbar clientToolbar;
	@Inject private OverlayManager overlayManager;
	@Inject private ConfigManager configManager;
	@Inject private MortimerHeartConfig config;
	@Inject private OkHttpClient httpClient;
	@Inject private ScheduledExecutorService executor;
	@Inject private SlayerPluginService slayerPluginService;

	private final MortimerWidgetReader widgetReader = new MortimerWidgetReader();
	private final MortimerBlockListReader blockListReader = new MortimerBlockListReader();
	private final MortimerRepeatChoiceReader repeatChoiceReader = new MortimerRepeatChoiceReader();
	private final MortimerRecommendationOverlay recommendationOverlay = new MortimerRecommendationOverlay();
	private final BraceletReminderOverlay braceletReminderOverlay = new BraceletReminderOverlay();
	private MortimerHeartPanel panel;
	private NavigationButton navigationButton;
	private TaskPerformanceService performance;
	private WikiDpsResolver wikiDpsResolver;
	private int gameTickCounter;
	private String lastImportSignature = "";
	private String lastRecommendationSignature = "";
	private MortimerOverlayRecommendation lastRecommendation;
	private List<GrindRecord> grindRecords = new ArrayList<>();
	private List<MortimerDetectedOffer> lastDetectedOffers = new ArrayList<>();
	private ActiveMortimerTask activeTask;
	private ActiveMortimerTask lastCompletedTask;
	private boolean eliteCa;
	private boolean slayerCape;
	private Bracelet detectedBracelet = Bracelet.NONE;
	private Set<String> blockedTasks = new LinkedHashSet<>();
	private int slayerLevel = 99;
	private int slayerPoints;
	private String measurementTaskName = "";
	private int measurementStartRemaining = -1;
	private long measurementStartedAt;
	private String loadedRsProfileKey = "";
	private PersonalPaceTask personalPaceEditorTask;
	private boolean updatingPersonalPaceEditor;

	@Override
	protected void startUp()
	{
		performance = new TaskPerformanceService(configManager);
		wikiDpsResolver = new WikiDpsResolver(httpClient);
		grindRecords = LocalGrindCodec.decode(config.localGrindData());
		activeTask = ActiveMortimerTaskCodec.decode(config.activeTaskData());
		blockedTasks = BlockedTaskCodec.decode(config.blockedTasksData());
		personalPaceEditorTask = config.personalPaceTask();
		loadPersonalPaceEditor();
		overlayManager.add(recommendationOverlay);
		overlayManager.add(braceletReminderOverlay);
		SwingUtilities.invokeLater(() ->
		{
			panel = new MortimerHeartPanel(false, config.showMonsterVariants(), config.preferredGrind(),
				this::undoLastGrindRecord,
				performance, this::selectActiveVariant, this::setActiveModifier);
			panel.setGrindSummary(GrindSummary.from(grindRecords));
			navigationButton = NavigationButton.builder()
				.tooltip("Mortimer Slayer")
				.icon(ImageUtil.loadImageResource(MortimerHeartPlugin.class, "slayer_icon.png"))
				.priority(6)
				.panel(panel)
				.build();
			clientToolbar.addNavigation(navigationButton);
		});
		clientThread.invoke(this::updateClientSnapshot);
		resolveAllWikiLinks();
		log.debug("Mortimer Slayer started in local grind mode");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(recommendationOverlay);
		overlayManager.remove(braceletReminderOverlay);
		recommendationOverlay.clear();
		braceletReminderOverlay.clear();
		if (navigationButton != null)
		{
			clientToolbar.removeNavigation(navigationButton);
		}
		panel = null;
		navigationButton = null;
		performance = null;
		wikiDpsResolver = null;
		lastImportSignature = "";
		lastRecommendationSignature = "";
		lastRecommendation = null;
		lastDetectedOffers = new ArrayList<>();
		blockedTasks = new LinkedHashSet<>();
		grindRecords = new ArrayList<>();
	}

	private void completeActiveTask()
	{
		if (activeTask == null)
		{
			return;
		}
		ActiveMortimerTask completed = activeTask;
		HeartTask completedTask = HeartData.findTask(completed.getTaskName());
		if (completedTask != null && completed.getTaskName().equals(measurementTaskName))
		{
			String learned = performance.recordLearnedPace(completedTask,
				(int) Math.round(detectedBracelet.adjustKills(Math.max(0, measurementStartRemaining))),
				Math.max(0L, System.currentTimeMillis() - measurementStartedAt));
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				configManager.setRSProfileConfiguration(MortimerHeartConfig.GROUP, "learnedPaceData", learned);
			}
		}
		lastCompletedTask = completed;
		grindRecords.add(completed.toRecord());
		activeTask = null;
		braceletReminderOverlay.clear();
		resetTaskMeasurement();
		saveActiveTask();
		saveGrindRecords();
		if (panel != null)
		{
			SwingUtilities.invokeLater(() ->
			{
				if (panel != null)
				{
					panel.setGrindSummary(GrindSummary.from(grindRecords));
					panel.setActiveTask(null, 0);
					panel.setStatus(completed.getTaskName() + " completed and added automatically.", true);
				}
			});
		}
	}

	private void cancelActiveTask(String reason)
	{
		if (activeTask == null)
		{
			return;
		}
		activeTask = null;
		lastCompletedTask = null;
		braceletReminderOverlay.clear();
		resetTaskMeasurement();
		saveActiveTask();
		if (panel != null)
		{
			SwingUtilities.invokeLater(() ->
			{
				if (panel != null)
				{
					panel.setActiveTask(null, 0);
					panel.setStatus(reason, false);
				}
			});
		}
	}

	private void undoLastGrindRecord()
	{
		if (grindRecords.isEmpty())
		{
			return;
		}
		GrindRecord removed = grindRecords.remove(grindRecords.size() - 1);
		saveGrindRecords();
		if (panel != null)
		{
			panel.setGrindSummary(GrindSummary.from(grindRecords));
			panel.setStatus("Removed the last automatic task: " + removed.getTaskName() + ".", true);
		}
	}

	private void saveGrindRecords()
	{
		String encoded = LocalGrindCodec.encode(grindRecords);
		configManager.setConfiguration(MortimerHeartConfig.GROUP, "localGrindData", encoded);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			configManager.setRSProfileConfiguration(MortimerHeartConfig.GROUP, "localGrindData", encoded);
		}
	}

	private void saveActiveTask()
	{
		String encoded = ActiveMortimerTaskCodec.encode(activeTask);
		configManager.setConfiguration(MortimerHeartConfig.GROUP, "activeTaskData", encoded);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			configManager.setRSProfileConfiguration(MortimerHeartConfig.GROUP, "activeTaskData", encoded);
		}
	}

	@Provides
	MortimerHeartConfig provideConfig(ConfigManager manager)
	{
		return manager.getConfig(MortimerHeartConfig.class);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		clientThread.invokeLater(() ->
		{
			scanMortimerBlockList();
			scanMortimerScreen();
		});
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		// Offer and Slayer-cape highlights are tied to widget bounds. Clear them as
		// soon as any interface closes so a cached rectangle cannot remain painted
		// over the game after Mortimer's dialogue has disappeared. The regular game
		// tick scan will restore the highlight if the choice interface is still open.
		recommendationOverlay.clear();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		gameTickCounter++;
		if (gameTickCounter % 2 == 0)
		{
			scanMortimerScreen();
		}
		trackSlayerAssignment();
		if (gameTickCounter % 5 == 0)
		{
			updateClientSnapshot();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}
		String message = event.getMessage().replaceAll("<[^>]+>", "").toLowerCase(Locale.ROOT);
		syncBlockedTaskFromMessage(message);
		if (syncAssignmentFromMessage(message))
		{
			return;
		}
		if (message.contains("a superior foe has appeared"))
		{
			recordSuperiorRoll();
		}
		else if (message.contains("you have completed your task") || message.contains("you've completed your task"))
		{
			completeActiveTask();
		}
		else if (message.contains("cancelled your slayer task") || message.contains("canceled your slayer task")
			|| message.contains("task has been cancelled") || message.contains("task has been canceled"))
		{
			cancelActiveTask("The active task was cancelled and was not added to the grind.");
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		if (activeTask == null || event.getSource() != client.getLocalPlayer())
		{
			return;
		}
		Actor target = event.getTarget();
		if (!(target instanceof NPC))
		{
			return;
		}
		String npcName = ((NPC) target).getName();
		HeartTask task = HeartData.findTask(activeTask.getTaskName());
		if (task == null)
		{
			return;
		}
		for (SuperiorOption superior : task.getSuperiors())
		{
			if (superior.matchesMonster(npcName))
			{
				selectActiveVariant(superior);
				return;
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			lastImportSignature = "";
			clientThread.invokeLater(() ->
			{
				loadRsProfileState();
				updateClientSnapshot();
			});
		}
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		clientThread.invokeLater(this::loadRsProfileState);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.EQUIPMENT.getId())
		{
			updateClientSnapshot();
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		switch (event.getSkill())
		{
			case ATTACK:
			case STRENGTH:
			case RANGED:
			case MAGIC:
			case SLAYER:
				updateClientSnapshot();
				break;
			default:
				break;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarbitId() == VarbitID.CA_THRESHOLD_ELITE)
		{
			updateEliteCaFromClient();
		}
		else if (event.getVarbitId() == VarbitID.SLAYER_POINTS)
		{
			updateClientSnapshot();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!MortimerHeartConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}
		if (event.getKey().endsWith("Dps"))
		{
			HeartTask task = taskForDpsKey(event.getKey());
			if (task != null)
			{
				resolveWikiLink(task);
			}
		}
		if ((event.getKey().endsWith("Dps") || event.getKey().endsWith("Cannon")) && panel != null)
		{
			SwingUtilities.invokeLater(panel::refreshCalculations);
		}
		if ("showMonsterVariants".equals(event.getKey()) && panel != null)
		{
			SwingUtilities.invokeLater(() -> panel.setShowMonsterVariants(config.showMonsterVariants()));
		}
		if ("personalPaceTask".equals(event.getKey()))
		{
			personalPaceEditorTask = config.personalPaceTask();
			loadPersonalPaceEditor();
		}
		else if ("personalKillsPerHour".equals(event.getKey())
			|| "personalTravelSeconds".equals(event.getKey())
			|| "personalTaskPreference".equals(event.getKey()))
		{
			savePersonalPaceEditor();
		}
		boolean paceChanged = "paceMode".equals(event.getKey())
			|| "preparationSeconds".equals(event.getKey())
			|| "personalPaceData".equals(event.getKey())
			|| "learnedPaceData".equals(event.getKey());
		if (paceChanged && panel != null)
		{
			lastRecommendationSignature = "";
			SwingUtilities.invokeLater(panel::refreshCalculations);
			clientThread.invokeLater(this::scanMortimerScreen);
		}
		if ("preferredGrind".equals(event.getKey()) && panel != null)
		{
			SwingUtilities.invokeLater(() -> panel.setGrindPreference(config.preferredGrind()));
		}
		boolean manualBlockChanged = "manualBlockedTaskOne".equals(event.getKey())
			|| "manualBlockedTaskTwo".equals(event.getKey());
		boolean pointReserveChanged = "slayerPointReserve".equals(event.getKey());
		if (manualBlockChanged || pointReserveChanged)
		{
			lastRecommendationSignature = "";
			updatePanelRoutingContext();
		}
		if ("showMonsterVariants".equals(event.getKey()) || "preferredGrind".equals(event.getKey())
			|| manualBlockChanged || pointReserveChanged)
		{
			clientThread.invokeLater(this::scanMortimerScreen);
		}
		if ("braceletReminder".equals(event.getKey()))
		{
			clientThread.invokeLater(this::refreshBraceletReminder);
		}
	}

	private void loadPersonalPaceEditor()
	{
		if (updatingPersonalPaceEditor || personalPaceEditorTask == null)
		{
			return;
		}
		TaskPaceProfile profile = TaskPaceProfileCodec.decode(config.personalPaceData())
			.getOrDefault(personalPaceEditorTask.getTaskName(), TaskPaceProfile.DEFAULT);
		updatingPersonalPaceEditor = true;
		try
		{
			configManager.setConfiguration(MortimerHeartConfig.GROUP, "personalKillsPerHour",
				profile.getManualKillsPerHour());
			configManager.setConfiguration(MortimerHeartConfig.GROUP, "personalTravelSeconds",
				profile.getTravelSeconds());
			configManager.setConfiguration(MortimerHeartConfig.GROUP, "personalTaskPreference",
				profile.getPreference());
		}
		finally
		{
			updatingPersonalPaceEditor = false;
		}
	}

	private void savePersonalPaceEditor()
	{
		if (updatingPersonalPaceEditor || personalPaceEditorTask == null
			|| config.personalPaceTask() != personalPaceEditorTask)
		{
			return;
		}
		double kph = Math.max(0.0, Math.min(5_000.0, config.personalKillsPerHour()));
		int travel = Math.max(0, Math.min(3_600, config.personalTravelSeconds()));
		TaskPaceProfile profile = new TaskPaceProfile(kph, travel, config.personalTaskPreference());
		Map<String, TaskPaceProfile> profiles = TaskPaceProfileCodec.decode(config.personalPaceData());
		if (profile.isDefault())
		{
			profiles.remove(personalPaceEditorTask.getTaskName());
		}
		else
		{
			profiles.put(personalPaceEditorTask.getTaskName(), profile);
		}
		String encoded = TaskPaceProfileCodec.encode(profiles);
		if (!encoded.equals(config.personalPaceData()))
		{
			configManager.setConfiguration(MortimerHeartConfig.GROUP, "personalPaceData", encoded);
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				configManager.setRSProfileConfiguration(MortimerHeartConfig.GROUP, "personalPaceData", encoded);
			}
		}
		lastRecommendationSignature = "";
		if (panel != null)
		{
			SwingUtilities.invokeLater(panel::refreshCalculations);
		}
		clientThread.invokeLater(this::scanMortimerScreen);
	}

	private void saveBlockedTasks()
	{
		String encoded = BlockedTaskCodec.encode(blockedTasks);
		configManager.setConfiguration(MortimerHeartConfig.GROUP, "blockedTasksData", encoded);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			configManager.setRSProfileConfiguration(MortimerHeartConfig.GROUP, "blockedTasksData", encoded);
		}
	}

	private void scanMortimerScreen()
	{
		if (lastCompletedTask != null)
		{
			MortimerRepeatChoice repeatChoice = repeatChoiceReader.read(client);
			if (repeatChoice != null)
			{
				boolean accept = shouldRepeat(lastCompletedTask);
				recommendationOverlay.show(repeatChoice.recommendedBounds(accept),
					accept ? MortimerOverlayRecommendation.Style.HEART : MortimerOverlayRecommendation.Style.POINT_SKIP,
					accept ? "REPEAT · STRONG HEART" : "DECLINE · NEW OFFERS");
				return;
			}
		}
		List<MortimerOfferPlacement> placements = widgetReader.readScreen(client);
		if (placements.isEmpty())
		{
			recommendationOverlay.clear();
			return;
		}
		List<MortimerDetectedOffer> offers = placements.stream()
			.map(MortimerOfferPlacement::getOffer).collect(Collectors.toList());
		Set<String> effectiveBlocks = effectiveBlockedTasks();
		String recommendationSignature = config.preferredGrind() + ":" + config.showMonsterVariants()
			+ ":" + eliteCa + ":" + slayerLevel + ":" + spendableSlayerPoints() + ":" + slayerCape
			+ ":" + BlockedTaskCodec.encode(effectiveBlocks) + ":"
			+ offers.stream().map(offer -> offer.getTask().getName() + ':' + offer.getAmount()
				+ ':' + offer.getDropModifier() + ':' + offer.getXpModifier()
				+ ':' + performance.killsPerHour(offer.getTask())
				+ ':' + performance.overheadHours(offer.getTask())
				+ ':' + performance.preference(offer.getTask())).collect(Collectors.joining("|"));
		if (!recommendationSignature.equals(lastRecommendationSignature))
		{
			lastRecommendationSignature = recommendationSignature;
			lastRecommendation = MortimerOverlayRecommendationCalculator.calculate(
				offers, config.showMonsterVariants(), config.preferredGrind(), eliteCa,
				slayerLevel, spendableSlayerPoints(), effectiveBlocks, slayerCape, performance::killsPerHour,
				(task, amount) -> performance.overheadHours(task), performance::preference);
		}
		recommendationOverlay.show(placements, lastRecommendation);
		if (!offers.isEmpty())
		{
			importDetectedOffers(offers, false);
		}
	}

	private void importDetectedOffers(List<MortimerDetectedOffer> offers, boolean force)
	{
		String signature = offers.stream().map(offer -> offer.getTask().getName() + ':' + offer.getAmount()
			+ ':' + offer.getDropModifier() + ':' + offer.getXpModifier()).collect(Collectors.joining("|"));
		if (!force && signature.equals(lastImportSignature))
		{
			return;
		}
		lastImportSignature = signature;
		lastCompletedTask = null;
		lastDetectedOffers = new ArrayList<>(offers);
		if (panel != null)
		{
			SwingUtilities.invokeLater(() -> panel.importOffers(offers));
		}
	}

	private void scanMortimerBlockList()
	{
		MortimerBlockListReader.Result detected = blockListReader.read(client);
		if (detected == null)
		{
			return;
		}
		Set<String> updated = new LinkedHashSet<>(blockedTasks);
		updated.addAll(detected.getBlocked());
		updated.removeAll(detected.getUnblocked());
		if (updated.equals(blockedTasks))
		{
			return;
		}
		blockedTasks = updated;
		saveBlockedTasks();
		lastRecommendationSignature = "";
		updatePanelRoutingContext();
	}

	private void syncBlockedTaskFromMessage(String message)
	{
		boolean removal = message.contains("unblocked") || message.contains("removed from")
			|| message.contains("will assign") || message.contains("can assign");
		boolean addition = !removal && !message.contains("cannot") && !message.contains("can't")
			&& (message.contains("have blocked") || message.contains("now blocked")
				|| message.contains("will no longer assign"));
		if (!removal && !addition)
		{
			return;
		}
		for (HeartTask task : HeartData.TASKS)
		{
			if (!HeartData.textContainsTask(message, task))
			{
				continue;
			}
			boolean changed = removal ? blockedTasks.remove(task.getName()) : blockedTasks.add(task.getName());
			if (changed)
			{
				saveBlockedTasks();
				lastRecommendationSignature = "";
				updatePanelRoutingContext();
			}
			return;
		}
	}

	private boolean shouldRepeat(ActiveMortimerTask task)
	{
		if (effectiveBlockedTasks().contains(task.getTaskName()))
		{
			return false;
		}
		OfferState offer = activeOffer(task);
		return offer != null && HeartCalculator.calculate(offer, eliteCa).getHoursOnRate()
			<= OptimalRoutingCalculator.TARGET_HEART_HOURS;
	}

	private OfferState activeOffer(ActiveMortimerTask task)
	{
		if (task == null || performance == null)
		{
			return null;
		}
		HeartTask heartTask = HeartData.findTask(task.getTaskName());
		if (heartTask == null)
		{
			return null;
		}
		SuperiorOption superior = findSuperior(heartTask, task.getSuperiorName());
		double kph = superior.effectiveKillsPerHour(performance.killsPerHour(heartTask));
		return new OfferState(heartTask, superior, task.getAssignedAmount(),
			Math.max(0.0, task.getDropModifier()), 0.0, kph,
			performance.overheadHours(heartTask), Bracelet.NONE);
	}

	private static SuperiorOption findSuperior(HeartTask task, String name)
	{
		for (SuperiorOption superior : task.getSuperiors())
		{
			if (superior.getName().equals(name))
			{
				return superior;
			}
		}
		return task.getSuperiors().get(0);
	}

	private void refreshBraceletReminder()
	{
		if (!config.braceletReminder() || activeTask == null)
		{
			braceletReminderOverlay.clear();
			return;
		}
		OfferState offer = activeOffer(activeTask);
		if (offer == null)
		{
			braceletReminderOverlay.clear();
			return;
		}
		Bracelet recommended = BraceletAdvisor.recommend(offer, eliteCa);
		if (recommended == detectedBracelet)
		{
			braceletReminderOverlay.clear();
		}
		else
		{
			braceletReminderOverlay.show(recommended);
		}
	}

	private void updatePanelRoutingContext()
	{
		if (panel != null)
		{
			Set<String> snapshot = effectiveBlockedTasks();
			SwingUtilities.invokeLater(() ->
			{
				if (panel != null)
				{
					panel.setRoutingContext(slayerLevel, spendableSlayerPoints(), slayerCape, snapshot);
				}
			});
		}
	}

	private Set<String> effectiveBlockedTasks()
	{
		Set<String> effective = new LinkedHashSet<>(blockedTasks);
		addManualBlock(effective, config.manualBlockedTaskOne());
		addManualBlock(effective, config.manualBlockedTaskTwo());
		return effective;
	}

	private int spendableSlayerPoints()
	{
		return Math.max(0, slayerPoints - Math.max(0, config.slayerPointReserve()));
	}

	private static void addManualBlock(Set<String> tasks, ManualBlockedTask manualBlock)
	{
		if (manualBlock != null && manualBlock.getTaskName() != null)
		{
			tasks.add(manualBlock.getTaskName());
		}
	}

	private void trackSlayerAssignment()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		String taskName = slayerPluginService.getTask();
		int assigned = slayerPluginService.getInitialAmount();
		int remaining = slayerPluginService.getRemainingAmount();
		if (taskName == null || taskName.trim().isEmpty() || assigned <= 0)
		{
			taskName = profileValue("taskName");
			assigned = profileInt("initialAmount");
			remaining = profileInt("amount");
		}
		HeartTask current = HeartData.findTask(taskName);
		if (current != null && assigned > 0)
		{
			MortimerDetectedOffer selected = lastDetectedOffers.stream()
				.filter(offer -> offer.getTask().getName().equals(current.getName()))
				.findFirst().orElse(null);
			boolean taskChanged = activeTask == null || !activeTask.getTaskName().equals(current.getName());
			if (taskChanged && (activeTask == null || selected != null))
			{
				activeTask = createActiveTask(current, assigned, selected);
				resetTaskMeasurement();
				saveActiveTask();
				lastDetectedOffers = new ArrayList<>();
				lastImportSignature = "";
				if (panel != null)
				{
					SwingUtilities.invokeLater(() -> panel.setStatus(current.getName()
						+ " accepted. Completion will be recorded automatically.", true));
				}
			}
			else if (activeTask != null && activeTask.getTaskName().equals(current.getName())
				&& assigned >= remaining && assigned != activeTask.getAssignedAmount())
			{
				activeTask = activeTask.withAssignedAmount(assigned);
				saveActiveTask();
			}
		}
		if (panel != null && activeTask != null)
		{
			String displayName = activeTask.getTaskName();
			int displayAssigned = activeTask.getAssignedAmount();
			int displayRemaining = current != null && current.getName().equals(displayName) ? remaining : displayAssigned;
			updateTaskMeasurement(displayName, displayRemaining);
			double actualDps = current == null || !current.getName().equals(displayName) ? 0.0 : performance.measuredDps(current,
				detectedBracelet.adjustKills(Math.max(0, measurementStartRemaining - displayRemaining)),
				Math.max(0L, System.currentTimeMillis() - measurementStartedAt));
			ActiveMortimerTask taskSnapshot = activeTask;
			SwingUtilities.invokeLater(() ->
			{
				if (panel != null)
				{
					panel.setActiveTask(taskSnapshot, displayRemaining);
					panel.setActualDps(actualDps);
				}
			});
			refreshBraceletReminder();
		}
	}

	private boolean syncAssignmentFromMessage(String message)
	{
		Matcher matcher = ASSIGNMENT_STATUS.matcher(message);
		if (!matcher.find())
		{
			return false;
		}
		HeartTask detectedTask = HeartData.findTask(matcher.group(1));
		if (detectedTask == null)
		{
			return false;
		}
		int remaining;
		try
		{
			remaining = Integer.parseInt(matcher.group(2).replace(",", ""));
		}
		catch (NumberFormatException ignored)
		{
			return false;
		}
		MortimerDetectedOffer selected = lastDetectedOffers.stream()
			.filter(offer -> offer.getTask().getName().equals(detectedTask.getName()))
			.findFirst().orElse(null);
		int profileAssigned = profileInt("initialAmount");
		HeartTask profileTask = HeartData.findTask(profileValue("taskName"));
		int assigned = profileTask != null && profileTask.getName().equals(detectedTask.getName())
			&& profileAssigned >= remaining ? profileAssigned : remaining;
		if (activeTask == null || !activeTask.getTaskName().equals(detectedTask.getName()))
		{
			activeTask = createActiveTask(detectedTask, assigned, selected);
			resetTaskMeasurement();
			saveActiveTask();
			lastDetectedOffers = new ArrayList<>();
			lastImportSignature = "";
		}
		ActiveMortimerTask snapshot = activeTask;
		if (panel != null)
		{
			SwingUtilities.invokeLater(() ->
			{
				if (panel != null)
				{
					panel.setActiveTask(snapshot, remaining);
				}
			});
		}
		refreshBraceletReminder();
		return true;
	}

	private ActiveMortimerTask createActiveTask(HeartTask task, int assigned, MortimerDetectedOffer selected)
	{
		SuperiorOption superior = task.getSuperiors().get(0);
		double modifier = selected == null ? -1.0 : selected.getDropModifier();
		if (selected == null && lastCompletedTask != null
			&& task.getName().equals(lastCompletedTask.getTaskName()))
		{
			for (SuperiorOption candidate : task.getSuperiors())
			{
				if (candidate.getName().equals(lastCompletedTask.getSuperiorName()))
				{
					superior = candidate;
					break;
				}
			}
			modifier = lastCompletedTask.getDropModifier();
		}
		lastCompletedTask = null;
		return new ActiveMortimerTask(task.getName(), superior.getName(), assigned,
			superior.getHeartRate(), modifier, eliteCa ? 150.0 : 200.0);
	}

	private void selectActiveVariant(SuperiorOption superior)
	{
		if (activeTask == null || superior == null || superior.getName().equals(activeTask.getSuperiorName()))
		{
			return;
		}
		HeartTask task = HeartData.findTask(activeTask.getTaskName());
		if (task == null || !task.getSuperiors().contains(superior))
		{
			return;
		}
		activeTask = activeTask.withSuperior(superior);
		saveActiveTask();
		refreshBraceletReminder();
		int remaining = profileInt("amount");
		ActiveMortimerTask snapshot = activeTask;
		if (panel != null)
		{
			SwingUtilities.invokeLater(() ->
			{
				if (panel != null)
				{
					panel.setActiveTask(snapshot, remaining);
				}
			});
		}
	}

	private void setActiveModifier(Double modifier)
	{
		if (activeTask == null || modifier == null || !Double.isFinite(modifier) || modifier < 0.0)
		{
			return;
		}
		activeTask = activeTask.withDropModifier(modifier);
		saveActiveTask();
		refreshBraceletReminder();
		int remaining = slayerPluginService.getRemainingAmount();
		if (remaining <= 0)
		{
			remaining = profileInt("amount");
		}
		ActiveMortimerTask snapshot = activeTask;
		int displayRemaining = remaining > 0 ? remaining : snapshot.getAssignedAmount();
		if (panel != null)
		{
			SwingUtilities.invokeLater(() ->
			{
				if (panel != null)
				{
					panel.setActiveTask(snapshot, displayRemaining);
				}
			});
		}
	}

	private void updateTaskMeasurement(String taskName, int remaining)
	{
		if (!taskName.equals(measurementTaskName) || measurementStartRemaining < 0)
		{
			measurementTaskName = taskName;
			measurementStartRemaining = remaining;
			measurementStartedAt = System.currentTimeMillis();
		}
	}

	private void resetTaskMeasurement()
	{
		measurementTaskName = "";
		measurementStartRemaining = -1;
		measurementStartedAt = 0L;
	}

	private void loadRsProfileState()
	{
		String profileKey = configManager.getRSProfileKey();
		if (profileKey == null || profileKey.isEmpty() || profileKey.equals(loadedRsProfileKey))
		{
			return;
		}
		loadedRsProfileKey = profileKey;
		String remoteGrind = configManager.getRSProfileConfiguration(MortimerHeartConfig.GROUP, "localGrindData");
		String remoteActive = configManager.getRSProfileConfiguration(MortimerHeartConfig.GROUP, "activeTaskData");
		String remoteBlocks = configManager.getRSProfileConfiguration(MortimerHeartConfig.GROUP, "blockedTasksData");
		String remotePersonalPace = configManager.getRSProfileConfiguration(MortimerHeartConfig.GROUP, "personalPaceData");
		String remoteLearnedPace = configManager.getRSProfileConfiguration(MortimerHeartConfig.GROUP, "learnedPaceData");
		if (remoteGrind == null || remoteGrind.trim().isEmpty())
		{
			saveGrindRecords();
		}
		else
		{
			grindRecords = LocalGrindCodec.decode(remoteGrind);
			configManager.setConfiguration(MortimerHeartConfig.GROUP, "localGrindData", remoteGrind);
		}
		if (remoteActive == null || remoteActive.trim().isEmpty())
		{
			saveActiveTask();
		}
		else
		{
			activeTask = ActiveMortimerTaskCodec.decode(remoteActive);
			configManager.setConfiguration(MortimerHeartConfig.GROUP, "activeTaskData", remoteActive);
			resetTaskMeasurement();
		}
		if (remoteBlocks == null || remoteBlocks.trim().isEmpty())
		{
			saveBlockedTasks();
		}
		else
		{
			blockedTasks = BlockedTaskCodec.decode(remoteBlocks);
			configManager.setConfiguration(MortimerHeartConfig.GROUP, "blockedTasksData", remoteBlocks);
		}
		if (remotePersonalPace == null || remotePersonalPace.trim().isEmpty())
		{
			configManager.setRSProfileConfiguration(MortimerHeartConfig.GROUP, "personalPaceData",
				config.personalPaceData());
		}
		else
		{
			configManager.setConfiguration(MortimerHeartConfig.GROUP, "personalPaceData", remotePersonalPace);
		}
		if (remoteLearnedPace == null || remoteLearnedPace.trim().isEmpty())
		{
			configManager.setRSProfileConfiguration(MortimerHeartConfig.GROUP, "learnedPaceData",
				config.learnedPaceData());
		}
		else
		{
			configManager.setConfiguration(MortimerHeartConfig.GROUP, "learnedPaceData", remoteLearnedPace);
		}
		loadPersonalPaceEditor();
		if (panel != null)
		{
			GrindSummary summary = GrindSummary.from(grindRecords);
			ActiveMortimerTask taskSnapshot = activeTask;
			int remaining = slayerPluginService.getRemainingAmount();
			SwingUtilities.invokeLater(() ->
			{
				if (panel != null)
				{
					panel.setGrindSummary(summary);
					panel.setActiveTask(taskSnapshot, taskSnapshot == null ? 0
						: remaining > 0 ? remaining : taskSnapshot.getAssignedAmount());
					panel.setRoutingContext(slayerLevel, spendableSlayerPoints(), slayerCape, effectiveBlockedTasks());
				}
			});
		}
		refreshBraceletReminder();
	}

	private void recordSuperiorRoll()
	{
		if (activeTask == null)
		{
			return;
		}
		activeTask = activeTask.withSuperiorRoll();
		saveActiveTask();
		int remaining = profileInt("amount");
		ActiveMortimerTask snapshot = activeTask;
		if (panel != null)
		{
			SwingUtilities.invokeLater(() ->
			{
				if (panel != null)
				{
					panel.setActiveTask(snapshot, remaining);
				}
			});
		}
	}

	private String profileValue(String key)
	{
		String value = configManager.getRSProfileConfiguration(SLAYER_GROUP, key);
		return value == null ? "" : value;
	}

	private int profileInt(String key)
	{
		try
		{
			return Integer.parseInt(profileValue(key));
		}
		catch (NumberFormatException ignored)
		{
			return 0;
		}
	}

	private void resolveAllWikiLinks()
	{
		for (HeartTask task : HeartData.TASKS)
		{
			resolveWikiLink(task);
		}
	}

	private void resolveWikiLink(HeartTask task)
	{
		if (performance == null || wikiDpsResolver == null)
		{
			return;
		}
		String link = performance.wikiLink(task);
		if (link.isEmpty())
		{
			return;
		}
		executor.execute(() ->
		{
			try
			{
				WikiDpsResolver.Result result = wikiDpsResolver.resolve(link, task);
				performance.saveResolved(task, link, result.getEffectiveDps());
				log.debug("Resolved Wiki DPS for {} against {} at {} effective DPS ({}x)", task.getName(),
					result.getMonsterName(), result.getEffectiveDps(), result.getMultiplier());
				if (panel != null)
				{
					SwingUtilities.invokeLater(panel::refreshCalculations);
				}
			}
			catch (Exception ex)
			{
				log.warn("Could not resolve Wiki DPS for {}: {}", task.getName(), ex.getMessage());
			}
		});
	}

	private static HeartTask taskForDpsKey(String key)
	{
		for (HeartTask task : HeartData.TASKS)
		{
			if ((TaskPerformanceService.key(task) + "Dps").equals(key))
			{
				return task;
			}
		}
		return null;
	}

	private void updateClientSnapshot()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		slayerLevel = Math.max(1, client.getRealSkillLevel(Skill.SLAYER));
		slayerPoints = Math.max(0, client.getVarbitValue(VarbitID.SLAYER_POINTS));
		updateEliteCaFromClient();
		if (panel == null)
		{
			return;
		}
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		int equippedCount = equipment == null ? 0 : equipment.count();
		detectedBracelet = detectBracelet(equipment);
		slayerCape = detectSlayerCape(equipment);
		int attack = client.getRealSkillLevel(Skill.ATTACK);
		int strength = client.getRealSkillLevel(Skill.STRENGTH);
		int ranged = client.getRealSkillLevel(Skill.RANGED);
		int magic = client.getRealSkillLevel(Skill.MAGIC);
		SwingUtilities.invokeLater(() ->
		{
			if (panel != null)
			{
				panel.setClientSnapshot(attack, strength, ranged, magic, equippedCount);
				panel.setDetectedBracelet(detectedBracelet);
				panel.setRoutingContext(slayerLevel, spendableSlayerPoints(), slayerCape, effectiveBlockedTasks());
			}
		});
		refreshBraceletReminder();
	}

	private void updateEliteCaFromClient()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		boolean detected = client.getVarbitValue(VarbitID.CA_THRESHOLD_ELITE) != 0;
		if (detected == eliteCa)
		{
			return;
		}
		eliteCa = detected;
		lastRecommendationSignature = "";
		refreshBraceletReminder();
		if (panel != null)
		{
			SwingUtilities.invokeLater(() ->
			{
				if (panel != null)
				{
					panel.setEliteCa(detected);
				}
			});
		}
	}

	private static Bracelet detectBracelet(ItemContainer equipment)
	{
		if (equipment == null)
		{
			return Bracelet.NONE;
		}
		for (Item item : equipment.getItems())
		{
			if (item.getId() == ItemID.EXPEDITIOUS_BRACELET)
			{
				return Bracelet.EXPEDITIOUS;
			}
			if (item.getId() == ItemID.BRACELET_OF_SLAUGHTER)
			{
				return Bracelet.SLAUGHTER;
			}
		}
		return Bracelet.NONE;
	}

	private boolean detectSlayerCape(ItemContainer equipment)
	{
		if (equipment == null)
		{
			return false;
		}
		for (Item item : equipment.getItems())
		{
			int id = item.getId();
			if (id == net.runelite.api.ItemID.SLAYER_CAPE
				|| id == net.runelite.api.ItemID.SLAYER_CAPET
				|| id == net.runelite.api.ItemID.MAX_CAPE)
			{
				return true;
			}
			String name = client.getItemDefinition(id).getName().toLowerCase(Locale.ROOT);
			if (name.contains("slayer cape") || name.contains("max cape"))
			{
				return true;
			}
		}
		return false;
	}

}
