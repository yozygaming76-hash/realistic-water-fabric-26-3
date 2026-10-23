
package dev.example.realisticwater.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.GameRenderer;

import dev.example.realisticwater.RealisticWaterClient;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "close", at = @At("RETURN"))
    private void realisticWater$onClose(CallbackInfo ci) {
        RealisticWaterClient.close();
    }
}
