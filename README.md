# AdvancedBanReworked (Paper 1.21+ & LibertyBans Modern Architecture)

An enhanced rework fork of [DevLeoko/AdvancedBan](https://github.com/DevLeoko/AdvancedBan) bringing **Ban Disguise Mode (`/bandisguise`)**, **Silent Punishment Flags (`-s`)**, **Native Geyser/Floodgate (Bedrock) Support**, **Paper 1.21+ & Java 21 compatibility**, and **LibertyBans-style Alt Detection (`/alts`)**.

![Minecraft Version 1.7-1.21.x](https://img.shields.io/badge/Minecraft-1.7--1.21.x-brightgreen.svg)
![Java Version 17/21](https://img.shields.io/badge/Java-17%20%7C%2021-orange.svg)
![Paper Compatible](https://img.shields.io/badge/Paper-1.21.x%20Ready-blue.svg)
![license GPL-3.0](https://img.shields.io/badge/license-GPL--3.0-lightgrey.svg)

---

## 🌟 Rework Highlights & New Features

### 1. 🎭 Ban Disguise Mode (`/bandisguise` or `/bdisguise`)
Protect staff privacy and prevent player retaliation:
- **Public Masking:** When disguise mode is enabled for a staff member, any punishment they execute (`/ban`, `/tempban`, `/mute`, `/tempmute`, `/kick`, `/ipban`, `/warn`, `/unban`) will broadcast to players and show on the target's disconnect screen as **`CONSOLE`** (or customized name in `config.yml`).
- **Internal Staff Accountability:** The database (`PUNISHMENTS`, `PUNISHMENTHISTORY`), `/banlist`, `/history`, and `/check` **always preserve and display the real staff member's name** (e.g. `DarkIgnite`), preventing staff abuse and keeping full accountability for owners and head admins.
- **Persistent State:** Disguise status is saved per-staff and persists across server restarts.
- **Commands:**
  - `/bandisguise` or `/bandisguise toggle` - Toggle disguise mode on/off.
  - `/bandisguise on` - Enable disguise mode.
  - `/bandisguise off` - Disable disguise mode.
  - `/bandisguise status` - Check current disguise status.
- **Permission:** `ab.bandisguise`

---

### 2. 🤫 Silent Punishment Flag (`-s`)
Execute punishments quietly without spamming global chat:
- Supported across commands: `/ban -s <Player> [Reason]`, `/tempban -s <Player> <Duration> [Reason]`, `/mute -s <Player> [Reason]`, `/kick -s <Player> [Reason]`, `/unban -s <Player>`, etc.
- Regular players receive **no broadcast**.
- Staff with `ab.notify.silent` (or `ab.undoNotify.silent`) receive a quiet notification tagged with `&7[Silent]`.
- Output is logged to console with `[Silent]`.

---

### 3. 🎮 Geyser & Floodgate (Bedrock Edition) Native Compatibility
- **Mojang Lookup Bypass:** Automatically identifies Bedrock players with the `.` prefix (e.g. `.EeryCandy5337`) and prevents sending external REST queries to Mojang authlib APIs (which previously threw 404 errors).
- **Safe Banlist Synchronization:** Protected `InternalListener` calls to Paper's `CraftProfileBanList` (`pardon` and `addBan`) from uncaught `MinecraftClientHttpException`, ensuring Bedrock players can be banned, unbanned, and tempbanned without server exceptions.

---

### 4. 🐛 Critical Bug Fixes & Paper 1.21 Stability
- **Fixed `/banlist` NullPointerException:** Fixed a critical bug in `ListProcessor` where modifying an `ArrayList` during Java Stream iteration caused remaining slots to become `null`, crashing `/banlist` when expired punishments were present.
- Added strict null-safety and safe removal across list iteration and pagination.

---

### 5. 🔄 Seamless Automatic Legacy Database Migration
- On first startup under `AdvancedBanReworked`, if existing data is detected in `plugins/AdvancedBan/data/` (`storage.script`, `storage.log`), the plugin **automatically imports all active punishments, history, and configuration files** to `plugins/AdvancedBanReworked/`.
- Original legacy data is preserved safely as a backup. Zero manual database transfer required!

---

### 6. 🔍 LibertyBans-Style Alt Account Detection (`/alts`)
- **Persistent IP & UUID History:** Logs connection addresses in MySQL/HSQLDB.
- **Two-Tier Possibility:** `NORMAL` (exact current IP) vs `STRICT` (historical IP overlap).
- **Visual Status:** Banned alts in red (`&c&o`), muted alts in yellow (`&e&o`), clean in gray (`&7&o`).
- **Staff Join Alerts:** Alerts staff with `ab.alts.notify` when players with known alts connect.
- **Permission:** `ab.alts`, `ab.alts.ip`, `ab.alts.notify`.

---

### 7. 🛡️ Guard IP Geolocation Analysis (`/guard analyze <player>`)
- Resolves player IP to Country, City, Region, ISP, and Timezone.
- Calculates and displays City Accuracy Percentage (e.g. `Bekasi (88% keakuratan)`).
- Privacy masking with `ab.guard.ip` permission.
- **Permission:** `ab.guard`.

---

## 💻 Commands & Permissions Summary

| Command | Aliases | Permission | Description |
| :--- | :--- | :--- | :--- |
| `/bandisguise [toggle\|on\|off\|status]` | `/bdisguise` | `ab.bandisguise` | Toggles ban disguise mode (shows CONSOLE to players). |
| `/ban (-s) <Player> [Reason]` | - | `ab.ban` | Permanently bans a player (supports `-s` silent flag). |
| `/tempban (-s) <Player> <Time> [Reason]` | - | `ab.tempban` | Temporarily bans a player (supports `-s` silent flag). |
| `/mute (-s) <Player> [Reason]` | - | `ab.mute` | Mutes a player (supports `-s` silent flag). |
| `/tempmute (-s) <Player> <Time> [Reason]` | - | `ab.tempmute` | Temporarily mutes a player (supports `-s` silent flag). |
| `/kick (-s) <Player> [Reason]` | - | `ab.kick` | Kicks a player (supports `-s` silent flag). |
| `/unban (-s) <Player>` | - | `ab.ban.undo` | Unbans a player (supports `-s` silent flag). |
| `/unmute (-s) <Player>` | - | `ab.mute.undo` | Unmutes a player (supports `-s` silent flag). |
| `/banlist [page]` | - | `ab.banlist` | Lists all active punishments (shows real staff names). |
| `/history <Player> [page]` | - | `ab.history` | Shows punishment history of a player. |
| `/guard analyze <Player>` | `/abguard` | `ab.guard` | Analyzes IP, country, city accuracy, and ISP. |
| `/alts <Player> [page]` | `/alt`, `/altcheck` | `ab.alts` | Scans and lists all associated alt accounts. |
| `/check <Player>` | - | `ab.check` | Displays detailed punishment and alt summary. |
| *(Silent Notification)* | - | `ab.notify.silent` | Receives silent punishment notifications. |

---

## ⚙️ Configuration Additions (`config.yml`)

```yaml
# Ban Disguise settings (/bandisguise)
# When active for a staff member, public punishment announcements and kick screens show DisguisedName
# Internal punishment records, /banlist, /history, and /check will still display the real staff name.
Disguise:
  Enabled: true
  DisguisedName: "CONSOLE"

# Alt accounts detection and notification settings (LibertyBans-style)
Alts:
  AlertOnJoin: true         # Notify staff when a player with alts connects
  AlertOnlyPunished: false  # Only notify staff if at least one alt is banned/muted
  AlertLimit: 5             # Maximum number of alts to display in join notifications
  PerPage: 10               # Number of alts per page in /alts
```

---

## 🔨 Building from Source

### Prerequisites
- JDK 17 or JDK 21
- Apache Maven 3.8+

```bash
git clone https://github.com/DarkIgnite/AdvancedBan.git
cd AdvancedBan
mvn clean package
```

The compiled shaded plugin jar will be generated at:
- `bundle/target/AdvancedBan-Bundle-2.3.0-RELEASE.jar` (Universal plugin for Bukkit/Paper and BungeeCord)

---

## 📜 Credits & License
- Original plugin created by [DevLeoko](https://github.com/DevLeoko/AdvancedBan).
- Rework, Geyser/Floodgate integration, Ban Disguise mode, and LibertyBans alts maintained by [DarkIgnite](https://github.com/DarkIgnite/AdvancedBan).
- Licensed under [GNU General Public License v3.0](LICENSE).
