package dev.heypr.yggdrasil.misc;

import dev.heypr.yggdrasil.Yggdrasil;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.scheduler.CraftScheduler;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public final class BukkitSchedulerWrapper extends CraftScheduler {
    private final BukkitScheduler scheduler;

    public BukkitSchedulerWrapper() {
        this.scheduler = Bukkit.getScheduler();
    }

    @Override
    public BukkitTask runTaskLater(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay) throws IllegalArgumentException {
        return this.runTaskLater(plugin, runnable, delay, false);
    }

    @Override
    public BukkitTask runTask(Plugin plugin, Runnable runnable) throws IllegalArgumentException {
        return scheduler.runTask(plugin, runnable);
    }

    private void addToCancelMap(final BukkitTask task, final boolean cancelOnSessionStop) {
        Yggdrasil.plugin.getCancelOnShutdown().add(task);

        if (cancelOnSessionStop)
            Yggdrasil.plugin.getCancelOnSessionStop().add(task);
    }

    public BukkitTask runTaskLater(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay, boolean cancelOnSessionStop) throws IllegalArgumentException {
        final BukkitTask task = scheduler.runTaskLater(plugin, runnable, delay);

        addToCancelMap(task, cancelOnSessionStop);

        return task;
    }
}