package com.electrifiedded.shadowworld.client;

import com.electrifiedded.shadowworld.client.model.*;
import com.electrifiedded.shadowworld.entity.SWEntities.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import java.util.function.Predicate;

/** Renderers for the 1.7 Shadow World mobs (1.7 Render* classes: textures, skins, scales, glowing eye passes). */
public final class SWMobRenderers {
    private SWMobRenderers() {}

    static ResourceLocation tex(String n) { return ClientProxy.tex(n); }

    /** 1.7 setSpiderEyeBrightness pass: additive full-bright overlay. */
    public static class LayerEyes<T extends EntityLivingBase> implements LayerRenderer<T> {
        private final RenderLivingBase<T> r; private final ResourceLocation t; private final Predicate<T> when;
        public LayerEyes(RenderLivingBase<T> r, ResourceLocation t, Predicate<T> when) { this.r = r; this.t = t; this.when = when; }
        @Override public void doRenderLayer(T e, float ls, float lsa, float pt, float age, float yaw, float pitch, float scale) {
            if (!when.test(e)) return;
            r.bindTexture(t);
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            GlStateManager.depthMask(!e.isInvisible());
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680.0F, 0.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
            r.getMainModel().render(e, ls, lsa, age, yaw, pitch, scale);
            Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
            int i = e.getBrightnessForRender();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, i % 65536, i / 65536);
            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
        }
        @Override public boolean shouldCombineTextures() { return false; }
    }

    /** 1.7 ModelZombie arm pose on a plain biped (the 1.12 ModelZombie casts to EntityZombie). */
    public static class ModelShadow extends ModelBiped {
        @Override public void setRotationAngles(float ls, float lsa, float age, float yaw, float pitch, float scale, Entity e) {
            super.setRotationAngles(ls, lsa, age, yaw, pitch, scale, e);
            float f6 = MathHelper.sin(swingProgress * (float) Math.PI);
            float f7 = MathHelper.sin((1.0F - (1.0F - swingProgress) * (1.0F - swingProgress)) * (float) Math.PI);
            bipedRightArm.rotateAngleZ = 0.0F; bipedLeftArm.rotateAngleZ = 0.0F;
            bipedRightArm.rotateAngleY = -(0.1F - f6 * 0.6F); bipedLeftArm.rotateAngleY = 0.1F - f6 * 0.6F;
            bipedRightArm.rotateAngleX = -((float) Math.PI / 2F); bipedLeftArm.rotateAngleX = -((float) Math.PI / 2F);
            bipedRightArm.rotateAngleX -= f6 * 1.2F - f7 * 0.4F; bipedLeftArm.rotateAngleX -= f6 * 1.2F - f7 * 0.4F;
            bipedRightArm.rotateAngleZ += MathHelper.cos(age * 0.09F) * 0.05F + 0.05F; bipedLeftArm.rotateAngleZ -= MathHelper.cos(age * 0.09F) * 0.05F + 0.05F;
            bipedRightArm.rotateAngleX += MathHelper.sin(age * 0.067F) * 0.05F; bipedLeftArm.rotateAngleX -= MathHelper.sin(age * 0.067F) * 0.05F;
        }
    }

    /** RenderShadowCreeper / RenderExplodingWolf: swell + white flash. */
    static class FuseRender<T extends FuseMob> extends RenderLiving<T> {
        private final ResourceLocation t;
        FuseRender(RenderManager m, ModelBase model, String tex) { super(m, model, 0.5F); t = tex(tex); }
        @Override protected void preRenderCallback(T e, float pt) {
            float f1 = e.getFlashIntensity(pt);
            float f2 = 1.0F + MathHelper.sin(f1 * 100.0F) * f1 * 0.01F;
            f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
            f1 *= f1; f1 *= f1;
            float f3 = (1.0F + f1 * 0.4F) * f2, f4 = (1.0F + f1 * 0.1F) / f2;
            GlStateManager.scale(f3, f4, f3);
        }
        @Override protected int getColorMultiplier(T e, float light, float pt) {
            float f2 = e.getFlashIntensity(pt);
            if ((int) (f2 * 10.0F) % 2 == 0) return 0;
            int i = MathHelper.clamp((int) (f2 * 0.2F * 255.0F), 0, 255);
            return i << 24 | 0xFFFFFF;
        }
        @Override protected ResourceLocation getEntityTexture(T e) { return t; }
    }

    static class SlimeRender<T extends EntitySlime> extends RenderLiving<T> {
        private final ResourceLocation t;
        SlimeRender(RenderManager m, String tex) {
            super(m, new ModelSlime(16), 0.25F);
            t = tex(tex);
            ModelSlime gel = new ModelSlime(0);
            RenderLiving<T> self = this;
            addLayer(new LayerRenderer<T>() {
                @Override public void doRenderLayer(T e, float ls, float lsa, float pt, float age, float yaw, float pitch, float scale) {
                    if (e.isInvisible()) return;
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.enableNormalize();
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    gel.setModelAttributes(self.getMainModel());
                    gel.render(e, ls, lsa, age, yaw, pitch, scale);
                    GlStateManager.disableBlend();
                    GlStateManager.disableNormalize();
                }
                @Override public boolean shouldCombineTextures() { return true; }
            });
        }
        @Override public void doRender(T e, double x, double y, double z, float yaw, float pt) {
            shadowSize = 0.25F * e.getSlimeSize();
            super.doRender(e, x, y, z, yaw, pt);
        }
        @Override protected void preRenderCallback(T e, float pt) {
            float f1 = e.getSlimeSize();
            float f2 = (e.prevSquishFactor + (e.squishFactor - e.prevSquishFactor) * pt) / (f1 * 0.5F + 1.0F);
            float f3 = 1.0F / (f2 + 1.0F);
            GlStateManager.scale(f3 * f1, 1.0F / f3 * f1, f3 * f1);
        }
        @Override protected ResourceLocation getEntityTexture(T e) { return t; }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register() {
        RenderingRegistry.registerEntityRenderingHandler(Shadow.class, m -> {
            RenderLiving<Shadow> r = new RenderLiving<Shadow>(m, new ModelShadow(), 1.0F) {
                @Override protected ResourceLocation getEntityTexture(Shadow e) { return tex("Shadow"); }
            };
            r.addLayer(new LayerEyes<>(r, tex("ShadowEyes"), e -> true));
            return r;
        });
        RenderingRegistry.registerEntityRenderingHandler(SkeletonSpider.class, m -> {
            RenderLiving<SkeletonSpider> r = new RenderLiving<SkeletonSpider>(m, new ModelSpider(), 1.3F) {
                @Override protected ResourceLocation getEntityTexture(SkeletonSpider e) { return tex("SkeletonSpider"); }
                @Override protected void preRenderCallback(SkeletonSpider e, float pt) { GlStateManager.scale(1.3F, 1.3F, 1.3F); }
                @Override protected float getDeathMaxRotation(SkeletonSpider e) { return 180.0F; }
            };
            r.addLayer(new LayerEyes<>(r, tex("SkeletonSpiderEyes"), e -> true));
            return r;
        });
        RenderingRegistry.registerEntityRenderingHandler(WolfSpider.class, m -> {
            RenderLiving<WolfSpider> r = new RenderLiving<WolfSpider>(m, new ModelSpider(), 0.75F) {
                @Override protected ResourceLocation getEntityTexture(WolfSpider e) { return tex("Wolfspider"); }
                @Override protected void preRenderCallback(WolfSpider e, float pt) { GlStateManager.scale(0.75F, 0.75F, 0.75F); }
                @Override protected float getDeathMaxRotation(WolfSpider e) { return 180.0F; }
            };
            r.addLayer(new LayerEyes<>(r, tex("WolfSpiderEyes"), e -> true));
            return r;
        });
        RenderingRegistry.registerEntityRenderingHandler(PassiveSpider.class, m -> new RenderLiving<PassiveSpider>(m, new ModelSpider(), 1.0F) {
            @Override protected ResourceLocation getEntityTexture(PassiveSpider e) { return tex("PassiveSpider"); }
            @Override protected void preRenderCallback(PassiveSpider e, float pt) { GlStateManager.scale(0.5F, 0.5F, 0.5F); }
            @Override protected float getDeathMaxRotation(PassiveSpider e) { return 180.0F; }
        });
        RenderingRegistry.registerEntityRenderingHandler(ShadowCreeper.class, m -> new FuseRender<ShadowCreeper>(m, new ModelCreeper(), "ShadowCreeper"));
        RenderingRegistry.registerEntityRenderingHandler(ExplodingWolf.class, m -> {
            FuseRender<ExplodingWolf> r = new FuseRender<>(m, new ModelExplodingWolf(), "ExplodingWolf");
            r.addLayer(new LayerEyes<>(r, tex("ExplodingWolfEyes"), e -> true));
            return r;
        });
        RenderingRegistry.registerEntityRenderingHandler(ShadowChicken.class, m -> {
            RenderLiving<ShadowChicken> r = new RenderLiving<ShadowChicken>(m, new ModelChicken(), 0.5F) {
                @Override protected ResourceLocation getEntityTexture(ShadowChicken e) { return tex("ShadowChicken"); }
            };
            r.addLayer(new LayerEyes<>(r, tex("ShadowChickenEyes"), e -> true));
            return r;
        });
        RenderingRegistry.registerEntityRenderingHandler(ShadowSlime.class, m -> new SlimeRender<ShadowSlime>(m, "ShadowSlime"));
        RenderingRegistry.registerEntityRenderingHandler(CookieSlime.class, m -> new SlimeRender<CookieSlime>(m, "CookieSlime"));
        RenderingRegistry.registerEntityRenderingHandler(ShadowPig.class, m -> {
            RenderLiving<ShadowPig> r = new RenderLiving<ShadowPig>(m, new ModelPig(), 0.5F) {
                @Override protected ResourceLocation getEntityTexture(ShadowPig e) { return tex("ShadowPig"); }
            };
            ResourceLocation saddle = new ResourceLocation("textures/entity/pig/pig_saddle.png");
            ModelPig saddleModel = new ModelPig(0.5F);
            r.addLayer(new LayerRenderer<ShadowPig>() {
                @Override public void doRenderLayer(ShadowPig e, float ls, float lsa, float pt, float age, float yaw, float pitch, float scale) {
                    if (!e.getSaddled()) return;
                    r.bindTexture(saddle);
                    saddleModel.setModelAttributes(r.getMainModel());
                    saddleModel.render(e, ls, lsa, age, yaw, pitch, scale);
                }
                @Override public boolean shouldCombineTextures() { return false; }
            });
            return r;
        });
        RenderingRegistry.registerEntityRenderingHandler(CookiePig.class, m -> {
            RenderLiving<CookiePig> r = new RenderLiving<CookiePig>(m, new ModelPig(), 0.5F) {
                @Override protected ResourceLocation getEntityTexture(CookiePig e) { return tex("CookiePig4"); }
            };
            ModelPig over = new ModelPig();
            r.addLayer(new LayerRenderer<CookiePig>() {
                @Override public void doRenderLayer(CookiePig e, float ls, float lsa, float pt, float age, float yaw, float pitch, float scale) {
                    int n = e.getEaten();
                    String t = n >= 5 ? "CookiePig" : n == 4 ? "CookiePig1" : n == 3 ? "CookiePig2" : n == 2 ? "CookiePig3" : "CookiePig5";
                    r.bindTexture(tex(t));
                    over.setModelAttributes(r.getMainModel());
                    over.render(e, ls, lsa, age, yaw, pitch, scale);
                }
                @Override public boolean shouldCombineTextures() { return false; }
            });
            return r;
        });
        RenderingRegistry.registerEntityRenderingHandler(ShadowWolf.class, m -> {
            RenderWolf r = new RenderWolf(m) {
                @Override protected ResourceLocation getEntityTexture(EntityWolf e) { return tex(e.isTamed() ? "ShadowWolf_tame" : e.isAngry() ? "ShadowWolf_angry" : "ShadowWolf"); }
                @Override protected void preRenderCallback(EntityWolf e, float pt) { GlStateManager.scale(1.5F, 1.5F, 1.5F); }
            };
            r.addLayer((LayerRenderer) new LayerEyes<EntityWolf>(r, tex("ShadowWolfEyes"), EntityWolf::isTamed));
            return r;
        });
        RenderingRegistry.registerEntityRenderingHandler(JellyFish.class, m -> new RenderLiving<JellyFish>(m, new ModelJellyFish(), 1.0F) {
            private final ResourceLocation[] skins = {tex("JellyFish"), tex("JellyFish2"), tex("JellyFish3"), tex("JellyFish4")};
            @Override protected ResourceLocation getEntityTexture(JellyFish e) { int s = e.getSkin(); return skins[s >= 1 && s <= 3 ? s : 0]; }
            @Override protected void applyRotations(JellyFish e, float age, float yaw, float pt) {
                float f3 = e.prevSquidPitch + (e.squidPitch - e.prevSquidPitch) * pt;
                float f4 = e.prevSquidYaw + (e.squidYaw - e.prevSquidYaw) * pt;
                GlStateManager.translate(0.0F, 0.5F, 0.0F);
                GlStateManager.rotate(180.0F - yaw, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(f3, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(f4, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(0.0F, -1.2F, 0.0F);
            }
            @Override protected float handleRotationFloat(JellyFish e, float pt) { return e.prevTentacleAngle + (e.tentacleAngle - e.prevTentacleAngle) * pt; }
            @Override protected void preRenderCallback(JellyFish e, float pt) {
                float s = Math.max(0.5F, e.getScale() - 0.5F); // 1.7 used scale-0.5, which turns scale 0 inside out
                GlStateManager.scale(s, s, s);
            }
        });
        RenderingRegistry.registerEntityRenderingHandler(Light.class, m -> new RenderLiving<Light>(m, new ModelLight(), 0.7F) {
            private final ResourceLocation[] skins = {tex("Light"), tex("Light2"), tex("Light3"), tex("Light4")};
            @Override protected ResourceLocation getEntityTexture(Light e) { int s = e.getSkin(); return skins[s >= 1 && s <= 3 ? s : 0]; }
        });
        RenderingRegistry.registerEntityRenderingHandler(Dragonfly.class, m -> new RenderLiving<Dragonfly>(m, new ModelShadowDragonfly(), 1.0F) {
            @Override protected ResourceLocation getEntityTexture(Dragonfly e) { return tex("ShadowDragonfly"); }
            @Override protected void preRenderCallback(Dragonfly e, float pt) { float s = e.getScale(); GlStateManager.scale(s, s, s); }
        });
    }
}
