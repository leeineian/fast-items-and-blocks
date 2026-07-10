package leeineian.fastitemsandblocks.mixin;

import leeineian.fastitemsandblocks.ItemQuadProvider;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemStackRenderState.class)
public class ItemStackRenderStateImpl implements ItemQuadProvider {
    @Shadow 
    private ItemStackRenderState.LayerRenderState[] layers;

    @Override
    public List<BakedQuad>[] fiab$getQuads() {
        if (this.layers == null) {
            return new ArrayList[0];
        }
        ArrayList<BakedQuad>[] resultQuads = new ArrayList[this.layers.length];
        for (int layerIndex = 0; layerIndex < this.layers.length; layerIndex++) {
            resultQuads[layerIndex] = new ArrayList<>(this.layers[layerIndex].prepareQuadList());
        }
        return resultQuads;
    }
}
