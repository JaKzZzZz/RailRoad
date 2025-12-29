package org.test.railroad.mixin;

import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.test.railroad.initialization.WorldInit;
import org.test.railroad.util.RailLog;

import static org.test.railroad.queue.Updater.ENABLED_THIS_WORLD;

@Mixin(StructurePlacement.class)
public abstract class StructurePlacementMixin {

    @Inject(
            method = "shouldGenerate",
            at = @At("HEAD"),
            cancellable = true
    )
    private void blockAnyStructureInRail(
            StructurePlacementCalculator calculator, int chunkX, int chunkZ, CallbackInfoReturnable<Boolean> cir) {
        if (!ENABLED_THIS_WORLD) return;

        int x = (chunkX << 4) + 8;
        int z = (chunkZ << 4) + 8;

        // проверяем пересечение с коридором
        if (x > -15 && z >= -100 && z <= 100) {
            cir.setReturnValue(false);
            RailLog.d("StructurePlacement убрал структуры по" + " x " + chunkX + " ,z " + chunkZ);
        }
    }
}