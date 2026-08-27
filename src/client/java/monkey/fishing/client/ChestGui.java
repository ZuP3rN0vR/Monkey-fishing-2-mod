package monkey.fishing.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import monkey.fishing.config.ConfigManager;
import monkey.fishing.config.MonkeyFishingConfig;
import java.util.List;


@Environment(EnvType.CLIENT)
public class ChestGui extends AbstractContainerScreen<ChestMenu> {
    private static final Identifier CHEST_GUI_TEXTURE = Identifier.fromNamespaceAndPath(
        "monkey-fishing-mod", "textures/gui/chest.png"
    );
    private static final int CONTAINER_WIDTH = 176;
    private static final int CONTAINER_HEIGHT = 166;

    private final List<MonkeyFishingConfig.CustomLootEntry> lootItems;

    public ChestGui(ChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, CONTAINER_WIDTH, CONTAINER_HEIGHT);
        this.inventoryLabelY = this.imageHeight - 94;
        this.lootItems = ConfigManager.CONFIG.customItems;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(extractor, mouseX, mouseY, partialTick);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // Fixed texture sampling bounds (0, 0) up to (176, 166) over a standard 256x256 image canvas
        extractor.blit(CHEST_GUI_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }
}
