package dev.heypr.yggdrasil.misc;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Predicate;

public final class ColorManager {
    public enum Colors {
        DARK_GREEN(TextColor.color(0, 170, 0)),
        GREEN(TextColor.color(85, 255, 85)),
        YELLOW(TextColor.color(255, 255, 85)),
        RED(TextColor.color(255, 85, 85)),
        GRAY(TextColor.color(170, 170, 170));

        private final TextColor rgb;

        Colors(final TextColor rgb) {
            this.rgb = rgb;
        }

        public TextColor getRgb() {
            return this.rgb;
        }

        public Color getColor() {
            return new Color(this.rgb.red(), this.rgb.green(), this.rgb.blue());
        }

        public static Colors from(final int lives) {
            switch (lives) {
                case 5, 6:
                    return Colors.DARK_GREEN;
                case 3, 4:
                    return Colors.GREEN;
                case 2:
                    return Colors.YELLOW;
                case 1:
                    return Colors.RED;
                case 0:
                case -1:
                    return Colors.GRAY;
            }

            return null;
        }

        public String getRoleName() {
            return this.name().toLowerCase();
        }
    }

    public static ChatColor getColor(int lives) {
        final Colors colors = Colors.from(lives);

        if (colors != null)
            return ChatColor.valueOf(colors.name());

        return null;
    }

    private static void grayScalePng(final File originalFile, final File saveTo) {
        try {
            final BufferedImage original = ImageIO.read(originalFile);

            // Find the bounding box of the non-transparent skin areas
            int minX = original.getWidth(), minY = original.getHeight();
            int maxX = 0, maxY = 0;

            for (int y = 0; y < original.getHeight(); y++) {
                for (int x = 0; x < original.getWidth(); x++) {
                    int rgba = original.getRGB(x, y);
                    int alpha = (rgba >> 24) & 0xFF;

                    // Check if pixel is not fully transparent
                    if (alpha > 0) {
                        if (x < minX) minX = x;
                        if (x > maxX) maxX = x;
                        if (y < minY) minY = y;
                        if (y > maxY) maxY = y;
                    }
                }
            }

            // Create a new image with just the skin bounds
            final BufferedImage grayScaleImage = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);

            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    final int rgb = original.getRGB(x, y);
                    final int alpha = (rgb >> 24) & 0xFF;

                    if (alpha > 0) {
                        final int red = (rgb >> 16) & 0xFF;
                        final int green = (rgb >> 8) & 0xFF;
                        final int blue = rgb & 0xFF;

                        final int gray = (int) (0.21 * red + 0.72 * green + 0.07 * blue);
                        final int newRGB = (alpha << 24) | (gray << 16) | (gray << 8) | gray;

                        grayScaleImage.setRGB(x, y, newRGB);
                    } else
                        grayScaleImage.setRGB(x, y, rgb);
                }
            }

            ImageIO.write(grayScaleImage, "png", saveTo);
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    public static File getSkinFile(final Yggdrasil plugin, OfflinePlayer player, final PlayerData data) {
        return getSkinFile(plugin, player, Colors.from(data.getLives()));
    }

    public static File getSkinFile(final Yggdrasil plugin, OfflinePlayer player, final Colors colors) {
        final UUID uuid = player.getUniqueId();
        final File dataFolder = plugin.getDataFolder();
        final File userFolder = new File(dataFolder, uuid.toString());

        if (!userFolder.exists() || !userFolder.isDirectory())
            return null;

        String colorName = colors.getRoleName();
        File skinFile = new File(userFolder, colorName + ".png");
        final Predicate<File> exists = file -> file != null && file.exists();

        if (colors == Colors.DARK_GREEN && !exists.test(skinFile)) {
            colorName = colorName.replace("dark_", "");
            skinFile = new File(userFolder, colorName + ".png");
        }

        if (!exists.test(skinFile)) {
            if (colors == Colors.GRAY) {
                final File greenSkinFile = getSkinFile(plugin, player, Colors.GREEN);

                if (exists.test(greenSkinFile)) {
                    final File saveTo = new File(userFolder, Colors.GRAY.getRoleName() + ".png");

                    grayScalePng(greenSkinFile, saveTo);

                    return saveTo;
                }
            }

            return null;
        }

        return skinFile;
    }

    private static void setSuffix(final Player player, final int lives) {
        final Scoreboard scoreboard = Yggdrasil.plugin.getScoreboard();

        if (scoreboard == null)
            return;

        final ChatColor color = getColor(lives);
        final String teamName = lives + "_team";

        Team team = scoreboard.getTeam(teamName);

        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);

            team.setColor(color);
        }

        team.addEntry(player.getName());
    }

    public static void setTabListName(final Player player, final PlayerData data) {
        final String livesStr = data.hasLastChance() ? " (Last Chance)" : " (" + data.getDisplayLives() + " lives)";
        final Component livesComp = Component.text(livesStr).decoration(TextDecoration.ITALIC, false).color(TextColor.color(128, 128, 128));
        final ColorManager.Colors colors = ColorManager.Colors.from(data.getLives());

        setSuffix(player, data.getLives());

        player.playerListName(player.name().color(colors.getRgb()).append(livesComp));
    }
}