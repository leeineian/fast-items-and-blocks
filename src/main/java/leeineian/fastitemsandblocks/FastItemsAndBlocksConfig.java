package leeineian.fastitemsandblocks;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

public class FastItemsAndBlocksConfig {

    public static FastItemsAndBlocksConfig INSTANCE = new FastItemsAndBlocksConfig();

    public Direction direction = Direction.SCREEN_HORZ;
    public boolean flatModels = true;
    public boolean renderBack = false;
    public boolean affect3DModels = false;

    public List<String> exceptions = new ArrayList<>(Arrays.asList(
            "minecraft:decorated_pot",
            "minecraft:shield",
            "minecraft:white_banner",
            "minecraft:orange_banner",
            "minecraft:magenta_banner",
            "minecraft:light_blue_banner",
            "minecraft:yellow_banner",
            "minecraft:pink_banner",
            "minecraft:gray_banner",
            "minecraft:light_gray_banner",
            "minecraft:cyan_banner",
            "minecraft:purple_banner",
            "minecraft:blue_banner",
            "minecraft:brown_banner",
            "minecraft:green_banner",
            "minecraft:red_banner",
            "minecraft:black_banner"
    ));

    private transient List<Identifier> cachedExceptionIdentifiers = null;

    public enum Direction {
        SCREEN_HORZ, CAMERA_HORZ, SPIN, SCREEN
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "fast-items-and-blocks.json");

    public static void load() {
        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                INSTANCE = GSON.fromJson(reader, FastItemsAndBlocksConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new FastItemsAndBlocksConfig();
                }
            } catch (IOException e) {
                FastItemsAndBlocks.LOGGER.error("Failed to load config", e);
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(INSTANCE, writer);
            INSTANCE.cachedExceptionIdentifiers = null;
        } catch (IOException e) {
            FastItemsAndBlocks.LOGGER.error("Failed to save config", e);
        }
    }

    public List<Identifier> getExceptionIdentifiers() {
        if (cachedExceptionIdentifiers == null) {
            cachedExceptionIdentifiers = new ArrayList<>();
            for (String ex : exceptions) {
                try {
                    cachedExceptionIdentifiers.add(Identifier.tryParse(ex));
                } catch (Exception e) {
                    FastItemsAndBlocks.LOGGER.error("Invalid identifier in exceptions: {}", ex, e);
                }
            }
        }
        return cachedExceptionIdentifiers;
    }
}
