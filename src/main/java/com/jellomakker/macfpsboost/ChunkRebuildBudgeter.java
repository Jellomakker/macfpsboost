package com.jellomakker.macfpsboost;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChunkRebuildBudgeter {
    private final AtomicInteger budget = new AtomicInteger(2); // rebuilds per frame default
    private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

    public void enqueue(Runnable rebuildTask) {
        queue.add(rebuildTask);
    }

    public void setBudget(int n) { budget.set(Math.max(0, n)); }

    public void increaseBudget(int delta) { budget.addAndGet(delta); }

    public void decreaseBudget(int delta) { budget.addAndGet(-Math.abs(delta)); }

    public int drainAndRun() {
        int allowed = Math.max(0, budget.get());
        int ran = 0;
        while (ran < allowed) {
            Runnable r = queue.poll();
            if (r == null) break;
            try { r.run(); } catch (Throwable t) { t.printStackTrace(); }
            ran++;
        }
        return ran;
    }
}
