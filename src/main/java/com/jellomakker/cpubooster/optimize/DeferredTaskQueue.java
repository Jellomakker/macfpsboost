package com.jellomakker.cpubooster.optimize;

import com.jellomakker.cpubooster.CpuBoosterMod;
import java.util.*;

/**
 * Spreads heavy work (IO, caching) across multiple ticks to avoid frame spikes.
 * Tasks are executed only after world is initialized and budget is available.
 */
public class DeferredTaskQueue {
    private final Queue<DeferredTask> taskQueue = new LinkedList<>();
    private volatile int budgetMs = 5000; // Default: 5ms per tick budget
    private volatile boolean worldReady = false;
    private volatile long tickStartNs = 0;

    public interface DeferredTask {
        /**
         * Execute task. Should return true if complete, false if needs to continue.
         */
        boolean execute();

        /**
         * Human-readable name for logging.
         */
        String getName();
    }

    public void setTickBudget(int ms) {
        this.budgetMs = Math.max(1, Math.min(ms, 20)); // Clamp 1-20ms
    }

    public void setWorldReady(boolean ready) {
        this.worldReady = ready;
    }

    public void queueTask(DeferredTask task) {
        if (task != null) {
            taskQueue.offer(task);
        }
    }

    public void onTick() {
        if (!worldReady || taskQueue.isEmpty()) {
            return;
        }

        tickStartNs = System.nanoTime();
        long budgetNs = budgetMs * 1_000_000L;

        while (!taskQueue.isEmpty()) {
            long elapsedNs = System.nanoTime() - tickStartNs;
            if (elapsedNs > budgetNs) {
                // Budget exhausted
                break;
            }

            DeferredTask task = taskQueue.peek();
            try {
                boolean complete = task.execute();
                if (complete) {
                    taskQueue.poll();
                    CpuBoosterMod.LOGGER.debug("Deferred task completed: {}", task.getName());
                } else {
                    // Task needs more time; yield
                    break;
                }
            } catch (Throwable e) {
                // Task crashed; remove it and log
                taskQueue.poll();
                CpuBoosterMod.LOGGER.warn("Deferred task failed ({}): {}", task.getName(), e.getMessage());
                CpuBoosterMod.LOGGER.debug("Deferred task error details:", e);
            }
        }
    }

    public int getQueuedTaskCount() {
        return taskQueue.size();
    }

    public void clear() {
        taskQueue.clear();
    }
}
