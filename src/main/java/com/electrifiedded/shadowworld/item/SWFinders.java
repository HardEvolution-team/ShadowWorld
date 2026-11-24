package com.electrifiedded.shadowworld.item;

import com.electrifiedded.shadowworld.SWContent;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/** Dungeon Finder (thrown "ender eye" towards the nearest Shadow Village) and the Shadow Compass of the 1.7 mod. */
public final class SWFinders {
    private SWFinders() {}

    /** Item.DungeonFinder. */
    public static class DungeonFinderItem extends Item {
        @Override @SideOnly(Side.CLIENT) public boolean hasEffect(ItemStack s) { return true; }
        @Override public EnumRarity getRarity(ItemStack s) { return EnumRarity.RARE; }

        @Override
        @SideOnly(Side.CLIENT)
        public void addInformation(ItemStack s, @Nullable World w, List<String> tip, ITooltipFlag f) {
            tip.add(TextFormatting.ITALIC + "Used to find Shadow Villages");
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World w, EntityPlayer p, EnumHand hand) {
            ItemStack s = p.getHeldItem(hand);
            if (!w.isRemote) {
                BlockPos target = com.electrifiedded.shadowworld.world.SWVillage.findClosest(w, new BlockPos(p));
                if (target != null) {
                    EntityDungeonFinder eye = new EntityDungeonFinder(w, p.posX, p.posY + p.getEyeHeight(), p.posZ);
                    eye.moveTowards(target.getX(), target.getY(), target.getZ());
                    w.spawnEntity(eye);
                    w.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
                    w.playEvent(null, 1003, new BlockPos(p), 0);
                    if (!p.capabilities.isCreativeMode) s.shrink(1);
                }
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, s);
        }
    }

    /** EntityDungeonFinder: flies 12 blocks towards the target, rises, then drops (80 %) or shatters. */
    public static class EntityDungeonFinder extends Entity {
        private double targetX, targetY, targetZ;
        private int despawnTimer;
        private boolean shatterOrDrop;

        public EntityDungeonFinder(World w) {
            super(w);
            setSize(0.25F, 0.25F);
        }

        public EntityDungeonFinder(World w, double x, double y, double z) {
            this(w);
            setPosition(x, y, z);
        }

        @Override protected void entityInit() {}

        @Override
        @SideOnly(Side.CLIENT)
        public boolean isInRangeToRenderDist(double d) {
            double l = getEntityBoundingBox().getAverageEdgeLength() * 4.0 * 64.0;
            return d < l * l;
        }

        public void moveTowards(double x, int y, double z) {
            double dx = x - posX, dz = z - posZ;
            float f = MathHelper.sqrt(dx * dx + dz * dz);
            if (f > 12.0F) {
                targetX = posX + dx / f * 12.0;
                targetZ = posZ + dz / f * 12.0;
                targetY = posY + 8.0;
            } else {
                targetX = x;
                targetY = y;
                targetZ = z;
            }
            despawnTimer = 0;
            shatterOrDrop = rand.nextInt(5) > 0;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void setVelocity(double x, double y, double z) {
            motionX = x;
            motionY = y;
            motionZ = z;
            if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F) {
                float f = MathHelper.sqrt(x * x + z * z);
                prevRotationYaw = rotationYaw = (float) (Math.atan2(x, z) * 180.0 / Math.PI);
                prevRotationPitch = rotationPitch = (float) (Math.atan2(y, f) * 180.0 / Math.PI);
            }
        }

        @Override
        public void onUpdate() {
            lastTickPosX = posX;
            lastTickPosY = posY;
            lastTickPosZ = posZ;
            super.onUpdate();
            posX += motionX;
            posY += motionY;
            posZ += motionZ;
            float f = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
            rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0 / Math.PI);
            rotationPitch = (float) (Math.atan2(motionY, f) * 180.0 / Math.PI);
            while (rotationPitch - prevRotationPitch < -180.0F) prevRotationPitch -= 360.0F;
            while (rotationPitch - prevRotationPitch >= 180.0F) prevRotationPitch += 360.0F;
            while (rotationYaw - prevRotationYaw < -180.0F) prevRotationYaw -= 360.0F;
            while (rotationYaw - prevRotationYaw >= 180.0F) prevRotationYaw += 360.0F;
            rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
            rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
            if (!world.isRemote) {
                double dx = targetX - posX, dz = targetZ - posZ;
                float dist = (float) Math.sqrt(dx * dx + dz * dz);
                float ang = (float) Math.atan2(dz, dx);
                double sp = f + (dist - f) * 0.0025;
                if (dist < 1.0F) {
                    sp *= 0.8;
                    motionY *= 0.8;
                }
                motionX = Math.cos(ang) * sp;
                motionZ = Math.sin(ang) * sp;
                motionY += ((posY < targetY ? 1.0 : -1.0) - motionY) * 0.015F;
            }
            float t = 0.25F;
            if (isInWater()) {
                for (int i = 0; i < 4; i++)
                    world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX - motionX * t, posY - motionY * t, posZ - motionZ * t, motionX, motionY, motionZ);
            } else {
                world.spawnParticle(EnumParticleTypes.PORTAL, posX - motionX * t + rand.nextDouble() * 0.6 - 0.3, posY - motionY * t - 0.5,
                        posZ - motionZ * t + rand.nextDouble() * 0.6 - 0.3, motionX, motionY, motionZ);
            }
            if (!world.isRemote) {
                setPosition(posX, posY, posZ);
                if (++despawnTimer > 80) {
                    setDead();
                    if (shatterOrDrop) {
                        world.spawnEntity(new EntityItem(world, posX, posY, posZ, new ItemStack(SWContent.DungeonFinder)));
                        world.playEvent(2003, new BlockPos(this), 0);
                    }
                }
            }
        }

        @Override protected void writeEntityToNBT(NBTTagCompound c) {}
        @Override protected void readEntityFromNBT(NBTTagCompound c) {}
        @Override public float getBrightness() { return 1.0F; }
        @Override @SideOnly(Side.CLIENT) public int getBrightnessForRender() { return 15728880; }
        @Override public boolean canBeAttackedWithItem() { return false; }
    }

    /** Item.ShadowCompass: right click a block to store its X/Z; the needle (AngleSprite) points there. */
    public static class ShadowCompassItem extends Item {
        @Override @SideOnly(Side.CLIENT) public boolean hasEffect(ItemStack s) { return true; }
        @Override public boolean getShareTag() { return true; }

        @Override
        @SideOnly(Side.CLIENT)
        public void addInformation(ItemStack s, @Nullable World w, List<String> tip, ITooltipFlag f) {
            tip.add(TextFormatting.ITALIC + "A special Compass");
            if (s.hasTagCompound()) {
                tip.add(TextFormatting.ITALIC + "X: " + s.getTagCompound().getDouble("posX"));
                tip.add(TextFormatting.ITALIC + "Z: " + s.getTagCompound().getDouble("posZ"));
            }
        }

        @Override
        public EnumActionResult onItemUseFirst(EntityPlayer p, World w, BlockPos pos, EnumFacing side, float hx, float hy, float hz, EnumHand hand) {
            ItemStack s = p.getHeldItem(hand);
            w.playSound(p, p.posX, p.posY, p.posZ, SoundEvents.BLOCK_NOTE_HARP, SoundCategory.PLAYERS, 1.2F, 2.0F);
            if (!s.hasTagCompound()) s.setTagCompound(new NBTTagCompound());
            s.getTagCompound().setDouble("posX", pos.getX());
            s.getTagCompound().setDouble("posZ", pos.getZ());
            return EnumActionResult.SUCCESS;
        }
    }
}
