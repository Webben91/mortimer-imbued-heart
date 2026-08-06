package com.mortimer.heart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

final class MortimerHeartPanel extends PluginPanel
{
	private static final Color GOLD = new Color(214, 168, 63);
	private static final Color PALE_GOLD = new Color(242, 218, 155);
	private static final Color DEEP_GREEN = new Color(29, 50, 35);
	private static final Color MUTED = new Color(170, 164, 148);
	private static final Color BORDER = new Color(74, 71, 64);
	private static final Color SUCCESS = new Color(133, 190, 137);
	private static final int CARD_TEXT_WIDTH = 164;
	private static final int ACTIVE_TEXT_WIDTH = 180;

	private final JPanel flow = new JPanel();
	private final JPanel content = new JPanel();
	private final JPanel offersContainer = new JPanel();
	private final JPanel offersHeader = new JPanel(new BorderLayout());
	private final JPanel routingCard;
	private final JLabel routingTitle = new JLabel();
	private final JLabel routingDetails = new JLabel();
	private final JPanel grindCard;
	private final JPanel emptyCard;
	private final JPanel activeTaskCard;
	private final JPanel activeTotalCard;
	private final List<OfferCard> offerCards = new ArrayList<>();
	private final JLabel clientSnapshot = new JLabel("Waiting for the logged-in client", SwingConstants.CENTER);
	private final JLabel rulesSnapshot = new JLabel("", SwingConstants.CENTER);
	private final JLabel activeTaskTitle = new JLabel();
	private final JLabel activeTaskName = new JLabel();
	private final JLabel activeTaskSummary = new JLabel();
	private final JPanel activeTaskResult = new JPanel();
	private final JLabel expectedDpsValue = new JLabel();
	private final JLabel actualDpsValue = new JLabel();
	private final JLabel taskChanceValue = new JLabel();
	private final JLabel superiorCountValue = new JLabel();
	private final JLabel expectedSuperiorsValue = new JLabel();
	private final JLabel overallChanceValue = new JLabel();
	private final JLabel timeRemainingValue = new JLabel();
	private final JLabel activeTotalChance = new JLabel("0.0000%", SwingConstants.CENTER);
	private final JLabel activeTotalDetails = new JLabel("", SwingConstants.CENTER);
	private final JComboBox<SuperiorOption> activeVariantSelector = new JComboBox<>();
	private final JButton manualModifierButton = new JButton("Enter Mortimer modifier");
	private final JLabel grindChance = new JLabel("0.0000%", SwingConstants.CENTER);
	private final JLabel grindDetails = new JLabel("No completed Mortimer tasks", SwingConstants.CENTER);
	private final JButton undoGrindButton = new JButton("Undo last automatic task");
	private final JLabel status = new JLabel();
	private final Runnable undoLastGrind;
	private final TaskPerformanceService performance;
	private final Consumer<SuperiorOption> variantSelectionAction;
	private final Consumer<Double> modifierSelectionAction;
	private final BiPredicate<HeartTask, SuperiorOption> wildernessTask;
	private boolean eliteCa;
	private Bracelet detectedBracelet = Bracelet.NONE;
	private ActiveMortimerTask currentActiveTask;
	private int currentRemaining;
	private GrindSummary currentGrindSummary = GrindSummary.from(new ArrayList<>());
	private List<MortimerDetectedOffer> lastDetectedOffers = new ArrayList<>();
	private boolean showMonsterVariants;
	private boolean updatingVariantSelector;
	private double currentActualDps;
	private int slayerLevel = 99;
	private int slayerPoints;
	private boolean slayerCape;
	private Set<String> blockedTasks = Collections.emptySet();
	private GrindPreference grindPreference;

	MortimerHeartPanel(boolean eliteCombatAchievements, boolean showMonsterVariants, GrindPreference grindPreference,
		Runnable undoLastGrind,
		TaskPerformanceService performance, Consumer<SuperiorOption> variantSelectionAction,
		Consumer<Double> modifierSelectionAction,
		BiPredicate<HeartTask, SuperiorOption> wildernessTask)
	{
		this.eliteCa = eliteCombatAchievements;
		this.showMonsterVariants = showMonsterVariants;
		this.grindPreference = grindPreference == null ? GrindPreference.IMBUED_HEART : grindPreference;
		this.undoLastGrind = undoLastGrind;
		this.performance = performance;
		this.variantSelectionAction = variantSelectionAction;
		this.modifierSelectionAction = modifierSelectionAction;
		this.wildernessTask = wildernessTask;
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBackground(ColorScheme.DARK_GRAY_COLOR);
		content.setBorder(new EmptyBorder(8, 4, 8, 4));

		JLabel eyebrow = label("MORTIMER SLAYER", GOLD, FontManager.getRunescapeSmallFont());
		eyebrow.setAlignmentX(LEFT_ALIGNMENT);
		content.add(eyebrow);
		JLabel title = label("Offer Planner", Color.WHITE, FontManager.getRunescapeBoldFont());
		title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
		title.setAlignmentX(LEFT_ALIGNMENT);
		content.add(title);
		content.add(Box.createVerticalStrut(8));

		JPanel clientCard = card();
		JPanel clientBody = verticalBody();
		styleCentered(clientSnapshot, Color.WHITE, FontManager.getRunescapeSmallFont());
		clientBody.add(clientSnapshot);
		styleCentered(rulesSnapshot, MUTED, FontManager.getRunescapeSmallFont());
		clientBody.add(rulesSnapshot);
		clientCard.add(clientBody, BorderLayout.CENTER);
		content.add(clientCard);
		content.add(Box.createVerticalStrut(9));

		offersHeader.setOpaque(false);
		offersHeader.add(label("MORTIMER OFFERS", GOLD, FontManager.getRunescapeSmallFont()), BorderLayout.WEST);
		offersHeader.setAlignmentX(LEFT_ALIGNMENT);
		offersHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
		content.add(offersHeader);
		content.add(Box.createVerticalStrut(5));

		status.setFont(FontManager.getRunescapeSmallFont());
		status.setAlignmentX(LEFT_ALIGNMENT);
		setStatus("Open Mortimer's task-choice interface. Offers are read directly from the game.", true);
		content.add(status);
		content.add(Box.createVerticalStrut(8));

		offersContainer.setLayout(new BoxLayout(offersContainer, BoxLayout.Y_AXIS));
		offersContainer.setOpaque(false);
		offersContainer.setAlignmentX(LEFT_ALIGNMENT);

		routingCard = borderedCard(new Color(108, 86, 31), 1);
		JPanel routingBody = verticalBody();
		routingTitle.setForeground(GOLD);
		routingTitle.setFont(FontManager.getRunescapeBoldFont());
		routingTitle.setAlignmentX(LEFT_ALIGNMENT);
		routingTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
		routingBody.add(routingTitle);
		routingBody.add(Box.createVerticalStrut(4));
		routingDetails.setForeground(Color.WHITE);
		routingDetails.setFont(FontManager.getRunescapeSmallFont());
		routingDetails.setAlignmentX(LEFT_ALIGNMENT);
		routingDetails.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
		routingBody.add(routingDetails);
		routingCard.add(routingBody, BorderLayout.CENTER);
		routingCard.setVisible(false);

		activeTaskCard = borderedCard(GOLD, 2);
		JPanel activeBody = verticalBody();
		activeTaskTitle.setForeground(GOLD);
		activeTaskTitle.setFont(FontManager.getRunescapeBoldFont());
		activeTaskTitle.setAlignmentX(LEFT_ALIGNMENT);
		activeTaskTitle.setHorizontalAlignment(SwingConstants.LEFT);
		activeTaskTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
		activeBody.add(activeTaskTitle);
		activeBody.add(Box.createVerticalStrut(5));
		activeTaskName.setForeground(PALE_GOLD);
		activeTaskName.setFont(FontManager.getRunescapeBoldFont().deriveFont(16f));
		activeTaskName.setAlignmentX(LEFT_ALIGNMENT);
		activeTaskName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		activeBody.add(activeTaskName);
		activeVariantSelector.setFont(FontManager.getRunescapeSmallFont());
		activeVariantSelector.setAlignmentX(LEFT_ALIGNMENT);
		activeVariantSelector.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		activeVariantSelector.setVisible(false);
		activeVariantSelector.addActionListener(event ->
		{
			if (!updatingVariantSelector && activeVariantSelector.getSelectedItem() instanceof SuperiorOption)
			{
				variantSelectionAction.accept((SuperiorOption) activeVariantSelector.getSelectedItem());
			}
		});
		activeBody.add(activeVariantSelector);
		activeBody.add(Box.createVerticalStrut(5));
		activeTaskSummary.setForeground(MUTED);
		activeTaskSummary.setFont(FontManager.getRunescapeSmallFont());
		activeTaskSummary.setAlignmentX(LEFT_ALIGNMENT);
		activeTaskSummary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
		activeBody.add(activeTaskSummary);
		manualModifierButton.setFont(FontManager.getRunescapeSmallFont());
		manualModifierButton.setAlignmentX(LEFT_ALIGNMENT);
		manualModifierButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
		manualModifierButton.setVisible(false);
		manualModifierButton.addActionListener(event -> promptForModifier());
		activeBody.add(manualModifierButton);
		activeBody.add(Box.createVerticalStrut(8));
		activeTaskResult.setLayout(new BoxLayout(activeTaskResult, BoxLayout.Y_AXIS));
		activeTaskResult.setOpaque(true);
		activeTaskResult.setBackground(DEEP_GREEN);
		activeTaskResult.setBorder(new EmptyBorder(8, 8, 8, 8));
		activeTaskResult.setAlignmentX(LEFT_ALIGNMENT);
		activeTaskResult.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
		activeTaskResult.add(metricRow("Expected DPS", expectedDpsValue));
		activeTaskResult.add(metricRow("Actual DPS", actualDpsValue));
		activeTaskResult.add(metricRow("Heart chance", taskChanceValue));
		activeTaskResult.add(metricRow("Superiors", superiorCountValue));
		activeTaskResult.add(metricRow("Expected superiors left", expectedSuperiorsValue));
		activeTaskResult.add(metricRow("Overall grind chance", overallChanceValue));
		activeTaskResult.add(metricRow("Time remaining", timeRemainingValue));
		activeBody.add(activeTaskResult);
		activeTaskCard.add(activeBody, BorderLayout.CENTER);

		activeTotalCard = borderedCard(new Color(67, 117, 77), 1);
		JPanel activeTotalBody = verticalBody();
		activeTotalBody.add(centeredLabel("TOTAL IMBUED HEART CHANCE", MUTED,
			FontManager.getRunescapeSmallFont()));
		styleCentered(activeTotalChance, PALE_GOLD, FontManager.getRunescapeBoldFont().deriveFont(22f));
		activeTotalBody.add(activeTotalChance);
		styleCentered(activeTotalDetails, Color.WHITE, FontManager.getRunescapeSmallFont());
		activeTotalBody.add(activeTotalDetails);
		activeTotalCard.add(activeTotalBody, BorderLayout.CENTER);

		emptyCard = card();
		JLabel emptyText = new JLabel("<html><div style='text-align:center;width:" + CARD_TEXT_WIDTH
			+ "px'>No offers are present.<br>Open the Slayer Task Choice screen.</div></html>", SwingConstants.CENTER);
		emptyText.setForeground(MUTED);
		emptyText.setFont(FontManager.getRunescapeSmallFont());
		emptyCard.add(emptyText, BorderLayout.CENTER);

		grindCard = borderedCard(new Color(67, 117, 77), 1);
		JPanel grindBody = verticalBody();
		grindBody.add(centeredLabel("IMBUED HEART CHANCE", MUTED, FontManager.getRunescapeSmallFont()));
		styleCentered(grindChance, PALE_GOLD, FontManager.getRunescapeBoldFont().deriveFont(22f));
		grindBody.add(grindChance);
		styleCentered(grindDetails, Color.WHITE, FontManager.getRunescapeSmallFont());
		grindBody.add(grindDetails);
		undoGrindButton.setFont(FontManager.getRunescapeSmallFont());
		undoGrindButton.setEnabled(false);
		undoGrindButton.setAlignmentX(CENTER_ALIGNMENT);
		undoGrindButton.setMaximumSize(new Dimension(178, 25));
		undoGrindButton.addActionListener(event -> undoLastGrind.run());
		grindBody.add(Box.createVerticalStrut(5));
		grindBody.add(undoGrindButton);
		grindCard.add(grindBody, BorderLayout.CENTER);

		flow.setLayout(new BoxLayout(flow, BoxLayout.Y_AXIS));
		flow.setOpaque(false);
		flow.setAlignmentX(LEFT_ALIGNMENT);
		content.add(flow);
		rebuildFlow();

		// PluginPanel already wraps itself in RuneLite's sidebar scroll pane. Keeping a
		// second scroll pane here prevented mouse-wheel events from reaching the native
		// sidebar scrollbar, even though dragging the nested scrollbar still worked.
		add(content, BorderLayout.CENTER);
		getScrollPane().getVerticalScrollBar().setUnitIncrement(16);
		updateRulesSnapshot();
	}

	void setEliteCa(boolean value)
	{
		if (eliteCa == value)
		{
			return;
		}
		eliteCa = value;
		updateRulesSnapshot();
		recalculate();
	}

	void setDetectedBracelet(Bracelet bracelet)
	{
		Bracelet value = bracelet == null ? Bracelet.NONE : bracelet;
		if (detectedBracelet == value)
		{
			return;
		}
		detectedBracelet = value;
		updateRulesSnapshot();
		recalculate();
	}

	void setRoutingContext(int newSlayerLevel, int newSlayerPoints, boolean newSlayerCape,
		Set<String> newBlockedTasks)
	{
		int level = Math.max(1, newSlayerLevel);
		int points = Math.max(0, newSlayerPoints);
		Set<String> blocks = newBlockedTasks == null
			? Collections.emptySet() : new LinkedHashSet<>(newBlockedTasks);
		if (slayerLevel == level && slayerPoints == points && slayerCape == newSlayerCape
			&& blockedTasks.equals(blocks))
		{
			return;
		}
		slayerLevel = level;
		slayerPoints = points;
		slayerCape = newSlayerCape;
		blockedTasks = blocks;
		updateRulesSnapshot();
		recalculate();
	}

	void setClientSnapshot(int attack, int strength, int ranged, int magic, int equippedItems)
	{
		if (attack <= 0 || strength <= 0 || ranged <= 0 || magic <= 0)
		{
			clientSnapshot.setText("Client connected · syncing combat stats");
			return;
		}
		clientSnapshot.setText(String.format(Locale.ENGLISH, "%d Atk · %d Str · %d Rng · %d Mag",
			attack, strength, ranged, magic));
		clientSnapshot.setToolTipText(equippedItems + " equipped items read from the client");
	}

	void setActiveTask(ActiveMortimerTask task, int remaining)
	{
		int nextRemaining = Math.max(0, remaining);
		if (currentActiveTask == task && currentRemaining == nextRemaining)
		{
			return;
		}
		boolean wasActive = currentActiveTask != null;
		currentActiveTask = task;
		currentRemaining = nextRemaining;
		if (task == null && wasActive)
		{
			offerCards.clear();
			offersContainer.removeAll();
		}
		offersHeader.setVisible(task == null);
		status.setVisible(task == null && status.isVisible());
		rebuildFlow();
		recalculate();
	}

	void setActualDps(double actualDps)
	{
		currentActualDps = Math.max(0.0, actualDps);
		recalculateActiveTask();
	}

	void setShowMonsterVariants(boolean value)
	{
		showMonsterVariants = value;
		if (currentActiveTask == null && !lastDetectedOffers.isEmpty())
		{
			importOffers(lastDetectedOffers);
		}
	}

	void setGrindPreference(GrindPreference value)
	{
		GrindPreference preference = value == null ? GrindPreference.IMBUED_HEART : value;
		if (grindPreference == preference)
		{
			return;
		}
		grindPreference = preference;
		recalculate();
	}

	void setGrindSummary(GrindSummary summary)
	{
		currentGrindSummary = summary;
		grindChance.setText(percent(summary.getHeartChance()));
		grindDetails.setText(String.format(Locale.ENGLISH, "%d tasks · %,d assigned · %.3f expected",
			summary.getTasks(), summary.getKills(), summary.getExpectedHearts()));
		undoGrindButton.setEnabled(summary.getTasks() > 0);
		recalculateActiveTask();
	}

	void setStatus(String message, boolean success)
	{
		if (currentActiveTask != null)
		{
			status.setVisible(false);
			return;
		}
		status.setVisible(true);
		status.setText("<html><div style='width:" + CARD_TEXT_WIDTH + "px'>" + html(message) + "</div></html>");
		status.setForeground(success ? SUCCESS : new Color(220, 139, 120));
	}

	void importOffers(List<MortimerDetectedOffer> detected)
	{
		if (currentActiveTask != null)
		{
			return;
		}
		lastDetectedOffers = new ArrayList<>(detected);
		offerCards.clear();
		offersContainer.removeAll();
		offersContainer.add(routingCard);
		offersContainer.add(Box.createVerticalStrut(8));
		int position = 1;
		for (MortimerDetectedOffer offer : detected)
		{
			List<SuperiorOption> variants = variantsFor(offer.getTask());
			for (SuperiorOption superior : variants)
			{
				if (!offerCards.isEmpty())
				{
					offersContainer.add(Box.createVerticalStrut(8));
				}
				OfferCard card = new OfferCard(offer, superior, position++);
				offerCards.add(card);
				offersContainer.add(card);
			}
		}
		status.setVisible(false);
		rebuildFlow();
		recalculate();
	}

	void refreshCalculations()
	{
		recalculate();
	}

	private void rebuildFlow()
	{
		flow.removeAll();
		if (currentActiveTask != null)
		{
			flow.add(activeTaskCard);
			flow.add(Box.createVerticalStrut(8));
			flow.add(activeTotalCard);
		}
		else if (offerCards.isEmpty())
		{
			flow.add(grindCard);
			flow.add(Box.createVerticalStrut(8));
			flow.add(emptyCard);
		}
		else
		{
			flow.add(offersContainer);
			flow.add(Box.createVerticalStrut(10));
			flow.add(grindCard);
		}
		flow.revalidate();
		flow.repaint();
	}

	private void updateRulesSnapshot()
	{
		String bracelet = detectedBracelet == Bracelet.NONE ? "No Slayer bracelet" : detectedBracelet.toString();
		String blocks = blockedTasks.isEmpty() ? "No Mortimer blocks detected"
			: "Blocked: " + String.join(", ", blockedTasks);
		rulesSnapshot.setText("<html><div style='text-align:center'>"
			+ (eliteCa ? "Elite CA · Superior 1/150" : "Below Elite CA · Superior 1/200")
			+ " · " + (slayerCape ? "Slayer cape active" : "No Slayer cape")
			+ " · " + slayerPoints + " spendable pts"
			+ "<br>" + html(bracelet)
			+ "<br>" + html(blocks)
			+ "</div></html>");
	}

	private void recalculate()
	{
		recalculateActiveTask();
		List<HeartResult> results = new ArrayList<>();
		List<OfferState> routingOffers = new ArrayList<>();
		List<Double> xpRates = new ArrayList<>();
		for (OfferCard card : offerCards)
		{
			results.add(HeartCalculator.calculate(card.toOffer(), eliteCa));
			OfferState routingOffer = card.toOffer(Bracelet.NONE);
			routingOffers.add(routingOffer);
			xpRates.add(SlayerExperienceData.experiencePerHour(routingOffer));
		}
		RoutingDecision decision = grindPreference == GrindPreference.IMBUED_HEART
			? OptimalRoutingCalculator.calculate(routingOffers, eliteCa, slayerLevel,
				slayerPoints, lastDetectedOffers.size(), blockedTasks, slayerCape, performance::killsPerHour,
				(task, amount) -> performance.overheadHours(task), performance::preference, wildernessTask)
			: null;
		int preferenceIndex = decision == null
			? PreferenceRecommendationCalculator.bestIndex(routingOffers, eliteCa, grindPreference,
				performance::preference) : -1;
		if (decision == null)
		{
			renderPreferenceDecision(preferenceIndex, results, routingOffers, xpRates);
		}
		else
		{
			renderRoutingDecision(decision, routingOffers);
		}
		for (int index = 0; index < results.size(); index++)
		{
			String badge = "";
			if (decision == null && index == preferenceIndex)
			{
				TaskPreference personal = performance.preference(routingOffers.get(index).getTask());
				badge = personal == TaskPreference.ALWAYS ? "PERSONAL PICK · ALWAYS"
					: personal == TaskPreference.PREFER ? "PERSONAL PICK · PREFERRED"
					: grindPreference == GrindPreference.SLAYER_XP ? "BEST SLAYER XP" : "BALANCED PICK";
			}
			else if (decision != null)
			{
				if (decision.getType() == RoutingDecision.Type.POINT_SKIP
					&& index == decision.getFastestFallbackIndex())
				{
					badge = "FASTEST FALLBACK";
				}
				else if (index == decision.getPrimaryIndex())
				{
					TaskPreference personal = performance.preference(routingOffers.get(index).getTask());
					badge = personal == TaskPreference.ALWAYS ? "PERSONAL PICK · ALWAYS"
						: personal == TaskPreference.PREFER ? "PERSONAL PICK · PREFERRED"
						: decision.getType() == RoutingDecision.Type.FAST_REROLL
						? "FAST REROLL · EXPEDITIOUS"
						: "RECOMMENDED · " + decision.getBracelet().toString().toUpperCase(Locale.ROOT);
				}
				else if (decision.getType() != RoutingDecision.Type.HUNT
					&& index == decision.getBestHeartIndex())
				{
					badge = "BEST HEART OPTION";
				}
				else if (results.get(index).getHoursOnRate() <= OptimalRoutingCalculator.TARGET_HEART_HOURS * 1.25)
				{
					badge = "VIABLE HEART · SLAUGHTER";
				}
			}
			offerCards.get(index).renderResult(results.get(index), xpRates.get(index), badge);
		}
		refreshLayout();
	}

	private List<SuperiorOption> variantsFor(HeartTask task)
	{
		if (showMonsterVariants)
		{
			return task.getSuperiors();
		}
		for (SuperiorOption superior : task.getSuperiors())
		{
			if (wildernessTask.test(task, superior))
			{
				return Collections.singletonList(superior);
			}
		}
		return task.getSuperiors().subList(0, 1);
	}

	private void refreshLayout()
	{
		offersContainer.invalidate();
		flow.invalidate();
		content.invalidate();
		content.revalidate();
		revalidate();
		repaint();
		SwingUtilities.invokeLater(() ->
		{
			offersContainer.revalidate();
			flow.revalidate();
			content.revalidate();
			content.repaint();
		});
	}

	private void renderPreferenceDecision(int index, List<HeartResult> results,
		List<OfferState> offers, List<Double> xpRates)
	{
		if (index < 0 || offers.size() < 2)
		{
			routingCard.setVisible(false);
			return;
		}
		routingCard.setVisible(true);
		OfferState offer = offers.get(index);
			HeartResult heart = results.get(index);
			TaskPreference personalPreference = performance.preference(offer.getTask());
			String personalNote = personalPreference == TaskPreference.STANDARD ? ""
				: "<br>Personal preference: " + html(personalPreference.toString()) + ".";
		String task = offer.getTask().getName().toUpperCase(Locale.ROOT);
		if (grindPreference == GrindPreference.SLAYER_XP)
		{
			routingTitle.setText("XP PICK · " + task);
			routingDetails.setText("<html><div style='width:" + CARD_TEXT_WIDTH + "px'>"
				+ number(xpRates.get(index)) + " Slayer XP/hr"
				+ (offer.getXpModifier() > 0 ? " with +" + trim(offer.getXpModifier()) + "% XP" : "")
				+ ".<br>" + confidenceText(index, xpRates)
				+ personalNote + "</div></html>");
		}
		else
		{
			double bestXp = xpRates.stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
			double bestHeart = results.stream().mapToDouble(HeartResult::getChancePerHour).max().orElse(1.0);
			List<Double> balancedScores = new ArrayList<>();
			for (int scoreIndex = 0; scoreIndex < offers.size(); scoreIndex++)
			{
				balancedScores.add((xpRates.get(scoreIndex) / Math.max(1.0, bestXp)
					+ results.get(scoreIndex).getChancePerHour() / Math.max(1e-12, bestHeart)) / 2.0);
			}
			routingTitle.setText("BALANCED PICK · " + task);
			routingDetails.setText("<html><div style='width:" + CARD_TEXT_WIDTH + "px'>"
				+ number(xpRates.get(index)) + " Slayer XP/hr · " + hours(heart.getHoursOnRate())
				+ " on-rate Heart.<br>Equal weighting of relative XP and Heart efficiency."
				+ " " + confidenceText(index, balancedScores) + personalNote + "</div></html>");
		}
	}

	private void renderRoutingDecision(RoutingDecision decision, List<OfferState> offers)
	{
		if (decision == null || offers.size() < 2)
		{
			routingCard.setVisible(false);
			return;
		}
		routingCard.setVisible(true);
		OfferState primary = offers.get(decision.getPrimaryIndex());
		OfferState fallback = offers.get(decision.getFastestFallbackIndex());
		String chance = percent(decision.getProbabilityBetterNext());
		if (decision.getType() == RoutingDecision.Type.POINT_SKIP)
		{
			routingTitle.setText("SPEND 100 POINTS TO REROLL");
			double fallbackHours = HeartCalculator.calculate(
				new OfferState(fallback.getTask(), fallback.getSuperior(), fallback.getAmount(),
					fallback.getDropModifier(), fallback.getXpModifier(), fallback.getKillsPerHour(),
					fallback.getOverheadHours(), Bracelet.EXPEDITIOUS, fallback.isWilderness()), eliteCa).getTaskHours();
			routingDetails.setText("<html><div style='width:" + CARD_TEXT_WIDTH + "px'>All offers exceed the 120h target. "
				+ html(fallback.getTask().getName()) + " is the fallback at " + hours(fallbackHours)
				+ " with Expeditious.<br>Next offers have a " + chance + " chance to beat this set.</div></html>");
		}
		else if (decision.getType() == RoutingDecision.Type.FAST_REROLL)
		{
			double taskHours = HeartCalculator.calculate(
				new OfferState(primary.getTask(), primary.getSuperior(), primary.getAmount(),
					primary.getDropModifier(), primary.getXpModifier(), primary.getKillsPerHour(),
					primary.getOverheadHours(), Bracelet.EXPEDITIOUS, primary.isWilderness()), eliteCa).getTaskHours();
			routingTitle.setText("FAST REROLL · " + primary.getTask().getName().toUpperCase(Locale.ROOT));
			routingDetails.setText("<html><div style='width:" + CARD_TEXT_WIDTH + "px'>Use an Expeditious bracelet · "
				+ hours(taskHours) + " to new offers.<br>Next offers have a " + chance
				+ " chance to beat this set. Keep your Slayer points.</div></html>");
		}
		else
		{
			List<Double> heartRates = offers.stream()
				.map(offer -> HeartCalculator.calculate(offer, eliteCa).getChancePerHour())
				.collect(Collectors.toList());
			TaskPreference personalPreference = performance.preference(primary.getTask());
			String preferenceNote = personalPreference == TaskPreference.STANDARD ? ""
				: " Personal preference: " + html(personalPreference.toString()) + ".";
			routingTitle.setText("HUNT · " + primary.getSuperior().taskDisplayName(primary.getTask())
				.toUpperCase(Locale.ROOT));
			routingDetails.setText("<html><div style='width:" + CARD_TEXT_WIDTH + "px'>Use a "
				+ html(decision.getBracelet().toString()) + " bracelet. Best current Heart route. "
				+ "Modelled route to Heart: " + hours(decision.getExpectedHours())
				+ (slayerCape ? ". Slayer cape repeats are included" : "")
				+ ".<br>" + confidenceText(decision.getPrimaryIndex(), heartRates)
				+ preferenceNote + " Fresh-offer baseline: " + hours(decision.getFreshOfferHours()) + ".</div></html>");
		}
	}

	private void recalculateActiveTask()
	{
		if (currentActiveTask == null)
		{
			return;
		}
		ActiveMortimerTask task = currentActiveTask;
		HeartTask heartTask = HeartData.findTask(task.getTaskName());
		SuperiorOption selectedVariant = selectedVariant(heartTask, task.getSuperiorName());
		double kph = heartTask == null ? 1.0 : selectedVariant == null
			? performance.killsPerHour(heartTask) : performance.killsPerHour(heartTask, selectedVariant);
		boolean heartEligible = task.getBaseHeartRate() > 0.0;
		double heartPerSuperior = heartEligible ? task.getBaseHeartRate()
			/ (1.0 + Math.max(0.0, task.getDropModifier()) / 100.0) : Double.POSITIVE_INFINITY;
		double spawnRate = task.getSuperiorSpawnRate() > 0.0
			? task.getSuperiorSpawnRate() : HeartCalculator.superiorSpawnRate(eliteCa, false);
		double fullKills = detectedBracelet.adjustKills(task.getAssignedAmount());
		double remainingKills = detectedBracelet.adjustKills(currentRemaining);
		double taskChance = chance(fullKills, spawnRate * heartPerSuperior);
		double recordedChance = chance(task.getSuperiorRolls(), heartPerSuperior);
		double overallChance = 1.0 - (1.0 - currentGrindSummary.getHeartChance()) * (1.0 - recordedChance);
		activeTotalChance.setText(percent(overallChance));
		activeTotalDetails.setText(currentGrindSummary.getTasks() + " completed tasks · "
			+ task.getSuperiorRolls() + " superiors this task");
		manualModifierButton.setVisible(task.getDropModifier() < 0.0);

		activeTaskTitle.setText("<html><u><b>CURRENT TASK</b></u></html>");
		activeTaskName.setText("<html>" + html(task.getTaskName().toUpperCase(Locale.ROOT)) + " · <b>"
			+ currentRemaining + "/" + task.getAssignedAmount() + "</b></html>");
		updateVariantSelector(heartTask, selectedVariant);
		String heartRates = heartEligible
			? "<br><br><b>" + html(task.getSuperiorName()) + " base</b>  1/" + Math.round(task.getBaseHeartRate())
				+ "<br><b>" + html(task.getSuperiorName()) + " increased</b>  1/" + Math.round(heartPerSuperior)
			: "<br><br><b>" + html(task.getSuperiorName()) + "</b> · no superior or Heart chance";
		activeTaskSummary.setText("<html><div style='width:" + ACTIVE_TEXT_WIDTH + "px'>"
			+ (task.getDropModifier() < 0 ? "Mortimer modifier unavailable"
				: task.getDropModifier() > 0 ? "+" + trim(task.getDropModifier()) + "% Mortimer heart modifier" : "No Mortimer heart modifier")
			+ "<br>" + (heartEligible ? (spawnRate < HeartCalculator.superiorSpawnRate(eliteCa, false)
				? "Wilderness superior chance 1/" : "Superior chance 1/") + Math.round(spawnRate) : "No superior spawns")
			+ heartRates + "</div></html>");
		expectedDpsValue.setText(heartTask == null ? "Unknown"
			: String.format(Locale.ENGLISH, "%.2f", selectedVariant == null
				? performance.expectedDps(heartTask) : performance.expectedDps(heartTask, selectedVariant)));
		actualDpsValue.setText(currentActualDps > 0.0
			? String.format(Locale.ENGLISH, "~%.2f", currentActualDps) : "Collecting…");
		taskChanceValue.setText(percent(taskChance));
		superiorCountValue.setText(Integer.toString(task.getSuperiorRolls()));
		expectedSuperiorsValue.setText(heartEligible
			? String.format(Locale.ENGLISH, "%.2f", remainingKills / spawnRate) : "0.00");
		overallChanceValue.setText(percent(overallChance));
		timeRemainingValue.setText(hours(remainingKills / Math.max(1.0, kph)));
	}

	private void promptForModifier()
	{
		Object[] choices = {"Superior unique chance increase", "No heart modifier"};
		Object selected = JOptionPane.showInputDialog(this, "Which Mortimer modifier was on this task?",
			"Mortimer modifier", JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
		if (selected == null)
		{
			return;
		}
		if (choices[1].equals(selected))
		{
			modifierSelectionAction.accept(0.0);
			return;
		}
		JSpinner input = new JSpinner(new SpinnerNumberModel(50.0, 0.0, 1000.0, 5.0));
		int result = JOptionPane.showConfirmDialog(this, input, "Superior unique increase (%)",
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (result == JOptionPane.OK_OPTION)
		{
			modifierSelectionAction.accept(((Number) input.getValue()).doubleValue());
		}
	}

	private void updateVariantSelector(HeartTask task, SuperiorOption selected)
	{
		boolean visible = task != null && task.getSuperiors().size() > 1;
		activeVariantSelector.setVisible(visible);
		if (!visible)
		{
			return;
		}
		updatingVariantSelector = true;
		activeVariantSelector.removeAllItems();
		for (SuperiorOption superior : task.getSuperiors())
		{
			activeVariantSelector.addItem(superior);
		}
		activeVariantSelector.setSelectedItem(selected);
		updatingVariantSelector = false;
	}

	private static SuperiorOption selectedVariant(HeartTask task, String superiorName)
	{
		if (task == null)
		{
			return null;
		}
		for (SuperiorOption superior : task.getSuperiors())
		{
			if (superior.getName().equals(superiorName))
			{
				return superior;
			}
		}
		return task.getSuperiors().get(0);
	}

	@Override
	public void onActivate()
	{
		SwingUtilities.invokeLater(() ->
		{
			flow.revalidate();
			revalidate();
			repaint();
		});
	}

	private static double chance(double rolls, double denominator)
	{
		return rolls <= 0 || denominator <= 1.0 ? 0.0
			: 1.0 - Math.pow(1.0 - 1.0 / denominator, rolls);
	}

	private static JPanel card()
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(new EmptyBorder(8, 8, 8, 8));
		panel.setAlignmentX(LEFT_ALIGNMENT);
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));
		return panel;
	}

	private static JPanel borderedCard(Color color, int thickness)
	{
		JPanel panel = card();
		panel.setBorder(new CompoundBorder(BorderFactory.createLineBorder(color, thickness), new EmptyBorder(8, 8, 8, 8)));
		return panel;
	}

	private static JPanel verticalBody()
	{
		JPanel body = new JPanel();
		body.setOpaque(false);
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		return body;
	}

	private static JPanel metricRow(String text, JLabel value)
	{
		JPanel row = new JPanel(new BorderLayout(4, 0));
		row.setOpaque(false);
		row.setAlignmentX(LEFT_ALIGNMENT);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
		JLabel name = label(text, Color.WHITE, FontManager.getRunescapeSmallFont());
		value.setForeground(PALE_GOLD);
		value.setFont(FontManager.getRunescapeSmallFont());
		value.setHorizontalAlignment(SwingConstants.RIGHT);
		row.add(name, BorderLayout.WEST);
		row.add(value, BorderLayout.EAST);
		return row;
	}

	private static JLabel centeredLabel(String text, Color color, Font font)
	{
		JLabel value = label(text, color, font);
		value.setAlignmentX(CENTER_ALIGNMENT);
		return value;
	}

	private static JLabel label(String text, Color color, Font font)
	{
		JLabel value = new JLabel(text);
		value.setForeground(color);
		value.setFont(font);
		return value;
	}

	private static void styleCentered(JLabel label, Color color, Font font)
	{
		label.setForeground(color);
		label.setFont(font);
		label.setAlignmentX(CENTER_ALIGNMENT);
	}

	private static String percent(double probability)
	{
		return String.format(Locale.ENGLISH, probability < 0.0001 ? "%.4f%%" : "%.2f%%", probability * 100.0);
	}

	private static String number(double value)
	{
		return String.format(Locale.ENGLISH, "%,.0f", value);
	}

	private static String confidenceText(int selectedIndex, List<Double> values)
	{
		if (selectedIndex < 0 || selectedIndex >= values.size() || values.size() < 2)
		{
			return "Only one comparable route.";
		}
		double selected = values.get(selectedIndex);
		double bestOther = 0.0;
		for (int index = 0; index < values.size(); index++)
		{
			if (index != selectedIndex)
			{
				bestOther = Math.max(bestOther, values.get(index));
			}
		}
		if (selected <= 0.0 || bestOther <= 0.0)
		{
			return "Limited comparison data.";
		}
		double lead = selected / bestOther - 1.0;
		long difference = Math.round(Math.abs(lead) * 100.0);
		if (lead < -0.005)
		{
			return "Personal preference overrides a route about " + difference + "% stronger numerically.";
		}
		if (Math.abs(lead) <= 0.03)
		{
			return "Near tie · within " + Math.max(1, difference) + "% of the next option.";
		}
		if (lead >= 0.20)
		{
			return "Strong recommendation · about " + difference + "% ahead of the next option.";
		}
		return "Competitive recommendation · about " + difference + "% ahead of the next option.";
	}

	private static String hours(double value)
	{
		if (!Double.isFinite(value))
		{
			return "No Heart chance";
		}
		if (value < 1.0)
		{
			return Math.max(1, Math.round(value * 60.0)) + " min";
		}
		long wholeHours = (long) value;
		long minutes = Math.round((value - wholeHours) * 60.0);
		return wholeHours + "h " + minutes + "m";
	}

	private static String html(String text)
	{
		return text == null ? "" : text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private final class OfferCard extends JPanel
	{
		private final MortimerDetectedOffer detected;
		private final SuperiorOption superior;
		private final JLabel bestBadge = new JLabel();
		private final JLabel summary = new JLabel();
		private final JLabel result = new JLabel();

		private OfferCard(MortimerDetectedOffer detected, SuperiorOption superior, int position)
		{
			this.detected = detected;
			this.superior = superior;
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBackground(ColorScheme.DARKER_GRAY_COLOR);
			setBorder(new CompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(9, 9, 9, 9)));
			setAlignmentX(LEFT_ALIGNMENT);

			JPanel heading = new JPanel(new BorderLayout(5, 0));
			heading.setOpaque(false);
			JLabel offerTitle = label("#" + position + "  " + superior.taskDisplayName(detected.getTask()).toUpperCase(Locale.ROOT),
				GOLD, FontManager.getRunescapeBoldFont());
			heading.add(offerTitle, BorderLayout.WEST);
			bestBadge.setForeground(PALE_GOLD);
			bestBadge.setFont(FontManager.getRunescapeSmallFont());
			heading.setAlignmentX(LEFT_ALIGNMENT);
			heading.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
			add(heading);
			bestBadge.setAlignmentX(LEFT_ALIGNMENT);
			bestBadge.setVisible(false);
			add(bestBadge);
			add(Box.createVerticalStrut(5));

			summary.setForeground(MUTED);
			summary.setFont(FontManager.getRunescapeSmallFont());
			summary.setAlignmentX(LEFT_ALIGNMENT);
			add(summary);
			add(Box.createVerticalStrut(7));

			result.setOpaque(true);
			result.setBackground(DEEP_GREEN);
			result.setForeground(Color.WHITE);
			result.setBorder(new EmptyBorder(7, 7, 7, 7));
			result.setFont(FontManager.getRunescapeSmallFont());
			result.setAlignmentX(LEFT_ALIGNMENT);
			result.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
			add(result);
			updateSummary();
		}

		@Override
		public Dimension getMaximumSize()
		{
			Dimension preferred = getPreferredSize();
			return new Dimension(Integer.MAX_VALUE, preferred.height);
		}

		private void updateSummary()
		{
			double kph = performance.killsPerHour(detected.getTask(), superior);
			String heart = superior.canDropHeart() ? "Heart 1/" + Math.round(superior.getHeartRate()) : "No superior roll";
			boolean wilderness = wildernessTask.test(detected.getTask(), superior);
			double overheadMinutes = performance.overheadHours(detected.getTask()) * 60.0;
			summary.setText("<html><div style='width:" + CARD_TEXT_WIDTH + "px'>" + detected.getAmount() + " assigned · "
				+ (detected.getDropModifier() > 0 ? "+" + trim(detected.getDropModifier()) + "% Heart modifier"
					: detected.getXpModifier() > 0 ? "+" + trim(detected.getXpModifier()) + "% XP modifier" : "No Heart/XP modifier")
				+ "<br>" + html(superior.getMonsterName()) + " → " + html(superior.getName())
				+ " · " + heart + (wilderness ? " · Wilderness 1/" + Math.round(
					HeartCalculator.superiorSpawnRate(eliteCa, true)) : "")
				+ "<br>" + performance.paceLabel(detected.getTask(), superior) + " · " + Math.round(kph) + " kills/hr"
				+ (overheadMinutes >= 0.1 ? " · " + Math.round(overheadMinutes) + "m overhead" : "")
				+ "</div></html>");
		}

		private OfferState toOffer()
		{
			return toOffer(detectedBracelet);
		}

		private OfferState toOffer(Bracelet bracelet)
		{
			HeartTask task = detected.getTask();
			double kph = performance.killsPerHour(task, superior);
			return new OfferState(task, superior, detected.getAmount(), detected.getDropModifier(),
				detected.getXpModifier(), kph, performance.overheadHours(task), bracelet,
				wildernessTask.test(task, superior));
		}

		private void renderResult(HeartResult calculated, double xpPerHour, String badge)
		{
			updateSummary();
			Bracelet suggestedBracelet = BraceletAdvisor.recommend(toOffer(Bracelet.NONE), eliteCa);
			boolean highlighted = badge != null && !badge.isEmpty();
			setBorder(new CompoundBorder(BorderFactory.createLineBorder(highlighted ? GOLD : BORDER, highlighted ? 2 : 1),
				new EmptyBorder(highlighted ? 8 : 9, highlighted ? 8 : 9, highlighted ? 8 : 9, highlighted ? 8 : 9)));
			bestBadge.setText(highlighted ? badge : "");
			bestBadge.setVisible(highlighted);
			result.setText("<html><b>Heart this task</b>  " + percent(calculated.getTaskChance())
				+ "<br><b>Heart chance / hour</b>  " + percent(calculated.getChancePerHour())
				+ "<br><b>Slayer XP / hour</b>  " + number(xpPerHour)
				+ "<br><b>Task time</b>  " + hours(calculated.getTaskHours())
				+ "<br><b>Expected superiors</b>  " + String.format(Locale.ENGLISH, "%.2f", calculated.getExpectedSuperiors())
				+ "<br><b>Recommended bracelet</b>  " + html(suggestedBracelet.toString())
				+ "<br><b>On-rate Heart</b>  " + hours(calculated.getHoursOnRate()) + "</html>");
		}
	}

	private static String trim(double value)
	{
		return value == Math.rint(value) ? Long.toString(Math.round(value)) : String.format(Locale.ENGLISH, "%.1f", value);
	}

}
