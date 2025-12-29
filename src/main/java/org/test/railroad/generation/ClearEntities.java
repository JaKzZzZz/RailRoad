package org.test.railroad.generation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;

public class ClearEntities {
    public static void teleportEntities(ServerWorld world, StructureTemplate template, BlockPos pos) {

        Box box = new Box(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + template.getSize().getX(),
                pos.getY() + template.getSize().getY(),
                pos.getZ() + template.getSize().getZ()
        );

        List<LivingEntity> entities = world.getEntitiesByClass(
                LivingEntity.class,
                box,
                e -> !(e instanceof PlayerEntity)
        );

        for (LivingEntity entity : entities) {
            BlockPos base = entity.getBlockPos();

            BlockPos target = findSidePosition(world, base);

            if (target != null) {
                entity.requestTeleport(
                        target.getX() + 0.5,
                        target.getY(),
                        target.getZ() + 0.5
                );
            } else {
                entity.requestTeleport(
                        entity.getX(),
                        entity.getY() + 2,
                        entity.getZ()
                );
            }
        }
    }
    private static BlockPos findSidePosition(ServerWorld world, BlockPos start) {
        int radius = 6;

        for (int r = 1; r <= radius; r++) {
            if (isFree(world, start.add(0, 0, r))) return start.add(0, 0, r);
            if (isFree(world, start.add(0, 0, -r))) return start.add(0, 0, -r);
        }
        return null;
    }
    private static boolean isFree(ServerWorld world, BlockPos pos) {
        return world.getBlockState(pos).getCollisionShape(world, pos).isEmpty()
                && world.getBlockState(pos.up()).getCollisionShape(world, pos.up()).isEmpty();
    }
}