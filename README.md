# Mortimer Imbued Heart — RuneLite plugin

A local RuneLite sidebar calculator for comparing Mortimer Slayer offers. It contains no accounts, highscores or telemetry. The only optional network request is made directly to the OSRS Wiki when a player saves a Wiki DPS share link in a monster setting.

## Features

- Compare one to three Mortimer offers.
- Rank three strategies automatically: hunt the strongest Heart offer, rush the most efficient reroll with an Expeditious bracelet, or spend 100 Slayer points when every offer is both slow and outside the 120-hour target.
- Calculate Heart chance for the task, Heart chance per hour, regular-kill rate, expected superiors, estimated task duration and on-rate hours to a Heart.
- Detect the accepted Slayer task and assigned amount from RuneLite, count superior-spawn messages, then record it automatically when the in-game completion message appears.
- Reconcile the active assignment from the login status message when a task was accepted on another RuneLite client; if that client never saw the Mortimer offer, the unavailable modifier is labelled honestly.
- Read the built-in RuneLite Slayer service used by its overlay for live task name, starting amount and remaining amount, with saved-profile and chat parsing only as fallbacks.
- Store grind history and the active task in the character's RuneScape-profile configuration, while retaining a local backup. Cross-PC transfer requires RuneLite cloud sync to be enabled for the selected configuration profile.
- Switch automatically between the Mortimer offer comparison and a single live-task card showing remaining kills, actual superiors, current-task odds, chance accumulated so far and estimated time remaining.
- Optionally expand assignments with multiple valid monster variants into separate comparison cards, offer a live-task variant selector, and automatically switch the selected variant when the player attacks a recognised monster.
- Show expected DPS plus a session estimate derived from live task progress after enough data has been collected.
- Keep a fully local grind ledger with cumulative Imbued Heart chance, expected Hearts, tasks and assigned kills; the last automatic record can be undone.
- Detect Elite-or-higher Combat Achievements from RuneLite's `CA_THRESHOLD_ELITE` account varbit and automatically use `1/150`; otherwise use `1/200`.
- Directly scans the open Mortimer choice interface using RuneLite widgets whenever it opens, independent of window size or theme; no refresh button is needed.
- Displays Mortimer choices as compact read-only cards with no manual offer inputs.
- Reads the local player's combat levels and automatically detects equipped Expeditious or Slaughter bracelets.
- Shows the detected Combat Achievement tier result in the calculator panel; there is no default-on Elite setting.
- Provides a collapsed settings section for every supported monster, with a manual DPS or OSRS Wiki DPS-link override and all single/multicombat steel/granite cannon options.
- Uses a reliable Imbued Heart-styled RuneLite sidebar icon.
- Includes all supported Heart Slayer tasks and superior Heart rates locally.

Without an override, the plugin uses its conservative planning KPH. DPS overrides use monster Hitpoints plus a two-second per-kill handling/respawn allowance; Wiki links also apply the configured multi-target Barrage/Burst and charged Venator multipliers before cannon DPS.

The reroll model follows Mortimer's published task weights without replacement and samples assignment quantities and unlocked modifier ranges. Where Jagex has not published the relative odds of the five modifier categories, the model treats those categories as equally likely and labels the result as an estimate.
