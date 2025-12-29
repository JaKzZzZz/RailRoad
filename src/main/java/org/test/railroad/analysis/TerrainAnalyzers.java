package org.test.railroad.analysis;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.test.railroad.generation.RailGenerator;
import org.test.railroad.initialization.RailWorldState;
import org.test.railroad.util.RailLog;

public class TerrainAnalyzers {

    public static GroundScanResult getHighestGroundAhead(ServerWorld world, BlockPos pos, int distance) {
        RailLog.d("[LOG] getHighestGroundAhead: start pos=" + pos + " distance=" + distance);
        int highest = Integer.MIN_VALUE;
        int lowest = Integer.MAX_VALUE;
        boolean water = false;
        for (int i = 0; i < distance; i++) {
            int x = pos.getX() + i;
            int z = pos.getZ();

            world.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, true);

            int gy = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);

            BlockPos checkPos = new BlockPos(x, gy - 1, z);
            BlockState state = world.getBlockState(checkPos);

            if (!state.getFluidState().isEmpty()) {
                RailLog.d("[WATER] found fluid under template at " + checkPos);
                water = true;
            }

            RailLog.d("[LOG]  scan i = " + i + " -> (x = " + x + ",z = " + z + ") topY=" + gy + " (highest=" + highest + ")");
            if (gy > highest) {
                highest = gy;
                RailLog.d("[LOG]   updated highest -> " + highest);
            }
            if (gy < lowest) {
                lowest = gy;
            }
        }
        RailLog.d("[LOG] getHighestGroundAhead: result highest=" + highest);
        return new GroundScanResult(highest, lowest, water);
    }

    public static void getStartCords(ServerWorld world, int x, int z) {
        RailWorldState state = RailWorldState.get(world);

        ChunkGenerator generator = world.getChunkManager().getChunkGenerator();
        NoiseConfig noise = world.getChunkManager().getNoiseConfig();

        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        int[] dzOffsets = {-14, 0, 14};

        for (int dz : dzOffsets) {
            int sz = z + dz;

            for (int dx = 0; dx < 32; dx++) {

                int sx = x + dx;
                int y = generator.getHeight(
                        sx,
                        sz,
                        Heightmap.Type.WORLD_SURFACE_WG,
                        world,
                        noise
                );

                if (y <= world.getBottomY() + 1) {
                    RailLog.d("Chunk not ready at x=" + sx + ", retry forward");
                    getStartCords(world, x, z);
                    return;
                }

                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }

            state.spawnX = x;
            state.lastY = maxY - 2;
            state.enabled = true;
            state.isFirstSegment = true;
            state.markDirty();

            RailLog.d("Подходящая земля найдена: x=" + x + " y=" + state.lastY);
        }
    }
    public static StoneScanResult hasStoneOnBottomLine(
            ServerWorld world,
            BlockPos placePos,
            StructureTemplate template
    ) {
        int length = template.getSize().getX() / 2;
        int y = placePos.getY();
        int z = placePos.getZ() + (template.getSize().getZ() / 2);
        boolean mid = false;
        boolean top = false;

        for (int dx = 0; dx < length; dx++) {
            int x = placePos.getX() + dx;

            BlockPos posTop = new BlockPos(x, y + + (template.getSize().getY() / 2), z);
            BlockPos posMid = new BlockPos(x, y, z);

            BlockState stateTop = world.getBlockState(posTop);
            BlockState stateMid = world.getBlockState(posMid);

            if (stateTop.isOf(Blocks.STONE)
                    || stateTop.isOf(Blocks.DEEPSLATE)
                    || stateTop.isIn(BlockTags.BASE_STONE_OVERWORLD)
                    || stateTop.isIn(BlockTags.ICE)) {
                top = true;
            }
            else if (!(stateMid.isAir())) {
                mid = true;
            }
            return new StoneScanResult(top, mid);
        }
        return new StoneScanResult(false, false);
    }
}
