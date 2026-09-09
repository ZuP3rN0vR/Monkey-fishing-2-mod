package monkey.fishing.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import monkey.fishing.menu.ChestGuiMenu;


@Environment(EnvType.CLIENT)
public class ChestGui extends AbstractContainerScreen<ChestGuiMenu> {
    // Placeholder background reusing vanilla's generic container texture until custom art is added.
    private static final Identifier CHEST_GUI_TEXTURE = Identifier.fromNamespaceAndPath(
        "minecraft", "textures/gui/container/generic_54.png"
    );
    private static final int CONTAINER_WIDTH = 176;
    private static final int CONTAINER_HEIGHT = 166;

    public ChestGui(ChestGuiMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, CONTAINER_WIDTH, CONTAINER_HEIGHT);
        this.inventoryLabelY = this.imageHeight - 94;
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
