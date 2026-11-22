package com.electrifiedded.shadowworld.client;

import com.electrifiedded.archaiclib.crafting.LegacyCraftingContainer;
import com.electrifiedded.shadowworld.CommonProxy;
import com.electrifiedded.shadowworld.SWContent;
import com.electrifiedded.shadowworld.client.model.*;
import com.electrifiedded.shadowworld.entity.EntityElementStone;
import com.electrifiedded.shadowworld.entity.SWEntities.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import java.util.function.Function;

public class ClientProxy extends CommonProxy {
    static ResourceLocation tex(String n) { return new ResourceLocation("shadowworld", "textures/entity/" + n + ".png"); }

    /** Plain RenderLiving with a fixed texture. */
    static <T extends EntityLiving> IRenderFactory<T> living(ModelBase model, float shadow, String texture, float scale) {
        ResourceLocation t = tex(texture);
        return m -> new RenderLiving<T>(m, model, shadow) {
            @Override protected ResourceLocation getEntityTexture(T e) { return t; }
            @Override protected void preRenderCallback(T e, float pt) { if (scale != 1F) GlStateManager.scale(scale, scale, scale); }
        };
    }

    @Override
    public void preInit() {
        RenderingRegistry.registerEntityRenderingHandler(Shadow.class, m -> new RenderEnderman(m) {
            @Override protected ResourceLocation getEntityTexture(EntityEnderman e) { return tex("Shadow"); }
        });
        RenderingRegistry.registerEntityRenderingHandler(SkeletonSpider.class, m -> new RenderSpider<SkeletonSpider>(m) {
            @Override protected ResourceLocation getEntityTexture(SkeletonSpider e) { return tex("SkeletonSpider"); }
            @Override protected void preRenderCallback(SkeletonSpider e, float pt) { GlStateManager.scale(1.3F, 1.3F, 1.3F); }
        });
        RenderingRegistry.registerEntityRenderingHandler(WolfSpider.class, m -> new RenderSpider<WolfSpider>(m) {
            @Override protected ResourceLocation getEntityTexture(WolfSpider e) { return tex("Wolfspider"); }
            @Override protected void preRenderCallback(WolfSpider e, float pt) { GlStateManager.scale(0.75F, 0.75F, 0.75F); }
        });
        RenderingRegistry.registerEntityRenderingHandler(ShadowCreeper.class, m -> new RenderCreeper(m) {
            @Override protected ResourceLocation getEntityTexture(EntityCreeper e) { return tex("ShadowCreeper"); }
        });
        RenderingRegistry.registerEntityRenderingHandler(ExplodingWolf.class, living(new ModelExplodingWolf(), 0.5F, "ExplodingWolf", 1F));
        RenderingRegistry.registerEntityRenderingHandler(ShadowChicken.class, living(new ModelChicken(), 0.3F, "ShadowChicken", 1F));
        RenderingRegistry.registerEntityRenderingHandler(ShadowSlime.class, m -> new RenderSlime(m) {
            @Override protected ResourceLocation getEntityTexture(EntitySlime e) { return tex("ShadowSlime"); }
        });
        RenderingRegistry.registerEntityRenderingHandler(CookieSlime.class, m -> new RenderSlime(m) {
            @Override protected ResourceLocation getEntityTexture(EntitySlime e) { return tex("CookieSlime"); }
        });
        RenderingRegistry.registerEntityRenderingHandler(ShadowPig.class, m -> new RenderPig(m) {
            @Override protected ResourceLocation getEntityTexture(EntityPig e) { return tex("ShadowPig"); }
        });
        RenderingRegistry.registerEntityRenderingHandler(CookiePig.class, m -> new RenderPig(m) {
            @Override protected ResourceLocation getEntityTexture(EntityPig e) { return tex("CookiePig"); }
        });
        RenderingRegistry.registerEntityRenderingHandler(PassiveSpider.class, living(new ModelSpider(), 0.8F, "PassiveSpider", 1F));
        RenderingRegistry.registerEntityRenderingHandler(ShadowWolf.class, m -> new RenderWolf(m) {
            @Override protected ResourceLocation getEntityTexture(EntityWolf e) { return tex(e.isTamed() ? "ShadowWolf_tame" : e.isAngry() ? "ShadowWolf_angry" : "ShadowWolf"); }
            @Override protected void preRenderCallback(EntityWolf e, float pt) { GlStateManager.scale(1.5F, 1.5F, 1.5F); }
        });
        RenderingRegistry.registerEntityRenderingHandler(JellyFish.class, living(new ModelJellyFish(), 0.5F, "JellyFish", 1F));
        RenderingRegistry.registerEntityRenderingHandler(Light.class, living(new ModelLight(), 0.1F, "Light", 1F));
        RenderingRegistry.registerEntityRenderingHandler(Dragonfly.class, living(new ModelShadowDragonfly(), 0.2F, "ShadowDragonfly", 1F));
        RenderingRegistry.registerEntityRenderingHandler(FireGolem.class, living(new ModelGolem(), 1.0F, "FireGolem", 1F));
        RenderingRegistry.registerEntityRenderingHandler(EarthGolem.class, living(new ModelGolem(), 1.0F, "EarthGolem", 1F));
        RenderingRegistry.registerEntityRenderingHandler(WaterGolem.class, living(new ModelGolem(), 1.0F, "WaterGolem", 1F));
        RenderingRegistry.registerEntityRenderingHandler(AirGolem.class, living(new ModelGolem(), 1.0F, "AirGolem", 1F));
        RenderingRegistry.registerEntityRenderingHandler(ShadowWolfKing.class, living(new ModelShadowWolfKing(), 1.0F, "ShadowWolfKing", 1F));
        RenderingRegistry.registerEntityRenderingHandler(Angel.class, living(new ModelAngel(0.0F), 0.5F, "Angel", 1F));
        RenderingRegistry.registerEntityRenderingHandler(Dwarf.class, living(new ModelBiped(), 0.4F, "Dwarf1", 0.8F));
        RenderingRegistry.registerEntityRenderingHandler(TravellingDwarf.class, living(new ModelBiped(), 0.4F, "TravellingDwarf", 0.8F));
        RenderingRegistry.registerEntityRenderingHandler(AngelVillager.class, living(new ModelBiped(), 0.5F, "Angel1", 1F));
        RenderingRegistry.registerEntityRenderingHandler(EntityElementStone.class, m -> new RenderSnowball<EntityElementStone>(m, SWContent.FireStone, Minecraft.getMinecraft().getRenderItem()) {
            @Override public net.minecraft.item.ItemStack getStackToRender(EntityElementStone e) {
                Item it = switch (e.getType()) { case 0 -> SWContent.FireStone; case 1 -> SWContent.WaterStone; case 2 -> SWContent.EarthStone; case 3 -> SWContent.AirStone; default -> SWContent.ElementSphere; };
                return new net.minecraft.item.ItemStack(it);
            }
        });
    }

    @Override
    public Object craftingGui(LegacyCraftingContainer c, boolean plus) {
        return new GuiCraft(c, plus);
    }

    static class GuiCraft extends GuiContainer {
        private static final ResourceLocation VANILLA = new ResourceLocation("textures/gui/container/crafting_table.png");
        private static final ResourceLocation SHADOW = new ResourceLocation("shadowworld", "textures/gui/ShadowCrafting.png");
        private final boolean plus;
        GuiCraft(LegacyCraftingContainer c, boolean plus) { super(c); this.plus = plus; }

        @Override public void drawScreen(int mx, int my, float pt) { drawDefaultBackground(); super.drawScreen(mx, my, pt); renderHoveredToolTip(mx, my); }

        @Override
        protected void drawGuiContainerForegroundLayer(int mx, int my) {
            if (plus) {
                fontRenderer.drawString("§6Shadow", 120, 5, 4210752);
                fontRenderer.drawString("§6Crafting", 116, 20, 4210752);
            } else fontRenderer.drawString("Shadow Crafting", 28, 6, 4210752);
        }

        @Override
        protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
            GlStateManager.color(1F, 1F, 1F, 1F);
            int k = (width - xSize) / 2, l = (height - ySize) / 2;
            if (plus) {
                mc.getTextureManager().bindTexture(SHADOW);
                drawTexturedModalRect(k - 4, l - 4, 0, 0, xSize + 10, ySize + 10);
            } else {
                mc.getTextureManager().bindTexture(VANILLA);
                drawTexturedModalRect(k, l, 0, 0, xSize, ySize);
            }
        }
    }

    static Function<Entity, Entity> unused() { return e -> e; }
}
