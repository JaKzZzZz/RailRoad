package org.test.railroad.generation;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;

public class BuildSupports {
    static void placeSupport(ServerWorld world, BlockPos finalPlacePos, TemplateChoice choice, StructureTemplate template) {
        int[] supportZOffsets = {5, template.getSize().getZ() - 6};
        int[] supportZStartOffsets = {2, 3, 4, 18, 19, 20};

        if (choice.start()) {
            int x1 = finalPlacePos.getX() + template.getSize().getX() - 2;
            int x2 = finalPlacePos.getX() + template.getSize().getX() - 3;
            int x3 = finalPlacePos.getX() + 2;
            int x4 = finalPlacePos.getX() + 3;
            int x5 = finalPlacePos.getX() + 4;
            int x6 = finalPlacePos.getX() + template.getSize().getX() - 4;


            for (int zOffset : supportZStartOffsets) {

                // четыре точки:
                buildSupport(world, x1, finalPlacePos.getY(), finalPlacePos.getZ() + zOffset);
                buildSupport(world, x2, finalPlacePos.getY(), finalPlacePos.getZ() + zOffset);

                buildSupport(world, x3, finalPlacePos.getY(), finalPlacePos.getZ() + zOffset);
                buildSupport(world, x4, finalPlacePos.getY(), finalPlacePos.getZ() + zOffset);
                buildSupport(world, x5, finalPlacePos.getY(), finalPlacePos.getZ() + zOffset);
                buildSupport(world, x6, finalPlacePos.getY(), finalPlacePos.getZ() + zOffset);
            }
        }

        for (int zOffset : supportZOffsets) {
            int supportX = choice.up() ? finalPlacePos.getX() + template.getSize().getX() - 1 : finalPlacePos.getX();
            int supportZ = finalPlacePos.getZ() + zOffset;
            int supportY = finalPlacePos.getY();
            buildSupport(world, supportX, supportY, supportZ);
        }
    }

    private static void buildSupport(ServerWorld world, int x, int startY, int z) {
        int y = startY - 1;
        BlockPos pos = new BlockPos(x, y, z);

        while (world.isAir(pos) || !(world.getBlockState(pos).getFluidState().isEmpty()) || world.getBlockState(pos).isReplaceable()) {
            world.setBlockState(pos, Blocks.SPRUCE_LOG.getDefaultState(), Block.NOTIFY_LISTENERS);
            y--;
            pos = new BlockPos(x, y, z);
        }
    }

    static void buildGroundUpToTemplate(
            ServerWorld world,
            StructureTemplate template,
            BlockPos placePos
    ) {
        int baseY = placePos.getY();
        int bottomY = world.getBottomY();

        int sizeX = template.getSize().getX();
        int sizeZ = template.getSize().getZ();

        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                int wx = placePos.getX() + x;
                int wz = placePos.getZ() + z;

                // 1. ищем первый твёрдый блок снизу
                int y = baseY - 1;
                BlockState baseState = null;
                while (y > bottomY) {
                    BlockPos pos = new BlockPos(wx, y, wz);
                    BlockState state = world.getBlockState(pos);

                    if (!state.isAir() && state.getFluidState().isEmpty()) {
                        baseState = state;
                        break;
                    }
                    y--;
                }

                if (baseState == null) continue;

                boolean isGrass = baseState.isOf(Blocks.GRASS_BLOCK);

                // 2. идём вверх до шаблона, заменяя воздух И воду
                for (int fillY = y + 1; fillY < baseY; fillY++) {
                    BlockPos fillPos = new BlockPos(wx, fillY, wz);
                    BlockState current = world.getBlockState(fillPos);

                    if (current.isAir() || !current.getFluidState().isEmpty()) {
                        boolean isTop = (fillY == baseY - 1);

                        BlockState placeState =
                                (isGrass && !isTop)
                                        ? Blocks.DIRT.getDefaultState()
                                        : baseState;

                        world.setBlockState(fillPos, placeState, Block.NOTIFY_LISTENERS);
                    }
                }
            }
        }
    }
    // надо при маленьком измененнии все же достраивать гравий. для лефа можно без, потом сделать
}