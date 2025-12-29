package io.josemmo.bukkit.plugin.utils;

import io.josemmo.bukkit.plugin.YamipaPlugin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Custom task scheduler to ensure compatibility across several CraftBukkit servers.
 */
public class Scheduler {
    private final @NotNull ScheduledExecutorService externalExecutor;

    /**
     * Class constructor
     */
    public Scheduler() {
        externalExecutor = Executors.newScheduledThreadPool(6);
    }

    /**
     * Run task asynchronously
     * @param command Task to execute
     */
    public void run(@NotNull Runnable command) {
        externalExecutor.execute(command);
    }

    /**
     * Run in next game tick
     * @param command Task to execute
     */
    public void runInGame(@NotNull Runnable command) {
        if (Bukkit.isPrimaryThread()) {
            command.run();
            return;
        }
        YamipaPlugin plugin = YamipaPlugin.getInstance();
        Bukkit.getScheduler().runTask(plugin, command);
    }

    /**
     * Run interval asynchronously
     * @param  command          Task to execute
     * @param  initialDelayInMs Initial delay in milliseconds
     * @param  periodInMs       Period between executions in milliseconds
     * @return                  Scheduled future instance
     */
    public ScheduledFuture<?> runInterval(@NotNull Runnable command, long initialDelayInMs, long periodInMs) {
        return externalExecutor.scheduleAtFixedRate(command, initialDelayInMs, periodInMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Cancel all tasks and shutdown scheduler
     */
    public void stop() {
        YamipaPlugin plugin = YamipaPlugin.getInstance();
        Bukkit.getScheduler().cancelTasks(plugin);
        externalExecutor.shutdownNow();
    }
}
