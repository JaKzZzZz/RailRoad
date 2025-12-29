package org.test.railroad.initialization;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.test.railroad.generation.RailGenerator;

public class CompassInit {
    public static ItemStack createRailCompass(ServerWorld world) {
        ItemStack compass = new ItemStack(Items.COMPASS);

        RailWorldState state = RailWorldState.get(world);

        BlockPos target = new BlockPos(state.spawnX, 80, RailGenerator.RAIL_Z);

        NbtCompound nbt = new NbtCompound();
        nbt.put("LodestonePos", NbtHelper.fromBlockPos(target));
        nbt.putString("LodestoneDimension", "minecraft:overworld");
        nbt.putBoolean("LodestoneTracked", false);

        compass.setNbt(nbt);
        return compass;
    }
}
