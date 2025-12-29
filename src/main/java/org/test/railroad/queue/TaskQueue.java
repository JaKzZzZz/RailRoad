package org.test.railroad.queue;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import java.util.LinkedList;
import java.util.Queue;

public class TaskQueue {
    private static final Queue<Runnable> TASKS = new LinkedList<>();

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            int maxPerTick = 1;
            for (int i = 0; i < maxPerTick && !TASKS.isEmpty(); i++) {
                TASKS.poll().run();
            }
        });
    }

    public static void schedule(Runnable task) {
        TASKS.offer(task);
    }
}