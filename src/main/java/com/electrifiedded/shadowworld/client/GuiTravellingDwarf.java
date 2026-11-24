package com.electrifiedded.shadowworld.client;

import com.electrifiedded.shadowworld.entity.SWDwarves.TravellingDwarf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Random;

/** 1.7 GuiTravellingDwarf: the traveller tells his story page by page. */
public class GuiTravellingDwarf extends GuiScreen {
    static final ResourceLocation TEX = new ResourceLocation("shadowworld", "textures/gui/TravellingDwarf.png");
    private final int w = 176, h = 166;
    private int page;
    private final List<String> story;
    private final TravellingDwarf dwarf;
    private final Random rand = new Random();
    private Button next, done;

    public GuiTravellingDwarf(TravellingDwarf d) {
        dwarf = d;
        story = d.story();
        if (story.isEmpty()) story.add("I don't have anything to tell you.");
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(done = new Button(0, width / 2 - 80, h - 25, msg(true)));
        buttonList.add(next = new Button(1, width / 2 - 80, h - 50, "Mhm..."));
        next.visible = page < story.size() - 1;
    }

    @Override public void onGuiClosed() { dwarf.setCustomer(null); }

    private String msg(boolean bye) {
        int r = rand.nextInt(4);
        if (bye) return r == 0 ? "I have to go." : r == 1 ? "That's enough" : r == 2 ? "See you..." : "Good bye";
        return r == 0 ? "..." : r == 1 ? "...oh..." : r == 2 ? "And than?" : "Mhm...";
    }

    @Override
    protected void actionPerformed(GuiButton b) {
        if (b.id == 0) mc.displayGuiScreen(null);
        else if (b.id == 1 && page < story.size() - 1) {
            page++;
            next.displayString = msg(false);
            done.displayString = msg(true);
        }
        next.visible = page < story.size() - 1;
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        GlStateManager.color(1F, 1F, 1F, 1F);
        int k = (width - w) / 2;
        mc.getTextureManager().bindTexture(TEX);
        drawTexturedModalRect(k, 2, 0, 0, w, h);
        fontRenderer.drawString(dwarf.hasCustomName() ? dwarf.getCustomNameTag() + ":" : "Traveller:", k + 67, 18, 0);
        fontRenderer.drawSplitString("\"" + story.get(page) + "\"", k + 70, 34, 95, 0);
        super.drawScreen(mx, my, pt);
        GuiInventory.drawEntityOnScreen(k + 30, 72, 30, -20.0F, 0.0F, dwarf);
    }

    static class Button extends GuiButton {
        Button(int id, int x, int y, String s) { super(id, x, y, 161, 20, s); }

        @Override
        public void drawButton(Minecraft mc, int mx, int my, float pt) {
            if (!visible) return;
            mc.getTextureManager().bindTexture(TEX);
            GlStateManager.color(1F, 1F, 1F, 1F);
            hovered = mx >= x && my >= y && mx < x + width && my < y + height;
            int k = getHoverState(hovered);
            GlStateManager.enableBlend();
            drawTexturedModalRect(x, y, 0, 147 + k * 20, width / 2, height);
            drawTexturedModalRect(x + width / 2, y, 161 - width / 2, 147 + k * 20, width / 2, height);
            drawCenteredString(mc.fontRenderer, displayString, x + width / 2, y + (height - 8) / 2, hovered ? 0xFFFFA0 : 0xE0E0E0);
        }
    }
}
