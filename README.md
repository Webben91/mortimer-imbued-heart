# Mortimer Slayer — RuneLite plugin

A local RuneLite sidebar planner for comparing Mortimer Slayer offers by Imbued Heart chance, Slayer XP per hour, or an equal balance of both. It contains no accounts, highscores or telemetry. The only optional network request is made directly to the OSRS Wiki when a player saves a Wiki DPS share link in a monster setting.

## Features

- Compare one to three Mortimer offers.
- Choose an Imbued Heart, Slayer XP/hour, or Balanced recommendation preference from the plugin settings.
- Detect XP Mortifiers and calculate expected Slayer XP/hour from each monster's OSRS Wiki Slayer XP value and the configured task pace.
- Rank three strategies automatically: hunt the strongest Heart offer, rush the most efficient reroll with an Expeditious bracelet, or spend 100 Slayer points when every offer is both slow and outside the 120-hour target.
- Calculate Heart chance for the task, Heart chance per hour, regular-kill rate, expected superiors, estimated task duration and on-rate hours to a Heart.
- Detect the accepted Slayer task and assigned amount from RuneLite, count superior-spawn messages, then record it automatically when the in-game completion message appears.
- Reconcile the active assignment from the login status message when a task was accepted on another RuneLite client; if that client never saw the Mortimer offer, the unavailable modifier is labelled honestly.
- Read the built-in RuneLite Slayer service used by its overlay for live task name, starting amount and remaining amount, with saved-profile and chat parsing only as fallbacks.
- Store grind history and the active task in the character's RuneScape-profile configuration, while retaining a local backup. Cross-PC transfer requires RuneLite cloud sync to be enabled for the selected configuration profile.
- Switch automatically between the Mortimer offer comparison and a single live-task card showing remaining kills, actual superiors, current-task odds, chance accumulated so far and estimated time remaining.
- Optionally expand assignments with multiple valid monster variants into separate comparison cards, offer a live-task variant selector, and automatically switch the selected variant when the player attacks a recognised monster.
- Label expanded variants by their actual route, such as Banshees and Twisted banshees, instead of repeating the assignment name.
- Show expected DPS plus a session estimate derived from live task progress after enough data has been collected.
- Optionally learn task KPH from completed assignments and sync it with the character's RuneScape profile.
- Start learned timing after the first task kill, pause during long idle gaps or unrelated combat, and keep separate figures for monster variants such as Banshees and Twisted banshees.
- Configure a direct final KPH override, travel time and personal preference for any supported task from one collapsed Personal pace editor.
- Apply banking/gearing and travel overhead to task time, Heart chance per hour, Slayer XP per hour and long-term routing calculations.
- Explain whether a recommendation is a near tie, competitive or strong, and identify when a personal preference overrides the numerical leader.
- Keep a configurable Slayer-point reserve so a suggested 100-point skip never spends below the player's chosen safety balance.
- Keep a fully local grind ledger with cumulative Imbued Heart chance, expected Hearts, tasks and assigned kills; the last automatic record can be undone.
- Detect Elite-or-higher Combat Achievements from RuneLite's `CA_THRESHOLD_ELITE` account varbit and automatically use `1/150`; otherwise use `1/200`.
- Directly scans the open Mortimer choice interface using RuneLite widgets whenever it opens, independent of window size or theme; no refresh button is needed.
- Draws an animated sparkly border directly around the recommended Mortimer offer: purple for Heart, green for XP, gold for balanced, blue for a fast reroll, and red on the fastest fallback when a points skip is recommended.
- Displays Mortimer choices as compact read-only cards with no manual offer inputs.
- Reads the local player's combat levels and automatically detects equipped Expeditious or Slaughter bracelets.
- Shows the detected Combat Achievement tier result in the calculator panel; there is no default-on Elite setting.
- Provides a collapsed settings section for every supported monster, with a manual DPS or OSRS Wiki DPS-link override and all single/multicombat steel/granite cannon options.
- Uses the OSRS Slayer skill icon consistently in the RuneLite sidebar and Plugin Hub listing.
- Includes all supported Heart Slayer tasks and superior Heart rates locally.

Without an override, the plugin uses its conservative planning KPH. DPS overrides use monster Hitpoints plus a two-second per-kill handling/respawn allowance; Wiki links also apply the configured multi-target Barrage/Burst and charged Venator multipliers before cannon DPS.

The reroll model follows Mortimer's published task weights without replacement and samples assignment quantities and unlocked modifier ranges. Where Jagex has not published the relative odds of the five modifier categories, the model treats those categories as equally likely and labels the result as an estimate.
