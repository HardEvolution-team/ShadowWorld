package com.electrifiedded.shadowworld.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

/** 1.7 EffectHandler + EntityShadowFX / EntityGlowingFX / EntityShineFX (own particle sheet textures/entity/particles.png). */
public final class SWParticles {
    private SWParticles() {}

    static final ResourceLocation SHEET = new ResourceLocation("shadowworld", "textures/entity/particles.png");
    static final ResourceLocation VANILLA = new ResourceLocation("textures/particle/particles.png");

    /** EffectHandler.spawnParticle: names "Shadow", "Glow", "Shine"; 16-block range, honours the particle setting. */
    public static Particle spawn(String name, double x, double y, double z, double mx, double my, double mz) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getRenderViewEntity() == null || mc.effectRenderer == null || mc.world == null) return null;
        int setting = mc.gameSettings.particleSetting;
        if (setting == 1 && mc.world.rand.nextInt(3) == 0) setting = 2;
        Entity v = mc.getRenderViewEntity();
        double dx = v.posX - x, dy = v.posY - y, dz = v.posZ - z;
        if (dx * dx + dy * dy + dz * dz > 256.0 || setting > 1) return null;
        Particle p = switch (name) {
            case "Shadow" -> new Smoke(mc.world, x, y, z, mx, my, mz, false);
            case "Shine" -> new Smoke(mc.world, x, y, z, mx, my, mz, true);
            case "Glow" -> new Glow(mc.world, x, y, z, mx, my, mz);
            default -> null;
        };
        if (p != null) mc.effectRenderer.addEffect(p);
        return p;
    }

    /** Draws the particle with the mod sheet, then restores the vanilla sheet (same trick as the 1.7 code). */
    static void renderOwnSheet(Particle p, BufferBuilder buf, Entity e, float pt, float rx, float rz, float ryz, float rxy, float rxz, Runnable superRender) {
        Tessellator t = Tessellator.getInstance();
        t.draw();
        Minecraft.getMinecraft().getTextureManager().bindTexture(SHEET);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        superRender.run();
        t.draw();
        Minecraft.getMinecraft().getTextureManager().bindTexture(VANILLA);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
    }

    /** EntityShadowFX (black) / EntityShineFX (golden): rising smoke puff. */
    static class Smoke extends Particle {
        final float baseScale;

        Smoke(World w, double x, double y, double z, double mx, double my, double mz, boolean shine) {
            super(w, x, y, z, 0.0, 0.0, 0.0);
            motionX = motionX * 0.1F + mx;
            motionY = motionY * 0.1F + my;
            motionZ = motionZ * 0.1F + mz;
            if (shine) {
                particleRed = 0.7137255F + rand.nextInt(10) * 0.003F;
                particleGreen = 0.58431375F - rand.nextInt(10) * 0.03F;
                particleBlue = 0.3372549F - rand.nextInt(10) * 0.01F;
            } else particleRed = particleGreen = particleBlue = 0.0F;
            particleScale *= 4.0F;
            baseScale = particleScale;
            particleMaxAge = (int) (8.0 / (Math.random() * 0.8 + 0.2));
            canCollide = true;
        }

        @Override public int getBrightnessForRender(float pt) { return 200; }

        @Override
        public void renderParticle(BufferBuilder buf, Entity e, float pt, float rx, float rz, float ryz, float rxy, float rxz) {
            float f = (particleAge + pt) / particleMaxAge * 32.0F;
            particleScale = baseScale * Math.max(0.0F, Math.min(1.0F, f));
            renderOwnSheet(this, buf, e, pt, rx, rz, ryz, rxy, rxz, () -> super.renderParticle(buf, e, pt, rx, rz, ryz, rxy, rxz));
        }

        @Override
        public void onUpdate() {
            prevPosX = posX;
            prevPosY = posY;
            prevPosZ = posZ;
            if (particleAge++ >= particleMaxAge) setExpired();
            setParticleTextureIndex(7 - particleAge * 8 / particleMaxAge);
            motionY += 0.004;
            move(motionX, motionY, motionZ);
            if (posY == prevPosY) {
                motionX *= 1.1;
                motionZ *= 1.1;
            }
            motionX *= 0.96F;
            motionY *= 0.96F;
            motionZ *= 0.96F;
            if (onGround) {
                motionX *= 0.7F;
                motionZ *= 0.7F;
            }
        }
    }

    /** EntityGlowingFX: portal-like particle drifting back to its origin and fading in brightness. */
    static class Glow extends Particle {
        final double ox, oy, oz;

        Glow(World w, double x, double y, double z, double mx, double my, double mz) {
            super(w, x, y, z, mx, my, mz);
            motionX = mx;
            motionY = my;
            motionZ = mz;
            ox = posX = x;
            oy = posY = y;
            oz = posZ = z;
            float f = rand.nextFloat() * 0.6F + 0.4F;
            particleScale = rand.nextFloat() * 0.5F + 0.2F;
            particleRed = particleGreen = particleBlue = f;
            particleGreen *= 0.9F;
            particleRed *= 0.9F;
            particleMaxAge = (int) (Math.random() * 10.0) + 30;
            canCollide = false;
            nextTextureIndexX();
        }

        @Override
        public int getBrightnessForRender(float pt) {
            int i = super.getBrightnessForRender(pt);
            float f = (float) particleAge / particleMaxAge;
            f = f * f * f * f;
            int j = i & 0xFF, k = (i >> 16 & 0xFF) + (int) (f * 15.0F * 16.0F);
            return j | Math.min(k, 240) << 16;
        }

        @Override
        public void renderParticle(BufferBuilder buf, Entity e, float pt, float rx, float rz, float ryz, float rxy, float rxz) {
            renderOwnSheet(this, buf, e, pt, rx, rz, ryz, rxy, rxz, () -> super.renderParticle(buf, e, pt, rx, rz, ryz, rxy, rxz));
        }

        @Override
        public void onUpdate() {
            prevPosX = posX;
            prevPosY = posY;
            prevPosZ = posZ;
            float f = 1.0F - (float) particleAge / particleMaxAge;
            float f1 = 1.0F - f;
            f1 = f1 * f1 * f1 * f1;
            posX = ox + motionX * f;
            posY = oy - motionY * f + f1 * 1.2F;
            posZ = oz + motionZ * f;
            if (particleAge++ >= particleMaxAge) setExpired();
        }
    }
}
