# SR-Addons

Unified client-side Fabric mod that combines EntityFire, PartyCommands, and StarredMobHighlighter into a single addon for Minecraft 1.21.11.

## Features

### EntityFire
Hide fire animation on burning entities. Toggle it in the config GUI (`/sra gui`).

### PartyCommands
Type commands directly in chat using the `!` prefix (no `/pc` prefix needed). Designed for Hypixel SkyBlock party management.

#### Available Commands

**Info:** `!help` `!ping` `!tps` `!fps` `!time` `!location` `!loc` `!coords` `!co` `!holding` `!hold` `!status`

**Party Management (leader only):** `!warp` `!w` `!allinvite` `!allinv` `!transfer` `!pt` `!promote` `!demote` `!disband` `!kick` `!k` `!kickoffline` `!kickall` `!invite` `!inv`

**General:** `!leave`

**Dungeon Queue:** `!f1`–`!f7` `!m1`–`!m7` `!t1`–`!t5` (optional countdown: `!f7 30`)

**Fun:** `!fun cf` `!fun 8ball` `!fun dice` `!fun boop <player>` `!fun random [min] [max]`

**Utility:** `!forward` `!reload` `!cd <time>` `!clear` `!note [message]` `!gui` `!ver`

### StarredMobHighlighter
Highlights mobs with the `✯` star symbol in their name tag. Features configurable wireframe outlines and fill rendering with X-ray (see-through-walls) support. Useful for Hypixel SkyBlock dungeons.

## SR-Addons Command

```
/sra reload   — Reload configuration
/sra config   — Open config GUI
/sra gui      — Open config GUI (alias)
/sra version  — Show version info
/sra update   — Check for updates
```

## Key Bindings

| Key | Action |
|-----|--------|
| `` ` `` (Grave) | Open chat with `!` prefix |
| Unbound (default) | Open config GUI |
| Unbound (default) | Toggle PartyCommands |

## Configuration

All settings are managed through the YACL config GUI (`/sra gui`) with three tabs:

1. **EntityFire** — Toggle hidden fire
2. **PartyCommands** — Prefix, command toggles, response settings, note & sound
3. **StarredMob** — Enabled, color picker, render mode, line width, max distance, see-through-walls

Config file: `.minecraft/config/sraddons.json`

On first launch, old configs from EntityFire, PartyCommands, and StarredMobHighlighter are automatically migrated.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/) >= 0.16.0 for Minecraft 1.21.11
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Install [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) >= 1.13.0
4. Install [YACL](https://modrinth.com/mod/yacl) >= 3.8.0
5. Download `SR-Addons-1.0.0.jar` and place it in `.minecraft/mods/`

## Building from Source

```bash
./gradlew build
```

Output: `build/libs/SR-Addons-1.0.0.jar`

Requirements: JDK 21

## License

MIT
