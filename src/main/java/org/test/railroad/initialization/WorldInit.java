package org.test.railroad.initialization;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;
import org.test.railroad.analysis.TerrainAnalyzers;
import org.test.railroad.queue.Updater;
import org.test.railroad.generation.RailGenerator;
import org.test.railroad.util.RailLog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WorldInit {

    public static boolean firstInit = false;

    public static void init() {

        ServerWorldEvents.LOAD.register((server, world) -> {

            if (world.getRegistryKey() != World.OVERWORLD) return;
            isBrandNewWorld(server);

            ServerWorld overworld = server.getOverworld();
            RailWorldState state = overworld.getPersistentStateManager().getOrCreate(
                    RailWorldState::load,
                    RailWorldState::create,
                    "untitled2_railgen"
            );
            if (state.lastSegmentX == 0) TerrainAnalyzers.getStartCords(world, 0, RailGenerator.RAIL_Z);
            System.out.println("[INIT] Loaded lastSegmentEndX = " + state.lastSegmentX);

            Updater.ENABLED_THIS_WORLD = state.lastSegmentX > 0 || firstInit;
            System.out.println("ENABLED_THIS_WORLD:" + Updater.ENABLED_THIS_WORLD);
        });
    }

    public static void isBrandNewWorld(MinecraftServer server) {
        Path regionDir = server.getSavePath(WorldSavePath.ROOT).resolve("region");

        if (!Files.exists(regionDir) || !Files.isDirectory(regionDir)) {
            RailLog.d("[WORLD-DETECT] region folder missing → NEW WORLD");
            firstInit = true;
            return;
        }

        try {
            try (var files = Files.list(regionDir)) {
                boolean hasRegionFiles = files.anyMatch(path -> path.getFileName().toString().endsWith(".mca"));

                if (!hasRegionFiles) {
                    RailLog.d("[WORLD-DETECT] region exists but empty → NEW WORLD");
                    firstInit = true;
                } else {
                    RailLog.d("[WORLD-DETECT] region has mca files → OLD WORLD");
                    firstInit = false;
                }
            }
        } catch (IOException e) {
            System.err.println("[WORLD-DETECT] Error reading region folder, assuming OLD WORLD" + e.getMessage());
            firstInit = false;
        }
    }
}



