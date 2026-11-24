package com.electrifiedded.shadowworld.client;

import com.electrifiedded.shadowworld.client.model.ModelAngel;
import com.electrifiedded.shadowworld.entity.SWAngels;
import com.electrifiedded.shadowworld.tile.AngelPortals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;

import javax.annotation.Nullable;
import java.util.Random;

/** Client side of the Angel content (1.7 TileEntityAngelPortalRenderer, RenderFallingAngelPortal, RenderAngel, RenderAngelVillager, AngelSkyRenderer). */
public final class AngelRenderers {
    private AngelRenderers() {}

    static final ResourceLocation PORTAL_TEX = new ResourceLocation("shadowworld", "textures/blocks/AngelPortal.png");
    private static int sphereList = -1;

    /** ClientProxy.sphereID: a textured GLU sphere of radius 0.5. */
    static void drawSphere() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(PORTAL_TEX);
        if (sphereList < 0) {
            Sphere s = new Sphere();
            s.setDrawStyle(GLU.GLU_FILL);
            s.setNormals(GLU.GLU_SMOOTH);
            s.setOrientation(GLU.GLU_OUTSIDE);
            s.setTextureFlag(true);
            sphereList = GL11.glGenLists(1);
            GL11.glNewList(sphereList, GL11.GL_COMPILE);
            s.draw(0.5F, 32, 32);
            GL11.glEndList();
        }
        GlStateManager.callList(sphereList);
    }

    /** Ender-dragon-death style ray fan shared by the portal and the Angel's super attack. */
    static void rays(float f1, float rayY, int rgbIn, int rgbOut) {
        float f2 = f1 > 0.8F ? (f1 - 0.8F) / 0.2F : 0.0F;
        Random random = new Random(432L);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        GlStateManager.disableAlpha();
        GlStateManager.enableCull();
        GlStateManager.depthMask(false);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        int ri = rgbIn >> 16 & 255, gi = rgbIn >> 8 & 255, bi = rgbIn & 255, ro = rgbOut >> 16 & 255, go = rgbOut >> 8 & 255, bo = rgbOut & 255;
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
            buf.pos(0.0, 0.0, 0.0).color(ri, gi, bi, a).endVertex();
            buf.pos(-0.866 * len, rayY, -0.5F * len).color(ro, go, bo, 0).endVertex();
            buf.pos(0.866 * len, rayY, -0.5F * len).color(ro, go, bo, 0).endVertex();
            buf.pos(0.0, rayY, 1.0F * len).color(ro, go, bo, 0).endVertex();
            buf.pos(-0.866 * len, rayY, -0.5F * len).color(ro, go, bo, 0).endVertex();
            tess.draw();
        }
        GlStateManager.depthMask(true);
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        RenderHelper.enableStandardItemLighting();
    }

    public static class PortalRenderer extends TileEntitySpecialRenderer<AngelPortals.TilePortal> {
        @Override
        public void render(AngelPortals.TilePortal te, double x, double y, double z, float pt, int destroy, float alpha) {
            float bob = MathHelper.cos(te.floattime * 0.1F) / 10.0F;
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F + bob, (float) z + 0.5F);
            GlStateManager.rotate(te.rotation, 0.0F, 1.0F, 0.0F);
            drawSphere();
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F + bob, (float) z + 0.5F);
            GlStateManager.scale(0.45F, 0.45F, 0.45F);
            rays((360.0F - te.rotation) / 360.0F, 0.0F, 14168844, 16748075);
            GlStateManager.popMatrix();
        }
        @Override public boolean isGlobalRenderer(AngelPortals.TilePortal te) { return true; }
    }

    public static class FallingRenderer extends Render<AngelPortals.EntityFalling> {
        public FallingRenderer(RenderManager m) { super(m); shadowSize = 0.5F; }
        @Override
        public void doRender(AngelPortals.EntityFalling e, double x, double y, double z, float yaw, float pt) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x, (float) y + 0.5F, (float) z);
            drawSphere();
            GlStateManager.popMatrix();
        }
        @Override protected ResourceLocation getEntityTexture(AngelPortals.EntityFalling e) { return TextureMap.LOCATION_BLOCKS_TEXTURE; }
    }

    /** RenderAngel: ModelBiped x1.5 with Angel.png, armour, held sword and the super-attack rays. */
    public static class AngelRender extends RenderBiped<SWAngels.Angel> {
        private static final ResourceLocation TEX = new ResourceLocation("shadowworld", "textures/entity/Angel.png");
        public AngelRender(RenderManager m) {
            super(m, new ModelBiped(0.0F), 0.5F);
            addLayer(new net.minecraft.client.renderer.entity.layers.LayerBipedArmor(this) {
                @Override protected void initArmor() { modelLeggings = new ModelAngel(0.5F); modelArmor = new ModelAngel(1.0F); }
            });
            addLayer(new LayerRenderer<SWAngels.Angel>() {
                @Override
                public void doRenderLayer(SWAngels.Angel e, float a, float b, float pt, float c, float d, float f, float scale) {
                    if (e.getSuperAttackTime() <= 450) return;
                    float f1 = (e.getSuperAttackTime() / 10.0F + pt) / 200.0F;
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(0.0F, 1.0F, 0.0F);
                    rays(f1, 8.0F, 13757162, 13757162);
                    GlStateManager.popMatrix();
                }
                @Override public boolean shouldCombineTextures() { return false; }
            });
        }
        @Override protected ResourceLocation getEntityTexture(SWAngels.Angel e) { return TEX; }
        @Override protected void preRenderCallback(SWAngels.Angel e, float pt) { GlStateManager.scale(1.5F, 1.5F, 1.5F); }
    }

    /** RenderAngelVillager: Angel1..4 by profession (4 = Angel1). */
    public static class VillagerRender extends RenderBiped<SWAngels.AngelVillager> {
        private static final ResourceLocation[] TEX = new ResourceLocation[4];
        static { for (int i = 0; i < 4; i++) TEX[i] = new ResourceLocation("shadowworld", "textures/entity/Angel" + (i + 1) + ".png"); }
        public VillagerRender(RenderManager m) {
            super(m, new ModelBiped(0.0F), 0.5F);
            addLayer(new net.minecraft.client.renderer.entity.layers.LayerBipedArmor(this) {
                @Override protected void initArmor() { modelLeggings = new ModelAngel(0.5F); modelArmor = new ModelAngel(1.0F); }
            });
        }
        @Override protected ResourceLocation getEntityTexture(SWAngels.AngelVillager e) { int p = e.getProfession(); return TEX[p >= 0 && p <= 3 ? p : 0]; }
    }

    /** AngelSkyRenderer: black sky, one big sun texture and a dense star field (10000 stars). */
    public static class Sky extends IRenderHandler {
        private static final ResourceLocation SUN = new ResourceLocation("shadowworld", "textures/environment/sun.png");
        private int starList = -1;

        private void buildStars() {
            starList = GLAllocationHelper.list();
            GL11.glNewList(starList, GL11.GL_COMPILE);
            Random random = new Random(10842L);
            BufferBuilder buf = Tessellator.getInstance().getBuffer();
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            for (int i = 0; i < 10000; i++) {
                double d0 = random.nextFloat() * 2.0F - 1.0F, d1 = random.nextFloat() * 2.0F - 1.0F, d2 = random.nextFloat() * 2.0F - 1.0F;
                double d3 = 0.15F + random.nextFloat() * 0.1F;
                double d4 = d0 * d0 + d1 * d1 + d2 * d2;
                if (d4 < 1.0 && d4 > 0.001) {
                    d4 = 1.0 / Math.sqrt(d4);
                    d0 *= d4; d1 *= d4; d2 *= d4;
                    double d5 = d0 * 100.0, d6 = d1 * 100.0, d7 = d2 * 100.0;
                    double d8 = Math.atan2(d0, d2), d9 = Math.sin(d8), d10 = Math.cos(d8);
                    double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1), d12 = Math.sin(d11), d13 = Math.cos(d11);
                    double d14 = random.nextDouble() * Math.PI * 2.0, d15 = Math.sin(d14), d16 = Math.cos(d14);
                    for (int j = 0; j < 4; j++) {
                        double d18 = ((j & 2) - 1) * d3, d19 = ((j + 1 & 2) - 1) * d3;
                        double d20 = d18 * d16 - d19 * d15, d21 = d19 * d16 + d18 * d15;
                        double d22 = d20 * d12, d23 = -d20 * d13;
                        double d24 = d23 * d9 - d21 * d10, d25 = d21 * d9 + d23 * d10;
                        buf.pos(d5 + d24, d6 + d22, d7 + d25).endVertex();
                    }
                }
            }
            Tessellator.getInstance().draw();
            GL11.glEndList();
        }

        @Override
        public void render(float pt, WorldClient world, Minecraft mc) {
            if (starList < 0) buildStars();
            GlStateManager.disableFog();
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.depthMask(false);
            GlStateManager.enableTexture2D();
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(world.getCelestialAngle(pt) * 360.0F, 0.0F, 1.0F, 0.0F);
            float s = 30.0F;
            mc.getTextureManager().bindTexture(SUN);
            BufferBuilder buf = Tessellator.getInstance().getBuffer();
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buf.pos(-s, 100.0, -s).tex(0.0, 0.0).endVertex();
            buf.pos(s, 100.0, -s).tex(1.0, 0.0).endVertex();
            buf.pos(s, 100.0, s).tex(1.0, 1.0).endVertex();
            buf.pos(-s, 100.0, s).tex(0.0, 1.0).endVertex();
            Tessellator.getInstance().draw();
            GlStateManager.disableTexture2D();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.callList(starList);
            GlStateManager.popMatrix();
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableFog();
            GlStateManager.enableTexture2D();
            GlStateManager.depthMask(true);
        }
    }

    static final class GLAllocationHelper {
        static int list() { return net.minecraft.client.renderer.GLAllocation.generateDisplayLists(1); }
    }

}
