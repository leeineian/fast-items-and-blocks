package leeineian.fastitemsandblocks.mixin;

import leeineian.fastitemsandblocks.ItemQuadProvider;
import leeineian.fastitemsandblocks.ItemIdentifierProvider;
import leeineian.fastitemsandblocks.FastItemsAndBlocks;
import leeineian.fastitemsandblocks.FastItemsAndBlocksConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {

    @Unique
    private static final ThreadLocal<List<BakedQuad>> fiab$tempQuadBuffer = ThreadLocal.withInitial(ArrayList::new);

    @Unique
    private boolean fiab$shouldModifyRotation(ItemEntityRenderState renderState) {
        if (FastItemsAndBlocksConfig.INSTANCE.direction == FastItemsAndBlocksConfig.Direction.SPIN) {
            return false;
        }

        if (!FastItemsAndBlocksConfig.INSTANCE.affect3DModels && renderState.item.usesBlockLight()) {
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
        if (fiab$shouldModifyRotation(state)) {
            Camera activeCamera = Minecraft.getInstance().gameRenderer.mainCamera();
            Quaternionf adjustedRotation = new Quaternionf();

            if (FastItemsAndBlocksConfig.INSTANCE.direction == FastItemsAndBlocksConfig.Direction.CAMERA_HORZ) {
                Vec3 cameraPos = activeCamera.position();
                double deltaX = cameraPos.x - state.x;
                double deltaZ = cameraPos.z - state.z;
                return adjustedRotation.rotationY((float) (0.5 * Math.PI) - (float) Math.atan2(deltaZ, deltaX));
            } else {
                float rotY = 180.0F - activeCamera.yRot();
                return adjustedRotation.rotationY(rotY * ((float) Math.PI / 180.0F));
            }
        }
        return originalRotation;
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
        if (FastItemsAndBlocksConfig.INSTANCE.direction == FastItemsAndBlocksConfig.Direction.SCREEN && fiab$shouldModifyRotation(state)) {
            poseStack.mulPose(Axis.XN.rotationDegrees(Minecraft.getInstance().gameRenderer.mainCamera().xRot()));
        }
    }

    @Unique
    private static final RenderPipeline.Snippet SNIPPET_NO_SHADING = RenderPipeline.builder(new RenderPipeline.Snippet[]{
        RenderPipelines.ITEM_SNIPPET
    }).withVertexShader(Identifier.fromNamespaceAndPath(FastItemsAndBlocks.MOD_ID, "core/item_no_cardinal_shading"))
            .buildSnippet();

    @Unique
    private static final RenderPipeline PIPELINE_CUTOUT_NO_SHADING = RenderPipelines.register(RenderPipeline.builder(new RenderPipeline.Snippet[]{
        SNIPPET_NO_SHADING
    }).withLocation("pipeline/item_cutout_no_cardinal_shading")
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .build()
    );

    @Unique
    private static final RenderPipeline PIPELINE_TRANSLUCENT_NO_SHADING = RenderPipelines.register(RenderPipeline.builder(new RenderPipeline.Snippet[]{
        SNIPPET_NO_SHADING
    }).withLocation("pipeline/item_translucent_no_cardinal_shading")
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .build()
    );

    @Unique
    private static final Function<Identifier, RenderType> CUTOUT_SHADING_RESOLVER;
    @Unique
    private static final Function<Identifier, RenderType> TRANSLUCENT_SHADING_RESOLVER;

    static {
        CUTOUT_SHADING_RESOLVER = Util.memoize((texture) -> {
            RenderSetup setup = RenderSetup.builder(PIPELINE_CUTOUT_NO_SHADING).withTexture("Sampler0", texture).useLightmap().useOverlay().affectsCrumbling().setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).createRenderSetup();
            return RenderType.create("item_cutout_no_cardinal_shading", setup);
        });
        TRANSLUCENT_SHADING_RESOLVER = Util.memoize((texture) -> {
            RenderSetup setup = RenderSetup.builder(PIPELINE_TRANSLUCENT_NO_SHADING).withTexture("Sampler0", texture).setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET).useLightmap().useOverlay().affectsCrumbling().sortOnUpload().setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).createRenderSetup();
            return RenderType.create("item_translucent_no_cardinal_shading", setup);
        });
    }

    @Unique
    private static RenderType fiab$getCutoutNoShading(Identifier texture) {
        return CUTOUT_SHADING_RESOLVER.apply(texture);
    }

    @Unique
    private static RenderType fiab$getTranslucentNoShading(Identifier texture) {
        return TRANSLUCENT_SHADING_RESOLVER.apply(texture);
    }

    @Unique
    private static final RenderType CUTOUT_BLOCK_NO_SHADING = fiab$getCutoutNoShading(TextureAtlas.LOCATION_BLOCKS);
    @Unique
    private static final RenderType TRANSLUCENT_BLOCK_NO_SHADING = fiab$getTranslucentNoShading(TextureAtlas.LOCATION_BLOCKS);
    @Unique
    private static final RenderType CUTOUT_ITEM_NO_SHADING = fiab$getCutoutNoShading(TextureAtlas.LOCATION_ITEMS);
    @Unique
    private static final RenderType TRANSLUCENT_ITEM_NO_SHADING = fiab$getTranslucentNoShading(TextureAtlas.LOCATION_ITEMS);

    @Inject(
            method = "submit",
            at = @At(
                    shift = At.Shift.AFTER,
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"
            )
    )
    private void fiab$applyItemFlattening(ItemEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        if (!FastItemsAndBlocksConfig.INSTANCE.flatModels || state.item.usesBlockLight()) {
            return;
        }

        if (!(state instanceof ItemIdentifierProvider provider)) {
            return;
        }

        Identifier id = provider.fiab$getIdentifier();
        if (id == null || FastItemsAndBlocksConfig.INSTANCE.getExceptionIdentifiers().contains(id)) {
            return;
        }

        if (!(state.item instanceof ItemQuadProvider quadProvider)) {
            return;
        }

        List<BakedQuad>[] quadLayers = quadProvider.fiab$getQuads();
        List<BakedQuad> tempStorage = fiab$tempQuadBuffer.get();
        boolean renderBack = FastItemsAndBlocksConfig.INSTANCE.direction == FastItemsAndBlocksConfig.Direction.SPIN
                || (FastItemsAndBlocksConfig.INSTANCE.direction == FastItemsAndBlocksConfig.Direction.SCREEN_HORZ && FastItemsAndBlocksConfig.INSTANCE.renderBack);

        int totalLayers = quadLayers.length;
        for (int layerIndex = 0; layerIndex < totalLayers; layerIndex++) {
            List<BakedQuad> currentList = quadLayers[layerIndex];
            if (currentList == null || currentList.isEmpty()) {
                continue;
            }

            tempStorage.clear();
            int quadCount = currentList.size();
            for (int quadIndex = 0; quadIndex < quadCount; quadIndex++) {
                BakedQuad quad = currentList.get(quadIndex);
                if (quad.direction() != Direction.SOUTH) {
                    continue;
                }

                BakedQuad.MaterialInfo materialInfo = quad.materialInfo();
                RenderType itemRenderType;
                if (materialInfo.sprite().atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS)) {
                    itemRenderType = materialInfo.sprite().transparency().hasTranslucent() ? TRANSLUCENT_BLOCK_NO_SHADING : CUTOUT_BLOCK_NO_SHADING;
                } else {
                    itemRenderType = materialInfo.sprite().transparency().hasTranslucent() ? TRANSLUCENT_ITEM_NO_SHADING : CUTOUT_ITEM_NO_SHADING;
                }

                BakedQuad.MaterialInfo newMaterialInfo = new BakedQuad.MaterialInfo(
                        materialInfo.sprite(), materialInfo.layer(), itemRenderType,
                        materialInfo.tintIndex(), materialInfo.shade(), materialInfo.lightEmission());

                BakedQuad newQuad = new BakedQuad(
                        quad.position0(), quad.position1(), quad.position2(), quad.position3(),
                        quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
                        quad.direction(), newMaterialInfo);
                tempStorage.add(newQuad);

                if (renderBack) {
                    BakedQuad newBackQuad = new BakedQuad(
                            quad.position0(), quad.position3(), quad.position2(), quad.position1(),
                            quad.packedUV0(), quad.packedUV3(), quad.packedUV2(), quad.packedUV1(),
                            Direction.NORTH, newMaterialInfo);
                    tempStorage.add(newBackQuad);
                }
            }

            currentList.clear();
            currentList.addAll(tempStorage);
        }
    }
}
