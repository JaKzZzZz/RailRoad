package org.test.railroad.generation;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class StoneDetectProcessor extends StructureProcessor {

    private boolean touchedStone = false;

    public boolean touchedStone() {
        return touchedStone;
    }

    @Override
    public StructureTemplate.StructureBlockInfo process(
            WorldView world,
            BlockPos pos,
            BlockPos pivot,
            StructureTemplate.StructureBlockInfo original,
            StructureTemplate.StructureBlockInfo current,
            StructurePlacementData data
    ) {
        if (current == null) return null;

        BlockState existing = world.getBlockState(pos);

        if (existing.isOf(Blocks.STONE)
                || existing.isOf(Blocks.DEEPSLATE)
                || existing.isOf(Blocks.ANDESITE)
                || existing.isOf(Blocks.DIORITE)
                || existing.isOf(Blocks.GRANITE)) {

            touchedStone = true;
        }

        return current;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.NOP;
    }
}