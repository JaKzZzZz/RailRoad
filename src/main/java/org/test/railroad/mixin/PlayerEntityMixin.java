package org.test.railroad.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.test.railroad.generation.RailGenerator;
import org.test.railroad.initialization.CompassInit;
import org.test.railroad.initialization.RailWorldState;
import org.test.railroad.initialization.WorldInit;

import static org.test.railroad.queue.Updater.ENABLED_THIS_WORLD;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Unique
    private boolean untitled2_compassGiven = false;

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void writeNbtMixin(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("untitled2_compassGiven", untitled2_compassGiven);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void readNbtMixin(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("untitled2_compassGiven")) {
            untitled2_compassGiven = nbt.getBoolean("untitled2_compassGiven");
        }
    }

    @Inject(method = "onSpawn", at = @At("TAIL"))
    private void giveCompassOnFirstJoin(CallbackInfo ci) {
        if (!ENABLED_THIS_WORLD) return;
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        if (!untitled2_compassGiven) {

            ServerWorld world = player.getServerWorld();

            RailWorldState state = RailWorldState.get(world);

            ItemStack compass = CompassInit.createRailCompass(world);
            player.getInventory().insertStack(compass);
            player.sendMessage(Text.literal("Вам выдан компас, указывающий на начало железной дороги" + " (x = " + state.spawnX +")"), false);

            untitled2_compassGiven = true;
        }
    }
}