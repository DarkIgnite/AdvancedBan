# AdvancedBan (LibertyBans Alts Edition - Paper 1.21+ & Java 21)

An enhanced fork of [DevLeoko/AdvancedBan](https://github.com/DevLeoko/AdvancedBan) bringing **LibertyBans-style Alt Account Detection**, **Paper 1.21+ support**, and modern **Java 17/21** compatibility.

![Minecraft Version 1.7-1.21.x](https://img.shields.io/badge/Minecraft-1.7--1.21.x-brightgreen.svg)
![Java Version 17/21](https://img.shields.io/badge/Java-17%20%7C%2021-orange.svg)
![license GPL-3.0](https://img.shields.io/badge/license-GPL--3.0-lightgrey.svg)

---

## 🌟 Fork Highlights & New Features

### 1. 🔍 LibertyBans-Style Alt Account Detection (`/alts`)
AdvancedBan originally lacked historical IP tracking and comprehensive alt detection. This fork ports the powerful multi-tier alt detection engine and formatting from [LibertyBans](https://github.com/A248/LibertyBans):
- **Persistent IP & UUID History Tracking:** Logs every IP address associated with player connections in MySQL or HSQLDB (`Addresses` table).
- **Two-Tier Possibility Levels (`NORMAL` vs `STRICT`):**
  - `NORMAL` (Strong Possibility): Matches when accounts share the exact same current/most recent IP address.
  - `STRICT` (Mere Possibility): Matches when accounts share a common historical IP address from the past.
- **Dynamic Punishment-Based Formatting & Colors:**
  - **Banned Alts:** Displayed in red italics (`&c&o`).
  - **Muted Alts:** Displayed in yellow italics (`&e&o`).
  - **Clean / Unpunished Alts:** Displayed in white/gray italics (`&7&o`).
- **Interactive Hover & Click Details:** Hover over an alt to see their UUID, last seen timestamp, and current punishment status; click to run punishment checks.
- **Staff Join Alerts:** When a player joins, staff with `ab.alts.notify` receive an alert showing known alt accounts and whether any are currently banned or muted.
- **Staff Privacy Protection (`ab.alts.ip`):** IP addresses in `/alts` output are masked (e.g. `192.168.***.***`) unless the staff member has the `ab.alts.ip` permission.
- **Pagination Support:** Support for `/alts <player> [page]` for accounts with many associated alts.

### 2. ⚡ Paper 1.21.x & Modern Minecraft Compatibility
- Built and tested with **Java 21** and targeted for modern **Paper 1.21.x** backend servers.
- Fully compatible with BungeeCord / Waterfall / Velocity setups where backend handles punishments.
- Modernized Maven build configuration, plugins, and dependencies.

---

## 💻 Commands & Permissions

| Command | Aliases | Permission | Description |
| :--- | :--- | :--- | :--- |
| `/alts <player> [page]` | `/alt`, `/altcheck` | `ab.alts` | Scans and lists all associated alt accounts with possibility levels and punishment status. |
| *(View unmasked IPs)* | - | `ab.alts.ip` | Displays unmasked IP addresses in `/alts` lookup results. |
| *(Join Notifications)* | - | `ab.alts.notify` | Receives automatic staff alerts when players with known alts connect. |
| `/check <player>` | - | `ab.check` | Shows active punishments and now also displays total detected alt count. |

---

## ⚙️ Configuration

### `config.yml` additions:
```yaml
Alts:
  AlertOnJoin: true         # Notify staff when a player with alts connects
  AlertOnlyPunished: false  # Only notify staff if at least one alt is banned/muted
  AlertLimit: 5             # Maximum number of alts to display in join notifications
  PerPage: 8                # Number of alts per page in /alts
```

### `Messages.yml` additions:
Includes full LibertyBans-style customizable messages for `Alts.Header`, `Alts.Formatting.Layout`, `Alts.Formatting.NameDisplay`, `Alts.Formatting.Normal`, `Alts.Formatting.Strict`, `Alts.AlertOnJoin`, and `Check.Alts`.

---

## 🔨 Building from Source

### Prerequisites
- JDK 17 or JDK 21
- Apache Maven 3.8+

```bash
git clone https://github.com/DarkIgnite/AdvancedBan.git
cd AdvancedBan
mvn clean package -DskipTests
```

The compiled plugin jar will be generated at:
- `bundle/target/AdvancedBan-Bundle-2.3.0-RELEASE.jar` (Universal plugin for Bukkit/Paper and BungeeCord)
- `bukkit/target/AdvancedBan-Bukkit-2.3.0-RELEASE.jar` (Bukkit/Paper standalone)

---

## 📜 Original Credits
- Original plugin created by [DevLeoko](https://github.com/DevLeoko/AdvancedBan).
- Alt detection concepts and formatting inspired by [A248/LibertyBans](https://github.com/A248/LibertyBans).
- Licensed under [GNU General Public License v3.0](LICENSE).
