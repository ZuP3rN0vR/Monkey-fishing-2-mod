package monkey.fishing.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
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
    private static final int CONTAINER_HEIGHT = 168;
    private static final int CONTAINER_SECTION_HEIGHT = 17 + 3 * 18;
    private static final int PLAYER_INVENTORY_TEXTURE_Y = 126;

    public ChestGui(ChestGuiMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, CONTAINER_WIDTH, CONTAINER_HEIGHT);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        extractor.blit(
            RenderPipelines.GUI_TEXTURED,
            CHEST_GUI_TEXTURE,
            x,
            y,
            0.0F,
            0.0F,
            this.imageWidth,
            CONTAINER_SECTION_HEIGHT,
            256,
            256
        );
        extractor.blit(
            RenderPipelines.GUI_TEXTURED,
            CHEST_GUI_TEXTURE,
            x,
            y + CONTAINER_SECTION_HEIGHT,
            0.0F,
            (float) PLAYER_INVENTORY_TEXTURE_Y,
            this.imageWidth,
            this.imageHeight - CONTAINER_SECTION_HEIGHT,
            256,
            256
        );
        super.extractRenderState(extractor, mouseX, mouseY, partialTick);
    }
}
