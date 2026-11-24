package com.electrifiedded.shadowworld.client;

import com.electrifiedded.shadowworld.entity.EntityFlyingBoat;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/** 1.7 RenderFlyingBoat + ModelFlyingBoat (2.5x scaled hull, gentle wobble, optional angel / ender chest). */
public class RenderFlyingBoat extends Render<EntityFlyingBoat> {
    private static final ResourceLocation BOAT = new ResourceLocation("shadowworld", "textures/models/ModelFlyingBoat.png");
    private static final ResourceLocation CHEST = new ResourceLocation("shadowworld", "textures/entity/chest/angel.png");
    private static final ResourceLocation ENDER = new ResourceLocation("textures/entity/chest/ender.png");
    private final ModelFlyingBoat model = new ModelFlyingBoat();
    private final ModelChest chest = new ModelChest();

    public RenderFlyingBoat(RenderManager m) {
        super(m);
        shadowSize = 0.0F;
    }

    @Override
    public void doRender(EntityFlyingBoat e, double x, double y, double z, float yaw, float pt) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + 3.5F, (float) z);
        GlStateManager.rotate(180.0F - yaw, 0.0F, 1.0F, 0.0F);
        float hit = e.getTimeSinceHit() - pt, dmg = Math.max(0.0F, e.getDamageTaken() - pt);
        if (hit > 0.0F) GlStateManager.rotate(MathHelper.sin(hit) * hit * dmg / 20.0F * e.getForwardDirection(), 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(MathHelper.sin(e.ticksExisted * 0.05F), 0.0F, 0.9F, 1.0F);
        GlStateManager.scale(2.5F, 2.5F, 2.5F);
        bindEntityTexture(e);
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        model.render(e, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        if (e.getHasChest() || e.getHasEnderChest()) {
            bindTexture(e.getHasChest() ? CHEST : ENDER);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.translate(0.5F, 1.8F, -1.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            float lid = e.prevLidAngle + (e.lidAngle - e.prevLidAngle) * pt;
            lid = 1.0F - lid;
            lid = 1.0F - lid * lid * lid;
            chest.chestLid.rotateAngleX = -(lid * (float) Math.PI / 2.0F);
            chest.renderAll();
        }
        GlStateManager.popMatrix();
        super.doRender(e, x, y, z, yaw, pt);
    }

    @Override protected ResourceLocation getEntityTexture(EntityFlyingBoat e) { return BOAT; }

    /** 1.7 ModelFlyingBoat (Techne): {u, v, x, y, z, w, h, d, pivotY}. */
    static class ModelFlyingBoat extends ModelBase {
        private static final int[][] BOXES = {
                {0, 25, -4, 0, -11, 8, 1, 22, 23}, {0, 48, -3, -3, -23, 6, 1, 2, 23}, {0, 51, -4, -2, -21, 8, 1, 4, 23},
                {0, 56, -4, -1, -17, 8, 1, 6, 23}, {0, 56, -4, -1, 11, 8, 1, 6, 23}, {0, 51, -4, -2, 17, 8, 1, 4, 23},
                {0, 48, -3, -3, 21, 6, 1, 2, 23}, {60, 25, -4, -2, -6, 8, 1, 4, 23}, {0, 0, 4, -2, -11, 1, 3, 22, 22},
                {0, 0, -5, -3, -11, 1, 3, 22, 23}, {46, 0, 3, -4, -21, 1, 2, 4, 23}, {46, 0, -4, -4, -21, 1, 2, 4, 23},
                {56, 0, 4, -5, -17, 1, 3, 6, 24}, {56, 0, -5, -5, -17, 1, 3, 6, 24}, {70, 0, 4, -5, 11, 1, 3, 6, 24},
                {70, 0, -5, -5, 11, 1, 3, 6, 24}, {84, 0, -4, -4, 17, 1, 2, 4, 23}, {84, 0, 3, -4, 17, 1, 2, 4, 23}};
        private final ModelRenderer[] parts = new ModelRenderer[BOXES.length];

        ModelFlyingBoat() {
            textureWidth = 128;
            textureHeight = 128;
            for (int i = 0; i < BOXES.length; i++) {
                int[] b = BOXES[i];
                ModelRenderer r = new ModelRenderer(this, b[0], b[1]);
                r.addBox(b[2], b[3], b[4], b[5], b[6], b[7]);
                r.setRotationPoint(0.0F, b[8], 0.0F);
                r.setTextureSize(128, 128);
                r.mirror = true;
                parts[i] = r;
            }
        }

        @Override
        public void render(Entity e, float f, float f1, float f2, float f3, float f4, float scale) {
            for (ModelRenderer r : parts) r.render(scale);
        }
    }
}
