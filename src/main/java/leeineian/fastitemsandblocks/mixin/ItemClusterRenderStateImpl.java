package leeineian.fastitemsandblocks.mixin;

import leeineian.fastitemsandblocks.ItemIdentifierProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemClusterRenderState.class)
public abstract class ItemClusterRenderStateImpl extends EntityRenderState implements ItemIdentifierProvider {

    @Unique
    private Identifier fiab$itemId;

    @Override
    public Identifier fiab$getIdentifier() {
        return this.fiab$itemId;
    }

    @Inject(
            method = "extractItemGroupRenderState",
            at = @At("HEAD")
    )
    private void fiab$retrieveItemId(Entity entity, ItemStack stack, ItemModelResolver resolver, CallbackInfo ci) {
        if (stack == null || stack.isEmpty()) {
            this.fiab$itemId = null;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.level != null) {
            var registryLookup = mc.level.registryAccess().lookup(Registries.ITEM);
            if (registryLookup.isPresent()) {
                this.fiab$itemId = registryLookup.get().getKey(stack.getItem());
                return;
            }
        }
        this.fiab$itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
    }
}
