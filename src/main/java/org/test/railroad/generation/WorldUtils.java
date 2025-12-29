package org.test.railroad.generation;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;

public class WorldUtils {
    public static void dryArea(
            ServerWorld world,
            BlockPos origin,
            StructureTemplate template
    ) {
        BlockPos max = origin.add(
                template.getSize().getX() - 1,
                template.getSize().getY() - 1,
                template.getSize().getZ() - 1
        );

        for (BlockPos pos : BlockPos.iterate(origin, max)) {
            BlockState state = world.getBlockState(pos);

            // 1. Сброс WATERLOGGED
            if (state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED)) {
                state = state.with(Properties.WATERLOGGED, false);
                world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
            }

            // 2. Удаление воды внутри блока
            if (world.getFluidState(pos).isIn(FluidTags.WATER)) {
                world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
            }
        }
    }
}
