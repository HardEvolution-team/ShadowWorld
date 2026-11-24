package com.electrifiedded.shadowworld.entity;

import com.electrifiedded.shadowworld.SWContent;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/** Shadow World mobs, re-implemented on top of their 1.12 vanilla counterparts. */
public final class SWEntities {
    private SWEntities() {}

    static void drops(EntityLivingBase e, boolean hit, int looting, Object... items) {
        for (Object o : items) {
            Item it = o instanceof Item i ? i : null;
            if (it != null && e.getRNG().nextInt(2 + looting) > 0) e.entityDropItem(new ItemStack(it), 0F);
        }
        if (e.getRNG().nextInt(3) == 0) e.entityDropItem(new ItemStack(SWContent.Soul), 0F);
    }

    // ------------------------------------------------------------------ monsters

    public static class Shadow extends EntityEnderman {
        public Shadow(World w) { super(w); }
        @Override protected void initEntityAI() {
            super.initEntityAI();
            targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, Light.class, false));
        }
        @Override protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0);
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0);
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3);
        }
        @Override protected void dropFewItems(boolean hit, int looting) { drops(this, hit, looting, SWContent.Shadow); }
        @Override public boolean getCanSpawnHere() { return world.getDifficulty() != net.minecraft.world.EnumDifficulty.PEACEFUL; }
    }

    public static class SkeletonSpider extends EntitySpider {
        public SkeletonSpider(World w) { super(w); setSize(1.3F, 1.3F); }
        @Override protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.5);
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15.0);
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2);
        }
        @Override public boolean attackEntityAsMob(Entity e) {
            boolean b = super.attackEntityAsMob(e);
            if (b && e instanceof EntityLivingBase l) l.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 300, 0));
            return b;
        }
        @Override protected void dropFewItems(boolean hit, int looting) { drops(this, hit, looting, Items.BONE, Items.STRING); }
        @Override public boolean getCanSpawnHere() { return true; }
    }

    public static class WolfSpider extends EntitySpider {
        public WolfSpider(World w) { super(w); setSize(0.75F, 0.75F); }
        @Override protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10.0);
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0);
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4);
        }
        @Override protected void dropFewItems(boolean hit, int looting) { drops(this, hit, looting, Items.STRING); }
        @Override public boolean getCanSpawnHere() { return true; }
    }

    public static class ShadowCreeper extends EntityCreeper {
        public ShadowCreeper(World w) { super(w); }
        @Override protected void dropFewItems(boolean hit, int looting) { drops(this, hit, looting, Items.GUNPOWDER); }
        @Override public boolean getCanSpawnHere() { return true; }
    }

    /** Creeper-like wolf that explodes next to players. */
    public static class ExplodingWolf extends EntityCreeper {
        public ExplodingWolf(World w) { super(w); setSize(0.6F, 0.85F); }
        @Override protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3);
        }
        @Override protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_WOLF_GROWL; }
        @Override protected SoundEvent getHurtSound(DamageSource d) { return SoundEvents.ENTITY_WOLF_HURT; }
        @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_WOLF_DEATH; }
        @Override public boolean getCanSpawnHere() { return true; }
    }

    public static class ShadowChicken extends EntityMob {
        public ShadowChicken(World w) { super(w); setSize(0.3F, 0.7F); }
        @Override protected void initEntityAI() {
            tasks.addTask(0, new EntityAISwimming(this));
            tasks.addTask(2, new EntityAIAttackMelee(this, 1.2, false));
            tasks.addTask(5, new EntityAIWander(this, 1.0));
            tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
            tasks.addTask(7, new EntityAILookIdle(this));
            targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        }
        @Override protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0);
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16.0);
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25);
        }
        @Override public void fall(float d, float m) {}
        @Override public void onLivingUpdate() {
            super.onLivingUpdate();
            if (!onGround && motionY < 0.0) motionY *= 0.6;
        }
        @Override protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_CHICKEN_AMBIENT; }
        @Override protected SoundEvent getHurtSound(DamageSource d) { return SoundEvents.ENTITY_CHICKEN_HURT; }
        @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_CHICKEN_DEATH; }
        @Override protected void dropFewItems(boolean hit, int looting) { drops(this, hit, looting, SWContent.BlackFeather, isBurning() ? Items.COOKED_CHICKEN : Items.CHICKEN); }
    }

    public static class ShadowSlime extends EntitySlime {
        public ShadowSlime(World w) { super(w); }
        @Override protected EntitySlime createInstance() { return new ShadowSlime(world); }
        @Override public boolean getCanSpawnHere() { return true; }
        @Override protected Item getDropItem() { return getSlimeSize() == 1 ? SWContent.GlowingInkSac : null; }
        @Override protected EnumParticleTypes getParticleType() { return EnumParticleTypes.SPELL_MOB; }
    }

    public static class CookieSlime extends EntitySlime {
        public CookieSlime(World w) { super(w); }
        @Override protected EntitySlime createInstance() { return new CookieSlime(world); }
        @Override public boolean getCanSpawnHere() { return true; }
        @Override protected Item getDropItem() { return Items.COOKIE; }
        @Override protected boolean spawnCustomParticles() { return true; }
    }

    // ------------------------------------------------------------------ animals

    public static class ShadowPig extends EntityPig {
        public ShadowPig(World w) { super(w); }
        @Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0); }
        @Override public ShadowPig createChild(EntityAgeable a) { return new ShadowPig(world); }
        @Override protected void dropFewItems(boolean hit, int looting) {
            drops(this, hit, looting, SWContent.Shadow);
            if (getSaddled()) dropItem(Items.SADDLE, 1);
        }
        @Override public boolean getCanSpawnHere() { return true; }
    }

    public static class CookiePig extends EntityPig {
        public CookiePig(World w) { super(w); }
        @Override protected void initEntityAI() {
            super.initEntityAI();
            tasks.addTask(4, new EntityAITempt(this, 1.2, SWContent.WhiteChocolate, false));
        }
        @Override public boolean isBreedingItem(ItemStack s) { return s.getItem() == SWContent.WhiteChocolate; }
        @Override public CookiePig createChild(EntityAgeable a) { return new CookiePig(world); }
        @Override protected void dropFewItems(boolean hit, int looting) { for (int i = 0; i < 1 + rand.nextInt(3 + looting); i++) dropItem(Items.COOKIE, 1); }
        @Override public boolean getCanSpawnHere() { return true; }
    }

    /** Peaceful spider. */
    public static class PassiveSpider extends EntityAnimal {
        public PassiveSpider(World w) { super(w); setSize(1.4F, 0.9F); }
        @Override protected void initEntityAI() {
            tasks.addTask(0, new EntityAISwimming(this));
            tasks.addTask(1, new EntityAIPanic(this, 1.25));
            tasks.addTask(2, new EntityAIMate(this, 1.0));
            tasks.addTask(5, new EntityAIWander(this, 1.0));
            tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
            tasks.addTask(7, new EntityAILookIdle(this));
        }
        @Override protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12.0);
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3);
        }
        @Override public boolean isBreedingItem(ItemStack s) { return s.getItem() == Items.SPIDER_EYE; }
        @Override public PassiveSpider createChild(EntityAgeable a) { return new PassiveSpider(world); }
        @Override protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_SPIDER_AMBIENT; }
        @Override protected SoundEvent getHurtSound(DamageSource d) { return SoundEvents.ENTITY_SPIDER_HURT; }
        @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_SPIDER_DEATH; }
        @Override protected Item getDropItem() { return Items.STRING; }
        @Override public boolean getCanSpawnHere() { return true; }
    }

    /** Shadow wolf: tamed with souls, golden souls always tame. */
    public static class ShadowWolf extends EntityWolf {
        public ShadowWolf(World w) { super(w); setSize(1.0F, 1.2F); }
        @Override protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4);
        }
        @Override public void setTamed(boolean t) {
            super.setTamed(t);
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(t ? 40.0 : 20.0);
        }
        @Override public boolean processInteract(EntityPlayer p, EnumHand h) {
            ItemStack s = p.getHeldItem(h);
            boolean golden = s.getItem() == SWContent.GoldenSoul;
            if (!isTamed() && !isAngry() && (s.getItem() == SWContent.Soul || golden)) {
                if (!p.capabilities.isCreativeMode && !golden) s.shrink(1);
                if (!world.isRemote) {
                    if (!golden && rand.nextInt(3) != 0) { playTameEffect(false); world.setEntityState(this, (byte) 6); }
                    else {
                        setTamedBy(p);
                        navigator.clearPath();
                        setAttackTarget(null);
                        aiSit.setSitting(true);
                        setHealth(20.0F);
                        playTameEffect(true);
                        world.setEntityState(this, (byte) 7);
                    }
                }
                return true;
            }
            if (!isTamed() && s.getItem() == Items.BONE) return false;
            return super.processInteract(p, h);
        }
        @Override public boolean canMateWith(EntityAnimal o) { return o instanceof ShadowWolf && super.canMateWith(o); }
        @Override public ShadowWolf createChild(EntityAgeable a) {
            ShadowWolf w = new ShadowWolf(world);
            if (getOwnerId() != null) { w.setOwnerId(getOwnerId()); w.setTamed(true); }
            return w;
        }
        @Override protected float getSoundPitch() { return super.getSoundPitch() * 0.5F; }
        @Override public boolean getCanSpawnHere() { return true; }
    }

    public static class JellyFish extends EntitySquid {
        public JellyFish(World w) { super(w); }
        @Override public boolean attackEntityAsMob(Entity e) { return e.attackEntityFrom(DamageSource.causeMobDamage(this), 2F); }
        @Override public void onCollideWithPlayer(EntityPlayer p) { if (!world.isRemote && rand.nextInt(20) == 0) p.addPotionEffect(new PotionEffect(MobEffects.POISON, 60, 0)); }
        @Override protected Item getDropItem() { return SWContent.GlowingInkSac; }
        @Override protected void dropFewItems(boolean hit, int looting) { dropItem(SWContent.GlowingInkSac, 1 + rand.nextInt(2)); }
        @Override public boolean getCanSpawnHere() { return posY > 30 && posY < 90; }
    }

    /** Glowing flying light orb (bat movement); hunted by shadows. */
    public static class Light extends EntityBat {
        public Light(World w) { super(w); setSize(0.5F, 0.5F); }
        @Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(2.0); }
        @Override public boolean getIsBatHanging() { return false; }
        @Override public SoundEvent getAmbientSound() { return null; }
        @Override protected void dropFewItems(boolean hit, int looting) { dropItem(Items.GLOWSTONE_DUST, 1); if (rand.nextInt(3) == 0) dropItem(SWContent.Soul, 1); }
        @Override public int getBrightnessForRender() { return 0xF000F0; }
        @Override public float getBrightness() { return 1.0F; }
        @Override public boolean getCanSpawnHere() { return true; }
    }

    public static class Dragonfly extends EntityBat {
        public Dragonfly(World w) { super(w); setSize(0.5F, 0.4F); }
        @Override public boolean getIsBatHanging() { return false; }
        @Override public SoundEvent getAmbientSound() { return null; }
        @Override public boolean getCanSpawnHere() { return true; }
    }

    /** Elemental golems guarding the temples; drop their element stone. */
    public static class Golem extends EntityGolem {
        public final int element; // 0 fire 1 earth 2 water 3 air
        public Golem(World w, int element) { super(w); this.element = element; setSize(1.4F, 2.9F); if (element == 0) isImmuneToFire = true; }
        @Override protected void initEntityAI() {
            tasks.addTask(1, new EntityAIAttackMelee(this, 1.0, true));
            tasks.addTask(2, new EntityAIMoveTowardsTarget(this, 0.9, 32.0F));
            tasks.addTask(6, new EntityAIWander(this, 0.6));
            tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
            tasks.addTask(8, new EntityAILookIdle(this));
            targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        }
        @Override protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50.0);
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35);
            getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(7.0);
        }
        @Override public boolean attackEntityAsMob(Entity e) {
            boolean b = e.attackEntityFrom(DamageSource.causeMobDamage(this), 7 + rand.nextInt(15));
            if (b) {
                e.motionY += 0.4;
                if (element == 0) e.setFire(10);
                if (element == 3 && e instanceof EntityLivingBase l) l.addPotionEffect(new PotionEffect(MobEffects.LEVITATION, 60, 1));
                if (element == 2 && e instanceof EntityLivingBase l) l.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 2));
            }
            return b;
        }
        @Override protected boolean canDespawn() { return false; }
        @Override protected void dropFewItems(boolean hit, int looting) {
            Item stone = switch (element) { case 0 -> SWContent.FireStone; case 1 -> SWContent.EarthStone; case 2 -> SWContent.WaterStone; default -> SWContent.AirStone; };
            dropItem(stone, 1);
        }
        @Override public void fall(float d, float m) { if (element != 3) super.fall(d, m); }
    }

    public static class FireGolem extends Golem { public FireGolem(World w) { super(w, 0); } }
    public static class EarthGolem extends Golem { public EarthGolem(World w) { super(w, 1); } }
    public static class WaterGolem extends Golem { public WaterGolem(World w) { super(w, 2); } @Override public boolean canBreatheUnderwater() { return true; } }
    public static class AirGolem extends Golem { public AirGolem(World w) { super(w, 3); } }

    // ------------------------------------------------------------------ bosses

    public abstract static class Boss extends EntityMob {
        protected final BossInfoServer bossInfo;
        Boss(World w, BossInfo.Color c) { super(w); bossInfo = new BossInfoServer(getDisplayName(), c, BossInfo.Overlay.PROGRESS); experienceValue = 100; }
        @Override protected void updateAITasks() { super.updateAITasks(); bossInfo.setPercent(getHealth() / getMaxHealth()); }
        @Override public void addTrackingPlayer(net.minecraft.entity.player.EntityPlayerMP p) { super.addTrackingPlayer(p); bossInfo.addPlayer(p); }
        @Override public void removeTrackingPlayer(net.minecraft.entity.player.EntityPlayerMP p) { super.removeTrackingPlayer(p); bossInfo.removePlayer(p); }
        @Override public boolean isNonBoss() { return false; }
        @Override protected boolean canDespawn() { return false; }
        @Override protected void initEntityAI() {
            tasks.addTask(0, new EntityAISwimming(this));
            tasks.addTask(2, new EntityAIAttackMelee(this, 1.0, true));
            tasks.addTask(5, new EntityAIWander(this, 0.8));
            tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
            tasks.addTask(7, new EntityAILookIdle(this));
            targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        }
    }

    /** Desert boss: shoots fireballs and calls shadow wolves. Drops a golden soul. */
    public static class ShadowWolfKing extends Boss {
        public ShadowWolfKing(World w) { super(w, BossInfo.Color.PURPLE); setSize(1.75F, 1.75F); isImmuneToFire = true; }
        @Override protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0);
            getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0);
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(200.0);
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.34);
        }
        @Override public void onLivingUpdate() {
            super.onLivingUpdate();
            EntityLivingBase t = getAttackTarget();
            if (world.isRemote || t == null) return;
            if (ticksExisted % 40 == 0 && getDistanceSq(t) < 400) {
                double dx = t.posX - posX, dy = t.getEntityBoundingBox().minY + t.height / 2 - (posY + height / 2), dz = t.posZ - posZ;
                EntitySmallFireball f = new EntitySmallFireball(world, this, dx, dy, dz);
                f.posY = posY + height / 2 + 0.5;
                world.spawnEntity(f);
            }
            if (ticksExisted % 300 == 0 && rand.nextInt(2) == 0) {
                ShadowWolf w = new ShadowWolf(world);
                w.setLocationAndAngles(posX + rand.nextInt(5) - 2, posY, posZ + rand.nextInt(5) - 2, 0, 0);
                w.setAngry(true);
                w.setAttackTarget(t);
                world.spawnEntity(w);
            }
        }
        @Override protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_WOLF_GROWL; }
        @Override protected SoundEvent getHurtSound(DamageSource d) { return SoundEvents.ENTITY_WOLF_HURT; }
        @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_WOLF_DEATH; }
        @Override protected float getSoundPitch() { return 0.5F; }
        @Override protected void dropFewItems(boolean hit, int looting) { dropItem(SWContent.GoldenSoul, 1); }
    }

    @Nullable static BlockPos none() { return null; }
}
