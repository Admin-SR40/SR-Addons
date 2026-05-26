<h1 align="center">SR-Addons</h1>

<p align="center">Unified client-side Fabric mod for Hypixel SkyBlock — combines EntityFire, PartyCommands, StarredMobHighlighter, CarryModule, and some Helpers into a single addon.</p>

<p align="center">
  <a href="https://minecraft.net"><img src="https://img.shields.io/badge/Minecraft-1.21.11-green" alt="Minecraft"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue" alt="License"></a>
  <a href="https://github.com/Admin-SR40/SR-Addons/releases/latest"><img src="https://img.shields.io/badge/Version-1.5.6-orange" alt="Version"></a>
</p>

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/) >= 0.16.0 for Minecraft 1.21.11
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Install [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) >= 1.13.0
4. Install [YACL](https://modrinth.com/mod/yacl) >= 3.8.0
5. (Optional) Install [ModMenu](https://modrinth.com/mod/modmenu) to open config directly from the Mods screen
6. Download `SR-Addons-1.5.6.jar` and place it in `.minecraft/mods/`

---

## Features

<details open>
<summary>Modules</summary>

### EntityFire
- Hide fire animation on burning entities

### StarredMobHighlighter
- Wireframe / fill highlight for mobs with `✯` star symbol in name
- Configurable color (RGBA), render mode, line width, max distance

### PartyCommands
- Type commands in chat with `!` prefix — no `/pc` needed
- 40+ individually toggleable commands
- Ping, TPS, FPS, time, location, coords, holding, status info
- Full party management (warp, allinvite, transfer, promote, demote, kick, kickoffline, kickall, disband, invite, leave)
- Dungeon & Kuudra queue (`!f1`–`!f7`, `!m1`–`!m7`, `!t1`–`!t5`) with optional countdown
- Fun commands (coinflip, 8ball, dice, boop, random)
- Countdown timer with sound, note message, auto `!mod` reply
- Configurable response routing (party chat / local), separator line removal
- Auto party list updater with member online/offline tracking

### CarryModule
- Full carry order management via `/cm` commands
- Carry types with configurable unit & bulk pricing
- Client tracking with completed / remaining counts
- **Client Highlight** — green bounding box around client players
- **Boss Highlight** — red bounding box around bosses spawned by clients (detects "Spawned by:" name tags)
- **Miniboss Highlight** — orange bounding box for configurable Slayer miniboss names near clients (19 defaults, add/remove via commands)
- **Boss Spawn Notification** — subtitle alert when a client's boss spawns (toggleable, custom text)
- Earnings history, refund calculator, undo support
- Auto price calculation (no player name needed when only 1 client)

### Helper
- **Ragnarock Notifier** — subtitle alerts when casting Ragnarock or when cancelled, with configurable messages and optional party chat strength announcement
- **Calculator** — `/sra calc <expression>` evaluates math expressions (supports `+ - * / % ^`, parentheses, and K/M/B suffixes). Optional standalone `/calc` command via config

All modules support **i18n** (English / 简体中文).

</details>

---

## Commands

<details open>
<summary>/sra — ModManagement</summary>

| Command | Description |
|---------|-------------|
| `/sra` | Show help |
| `/sra reload` | Reload configuration |
| `/sra config` / `/sra gui` | Open config GUI |
| `/sra version` | Show version info |
| `/sra update` | Check for updates |
| `/sra calc <expr>` | Evaluate a math expression |

</details>

<details>
<summary>/cm — CarryModule</summary>

**Setup**

| Command | Description |
|---------|-------------|
| `/cm add-type <type>` | Add a carry type |
| `/cm add-client <player> <type> <n>` | Add a client with carry count |
| `/cm set-price <type> <price>` | Set unit price (e.g. `1.8M`, `500K`) |
| `/cm set-bulk-price <type> <price> <n>` | Set bulk price for `n+` carries |

**Management**

| Command | Description |
|---------|-------------|
| `/cm add-amount <player> <n>` | Increase carry count |
| `/cm set-amount <player> <n> [true\|false]` | Set carry count & bulk toggle |
| `/cm remove-amount <player> <n>` | Decrease carry count |
| `/cm remove-client <player>` | Remove a client |
| `/cm remove-type <type>` | Remove a carry type |

**Info**

| Command | Description |
|---------|-------------|
| `/cm calc-price [player]` | Calculate total price (auto if 1 client) |
| `/cm list-client` | Show all clients |
| `/cm list-type` | Show all carry types |
| `/cm status` | Show total earnings |

**Complete**

| Command | Description |
|---------|-------------|
| `/cm done [player] [n]` | Record completed carries (defaults to 1) |
| `/cm refund <player>` | Calculate refund |
| `/cm undo` | Undo last change |

**Utility**

| Command | Description |
|---------|-------------|
| `/cm clear-client` | Remove all clients |
| `/cm clear-history` | Reset earnings history |
| `/cm add-miniboss "<name>"` | Add a miniboss name for highlighting |
| `/cm remove-miniboss "<name>"` | Remove a miniboss name (TAB autocomplete) |

</details>

<details>
<summary>!command — PartyCommands</summary>

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
</details>

<details>
<summary>Key Bindings</summary>

| Key | Action |
|-----|--------|
| `` ` `` (Grave) | Open chat with `!` prefix |
| Unbound | Open config GUI |
| Unbound | Toggle PartyCommands on/off |

</details>

---

## Configuration

<details>
<summary>Configurations</summary>

All settings managed through the YACL config GUI (`/sra gui` or via ModMenu) with 6 tabs:

| Tab | Settings |
|-----|----------|
| **EntityFire** | Toggle hidden fire |
| **PartyCommands** | Prefix, 40+ individual command toggles (separate switches for each queue floor: f1–f7, m1–m7, t1–t5), response routing (party chat / local), separator removal, auto `!mod` reply, note message, countdown sound |
| **StarredMob** | Enabled, highlight color (RGBA), render mode (Outline/Fill/Both), line width, max distance |
| **Carry** | Master toggle, client / boss / miniboss highlight (separate colors), miniboss distance, boss spawn subtitle notification (toggle + custom text), render mode, line width, max distance |
| **Helper** | Ragnarock Notifier (cast/cancel alerts, customizable messages, strength display, party announcement), Calculator (standalone `/calc` toggle) |

Config file: `.minecraft/config/sraddons.json`

Old configs from EntityFire, PartyCommands, and StarredMobHighlighter are auto-migrated on first launch.

</details>

---

## Building from Source

```bash
./gradlew clean build
```

**Requirements:** JDK 21  
**Output:** `build/libs/SR-Addons-1.5.6.jar`

---

## License

This project is licensed under **MIT License**.
