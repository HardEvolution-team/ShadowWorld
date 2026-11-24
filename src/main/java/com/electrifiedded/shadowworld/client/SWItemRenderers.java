package com.electrifiedded.shadowworld.client;

import com.electrifiedded.archaiclib.v1710.api.client.ArchaicMinecraftForgeClient;
import com.electrifiedded.archaiclib.v1710.api.client.IItemRenderer;
import com.electrifiedded.shadowworld.SWContent;
import com.electrifiedded.shadowworld.client.model.ModelCrossbow;
import com.electrifiedded.shadowworld.client.model.ModelStaff;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBook;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/** 1.7 IItemRenderers: ShadowItemRenderer (3D crossbows), ShadowStaffRenderer (staffs) and ShadowDiaryRenderer (open book). */
public final class SWItemRenderers {
    private SWItemRenderers() {}

    public static void register() {
        held(SWContent.CrossBow, "CrossBow", new Crossbow("crossbow"));
        held(SWContent.FireCrossBow, "FireCrossBow", new Crossbow("firecrossbow"));
        held(SWContent.PoisonousCrossBow, "PoisonousCrossBow", new Crossbow("poisonouscrossbow"));
        held(SWContent.Teleporter, "Teleporter", new Staff("teleporter", true));
        held(SWContent.Confuser, "Confuser", new Staff("confuser", true));
        held(SWContent.Controller, "Controller", new Staff("controller", true));
        held(SWContent.WoodStaff, "WoodStaff", new Staff("woodstaff", false));
        held(SWContent.Grower, "Grower", new Staff("grower", true));
        held(SWContent.Diary, "Diary", new Diary());
    }

    private static void held(Item item, String regName, IItemRenderer r) {
        ArchaicMinecraftForgeClient.registerHeldItemRenderer(item, r, "shadowworld:" + regName.toLowerCase(java.util.Locale.ROOT));
    }

    static void bind(ResourceLocation t) { Minecraft.getMinecraft().getTextureManager().bindTexture(t); }

    /** ShadowItemRenderer: only the first-person view is the 3D crossbow model. */
    static class Crossbow implements IItemRenderer {
        final ResourceLocation tex;
        final ModelCrossbow bow = new ModelCrossbow();
        Crossbow(String t) { tex = new ResourceLocation("shadowworld", "textures/entity/" + t + ".png"); }
        @Override public boolean handleRenderType(ItemStack s, ItemRenderType t) { return t == ItemRenderType.EQUIPPED_FIRST_PERSON; }
        @Override public boolean shouldUseRenderHelper(ItemRenderType t, ItemStack s, ItemRendererHelper h) { return true; }
        @Override
        public void renderItem(ItemRenderType type, ItemStack s, Object... data) {
            GL11.glPushMatrix();
            bind(tex);
            GL11.glScalef(2.3F, 2.3F, 2.3F);
            GL11.glTranslatef(1.25F, 0.3F, -0.2F);
            GL11.glRotatef(40.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(190.0F, 0.0F, 0.0F, 1.0F);
            bow.render(entity(data), 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
            GL11.glPopMatrix();
        }
    }

    /** ShadowStaffRenderer: 3D staff (x3) in hand and on the ground, portal particles for the magic ones. */
    static class Staff implements IItemRenderer {
        final ResourceLocation tex;
        final boolean particles;
        final ModelStaff model = new ModelStaff();
        final Random rand = new Random();
        Staff(String t, boolean particles) { tex = new ResourceLocation("shadowworld", "textures/entity/" + t + ".png"); this.particles = particles; }
        @Override public boolean handleRenderType(ItemStack s, ItemRenderType t) { return t != ItemRenderType.INVENTORY; }
        @Override public boolean shouldUseRenderHelper(ItemRenderType t, ItemStack s, ItemRendererHelper h) { return true; }
        @Override
        public void renderItem(ItemRenderType type, ItemStack s, Object... data) {
            Entity e = entity(data);
            GL11.glPushMatrix();
            bind(tex);
            GL11.glScalef(3.0F, 3.0F, 3.0F);
            if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
                GL11.glTranslatef(1.25F, 0.3F, 0.0F);
                particles(e, 0.0);
            } else if (type != ItemRenderType.ENTITY) {
                GL11.glTranslatef(0.05F, 0.8F, 0.25F);
                GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
                particles(e, 0.0);
            } else {
                GL11.glTranslatef(0.0F, 0.8F, 0.0F);
            }
            GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            model.render(e, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
            GL11.glPopMatrix();
        }
        private void particles(Entity e, double dy) {
            if (!particles || e == null || Minecraft.getMinecraft().isGamePaused()) return;
            e.world.spawnParticle(EnumParticleTypes.PORTAL, e.posX + (rand.nextDouble() - 0.5) * e.width, e.posY + dy,
                    e.posZ + (rand.nextDouble() - 0.5) * e.width, (rand.nextDouble() - 0.5) * 2.0, -rand.nextDouble(), (rand.nextDouble() - 0.5) * 2.0);
        }
    }

    /** ShadowDiaryRenderer: a 3D book in first person, opening/flipping from the GUI's "open"/"flip"/"flip2" tags. */
    static class Diary implements IItemRenderer {
        final ModelBook model = new ModelBook();
        final ResourceLocation tex = new ResourceLocation("shadowworld", "textures/models/diary.png");
        @Override public boolean handleRenderType(ItemStack s, ItemRenderType t) { return t == ItemRenderType.EQUIPPED_FIRST_PERSON; }
        @Override public boolean shouldUseRenderHelper(ItemRenderType t, ItemStack s, ItemRendererHelper h) { return true; }
        @Override
        public void renderItem(ItemRenderType type, ItemStack is, Object... data) {
            GL11.glPushMatrix();
            bind(tex);
            GL11.glRotatef(22.5F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(0.4F, -0.1F, 0.5F);
            GL11.glRotatef(-135.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(0.0F, 0.2F, 0.0F);
            GL11.glEnable(GL11.GL_CULL_FACE);
            NBTTagCompound c = is.getTagCompound();
            Entity e = entity(data);
            if (c != null && c.hasKey("flip") && c.hasKey("open") && c.hasKey("flip2")) {
                float f4 = 1.0F - c.getFloat("flip");
                float f5 = 1.0F - c.getFloat("flip2");
                if (c.getFloat("flip2") != 1.0F) f4 = 1.0F - c.getFloat("flip2");
                f4 = (f4 - MathHelper.fastFloor(f4)) * 0.025F;
                f5 = (f5 - MathHelper.fastFloor(f5)) * 0.025F - 5.0E-4F;
                float open = 100.75F - c.getInteger("open") / 80.0F;
                model.render(e, open, f4, f5, open, 0.0F, 0.0625F);
            } else model.render(e, 100.75F, 0.0F, 0.0F, 100.75F, 0.0F, 0.0625F);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glPopMatrix();
        }
    }

    static Entity entity(Object[] data) { return data != null && data.length > 1 && data[1] instanceof Entity e ? e : null; }
}
