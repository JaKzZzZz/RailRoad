package org.test.railroad.queue;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.test.railroad.generation.RailGenerator;
import org.test.railroad.initialization.RailWorldState;

public class Updater {
    public static int triggerDistance = 200;
    public static boolean ENABLED_THIS_WORLD = false;
    public static void update(ServerPlayerEntity player) {
        if (!ENABLED_THIS_WORLD) return;

        ServerWorld world = player.getServerWorld();

        int px = player.getBlockX();

        RailWorldState state = RailWorldState.get(world);

        int distanceToEnd = state.lastSegmentX - px;

        if (distanceToEnd < 0)
            distanceToEnd = 0;

        if (distanceToEnd > triggerDistance) return;
        TaskQueue.schedule(() -> RailGenerator.generateSegment(world));
    }
}