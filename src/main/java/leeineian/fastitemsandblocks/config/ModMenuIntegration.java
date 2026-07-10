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
                    .name(Component.literal("Fast Items and Blocks"))
                    .group(OptionGroup.createBuilder()
                        .name(Component.literal("Configurations"))
                        .option(Option.<FastItemsAndBlocksConfig.Direction>createBuilder()
                            .name(Component.literal("Item Rotation Direction"))
                            .description(OptionDescription.of(Component.literal("Vanilla: Keeps the default spinning behavior.\nFace screen: Faces the screen horizontally.\nFace camera: Fully faces the camera (horizontal + vertical).\nNone: Keeps a fixed orientation (no spin or facing).")))
                            .binding(
                                Binding.generic(
                                    FastItemsAndBlocksConfig.Direction.SCREEN,
                                    () -> FastItemsAndBlocksConfig.INSTANCE.itemDirection,
                                    value -> FastItemsAndBlocksConfig.INSTANCE.itemDirection = value
                                )
                            )
                            .controller(option -> EnumControllerBuilder.create(option)
                                .enumClass(FastItemsAndBlocksConfig.Direction.class)
                                .formatValue(val -> switch (val) {
                                    case SPIN -> Component.literal("Vanilla");
                                    case SCREEN -> Component.literal("Face screen");
                                    case CAMERA -> Component.literal("Face camera");
                                    case STATIC -> Component.literal("Fixed");
                                })
                            )
                            .build())
                        .option(Option.<FastItemsAndBlocksConfig.Direction>createBuilder()
                            .name(Component.literal("Block Rotation Direction"))
                            .description(OptionDescription.of(Component.literal("Vanilla: Keeps the default spinning behavior.\nFace screen: Faces the screen horizontally.\nFace camera: Fully faces the camera (horizontal + vertical).\nNone: Keeps a fixed orientation (no spin or facing).")))
                            .binding(
                                Binding.generic(
                                    FastItemsAndBlocksConfig.Direction.SCREEN,
                                    () -> FastItemsAndBlocksConfig.INSTANCE.blockDirection,
                                    value -> FastItemsAndBlocksConfig.INSTANCE.blockDirection = value
                                )
                            )
                            .controller(option -> EnumControllerBuilder.create(option)
                                .enumClass(FastItemsAndBlocksConfig.Direction.class)
                                .formatValue(val -> switch (val) {
                                    case SPIN -> Component.literal("Vanilla");
                                    case SCREEN -> Component.literal("Face screen");
                                    case CAMERA -> Component.literal("Face camera");
                                    case STATIC -> Component.literal("Fixed");
                                })
                            )
                            .build())
                        .option(Option.<Boolean>createBuilder()
                            .name(Component.literal("Bobbing Animation"))
                            .description(OptionDescription.of(Component.literal("Toggles the up-and-down bobbing animation for dropped items and blocks.")))
                            .binding(
                                Binding.generic(
                                    true,
                                    () -> FastItemsAndBlocksConfig.INSTANCE.bobbingAnimation,
                                    value -> FastItemsAndBlocksConfig.INSTANCE.bobbingAnimation = value
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
