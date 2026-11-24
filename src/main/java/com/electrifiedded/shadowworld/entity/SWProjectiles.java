package com.electrifiedded.shadowworld.entity;

import com.electrifiedded.shadowworld.SWContent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/** 1.7 EntityCrossBowArrow and EntityExplodingItem. */
public final class SWProjectiles {
    private SWProjectiles() {}

    /** Crossbow bolt: an arrow with its own texture and item, optionally poisonous (Poison V for 50 s, as in 1.7). */
    public static class CrossBowArrow extends EntityArrow {
        public boolean ispoisonous;

        public CrossBowArrow(World w) { super(w); setDamage(2.0); }

        /** EntityCrossBowArrow(world, shooter, 2.5F): fixed speed 2.5 * 1.5, inaccuracy 1. */
        public CrossBowArrow(World w, EntityLivingBase shooter, float speed) {
            super(w, shooter);
            setDamage(2.0);
            if (shooter instanceof EntityPlayer) pickupStatus = PickupStatus.ALLOWED;
            shoot(shooter, shooter.rotationPitch, shooter.rotationYaw, 0.0F, speed * 1.5F, 1.0F);
        }

        @Override
        protected void arrowHit(EntityLivingBase e) {
            super.arrowHit(e);
            if (ispoisonous) e.addPotionEffect(new PotionEffect(MobEffects.POISON, 1000, 4));
        }

        @Override protected ItemStack getArrowStack() { return new ItemStack(SWContent.CrossBowArrow); }
        @Override public void writeEntityToNBT(NBTTagCompound c) { super.writeEntityToNBT(c); c.setBoolean("poisonous", ispoisonous); }
        @Override public void readEntityFromNBT(NBTTagCompound c) { super.readEntityFromNBT(c); ispoisonous = c.getBoolean("poisonous"); }
    }

    /** Item that explodes (power 2) the moment a player picks it up completely (the Shadow Chicken's eggs). */
    public static class ExplodingItem extends EntityItem {
        public ExplodingItem(World w) { super(w); }
        public ExplodingItem(World w, double x, double y, double z, ItemStack s) { super(w, x, y, z, s); }

        @Override
        public void onCollideWithPlayer(EntityPlayer p) {
            if (world.isRemote) return;
            boolean alive = !isDead;
            super.onCollideWithPlayer(p);
            if (alive && isDead) world.createExplosion(this, posX, posY, posZ, 2.0F, true);
        }
    }
}
