package org.test.railroad.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.test.railroad.initialization.WorldInit;
import org.test.railroad.queue.Updater;

import static org.test.railroad.queue.Updater.ENABLED_THIS_WORLD;

@Mixin(ServerPlayerEntity.class)
public class TickListener {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (!ENABLED_THIS_WORLD) return;
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        Updater.update(player);
    }
}