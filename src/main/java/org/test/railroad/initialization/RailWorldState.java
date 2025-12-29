package org.test.railroad.initialization;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

public class RailWorldState extends PersistentState {
    public boolean enabled = false;
    public int lastSegmentX = 0;
    public int lastY = 0;
    public boolean isFirstSegment = false;
    public int spawnX = 0;
    public RailWorldState() { super(); }

    public static RailWorldState load(NbtCompound nbt) {
        RailWorldState state = new RailWorldState();
        if (nbt.contains("enabled")) state.enabled = nbt.getBoolean("enabled");
        if (nbt.contains("lastSegmentX")) state.lastSegmentX = nbt.getInt("lastSegmentX");
        if (nbt.contains("lastY")) state.lastY = nbt.getInt("lastY");
        if (nbt.contains("isFirstSegment")) state.isFirstSegment = nbt.getBoolean("isFirstSegment");
        if (nbt.contains("spawnX")) state.spawnX = nbt.getInt("spawnX");

        System.out.println("[INIT] Loaded lastSegmentEndX = " + state.lastSegmentX);
        System.out.println("[INIT] Loaded lastY = " + state.lastY);
        return state;
    }

    public static RailWorldState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                RailWorldState::load,
                RailWorldState::create,
                "untitled2_railgen"
        );
    }

    public static RailWorldState create() {
        return new RailWorldState();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putBoolean("enabled", enabled);
        nbt.putInt("lastSegmentX", lastSegmentX);
        nbt.putInt("lastY", lastY);
        nbt.putBoolean("isFirstSegment", isFirstSegment);
        nbt.putInt("spawnX", spawnX);
        return nbt;
    }
}