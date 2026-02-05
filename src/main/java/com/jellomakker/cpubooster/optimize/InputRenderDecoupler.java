package com.jellomakker.cpubooster.optimize;

import com.jellomakker.cpubooster.CpuBoosterMod;
import com.jellomakker.cpubooster.config.CpuBoosterConfig;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Decouples input processing from render loop.
 * Mac's input handling can sometimes stall the render thread.
 * This defers expensive input operations to the end of the tick.
 */
public class InputRenderDecoupler {
    private final Queue<InputTask> deferredTasks = new LinkedList<>();

    /**
     * Represents a deferred input task.
     */
    public interface InputTask {
        void execute();
    }

    public void onTick(CpuBoosterConfig cfg) {
        if (!cfg.enableInputRenderDecoupling) {
            deferredTasks.clear();
            return;
        }

        try {
            // Process deferred input tasks at end of tick
            while (!deferredTasks.isEmpty()) {
                InputTask task = deferredTasks.poll();
                if (task != null) {
                    task.execute();
                }
            }
        } catch (Exception e) {
            CpuBoosterMod.LOGGER.warn("InputRenderDecoupler error: {}", e.getMessage());
        }
    }

    /**
     * Queue an input task for deferred execution.
     */
    public void deferInputTask(InputTask task) {
        if (task != null) {
            deferredTasks.offer(task);
        }
    }

    public int getDeferredTaskCount() {
        return deferredTasks.size();
    }

    public void clear() {
        deferredTasks.clear();
    }
}
