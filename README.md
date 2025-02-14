## Yggdrasil

This is a plugin for a Gamemode inspired by One Life!

This repository contains a plugin for managing game sessions, player skins, and integrating with a Discord bot for updating colors in the discord. Players can be assigned skins based on the number of lives they have left in the game, and there’s functionality for creating a special "last chance" session where all players dead or alive are present.

## Features

- **Player Skins**: Skins are applied based on the number of lives a player has.
- **Discord Bot Integration**: Allows for bot activation with a Discord token.
- **Culling Session**: A "last chance" session offering dead players the ability to come back to life by getting 3 kills.
- **PlaceholderAPI**: This plugin supports several placeholders. Useful for allowing other plugins compatible with PlaceholderAPI to display data from this plugin through setting config values.

### Discord Bot Setup

1. **Generate a Discord bot token** by following the instructions on [Discord Developer Portal](https://discord.com/developers/docs/intro).
2. **Set your bot token** by using the following command:

    ```bash
    /setdiscordtoken <your-token-here>
    ```

   - This will activate the Discord bot for your game plugin. Ensure that the token is correct and that your bot is authorized in your desired Discord server.
   - Once the bot starts working, it should generate 5 different roles: `dark_green`, `green`, `yellow`, `red`, and `gray`.
   - These roles are used to display how many lives you have my making your name colored in the discord.
   - In order for this feature to work properly, the players must use the `/link` command in the discord and then type the code given to them in the Minecraft server chat.

## Skin Configuration

Skins are assigned to players based on their remaining lives. You can customize skins for each player based on their UUID by placing the appropriate image files in a folder named with the player's UUID.

### Skin File Naming

For each player with a specific number of lives, the following images should be placed in a folder named after their UUID:

- `green.png` for players with 3, 4, 5 or 6 lives.
- `yellow.png` for players with 2 lives.
- `red.png` for players with 1 life.

It is also worth mentioning that it also supports a grayscaled version of the skin, however, this is automatically generated given that the green skin exists.
Another important thing to note is that it is impossible for players below 2 lives to become boogeyman.

## Placeholders

- `%yggdrasil_lives%`
- `%yggdrasil_is_boogey_man%`
- `%yggdrasil_kills%`
- `%yggdrasil_has_last_chance%`
- `%yggdrasil_lives_color%`
- `%yggdrasil_revealed_data%`
