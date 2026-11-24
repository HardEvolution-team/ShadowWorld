package com.electrifiedded.shadowworld.client;

import com.electrifiedded.shadowworld.SWContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** 1.7 GuiDiaryEntry: "New Diary Entry!" banner in the top-left corner for 5.5 s. */
public class DiaryToast extends Gui {
    static final ResourceLocation TEX = new ResourceLocation("shadowworld", "textures/gui/DiaryEntry.png");
    public static final DiaryToast INSTANCE = new DiaryToast();
    private long time;
    private String entry = "";
    private ItemStack stack = ItemStack.EMPTY;
    private EntityLivingBase entity;

    public void show(String name, ItemStack s, NBTTagCompound entityTag) {
        time = Minecraft.getSystemTime();
        entry = name == null ? "" : name;
        stack = s == null ? ItemStack.EMPTY : s;
        entity = null;
        if (entityTag != null && Minecraft.getMinecraft().world != null) {
            Entity en = EntityList.createEntityFromNBT(entityTag, Minecraft.getMinecraft().world);
            if (en instanceof EntityLivingBase l) entity = l;
        }
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Post e) {
        if (e.getType() != RenderGameOverlayEvent.ElementType.ALL || time == 0L) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;
        double d0 = (Minecraft.getSystemTime() - time) / 5500.0;
        if (d0 < 0.0 || d0 > 1.0) {
            time = 0L;
            return;
        }
        double d1 = d0 * 2.0;
        if (d1 > 1.0) d1 = 2.0 - d1;
        d1 = Math.max(0.0, 1.0 - d1 * 4.0);
        d1 = d1 * d1 * d1 * d1;
        int i = 0, j = -(int) (d1 * 36.0);
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        mc.getTextureManager().bindTexture(TEX);
        drawTexturedModalRect(i, j, 0, stack.isEmpty() && entity == null ? 0 : 32, 165, 32);
        if (entry.isEmpty()) mc.fontRenderer.drawStringWithShadow("New Diary!", i + 30, j + 7, -256);
        else {
            mc.fontRenderer.drawStringWithShadow("New Diary Entry!", i + 30, j + 7, -256);
            mc.fontRenderer.drawString(entry, i + 30, j + 18, -1);
        }
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        mc.getRenderItem().renderItemIntoGUI(new ItemStack(SWContent.Diary), i + 8, j + 8);
        if (!stack.isEmpty()) mc.getRenderItem().renderItemIntoGUI(stack, i + 140, j + 8);
        RenderHelper.disableStandardItemLighting();
        if (entity != null) renderEntity(i + 148, j + 24, 8, 80.0F, 0.0F, entity, false);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    /** GuiDiaryEntry.renderEntity (free yaw/pitch, diary) and drawEntityOnScreen (toast). */
    public static void renderEntity(int x, int y, int scale, float yaw, float pitch, EntityLivingBase e, boolean free) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, 50.0F);
        GlStateManager.scale((float) -scale, (float) scale, (float) scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float f2 = e.renderYawOffset, f3 = e.rotationYaw, f4 = e.rotationPitch, f5 = e.prevRotationYawHead, f6 = e.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        if (free) {
            GlStateManager.rotate(-pitch, 1.0F, 0.0F, 0.0F);
            e.renderYawOffset = yaw;
            e.rotationYaw = yaw;
            e.rotationPitch = pitch;
        } else GlStateManager.rotate(-((float) Math.atan(pitch / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
        e.rotationYawHead = e.rotationYaw;
        e.prevRotationYawHead = e.rotationYaw;
        RenderManager rm = Minecraft.getMinecraft().getRenderManager();
        rm.setPlayerViewY(180.0F);
        rm.setRenderShadow(false);
        rm.renderEntity(e, 0.0, 0.0, 0.0, 0.0F, 1.0F, false);
        rm.setRenderShadow(true);
        e.renderYawOffset = f2;
        e.rotationYaw = f3;
        e.rotationPitch = f4;
        e.prevRotationYawHead = f5;
        e.rotationYawHead = f6;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}
