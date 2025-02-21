package dev.heypr.yggdrasil.misc;

import dev.heypr.yggdrasil.Yggdrasil;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class WaitForCompletion<T> {
    private static final long INTERVAL = 5L;

    private final List<T> responses = new ArrayList<>();
    private final int expectedAmount;
    private final int waitLimitSeconds;

    private double timeElapsed = 0D;

    public WaitForCompletion(final int expectedAmount) {
        this(expectedAmount, 30);
    }

    public WaitForCompletion(final int expectedAmount, final int waitLimitSeconds) {
        this.expectedAmount = expectedAmount;
        this.waitLimitSeconds = waitLimitSeconds;
    }

    public void accept(final T response) {
        synchronized (this.responses) {
            this.responses.add(response);
        }
    }

    public void wait(final Consumer<List<T>> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (timeElapsed > waitLimitSeconds) {
                    super.cancel();
                    return;
                }

                if (responses.size() >= expectedAmount) {
                    callback.accept(responses);
                    super.cancel();
                    return;
                }

                timeElapsed += INTERVAL / 20;
            }
        }.runTaskTimer(Yggdrasil.plugin, INTERVAL, INTERVAL);
    }
}