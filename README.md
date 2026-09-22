# RandomSpawn

A Paper/Bukkit Minecraft plugin that teleports players to a random location picked from a predefined list — on join, on respawn, and via `/spawn`.

## Features

- **Random spawn on join** — send players to a random predefined location when they join the server.
- **First-join-only mode** — optionally limit the on-join teleport to a player's very first join, instead of every login.
- **Random respawn** — replace the vanilla bed/anchor respawn with a random location from the list.
- **`/spawn` command** — lets players teleport to a random location on demand.
- **`/spawn reload`** — reloads `config.yml` without restarting the server.
- **Configurable message** — a customizable, color-code-enabled message sent after each teleport.
- **Debug logging** — optional verbose console logging to help diagnose join/respawn behavior.

## Requirements

- A Paper server (build `26.2` or compatible) running **Java 25**.
- Maven, if building from source.

## Installation

1. Build the plugin (see [Building](#building)) or use the pre-built jar in `target/RandomSpawn-1.0.0.jar`.
2. Drop the jar into your server's `plugins/` folder.
3. Start (or restart) the server. This generates `plugins/RandomSpawn/config.yml`.
4. Edit `config.yml` to add your own spawn locations (see [Configuration](#configuration)).
5. Run `/spawn reload` or restart the server to apply changes.

## Building

```bash
mvn clean package
```

The compiled jar will be at `target/RandomSpawn-1.0.0.jar`.

## Configuration

`config.yml`:

```yaml
locations:
  - world: world
    x: 0.5
    y: 100.0
    z: 0.5
    yaw: 0.0
    pitch: 0.0
  - world: world
    x: 250.5
    y: 70.0
    z: -120.5
    yaw: 90.0
    pitch: 0.0
  - world: world_nether
    x: 10.5
    y: 64.0
    z: 10.5
    yaw: 0.0
    pitch: 0.0

settings:
  teleport-on-join: true
  first-join-only: false
  teleport-on-respawn: true
  teleport-message: "&aYou've been sent to a random spawn point!"
  debug: false
```

### `locations`

A list of possible spawn points. One is picked at random each time a player is sent to spawn (on join, on respawn, or via `/spawn`). You can add as many as you like.

| Field   | Required | Description                                      |
|---------|----------|---------------------------------------------------|
| `world` | Yes      | Must match an existing world name on your server. |
| `x`     | Yes      | X coordinate.                                     |
| `y`     | Yes      | Y coordinate.                                     |
| `z`     | Yes      | Z coordinate.                                     |
| `yaw`   | No       | Facing direction (defaults to `0.0`).             |
| `pitch` | No       | Up/down look angle (defaults to `0.0`).           |

If a location references an unknown world, or is missing `world`, it's skipped and a warning is logged. If the list ends up empty, `/spawn` and the automatic teleports do nothing until you add valid entries.

### `settings`

| Setting               | Default | Description                                                                                     |
|------------------------|---------|---------------------------------------------------------------------------------------------------|
| `teleport-on-join`     | `true`  | Teleport players to a random location whenever they join.                                        |
| `first-join-only`      | `false` | If `true`, `teleport-on-join` only applies to a player's very first join ever.                   |
| `teleport-on-respawn`  | `true`  | Send players to a random location on respawn, instead of their bed/respawn anchor.               |
| `teleport-message`     | *(set)* | Message shown after a teleport. Supports `&` color codes. Leave as `""` to disable.              |
| `debug`                | `false` | Logs detailed `[debug]` lines to console for join/respawn/spawn decisions.                       |

## Commands

| Command         | Description                                       | Permission           |
|-----------------|----------------------------------------------------|-----------------------|
| `/spawn`        | Teleport yourself to a random predefined location. | `randomspawn.use`     |
| `/spawn reload` | Reload `config.yml` without restarting.            | `randomspawn.reload`  |

## Permissions

| Permission             | Default | Description                              |
|-------------------------|---------|-------------------------------------------|
| `randomspawn.use`       | `true`  | Allows use of `/spawn`.                   |
| `randomspawn.reload`    | `op`    | Allows reloading the config via `/spawn reload`. |

## Notes

- Respawn handling runs at `EventPriority.HIGHEST` and includes a safety net (`PlayerPostRespawnEvent`) that force-corrects the player's position if another plugin overrides the respawn location afterward. If you notice this happening a lot, check for other spawn-related plugins and their load order.
- If troubleshooting unexpected teleport behavior, enable `debug: true`, reproduce the issue, and check the console for lines starting with `[debug]`.

## License

Not specified — add one if you plan to distribute this plugin.
