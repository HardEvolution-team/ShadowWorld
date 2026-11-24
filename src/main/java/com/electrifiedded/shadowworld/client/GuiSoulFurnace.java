package com.electrifiedded.shadowworld.client;

import com.electrifiedded.shadowworld.tile.SoulFurnaces.ContainerSoulFurnace;
import com.electrifiedded.shadowworld.tile.SoulFurnaces.TileSoulFurnace;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

/** 1.7 GuiSoulFurnace / GuiGoldenSoulFurnace (same layout, golden has no fuel flame). */
public class GuiSoulFurnace extends GuiContainer {
    private static final ResourceLocation SOUL = new ResourceLocation("shadowworld", "textures/gui/SoulFurnace.png");
    private static final ResourceLocation GOLDEN = new ResourceLocation("shadowworld", "textures/gui/GoldenSoulFurnace.png");
    private final TileSoulFurnace tile;

    public GuiSoulFurnace(ContainerSoulFurnace c) {
        super(c);
        this.tile = c.tile;
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        super.drawScreen(mx, my, pt);
        renderHoveredToolTip(mx, my);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        String s = tile.hasCustomName() ? tile.getName() : I18n.format(tile.getName());
        fontRenderer.drawString(s, xSize / 2 - fontRenderer.getStringWidth(s) / 2, 6, 4210752);
        fontRenderer.drawString(I18n.format("container.inventory"), 8, ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
        GlStateManager.color(1F, 1F, 1F, 1F);
        mc.getTextureManager().bindTexture(tile.golden ? GOLDEN : SOUL);
        int k = (width - xSize) / 2, l = (height - ySize) / 2;
        drawTexturedModalRect(k, l, 0, 0, xSize, ySize);
        if (!tile.golden && tile.isBurning()) {
            int i = tile.getBurnTimeRemainingScaled(12);
            drawTexturedModalRect(k + 56, l + 36 + 12 - i, 176, 12 - i, 14, i + 2);
        }
        int c = tile.getCookProgressScaled(24);
        drawTexturedModalRect(k + 79, l + 34, 176, 14, c + 1, 16);
    }
}
