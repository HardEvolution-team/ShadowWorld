package com.electrifiedded.shadowworld.client;

import com.electrifiedded.shadowworld.tile.Pedestals.TilePedestal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/** 1.7 TileEntityPedestalRenderer: model, spinning item, beacon beam on active pedestals, ender-dragon-like rays while charging. */
public class PedestalRenderer extends TileEntitySpecialRenderer<TilePedestal> {
    static final ResourceLocation TEX = new ResourceLocation("shadowworld", "textures/models/ModelPedestal.png");
    private static final ResourceLocation BEAM = new ResourceLocation("textures/entity/beacon_beam.png");
    static final ModelPedestal MODEL = new ModelPedestal();

    static void renderModel(double x, double y, double z) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEX);
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        GlStateManager.scale(0.1F, 0.1F, 0.1F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        MODEL.renderAll();
        GlStateManager.popMatrix();
    }

    @Override
    public void render(TilePedestal te, double d, double d1, double d2, float pt, int destroy, float alpha) {
        renderModel(d, d1, d2);
        if (te.pedestalitem == null || te.getStack().isEmpty()) return;
        if (te.craftingticks < 100 && te.isMiddlePedestal) renderRays(te, d, d1, d2);
        else if (te.isActivePedestal) renderBeam(te, d, d1, d2, pt);
        else {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) d + 0.5F, (float) d1 + 0.76F, (float) d2 + 0.5F);
            GlStateManager.rotate(te.itemage, 0.0F, 1.0F, 0.0F);
            te.pedestalitem.hoverStart = 0.0F;
            Minecraft.getMinecraft().getRenderManager().renderEntity(te.pedestalitem, 0.0, 0.0, 0.0, 0.0F, 0.0F, false);
            GlStateManager.popMatrix();
        }
    }

    private void renderRays(TilePedestal te, double d, double d1, double d2) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        RenderHelper.disableStandardItemLighting();
        float f1 = (100.0F - te.craftingticks) / 200.0F;
        float f2 = f1 > 0.8F ? (f1 - 0.8F) / 0.2F : 0.0F;
        Random random = new Random(432L);
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        GlStateManager.disableAlpha();
        GlStateManager.enableCull();
        GlStateManager.depthMask(false);
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        for (int i = 0; i < (f1 + f1 * f1) / 2.0F * 600.0F; i++) {
            GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F + f1 * 90.0F, 0.0F, 0.0F, 1.0F);
            float len = random.nextFloat() * 2.0F + 1.0F + f2 * 2.0F;
            int a = (int) (255.0F * (1.0F - f2));
            buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(0, 0, 0).color(0x55, 0x55, 0xFF, a).endVertex();
            buf.pos(-0.866 * len, 0, -0.5F * len).color(255, 255, 255, 0).endVertex();
            buf.pos(0.866 * len, 0, -0.5F * len).color(255, 255, 255, 0).endVertex();
            buf.pos(0, 0, 1.0F * len).color(255, 255, 255, 0).endVertex();
            buf.pos(-0.866 * len, 0, -0.5F * len).color(255, 255, 255, 0).endVertex();
            tess.draw();
        }
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        RenderHelper.enableStandardItemLighting();
    }

    private void renderBeam(TilePedestal te, double d, double d1, double d2, float pt) {
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        bindTexture(BEAM);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
        float time = (float) te.getWorld().getTotalWorldTime() + pt;
        float f3 = -time * 0.2F - MathHelper.floor(-time * 0.1F);
        double rot = time * 0.025 * (1.0 - 2.5);
        double y0 = d1 + 0.5, r = 0.2, h = 256.0;
        double x7 = 0.5 + Math.cos(rot + Math.PI * 3.0 / 4.0) * r, z9 = 0.5 + Math.sin(rot + Math.PI * 3.0 / 4.0) * r;
        double x11 = 0.5 + Math.cos(rot + Math.PI / 4) * r, z13 = 0.5 + Math.sin(rot + Math.PI / 4) * r;
        double x15 = 0.5 + Math.cos(rot + Math.PI * 5.0 / 4.0) * r, z17 = 0.5 + Math.sin(rot + Math.PI * 5.0 / 4.0) * r;
        double x19 = 0.5 + Math.cos(rot + Math.PI * 7.0 / 4.0) * r, z21 = 0.5 + Math.sin(rot + Math.PI * 7.0 / 4.0) * r;
        double v0 = -1.0F + f3, v1 = 256.0 * (0.5 / r) + v0;
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        quad(buf, d, d2, y0, h, x7, z9, x11, z13, 1, 0, v0, v1, 32);
        quad(buf, d, d2, y0, h, x19, z21, x15, z17, 1, 0, v0, v1, 32);
        quad(buf, d, d2, y0, h, x11, z13, x19, z21, 1, 0, v0, v1, 32);
        quad(buf, d, d2, y0, h, x15, z17, x7, z9, 1, 0, v0, v1, 32);
        tess.draw();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.depthMask(false);
        double w0 = -1.0F + f3, w1 = 256.0 + w0;
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        quad(buf, d, d2, y0, h, 0.2, 0.2, 0.8, 0.2, 1, 0, w0, w1, 32);
        quad(buf, d, d2, y0, h, 0.8, 0.8, 0.2, 0.8, 1, 0, w0, w1, 32);
        quad(buf, d, d2, y0, h, 0.8, 0.2, 0.8, 0.8, 1, 0, w0, w1, 32);
        quad(buf, d, d2, y0, h, 0.2, 0.8, 0.2, 0.2, 1, 0, w0, w1, 32);
        tess.draw();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.5F);
    }

    private static void quad(BufferBuilder b, double d, double d2, double y0, double h, double xa, double za, double xb, double zb, double u0, double u1, double v0, double v1, int alpha) {
        b.pos(d + xa, y0 + h, d2 + za).tex(u0, v1).color(255, 255, 255, alpha).endVertex();
        b.pos(d + xa, y0, d2 + za).tex(u0, v0).color(255, 255, 255, alpha).endVertex();
        b.pos(d + xb, y0, d2 + zb).tex(u1, v0).color(255, 255, 255, alpha).endVertex();
        b.pos(d + xb, y0 + h, d2 + zb).tex(u1, v1).color(255, 255, 255, alpha).endVertex();
    }

    /** 1.7 PedestalItemRenderer. */
    public static class Item extends TileEntityItemStackRenderer {
        @Override
        public void renderByItem(ItemStack stack, float pt) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, -0.4F, 0.0F);
            renderModel(0, 0, 0);
            GlStateManager.popMatrix();
        }
    }

    /** 1.7 ModelPedestal (Techne). */
    public static class ModelPedestal extends ModelBase {
        final ModelRenderer[] shapes = new ModelRenderer[4];

        public ModelPedestal() {
            textureWidth = 64;
            textureHeight = 32;
            shapes[0] = box(0, 0, -6, 0, -6, 12, 1, 12);
            shapes[1] = box(0, 13, -4, -1, -4, 8, 1, 8);
            shapes[2] = box(32, 13, -3, -7, -3, 6, 6, 6);
            shapes[3] = box(0, 13, -4, -8, -4, 8, 1, 8);
        }

        private ModelRenderer box(int u, int v, float x, float y, float z, int w, int h, int d) {
            ModelRenderer r = new ModelRenderer(this, u, v);
            r.addBox(x, y, z, w, h, d);
            r.setRotationPoint(0.0F, 23.0F, 0.0F);
            r.setTextureSize(64, 32);
            r.mirror = true;
            r.rotateAngleY = (float) (Math.PI / 4);
            return r;
        }

        public void renderAll() { for (ModelRenderer r : shapes) r.render(0.625F); }
    }
}
