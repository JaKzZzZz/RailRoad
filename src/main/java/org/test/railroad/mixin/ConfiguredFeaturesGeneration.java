package org.test.railroad.mixin;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.test.railroad.initialization.WorldInit;
import org.test.railroad.util.RailLog;

import static org.test.railroad.queue.Updater.ENABLED_THIS_WORLD;

@Mixin(ChunkGenerator.class)
public abstract class ConfiguredFeaturesGeneration {

    @Inject(method = "generateFeatures", at = @At("HEAD"), cancellable = true)
    public void untitled2$FeaturesGeneration(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor, CallbackInfo ci) {
        if (!ENABLED_THIS_WORLD) return;
        ChunkPos pos = chunk.getPos();

        if (pos.getStartX() > -15 && pos.getStartZ() >= -20 && pos.getStartZ() <= 1) {
            RailLog.d("ConfiguredFeaturesGeneration заблокирован спавн фич по адресу " + pos);
            ci.cancel();
        }
    }
}
