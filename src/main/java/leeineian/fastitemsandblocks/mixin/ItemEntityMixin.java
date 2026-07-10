package leeineian.fastitemsandblocks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import leeineian.fastitemsandblocks.FastItemsAndBlocksConfig;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityMixin {

    @ModifyExpressionValue(
            method = "submit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;sin(D)F"
            )
    )
    private float fiab$disableBobbingSin(float original) {
        return FastItemsAndBlocksConfig.INSTANCE.bobbingAnimation ? original : -1.0F;
    }

    @ModifyArg(
            method = "submit",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"
            ),
            index = 1
    )
    private float fiab$removeIdleHoverOffset(float originalY) {
        return FastItemsAndBlocksConfig.INSTANCE.bobbingAnimation ? originalY : originalY - 0.0625F;
    }
}