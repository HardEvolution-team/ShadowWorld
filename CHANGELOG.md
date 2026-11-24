# Changelog

## 1.1.1
Full code port: the original 1.7.10 logic is now ported line by line instead of being approximated. Requires ArchaicLib 1.43.0+.
### Added
- Angel World: the third dimension with the giant floating Angel Village (all 21 original village parts, ~450k blocks, generated chunk by chunk without lag), Angel Villager houses with chests and traders, flying boats with loot, the Angel World sky.
- The Angel boss is summoned by throwing a Shadow Crystal into Hell Fire; it flies, calls lightning and does its super attack like in 1.7, and dies into the floating Angel Portal (falls like sand, teleports to the Angel World and back).
- Angel Villagers trade with Shadow Coins (4 professions + guards), Angel Block chiseled/pillar variants.
- Soul Furnace / Golden Soul Furnace, Shadow and Angel chests, Pedestals (crafting with staffs, runes, beams), Diary and Notes with their GUIs and the diary entries/toasts, Lamps, hanging notes.
- Flying Boat (with chest / ender chest), Dungeon Finder, Shadow Compass, Shadow Village, Shadow dungeons, structure chest loot and spawners.
- Dwarves (5 professions, Shadow Coin trades) and Travelling Dwarves with their stories.
- Crossbow bolts (1.7 speed and charge, poisonous / burning crossbows), Shadow Chicken eggs that explode when picked up.
- 3D crossbows, staffs and the open diary book in hand; connected Azurite Runes (animated in a pedestal circle); connected Shadow Fences.
### Changed
- All remaining mobs now follow the original code instead of vanilla stand-ins: Shadow (dark zombie with glowing eyes, enderman behaviour), Light (7 skins), Shadow Dragonfly (4 sizes), Jellyfish (squid swimming, 4 skins, 4 sizes, glow), Shadow Creeper (radius 7, pulls everything in while it swells, hunts dwarves), Exploding Wolf (double blast), Shadow Slime (grows when killed), Cookie Slime (harmless, regenerates you, splits into many), Shadow Pig (saddle it and fly), Cookie Pig (bite it five times), Passive Spider (small, climbs), Skeleton/Wolf Spiders (glowing eyes, original drops and AI), Shadow Wolf (original taming and health).
- Shadow Wolf King: fireballs, the wolf-pack super attack and the dragon-like death from the original.
- Golems are guardians again: they hunt monsters and never attack players (Air Golem only when hit), with their own element attacks, particles and drops.

## 1.0.2
### Changed
- The original mod file is now downloaded automatically on first start (needs ArchaicLib 1.42.0+): sources and mirrors are listed in `config/archaiclib/originals/` and can be edited there. If the download fails, a banner on the main menu links to the manual download.

## 1.0.1
### Changed
- Performance: uses ArchaicLib 1.40.0 FastRandom instead of java.util.Random, plus the 1.40.0 cached block-state / allocation-free legacy world calls. Requires ArchaicLib 1.40.0+.

## 1.0.0
- First release of the 1.12.2 (Cleanroom) port of Shadow World by JaJo_11. Requires ArchaicLib 1.34.0+.
