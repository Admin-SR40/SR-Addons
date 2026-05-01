# SR-Addons

Unified client-side Fabric mod for Hypixel SkyBlock — combines EntityFire, PartyCommands, StarredMobHighlighter, and CarryModule into a single addon.

![](https://img.shields.io/badge/Minecraft-1.21.11-green) ![](https://img.shields.io/badge/License-MIT-blue) ![](https://img.shields.io/badge/Version-1.3.3-orange)

## Features

| Module | Description |
|--------|-------------|
| **EntityFire** | Hide fire animation on burning entities |
| **PartyCommands** | Type commands in chat with `!` prefix — no `/pc` needed |
| **StarredMobHighlighter** | Wireframe/fill highlight for mobs with `✯` star symbol in name |
| **CarryModule** | Track carry orders, calculate prices, record earnings. Highlight clients & bosses |

All modules support **i18n** (English / 简体中文).

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/) >= 0.16.0 for Minecraft 1.21.11
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Install [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) >= 1.13.0
4. Install [YACL](https://modrinth.com/mod/yacl) >= 3.8.0
5. (Optional) Install [ModMenu](https://modrinth.com/mod/modmenu) to open config directly from the Mods screen
6. Download `SR-Addons-1.3.3.jar` and place it in `.minecraft/mods/`

---

## Commands

### `/sra` — Mod Management

| Command | Description |
|---------|-------------|
| `/sra` | Show help |
| `/sra reload` | Reload configuration |
| `/sra config` | Open config GUI |
| `/sra gui` | Open config GUI (alias) |
| `/sra version` | Show version info |
| `/sra update` | Check for updates |

### `/cm` — Carry Module

**Setup**

| Command | Description |
|---------|-------------|
| `/cm add-type <type>` | Add a carry type |
| `/cm add <player> <type> <n>` | Add a client with carry count |
| `/cm set-price <type> <price>` | Set unit price (e.g. `1.8M`, `500K`) |
| `/cm set-bulk-price <type> <price> <n>` | Set bulk price for `n+` carries |

**Management**

| Command | Description |
|---------|-------------|
| `/cm add-amount <player> <n>` | Increase carry count |
| `/cm set-amount <player> <n> [true\|false]` | Set carry count & bulk toggle |
| `/cm remove-amount <player> <n>` | Decrease carry count |
| `/cm remove <player>` | Remove a client |
| `/cm remove-type <type>` | Remove a carry type |

**Info**

| Command | Description |
|---------|-------------|
| `/cm calc-price <player>` | Calculate total price |
| `/cm list-client` | Show all clients |
| `/cm list-type` | Show all carry types |
| `/cm status` | Show total earnings |

**Complete**

| Command | Description |
|---------|-------------|
| `/cm done [player] [n]` | Record completed carries (defaults to 1 if omitted) |
| `/cm refund <player>` | Calculate refund |
| `/cm undo` | Undo last change |

**Utility**

| Command | Description |
|---------|-------------|
| `/cm clear-client` | Remove all clients |
| `/cm clear-history` | Reset earnings history |

### PartyCommands — `!` Prefix

**Info**

| Command | Description |
|---------|-------------|
| `!help` | Show available commands |
| `!ping` | Show latency |
| `!tps` | Show server TPS |
| `!fps` | Show FPS |
| `!time` | Show current time |
| `!location` / `!loc` | Show location |
| `!coords` / `!co` | Show coordinates |
| `!holding` / `!hold` | Show held item |
| `!status` | Show party member list |
| `!ver` | Show mod version |

**Party Management** *(leader only)*

| Command | Description |
|---------|-------------|
| `!warp` / `!w` | Warp members to your hub |
| `!allinvite` / `!allinv` | Enable all invite |
| `!transfer <player>` / `!pt` | Transfer party leader |
| `!promote <player>` | Promote member |
| `!demote <player>` | Demote member |
| `!kick <player> [reason]` / `!k` | Kick a member |
| `!kickoffline` | Kick all offline members |
| `!kickall [players...]` | Kick all except specified |
| `!disband` | Disband the party |
| `!invite <player>` / `!inv` | Invite player to party |
| `!leave` | Leave the party |

**Dungeon Queue**

| Command | Description |
|---------|-------------|
| `!f1`–`!f7` | Queue Catacombs floor |
| `!m1`–`!m7` | Queue Master Mode floor |
| `!t1`–`!t5` | Queue Kuudra tier |

Add a countdown: `!f7 30` — enters in 30 seconds.

**Fun**

| Command | Description |
|---------|-------------|
| `!fun cf` | Coin flip (heads/tails) |
| `!fun 8ball` | Magic 8-ball |
| `!fun dice` | Roll a dice |
| `!fun boop <player>` | Boop a player |
| `!fun random [min] [max]` | Random number |

**Utility**

| Command | Description |
|---------|-------------|
| `!forward` | Toggle party chat forwarding |
| `!reload` | Reload configuration |
| `!cd <time>` | Start countdown (`60`, `5m`, `1h`, `5m30s`, max 12h) |
| `!clear` | Clear current countdown |
| `!note [message]` | Save/send note to party |
| `!gui` | Open config GUI |

---

## Key Bindings

| Key | Action |
|-----|--------|
| `` ` `` (Grave) | Open chat with `!` prefix |
| Unbound | Open config GUI |
| Unbound | Toggle PartyCommands on/off |

---

## Configuration

All settings managed through the YACL config GUI (`/sra gui` or via ModMenu) with 4 tabs:

| Tab | Settings |
|-----|----------|
| **EntityFire** | Toggle hidden fire |
| **PartyCommands** | Prefix, 40+ individual command toggles (separate switches for each queue floor: f1–f7, m1–m7, t1–t5), response routing (party chat / local), separator removal, auto `!mod` reply, note message, countdown sound |
| **StarredMob** | Enabled, highlight color (RGBA), render mode (Outline/Fill/Both), line width, max distance, see-through-walls |
| **Carry** | Master toggle, client & boss highlight (separate colors), render mode, line width, max distance, see-through-walls |

Config file: `.minecraft/config/sraddons.json`

Old configs from EntityFire, PartyCommands, and StarredMobHighlighter are auto-migrated on first launch.

---

## Building from Source

```bash
./gradlew clean build
```

**Requirements:** JDK 21

**Output:** `build/libs/SR-Addons-1.3.3.jar`

---

## License

This project is licensed under **MIT License**.