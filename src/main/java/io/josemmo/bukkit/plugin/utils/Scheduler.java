package io.josemmo.bukkit.plugin.utils;

import io.josemmo.bukkit.plugin.YamipaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
    private boolean isRunning;

    /**
     * Class constructor
     */
    public Scheduler() {
        externalExecutor = Executors.newScheduledThreadPool(6);
        isRunning = true;
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
        // Are we already running in the expected thread?
        boolean alreadyInThread = Internals.IS_FOLIA ? Bukkit.isGlobalTickThread() : Bukkit.isPrimaryThread();
        if (alreadyInThread) {
            command.run();
            return;
        }

        // Run in game thread
        YamipaPlugin plugin = YamipaPlugin.getInstance();
        if (Internals.IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().run(plugin, __ -> command.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, command);
        }
    }

    /**
     * Run in next game tick
     * @param command  Task to execute
     * @param location Location to find region task scheduler (for Folia)
     */
    public void runInGame(@NotNull Runnable command, @NotNull Location location) {
        Runnable wrappedCommand = () -> {
            if (isRunning) {
                command.run();
            }
        };

        // Are we already running in the expected thread?
        boolean alreadyInThread = Internals.IS_FOLIA ? Bukkit.isOwnedByCurrentRegion(location) : Bukkit.isPrimaryThread();
        if (alreadyInThread) {
            wrappedCommand.run();
            return;
        }

        // Run in another thread
        YamipaPlugin plugin = YamipaPlugin.getInstance();
        if (Internals.IS_FOLIA) {
            Bukkit.getRegionScheduler().execute(plugin, location, wrappedCommand);
        } else {
            Bukkit.getScheduler().runTask(plugin, wrappedCommand);
        }
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
        isRunning = false;

        // Cancel external tasks
        externalExecutor.shutdownNow();

        // Cancel game tasks
        YamipaPlugin plugin = YamipaPlugin.getInstance();
        if (Internals.IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
            // Region-specific tasks are skipped using the `isRunning` flag
        } else {
            Bukkit.getScheduler().cancelTasks(plugin);
        }
    }
}
