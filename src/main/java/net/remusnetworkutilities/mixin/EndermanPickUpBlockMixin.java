package net.remusnetworkutilities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.remusnetworkutilities.RemusNetworkUtilitiesSettings;

@Mixin(targets = "net.minecraft.entity.mob.EndermanEntity$PickUpBlockGoal")
public class EndermanPickUpBlockMixin {
    @Inject(method = "canStart()Z", at = @At("HEAD"), cancellable = true)
    private void preventBlockPickup(CallbackInfoReturnable<Boolean> cir) {
        if (RemusNetworkUtilitiesSettings.EndermanPickupBlock)
            cir.setReturnValue(false); // Prevents Endermen from picking up blocks
    }
}
