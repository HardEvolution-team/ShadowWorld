package com.electrifiedded.shadowworld.client;

import com.electrifiedded.shadowworld.tile.SWNotes.TileLamp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

/** 1.7 TileEntityLampRenderer: floating lantern with a flickering light level. */
public class LampRenderer extends TileEntitySpecialRenderer<TileLamp> {
    static final ResourceLocation TEX = new ResourceLocation("shadowworld", "textures/models/ModelLamp.png");
    static final ModelLamp MODEL = new ModelLamp();
    private final Random random = new Random();
    private int light = 85;

    @Override
    public void render(TileLamp te, double x, double y, double z, float pt, int destroy, float alpha) {
        bindTexture(TEX);
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        GlStateManager.translate(0.0F, MathHelper.cos(te.floattime * 0.1F) / 10.0F, 0.0F);
        GlStateManager.scale(0.1F, 0.1F, 0.1F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        if (pt * 10.0F % 10.0F <= 0.2) light = random.nextInt(10) + 80;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536);
        GlStateManager.color(1F, 1F, 1F, 1F);
        MODEL.renderAll();
        GlStateManager.popMatrix();
    }

    /** 1.7 LampItemRenderer. */
    public static class Item extends TileEntityItemStackRenderer {
        @Override
        public void renderByItem(ItemStack stack, float pt) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(TEX);
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.5F, 0.5F, 0.5F);
            GlStateManager.scale(0.1F, 0.1F, 0.1F);
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            MODEL.renderAll();
            GlStateManager.popMatrix();
        }
    }

    /** 1.7 ModelLamp. */
    public static class ModelLamp extends ModelBase {
        final ModelRenderer box1, box2;

        public ModelLamp() {
            textureWidth = 128;
            textureHeight = 32;
            box1 = new ModelRenderer(this, 40, 0);
            box1.addBox(-6.0F, -5.0F, -6.0F, 12, 10, 12);
            box1.setTextureSize(128, 32);
            box1.mirror = true;
            box2 = new ModelRenderer(this, 0, 0);
            box2.addBox(-5.0F, -7.0F, -5.0F, 10, 14, 10);
            box2.setTextureSize(128, 32);
            box2.mirror = true;
        }

        public void renderAll() {
            box1.render(0.625F);
            box2.render(0.625F);
        }
    }
}
