package leeineian.fastitemsandblocks;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FastItemsAndBlocks implements ClientModInitializer {
    public static final String MOD_ID = "fast-items-and-blocks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        FastItemsAndBlocksConfig.load();
    }
}
