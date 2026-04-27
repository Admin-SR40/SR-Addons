# SR-Addons

Unified client-side Fabric mod that combines EntityFire, PartyCommands, StarredMobHighlighter, and CarryModule into a single addon for Minecraft 1.21.11.

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

### CarryModule
Track carry orders, calculate prices, and record earnings via `/cm` commands. Designed for Hypixel SkyBlock carry services.

#### Available Commands

**Setup:** `/cm add-type <type>` `/cm add <player> <type> <amount>` `/cm set-price <type> <price>` `/cm set-bulk-price <type> <price> <threshold>`

**Management:** `/cm add-amount <player> <amount>` `/cm remove-amount <player> <amount>` `/cm remove <player>` `/cm remove-type <type>`

**Info:** `/cm list-client` `/cm list-type` `/cm calc-price <player>` `/cm status`

**Complete:** `/cm done <amount> <player>` `/cm refund <player>`

**Utility:** `/cm clear-client` `/cm clear-history`

## Commands

### /sra
```
/sra reload   — Reload configuration
/sra config   — Open config GUI
/sra gui      — Open config GUI (alias)
/sra version  — Show version info
/sra update   — Check for updates
```

### /cm (Carry Module)
```
/cm add-type <type>         — Add a carry type
/cm add <player> <type> <n> — Add a client
/cm set-price <type> <p>    — Set price (e.g. 1.8M, 500K)
/cm set-bulk-price <t> <p> <n> — Set bulk price (n+)
/cm add-amount <player> <n> — Increase carry count
/cm remove-amount <p> <n>   — Decrease carry count
/cm calc-price <player>     — Calculate total price
/cm remove <player>         — Remove a client
/cm remove-type <type>      — Remove a carry type
/cm list-client             — Show all clients
/cm list-type               — Show all carry types
/cm done <n> <player>       — Record completed carries
/cm refund <player>         — Calculate refund
/cm status                  — Show total earnings
/cm clear-client            — Clear all clients
/cm clear-history           — Reset earnings history
```

## Key Bindings

| Key | Action |
|-----|--------|
| `` ` `` (Grave) | Open chat with `!` prefix |
| Unbound (default) | Open config GUI |
| Unbound (default) | Toggle PartyCommands |

## Configuration

All settings are managed through the YACL config GUI (`/sra gui`) with four tabs:

1. **EntityFire** — Toggle hidden fire
2. **PartyCommands** — Prefix, command toggles, response settings, note & sound
3. **StarredMob** — Enabled, color picker, render mode, line width, max distance, see-through-walls
4. **Carry** — Master toggle for `/cm` commands

Config file: `.minecraft/config/sraddons.json`

On first launch, old configs from EntityFire, PartyCommands, and StarredMobHighlighter are automatically migrated.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/) >= 0.16.0 for Minecraft 1.21.11
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Install [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) >= 1.13.0
4. Install [YACL](https://modrinth.com/mod/yacl) >= 3.8.0
5. Download `SR-Addons-1.1.0.jar` and place it in `.minecraft/mods/`

## Building from Source

```bash
./gradlew build
```

Output: `build/libs/SR-Addons-1.1.0.jar`

Requirements: JDK 21

## License

MIT
