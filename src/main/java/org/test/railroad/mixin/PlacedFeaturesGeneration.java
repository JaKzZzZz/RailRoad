package org.test.railroad.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.test.railroad.initialization.WorldInit;
import org.test.railroad.util.RailLog;

import static org.test.railroad.queue.Updater.ENABLED_THIS_WORLD;

@Mixin(PlacedFeature.class)
public class PlacedFeaturesGeneration {
    @Inject(method = "generate(Lnet/minecraft/world/gen/feature/FeaturePlacementContext;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private void onPlaced(FeaturePlacementContext context, net.minecraft.util.math.random.Random random, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!ENABLED_THIS_WORLD) return;
        if (pos.getX() > -15 && pos.getZ() >= -1 && pos.getZ() <= 1) {

            PlacedFeature self = (PlacedFeature)(Object)this;
            Feature<?> feature = self.feature().value().feature();

            if (feature instanceof FreezeTopLayerFeature
                    || feature instanceof IcebergFeature
                    || feature instanceof BlockPileFeature)
                return;
            RailLog.d("PlacedFeaturesGeneration заблокирован спавн фич по адресу " + pos);

            cir.setReturnValue(false);
        }
    }
}