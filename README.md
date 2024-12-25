# OBSCraft

Connect Minecraft directly to OBS Studio using the OBS WebSocket plugin. Allows using command blocks, datapacks, and plugins to trigger various actions in OBS Studio.

This works by listening for a specially formatted translatable chat message, which is decoded then acted upon. You can generate a tellraw command using the `/obscraft` command.

**Right now this is in active development and still in alpha, please report any bugs you find!**

## Features

- Set the active scene from in-game!
- Update a Text source's text.
- Set a source's visibility in a given scene.
- Set the visible status of a filter on a source.

Each of these option support both typing literal names as well as looking up names from a scoreboard objective. You can either search for the first player with a specific score or search for the score of a specific player. (TODO phrase that better lol)

**NOTE: To have the client "see" a scoreboard objective for use in this mod, the scoreboard must be attached to a display. It can be any display, even if it's not visible to the player, like a different teams's sidebar.**

## Commands

- `/obscraft list` - List all OBS connections by ID. Gives connection status for each one.
- `/obscraft reconnect <id>` - Reconnect to an OBS connection by ID.
- There are more commands to generate tellraw commands, but the in-game documentation is setup to show them all as you type.

## Setup

### OBS Studio

First, we need to make sure the websocket server is enabled.

In OBS go to `Tools -> WebSocket Server Settings`.

Make sure of the following:
- `Enable WebSocket Server` is checked.
- `Enable Authentification` is checked.
- Note down the `Server Port` and `Server Password` Press `Show Connect Info` to see it.

### Minecraft

Note: The mod required the [Architectury API](https://modrinth.com/mod/architectury-api) installed to load.

Launch the game once to generate the config file, it should be with other configuration files for you mods.

It should look like this:
```json
{
  "connections": [
    {
      "ID": "default",
      "host": "127.0.0.1",
      "port": 4455,
      "password": "password"
    }
  ]
}
```

- `ID` is a unique identifier for the connection. You can have multiple connections to different OBS instances.
- `host` is the IP address of the computer running OBS. If it's on the same computer, use `127.0.0.1`
- `port` is the port number from the OBS WebSocket settings.
- `password` is the password from the OBS WebSocket settings.

For a simple setup just make sure port and password match the OBS settings.

After that restart the game and you should be good to go! Make sure everything is connected by running `/obscraft list` and `default` should show up under Available Connections.