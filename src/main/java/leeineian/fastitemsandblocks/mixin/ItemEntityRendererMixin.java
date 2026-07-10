package leeineian.fastitemsandblocks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import leeineian.fastitemsandblocks.FastItemsAndBlocksConfig;
import leeineian.fastitemsandblocks.ItemIdentifierProvider;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {
    private FastItemsAndBlocksConfig.Direction fiab$getDirection(ItemEntityRenderState renderState) {
        return renderState.item.usesBlockLight()
                ? FastItemsAndBlocksConfig.INSTANCE.blockDirection
                : FastItemsAndBlocksConfig.INSTANCE.itemDirection;
    }

    private boolean fiab$shouldModifyRotation(ItemEntityRenderState renderState) {
        if (fiab$getDirection(renderState) == FastItemsAndBlocksConfig.Direction.SPIN) {
            return false;
        }

        if (renderState instanceof ItemIdentifierProvider provider) {
            Identifier id = provider.fiab$getIdentifier();
            return id != null && !FastItemsAndBlocksConfig.INSTANCE.getExceptionIdentifiers().contains(id);
        }
        return false;
    }

    @ModifyExpressionValue(
            method = "submit",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/math/Axis;rotation(F)Lorg/joml/Quaternionf;"
            )
    )
    private Quaternionf fiab$adjustRotation(Quaternionf originalRotation, ItemEntityRenderState state) {
        if (!fiab$shouldModifyRotation(state)) {
            return originalRotation;
        }

        Camera activeCamera = Minecraft.getInstance().gameRenderer.mainCamera();
        Quaternionf adjustedRotation = new Quaternionf();
        FastItemsAndBlocksConfig.Direction direction = fiab$getDirection(state);

        if (direction == FastItemsAndBlocksConfig.Direction.SCREEN) {
            Vec3 cameraPos = activeCamera.position();
            double deltaX = cameraPos.x - state.x;
            double deltaZ = cameraPos.z - state.z;
            return adjustedRotation.rotationY((float) (0.5 * Math.PI) - (float) Math.atan2(deltaZ, deltaX));
        }
        if (direction == FastItemsAndBlocksConfig.Direction.STATIC) {
            return adjustedRotation;
        }

        float rotY = 180.0F - activeCamera.yRot();
        return adjustedRotation.rotationY(rotY * ((float) Math.PI / 180.0F));
    }

    @Inject(
            method = "submit",
            at = @At(
                    shift = At.Shift.AFTER,
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V"
            )
    )
    private void fiab$adjustVerticalRotation(ItemEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        if (fiab$getDirection(state) == FastItemsAndBlocksConfig.Direction.CAMERA && fiab$shouldModifyRotation(state)) {
            poseStack.mulPose(com.mojang.math.Axis.XN.rotationDegrees(Minecraft.getInstance().gameRenderer.mainCamera().xRot()));
        }
    }
}
