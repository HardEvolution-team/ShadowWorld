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
        private int timeUntilNextEgg;
        public ShadowChicken(World w) { super(w); setSize(0.3F, 0.7F); timeUntilNextEgg = rand.nextInt(6000) + 6000; }
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
            if (!world.isRemote && --timeUntilNextEgg <= 0) {
                playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
                world.spawnEntity(new SWProjectiles.ExplodingItem(world, posX, posY, posZ, new ItemStack(Items.EGG)));
                timeUntilNextEgg = rand.nextInt(6000) + 6000;
            }
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

    /** 1.7 EntityFire/Earth/Water/AirGolem: built guardians that hunt monsters (never players, except a provoked Air Golem). */
    public static class Golem extends EntityGolem {
        public final int element; // 0 fire 1 earth 2 water 3 air
        public Golem(World w, int element) {
            super(w);
            this.element = element;
            setSize(1.4F, 2.9F);
            if (element == 0 || element == 3) isImmuneToFire = true;
        }
        @Override protected void initEntityAI() {
            tasks.addTask(1, new EntityAIAttackMelee(this, 1.0, true));
            tasks.addTask(2, new EntityAIMoveTowardsTarget(this, 0.9, 32.0F));
            tasks.addTask(4, new EntityAIMoveTowardsRestriction(this, 1.0));
            if (element == 2) tasks.addTask(5, new EntityAISwimming(this));
            tasks.addTask(6, new EntityAIWander(this, 0.6));
            tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
            tasks.addTask(8, new EntityAILookIdle(this));
            targetTasks.addTask(2, new EntityAIHurtByTarget(this, false));
            targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityLiving.class, 0, false, true, IMob.MOB_SELECTOR));
        }
        @Override protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            int idx = Math.max(0, Math.min(3, element));
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(new double[]{50.0, 50.0, 25.0, 75.0}[idx]);
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(element == 0 ? 0.35 : 0.25);
            getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(7.0);
        }
        @Override protected int decreaseAirSupply(int air) { return element == 1 ? super.decreaseAirSupply(air) : air; }
        @Override protected void collideWithEntity(Entity e) {
            if (e instanceof IMob && e instanceof EntityLivingBase l && getRNG().nextInt(20) == 0) setAttackTarget(l);
            super.collideWithEntity(e);
        }
        @Override public boolean canAttackClass(Class<? extends EntityLivingBase> c) {
            if (element == 3) return super.canAttackClass(c);
            return !ShadowWolfKing.class.isAssignableFrom(c) && !EntityPlayer.class.isAssignableFrom(c) && super.canAttackClass(c);
        }
        @Override public void onLivingUpdate() {
            super.onLivingUpdate();
            if (getHealth() < getMaxHealth()) addPotionEffect(new PotionEffect(MobEffects.REGENERATION, element == 1 ? 10 : 1, 1));
            if (!world.isRemote) return;
            switch (element) {
                case 0 -> {
                    if (rand.nextInt(24) == 0) world.playSound(posX + 0.5, posY + 0.5, posZ + 0.5, SoundEvents.BLOCK_FIRE_AMBIENT, getSoundCategory(), 1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F, false);
                    particles(EnumParticleTypes.SMOKE_LARGE, 2);
                    particles(EnumParticleTypes.FLAME, 2);
                }
                case 1 -> particles(EnumParticleTypes.TOWN_AURA, 6);
                case 2 -> { if (isInWater()) particles(EnumParticleTypes.WATER_BUBBLE, 2); }
                default -> particles(EnumParticleTypes.CLOUD, 2);
            }
        }
        private void particles(EnumParticleTypes t, int n) {
            for (int i = 0; i < n; i++)
                world.spawnParticle(t, posX + (rand.nextDouble() - 0.5) * width, posY + rand.nextDouble() * height, posZ + (rand.nextDouble() - 0.5) * width, 0.0, 0.0, 0.0);
        }
        @Override public boolean attackEntityAsMob(Entity e) {
            boolean hit = e.attackEntityFrom(DamageSource.causeMobDamage(this), 7 + rand.nextInt(15));
            switch (element) {
                case 0 -> {
                    if (hit) e.motionY += 0.4F;
                    if (!e.isImmuneToFire()) e.setFire(10);
                    return true;
                }
                case 1 -> {
                    if (hit) {
                        e.motionY += 0.1F;
                        world.createExplosion(this, e.posX, e.posY, e.posZ, 1.0F, false);
                    }
                    return hit;
                }
                case 2 -> {
                    if (hit) {
                        e.motionY -= 0.6F;
                        e.noClip = true;
                    }
                    return hit;
                }
                default -> {
                    if (hit) e.motionY += 0.4F;
                    if (!e.isImmuneToFire()) world.addWeatherEffect(new EntityLightningBolt(world, e.posX, e.posY, e.posZ, false));
                    return true;
                }
            }
        }
        @Override protected SoundEvent getAmbientSound() { return element == 0 ? SoundEvents.ENTITY_BLAZE_AMBIENT : null; }
        @Override protected SoundEvent getHurtSound(DamageSource d) { return element == 0 ? SoundEvents.ENTITY_BLAZE_HURT : element == 3 ? null : SoundEvents.ENTITY_IRONGOLEM_HURT; }
        @Override protected SoundEvent getDeathSound() { return element == 0 ? SoundEvents.ENTITY_BLAZE_DEATH : SoundEvents.ENTITY_IRONGOLEM_DEATH; }
        @Override protected void playStepSound(BlockPos p, net.minecraft.block.Block b) {}
        @Override protected boolean canDespawn() { return false; }
        @Override protected void dropFewItems(boolean hit, int looting) {
            Item stone = switch (element) { case 0 -> SWContent.FireStone; case 1 -> SWContent.EarthStone; case 2 -> SWContent.WaterStone; default -> SWContent.AirStone; };
            int k = rand.nextInt(2);
            for (int i = 0; i < k; i++) entityDropItem(new ItemStack(stone), 0.0F);
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

    /** 1.7 EntityShadowWolfKing: fireball every 60 ticks, super attack (howl, 10 angry shadow wolves + explosions), dragon-like death. */
    public static class ShadowWolfKing extends Boss {
        public int deathTicks, superattacktime, attackCounter;

        public ShadowWolfKing(World w) { super(w, BossInfo.Color.PURPLE); setSize(1.75F, 1.75F); isImmuneToFire = true; stepHeight = 2.0F; experienceValue = 0; }

        @Override protected void initEntityAI() {
            tasks.addTask(0, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
            tasks.addTask(1, new EntityAIAttackMelee(this, 1.0, true));
            tasks.addTask(2, new EntityAIWander(this, 1.0));
            tasks.addTask(3, new EntityAILookIdle(this));
            targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, false));
            targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        }

        @Override protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(200.0);
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.34);
            getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0);
            getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0);
        }

        @Override protected void jump() { super.jump(); motionY += 4.0; }
        @Override public void setInWeb() {}
        @Override public int getTotalArmorValue() { return 4; }
        @Override public void addPotionEffect(PotionEffect e) {}
        @Override public EnumCreatureAttribute getCreatureAttribute() { return EnumCreatureAttribute.UNDEFINED; }
        @Override protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_WOLF_GROWL; }
        @Override protected SoundEvent getHurtSound(DamageSource d) { return SoundEvents.ENTITY_WOLF_HURT; }
        @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_WOLF_DEATH; }
        @Override protected float getSoundVolume() { return 20.0F; }
        @Override protected float getSoundPitch() { return 0.5F; }
        @Override public boolean canAttackClass(Class<? extends EntityLivingBase> c) { return c != SWAngels.Angel.class && super.canAttackClass(c); }

        @Override protected void updateAITasks() {
            super.updateAITasks();
            if (ticksExisted % 100 == 0 && getHealth() < 100.0F) heal(5.0F);
            superattacktime++;
            EntityLivingBase t = getAttackTarget();
            if (t == null) t = world.getNearestAttackablePlayer(this, 16.0, 16.0);
            if (superattacktime > 525) {
                motionX = motionY = motionZ = 0.0;
                if (superattacktime > 550) {
                    playSound(SoundEvents.ENTITY_WOLF_HOWL, getSoundVolume(), 0.5F);
                    for (int i = 0; i < 10; i++) {
                        int x = (int) posX + rand.nextInt(50) - 25, y = (int) posY + 5, z = (int) posZ + rand.nextInt(50) - 25;
                        if (!world.isAirBlock(new BlockPos(x, y, z))) world.createExplosion(this, x, y, z, 5.0F, true);
                        ShadowWolf wolf = new ShadowWolf(world);
                        wolf.setAngry(true);
                        wolf.setPosition(x, y, z);
                        wolf.setAttackTarget(t);
                        world.spawnEntity(wolf);
                        if (world instanceof net.minecraft.world.WorldServer ws)
                            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x + 0.5, y - 0.5, z + 0.5, 60, 0.4, 1.2, 0.4, 0.0);
                    }
                    superattacktime = -100;
                }
            }
            if (t != null && t.getDistanceSq(this) < 64.0 * 64.0) {
                double d5 = t.posX - posX, d6 = t.getEntityBoundingBox().minY + t.height / 2.0F - (posY + height / 2.0F), d7 = t.posZ - posZ;
                renderYawOffset = rotationYaw = -((float) Math.atan2(d5, d7)) * 180.0F / (float) Math.PI;
                if (canEntityBeSeen(t) && deathTicks == 0) {
                    if (++attackCounter == 20) {
                        world.playEvent(null, 1018, new BlockPos(this), 0);
                        EntitySmallFireball f = new EntitySmallFireball(world, this, d5, d6, d7);
                        net.minecraft.util.math.Vec3d look = getLook(1.0F);
                        f.posX = posX + look.x;
                        f.posY = posY + height / 2.0F + 0.5;
                        f.posZ = posZ + look.z;
                        world.spawnEntity(f);
                        attackCounter = -40;
                    }
                } else if (attackCounter > 0) attackCounter--;
            } else if (attackCounter > 0) attackCounter--;
        }

        @Override protected void onDeathUpdate() {
            deathTicks++;
            superattacktime = -999;
            float f = (rand.nextFloat() - 0.5F) * 8.0F, f1 = (rand.nextFloat() - 0.5F) * 4.0F, f2 = (rand.nextFloat() - 0.5F) * 8.0F;
            if (deathTicks >= 180 && deathTicks <= 200) {
                world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, posX + f, posY + 2.0 + f1, posZ + f2, 0.0, 0.0, 0.0);
                if (!world.isRemote) world.createExplosion(this, posX, posY, posZ, 15.0F, false);
            }
            if (!world.isRemote) {
                if (deathTicks > 150 && deathTicks % 5 == 0) xp(1000);
                if (deathTicks == 1) world.playBroadcastSound(1028, new BlockPos(this), 0);
            }
            for (int i = 0; i < 3; i++) world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX + f, posY + 2.0 + f1, posZ + f2, 0.0, 0.0, 0.0);
            motionX = motionY = motionZ = 0.0;
            if (deathTicks == 200 && !world.isRemote) {
                xp(2000);
                world.spawnEntity(new net.minecraft.entity.item.EntityItem(world, posX, posY, posZ, new ItemStack(SWContent.GoldenSoul)));
                setDead();
            }
        }

        private void xp(int i) {
            while (i > 0) {
                int j = net.minecraft.entity.item.EntityXPOrb.getXPSplit(i);
                i -= j;
                world.spawnEntity(new net.minecraft.entity.item.EntityXPOrb(world, posX, posY, posZ, j));
            }
        }

        @Override public void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound c) { super.writeEntityToNBT(c); c.setInteger("SuperAttack", superattacktime); }
        @Override public void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound c) { super.readEntityFromNBT(c); superattacktime = c.getInteger("SuperAttack"); }
    }

    @Nullable static BlockPos none() { return null; }
}
