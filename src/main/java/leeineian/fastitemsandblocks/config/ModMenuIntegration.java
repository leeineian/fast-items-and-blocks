package leeineian.fastitemsandblocks.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.network.chat.Component;
import leeineian.fastitemsandblocks.FastItemsAndBlocksConfig;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> {
            YetAnotherConfigLib configLib = YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Fast Items and Blocks Settings"))
                .category(ConfigCategory.createBuilder()
                    .name(Component.literal("General"))
                    .tooltip(Component.literal("Core configuration options for the mod."))
                    .group(OptionGroup.createBuilder()
                        .name(Component.literal("Rendering Options"))
                        .option(Option.<FastItemsAndBlocksConfig.Direction>createBuilder()
                            .name(Component.literal("Rotation Direction"))
                            .description(OptionDescription.of(Component.literal("Controls how 2D items rotate to face the player.")))
                            .binding(
                                Binding.generic(
                                    FastItemsAndBlocksConfig.Direction.SCREEN_HORZ,
                                    () -> FastItemsAndBlocksConfig.INSTANCE.direction,
                                    value -> FastItemsAndBlocksConfig.INSTANCE.direction = value
                                )
                            )
                            .controller(option -> EnumControllerBuilder.create(option)
                                .enumClass(FastItemsAndBlocksConfig.Direction.class)
                                .formatValue(val -> Component.literal(val.name()))
                            )
                            .build())
                        .option(Option.<Boolean>createBuilder()
                            .name(Component.literal("Flat Models"))
                            .description(OptionDescription.of(Component.literal("Strips the 3D extrusion quads of the items, rendering them flat.")))
                            .binding(
                                Binding.generic(
                                    true,
                                    () -> FastItemsAndBlocksConfig.INSTANCE.flatModels,
                                    value -> FastItemsAndBlocksConfig.INSTANCE.flatModels = value
                                )
                            )
                            .controller(TickBoxControllerBuilder::create)
                            .build())
                        .option(Option.<Boolean>createBuilder()
                            .name(Component.literal("Render Backside"))
                            .description(OptionDescription.of(Component.literal("Draws the flipside texture on the back of flat items.")))
                            .binding(
                                Binding.generic(
                                    false,
                                    () -> FastItemsAndBlocksConfig.INSTANCE.renderBack,
                                    value -> FastItemsAndBlocksConfig.INSTANCE.renderBack = value
                                )
                            )
                            .controller(TickBoxControllerBuilder::create)
                            .build())
                        .option(Option.<Boolean>createBuilder()
                            .name(Component.literal("Affect 3D Block Models"))
                            .description(OptionDescription.of(Component.literal("Forces blocks rendered as items (e.g. grass blocks in hand) to also be flattened.")))
                            .binding(
                                Binding.generic(
                                    false,
                                    () -> FastItemsAndBlocksConfig.INSTANCE.affect3DModels,
                                    value -> FastItemsAndBlocksConfig.INSTANCE.affect3DModels = value
                                )
                            )
                            .controller(TickBoxControllerBuilder::create)
                            .build())
                        .build())
                    .build())
                .save(FastItemsAndBlocksConfig::save)
                .build();
            return configLib.generateScreen(parentScreen);
        };
    }
}
