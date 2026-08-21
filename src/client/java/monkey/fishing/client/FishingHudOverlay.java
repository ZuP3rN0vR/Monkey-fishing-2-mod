package com.example.mixin; // Replace with your actual package name

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.util.Identifier;

public class FishingHudOverlay implements HudRenderCallback {
    // A simple default texture from Minecraft (or you can use your own)
    private static final Identifier BAR_TEXTURE = Identifier.of("minecraft", "textures/gui/sprites/hud/air.png");

    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        ClientPlayerEntity player = client.player;
        // Only render if the player exists and their fishing bobber is active
        if (player != null && player.fishHook != null) {
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();

            // Position the bar on the right side of the screen
            int x = width - 30; 
            int y = height / 2 - 40; // Centered vertically

            // Draw a simple background bar (Width: 10, Height: 80)
            drawContext.fill(x, y, x + 10, y + 80, 0xaa000000); // Semi-transparent black

            // Draw an inner coloured bar indicator
            drawContext.fill(x + 2, y + 2, x + 8, y + 78, 0xff3498db); // Light blue
        }
    }
}
