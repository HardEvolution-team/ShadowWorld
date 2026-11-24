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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.MathHelper;

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

    static <T> DataParameter<T> key(Class<? extends Entity> c, net.minecraft.network.datasync.DataSerializer<T> s) { return EntityDataManager.createKey(c, s); }

    static void shadowParticles(EntityLivingBase e, int n) {
        for (int i = 0; i < n; i++) {
            float f = e.getRNG().nextFloat() * (float) Math.PI * 2.0F;
            float f1 = e.getRNG().nextFloat() * 0.5F + 0.5F;
            float f2 = MathHelper.sin(f) * i * 0.5F * f1;
            float f3 = MathHelper.cos(f) * i * 0.5F * f1;
            com.electrifiedded.shadowworld.ShadowWorld.proxy.spawnParticle("Shadow", e.posX + f2, e.getEntityBoundingBox().minY, e.posZ + f3, 0, 0, 0);
        }
    }

    /** 1.7 EntityShadow: an Enderman (old AI: stare/teleport/carry) drawn as a dark zombie with glowing eyes. */
    public static class Shadow extends EntityEnderman {
        public Shadow(World w) { super(w); experienceValue = 20; }
        @Override protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0);
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3);
            getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0);
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0);
        }
        @Override public EnumCreatureAttribute getCreatureAttribute() { return EnumCreatureAttribute.UNDEAD; }
        @Nullable @Override public IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance d, @Nullable IEntityLivingData data) { return data; }
        @Override protected Item getDropItem() { return SWContent.Shadow; }
        @Override protected void dropFewItems(boolean hit, int looting) {
            if (hit) { entityDropItem(new ItemStack(SWContent.Soul), 0F); entityDropItem(new ItemStack(SWContent.Shadow), 0F); }
        }
        @Override protected SoundEvent getAmbientSound() { return null; }
        @Override protected SoundEvent getHurtSound(DamageSource d) { return SoundEvents.ENTITY_ZOMBIE_HURT; }
        @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_ZOMBIE_DEATH; }
        @Override protected void playStepSound(BlockPos p, net.minecraft.block.Block b) {}
        @Override protected float getSoundPitch() { return super.getSoundPitch() * -5.0F; }
        @Override public void onLivingUpdate() {
            if (world.isRemote) for (int i = 0; i < 2; i++) {
                world.spawnParticle(EnumParticleTypes.PORTAL, posX + (rand.nextDouble() - 0.5) * width, posY + rand.nextDouble() * height - 0.25,
                        posZ + (rand.nextDouble() - 0.5) * width, (rand.nextDouble() - 0.5) * 2.0, -rand.nextDouble(), (rand.nextDouble() - 0.5) * 2.0);
                float f = rand.nextFloat() * (float) Math.PI * 2.0F, f1 = rand.nextFloat() * 0.5F + 0.5F;
                com.electrifiedded.shadowworld.ShadowWorld.proxy.spawnParticle("Shadow", posX + MathHelper.sin(f) * i * 0.5F * f1, getEntityBoundingBox().minY, posZ + MathHelper.cos(f) * i * 0.5F * f1, 0, 0, 0);
            }
            super.onLivingUpdate();
        }
        @Override public boolean getCanSpawnHere() { return world.getDifficulty() != net.minecraft.world.EnumDifficulty.PEACEFUL; }
    }

    public static class SkeletonSpider extends EntitySpider {
        public SkeletonSpider(World w) { super(w); setSize(1.3F, 1.3F); experienceValue = 10; }
        @Override protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0);
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15.0);
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2);
            getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.5);
        }
        @Override public boolean attackEntityAsMob(Entity e) {
            if (!super.attackEntityAsMob(e)) return false;
            if (e instanceof EntityLivingBase l) l.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 300, 0));
            return true;
        }
        @Nullable @Override public IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance d, @Nullable IEntityLivingData data) { return data; }
        @Override protected Item getDropItem() { return Items.BONE; }
        @Override protected void dropFewItems(boolean hit, int looting) {
            super.dropFewItems(hit, looting);
            if (hit) { entityDropItem(new ItemStack(SWContent.Soul), 0F); entityDropItem(new ItemStack(Items.BONE), 0F); }
            if (hit && (rand.nextInt(3) == 0 || rand.nextInt(1 + looting) > 0)) entityDropItem(new ItemStack(Items.STRING, 2), 0F);
        }
        @Override protected net.minecraft.util.ResourceLocation getLootTable() { return null; }
        @Override protected SoundEvent getHurtSound(DamageSource d) { return SoundEvents.ENTITY_SKELETON_HURT; }
        @Override protected void playStepSound(BlockPos p, net.minecraft.block.Block b) { playSound(SoundEvents.ENTITY_SKELETON_STEP, 0.15F, -1.0F); }
        @Override protected float getSoundPitch() { return super.getSoundPitch() * -1.0F; }
        @Override public boolean getCanSpawnHere() { return true; }
    }

    public static class WolfSpider extends EntitySpider {
        public WolfSpider(World w) { super(w); experienceValue = 100; setSize(0.75F, 0.75F); ((net.minecraft.pathfinding.PathNavigateGround) getNavigator()).setBreakDoors(true); }
        @Override protected void initEntityAI() {
            tasks.addTask(0, new EntityAISwimming(this));
            tasks.addTask(1, new EntityAIBreakDoor(this));
            tasks.addTask(4, new EntityAIAttackMelee(this, 1.0, false));
            tasks.addTask(4, new EntityAIMoveTowardsRestriction(this, 1.0));
            tasks.addTask(5, new EntityAIMoveThroughVillage(this, 0.5, false));
            tasks.addTask(6, new EntityAIWander(this, 0.5));
            tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
            tasks.addTask(7, new EntityAILookIdle(this));
            targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityVillager.class, false));
        }
        @Override protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0);
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0);
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4);
            getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10.0);
        }
        @Nullable @Override public IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance d, @Nullable IEntityLivingData data) { return data; }
        @Override protected net.minecraft.util.ResourceLocation getLootTable() { return null; }
        @Override protected Item getDropItem() { return Items.STRING; }
        @Override protected void dropFewItems(boolean hit, int looting) {
            super.dropFewItems(hit, looting);
            if (hit) entityDropItem(new ItemStack(SWContent.Soul), 0F);
        }
        @Override protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_WOLF_HOWL; }
        @Override protected float getSoundVolume() { return 1.4F; }
        @Override protected float getSoundPitch() { return super.getSoundPitch() - rand.nextFloat(); }
        @Override public boolean getCanSpawnHere() { return true; }
    }

    /** 1.7 EntityShadowCreeper / EntityExplodingWolf: own fuse logic (30/44 swell range, flint ignition, no melee damage). */
    public abstract static class FuseMob extends EntityMob {
        private static final DataParameter<Integer> STATE = key(FuseMob.class, DataSerializers.VARINT);
        private static final DataParameter<Boolean> IGNITED = key(FuseMob.class, DataSerializers.BOOLEAN);
        private int lastActiveTime, timeSinceIgnited, fuseTime = 20;
        protected int explosionRadius;
        protected FuseMob(World w, int radius) { super(w); explosionRadius = radius; }
        @Override protected void initEntityAI() {
            tasks.addTask(1, new EntityAISwimming(this));
            tasks.addTask(2, new Swell(this));
            tasks.addTask(4, new EntityAIAttackMelee(this, 1.0, false));
            tasks.addTask(5, new EntityAIWander(this, 0.8));
            tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
            tasks.addTask(6, new EntityAILookIdle(this));
            targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
            targetTasks.addTask(2, new EntityAIHurtByTarget(this, false));
        }
        @Override protected void entityInit() { super.entityInit(); dataManager.register(STATE, -1); dataManager.register(IGNITED, false); }
        public int getFuseState() { return dataManager.get(STATE); }
        public void setFuseState(int s) { dataManager.set(STATE, s); }
        public boolean isIgnited() { return dataManager.get(IGNITED); }
        public void ignite() { dataManager.set(IGNITED, true); }
        public float getFlashIntensity(float pt) { return (lastActiveTime + (timeSinceIgnited - lastActiveTime) * pt) / (fuseTime - 2); }
        @Override public int getMaxFallHeight() { return getAttackTarget() == null ? 3 : 3 + (int) (getHealth() - 1.0F); }
        @Override public void fall(float d, float m) {
            super.fall(d, m);
            timeSinceIgnited = (int) (timeSinceIgnited + d * 1.5F);
            if (timeSinceIgnited > fuseTime - 5) timeSinceIgnited = fuseTime - 5;
        }
        @Override public void writeEntityToNBT(NBTTagCompound t) {
            super.writeEntityToNBT(t);
            t.setShort("Fuse", (short) fuseTime); t.setByte("ExplosionRadius", (byte) explosionRadius); t.setBoolean("ignited", isIgnited());
        }
        @Override public void readEntityFromNBT(NBTTagCompound t) {
            super.readEntityFromNBT(t);
            if (t.hasKey("Fuse", 99)) fuseTime = t.getShort("Fuse");
            if (t.hasKey("ExplosionRadius", 99)) explosionRadius = t.getByte("ExplosionRadius");
            if (t.getBoolean("ignited")) ignite();
        }
        protected void onPrimed() { playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 10.0F, 0.5F); }
        protected void whileFused() {}
        protected void explode() { world.createExplosion(this, posX, posY, posZ, explosionRadius, net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world, this)); }
        protected int fuse() { return timeSinceIgnited; }
        @Override public void onUpdate() {
            if (isEntityAlive()) {
                lastActiveTime = timeSinceIgnited;
                if (isIgnited()) setFuseState(1);
                int i = getFuseState();
                if (i > 0 && timeSinceIgnited == 0) onPrimed();
                timeSinceIgnited += i;
                if (timeSinceIgnited < 0) timeSinceIgnited = 0;
                else if (timeSinceIgnited != 0) whileFused();
                if (timeSinceIgnited >= fuseTime) {
                    timeSinceIgnited = fuseTime;
                    if (!world.isRemote) { explode(); setDead(); }
                }
            }
            super.onUpdate();
        }
        @Override public boolean attackEntityAsMob(Entity e) { return true; }
        @Override protected Item getDropItem() { return Items.GUNPOWDER; }
        @Override protected net.minecraft.util.ResourceLocation getLootTable() { return null; }
        @Override protected boolean processInteract(EntityPlayer p, EnumHand h) {
            ItemStack s = p.getHeldItem(h);
            if (s.getItem() == Items.FLINT_AND_STEEL) {
                world.playSound(p, posX, posY, posZ, SoundEvents.ITEM_FLINTANDSTEEL_USE, getSoundCategory(), 1.0F, rand.nextFloat() * 0.4F + 0.8F);
                p.swingArm(h);
                if (!world.isRemote) { ignite(); s.damageItem(1, p); return true; }
            }
            return super.processInteract(p, h);
        }
        @Override public boolean getCanSpawnHere() { return true; }

        static class Swell extends EntityAIBase {
            final FuseMob m; EntityLivingBase target;
            Swell(FuseMob m) { this.m = m; setMutexBits(1); }
            @Override public boolean shouldExecute() {
                EntityLivingBase t = m.getAttackTarget();
                return m.getFuseState() > 0 || t != null && m.getDistanceSq(t) < 30.0;
            }
            @Override public void startExecuting() { m.getNavigator().clearPath(); target = m.getAttackTarget(); }
            @Override public void resetTask() { target = null; }
            @Override public void updateTask() {
                if (target == null || m.getDistanceSq(target) > 44.0 || !m.getEntitySenses().canSee(target)) m.setFuseState(-1);
                else m.setFuseState(1);
            }
        }
    }

    /** Radius-7 creeper that sucks everything in while it swells and also hunts dwarves. */
    public static class ShadowCreeper extends FuseMob {
        public ShadowCreeper(World w) { super(w, 7); }
        @Override protected void initEntityAI() {
            super.initEntityAI();
            tasks.addTask(3, new EntityAIAvoidEntity<>(this, EntityOcelot.class, 6.0F, 1.0, 1.2));
            targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, SWDwarves.Dwarf.class, true));
        }
        @Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25); }
        @Override protected void onPrimed() { super.onPrimed(); playSound(SoundEvents.ENTITY_WITHER_AMBIENT, 10.0F, 0.5F); }
        @Override protected void whileFused() {
            double r = explosionRadius * 2.0 + 1.0;
            net.minecraft.util.math.AxisAlignedBB bb = new net.minecraft.util.math.AxisAlignedBB(
                    MathHelper.floor(posX - r), MathHelper.floor(posY - r), MathHelper.floor(posZ - r),
                    MathHelper.floor(posX + r), MathHelper.floor(posY + r), MathHelper.floor(posZ + r));
            int div = 70 / fuse();
            for (Entity e : world.getEntitiesWithinAABBExcludingEntity(this, bb)) {
                e.motionX -= (e.posX - posX) / div;
                e.motionY -= (e.posY - posY) / div;
                e.motionZ -= (e.posZ - posZ) / div;
            }
        }
        @Override protected SoundEvent getHurtSound(DamageSource d) { return SoundEvents.ENTITY_CREEPER_HURT; }
        @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_CREEPER_DEATH; }
    }

    /** Wolf-shaped creeper: radius 8 blast followed by a radius 4 one. */
    public static class ExplodingWolf extends FuseMob {
        public ExplodingWolf(World w) { super(w, 8); }
        @Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3); }
        @Override protected void explode() {
            boolean g = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world, this);
            world.createExplosion(this, posX, posY, posZ, explosionRadius, g);
            world.createExplosion(this, posX, posY, posZ, explosionRadius / 2.0F, g);
        }
        @Override protected SoundEvent getHurtSound(DamageSource d) { return SoundEvents.ENTITY_WOLF_GROWL; }
        @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_CREEPER_DEATH; }
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

    /** Grows when killed (children size..size+2 while below 6); hits for size-3, drops Shadows when big. */
    public static class ShadowSlime extends EntitySlime {
        public ShadowSlime(World w) { super(w); }
        @Override protected EntitySlime createInstance() { return new ShadowSlime(world); }
        @Override public void setDead() {
            int i = getSlimeSize();
            if (!world.isRemote && !isDead && i < 6 && getHealth() <= 0.0F && !isInWater()) {
                int j = rand.nextInt(3) + 1;
                for (int k = 0; k < j; k++) {
                    float f = (k % 2 - 0.5F) * i / 4.0F, f1 = (k / 2 - 0.5F) * i / 4.0F;
                    ShadowSlime s = new ShadowSlime(world);
                    s.setSlimeSize(i + rand.nextInt(3), true);
                    s.setLocationAndAngles(posX + f, posY + 0.5, posZ + f1, rand.nextFloat() * 360.0F, 0.0F);
                    world.spawnEntity(s);
                }
            }
            if (!world.isRemote && getSlimeSize() > 1) setSlimeSize(1, false); // skip EntitySlime's own split
            super.setDead();
        }
        @Override protected boolean canDamagePlayer() { return true; }
        @Override protected int getAttackStrength() { return getSlimeSize() - 3; }
        @Override protected void dealDamage(EntityLivingBase e) { if (getAttackStrength() > 0) super.dealDamage(e); }
        @Override protected Item getDropItem() { return getSlimeSize() > 4 ? SWContent.Shadow : null; }
        @Override protected net.minecraft.util.ResourceLocation getLootTable() { return null; }
        @Override protected EnumParticleTypes getParticleType() { return EnumParticleTypes.SMOKE_NORMAL; }
        @Override public int getBrightnessForRender() { return 99; }
        @Override public int getMaxSpawnedInChunk() { return 1; }
        @Override protected float getSoundPitch() { return super.getSoundPitch() * -2.0F; }
        @Override public boolean getCanSpawnHere() { return world.getDifficulty() != net.minecraft.world.EnumDifficulty.PEACEFUL; }
    }

    /** Harmless: touching it gives regeneration and hearts; splits into 3-5 smaller slimes, small ones drop golden cookies. */
    public static class CookieSlime extends EntitySlime {
        public CookieSlime(World w) { super(w); }
        @Override protected EntitySlime createInstance() { return new CookieSlime(world); }
        @Override public void setDead() {
            int i = getSlimeSize();
            if (!world.isRemote && !isDead && i > 1 && getHealth() <= 0.0F && !isInWater()) {
                int j = rand.nextInt(3) + 3;
                for (int k = 0; k < j; k++) {
                    float f = (k % 2 - 0.5F) * i / 4.0F, f1 = (k / 2 - 0.5F) * i / 4.0F;
                    CookieSlime s = new CookieSlime(world);
                    s.setSlimeSize(Math.max(1, i - rand.nextInt(3) - 1), true);
                    s.setLocationAndAngles(posX + f, posY + 0.5, posZ + f1, rand.nextFloat() * 360.0F, 0.0F);
                    world.spawnEntity(s);
                }
            }
            if (!world.isRemote && getSlimeSize() > 1) setSlimeSize(1, false);
            super.setDead();
        }
        @Override protected boolean canDamagePlayer() { return false; }
        @Override public void onCollideWithPlayer(EntityPlayer p) {
            int i = getSlimeSize();
            if (world instanceof net.minecraft.world.WorldServer ws) for (int j = 0; j < i * 8; j++) {
                float f = rand.nextFloat() * (float) Math.PI * 2.0F, f1 = rand.nextFloat() * 0.5F + 0.5F;
                ws.spawnParticle(EnumParticleTypes.HEART, posX + MathHelper.sin(f) * i * 0.5F * f1, getEntityBoundingBox().maxY, posZ + MathHelper.cos(f) * i * 0.5F * f1, 1, 0, 0, 0, 0.0);
            }
            p.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 5));
        }
        @Override protected Item getDropItem() { return getSlimeSize() < 4 ? SWContent.GoldenCookie : null; }
        @Override protected net.minecraft.util.ResourceLocation getLootTable() { return null; }
        @Override protected EnumParticleTypes getParticleType() { return EnumParticleTypes.SMOKE_NORMAL; }
        @Override public int getMaxSpawnedInChunk() { return 3; }
        @Override protected float getSoundPitch() { return super.getSoundPitch() * 5.0F; }
        @Override public boolean getCanSpawnHere() { return true; }
    }

    // ------------------------------------------------------------------ animals

    /** Saddle it and it flies: forward = up, back = down, with wither sounds. */
    public static class ShadowPig extends EntityAnimal {
        private static final DataParameter<Boolean> SADDLED = key(ShadowPig.class, DataSerializers.BOOLEAN);
        public ShadowPig(World w) { super(w); setSize(0.9F, 0.9F); }
        @Override protected void initEntityAI() {
            tasks.addTask(0, new EntityAISwimming(this));
            tasks.addTask(1, new EntityAIPanic(this, 1.25));
            tasks.addTask(3, new EntityAIMate(this, 1.0));
            tasks.addTask(4, new EntityAITempt(this, 1.2, Items.CARROT_ON_A_STICK, false));
            tasks.addTask(4, new EntityAITempt(this, 1.2, Items.CARROT, false));
            tasks.addTask(5, new EntityAIFollowParent(this, 1.1));
            tasks.addTask(6, new EntityAIWander(this, 1.0));
            tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
            tasks.addTask(8, new EntityAILookIdle(this));
        }
        @Override protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0);
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25);
        }
        @Override protected void entityInit() { super.entityInit(); dataManager.register(SADDLED, false); }
        public boolean getSaddled() { return dataManager.get(SADDLED); }
        public void setSaddled(boolean b) { dataManager.set(SADDLED, b); }
        @Override public void writeEntityToNBT(NBTTagCompound t) { super.writeEntityToNBT(t); t.setBoolean("Saddle", getSaddled()); }
        @Override public void readEntityFromNBT(NBTTagCompound t) { super.readEntityFromNBT(t); setSaddled(t.getBoolean("Saddle")); }
        @Override protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_PIG_AMBIENT; }
        @Override protected SoundEvent getHurtSound(DamageSource d) { return SoundEvents.ENTITY_PIG_AMBIENT; }
        @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_PIG_DEATH; }
        @Override protected void playStepSound(BlockPos p, net.minecraft.block.Block b) { playSound(SoundEvents.ENTITY_PIG_STEP, 0.15F, 1.0F); }
        @Override protected float getSoundPitch() { return super.getSoundPitch() * -1.0F; }
        @Override public boolean processInteract(EntityPlayer p, EnumHand h) {
            if (super.processInteract(p, h)) return true;
            ItemStack s = p.getHeldItem(h);
            if (!getSaddled() && s.getItem() == Items.SADDLE) {
                if (!world.isRemote) { setSaddled(true); if (!p.capabilities.isCreativeMode) s.shrink(1); }
                return true;
            }
            if (!getSaddled() || h != EnumHand.MAIN_HAND || isBeingRidden() && !isPassenger(p)) return false;
            if (!world.isRemote) p.startRiding(this);
            return true;
        }
        @Override protected Item getDropItem() { return SWContent.Shadow; }
        @Override protected net.minecraft.util.ResourceLocation getLootTable() { return null; }
        @Override protected void dropFewItems(boolean hit, int looting) {
            int j = rand.nextInt(3) + 1 + rand.nextInt(1 + looting);
            for (int k = 0; k < j; k++) entityDropItem(new ItemStack(SWContent.Shadow), 0F);
            entityDropItem(new ItemStack(SWContent.Soul), 0F);
            if (getSaddled()) entityDropItem(new ItemStack(Items.SADDLE), 0F);
        }
        @Override public ShadowPig createChild(EntityAgeable a) { return new ShadowPig(world); }
        @Override public boolean isBreedingItem(ItemStack s) { return s.getItem() == Items.CARROT; }
        @Override public void onLivingUpdate() { if (world.isRemote) shadowParticles(this, 2); super.onLivingUpdate(); }
        @Override public boolean canBePushed() { return !isBeingRidden(); }
        @Override public boolean attackEntityFrom(DamageSource src, float a) {
            return isBeingRidden() && src.getTrueSource() != null && isPassenger(src.getTrueSource()) ? false : super.attackEntityFrom(src, a);
        }
        @Nullable @Override public Entity getControllingPassenger() { return getPassengers().isEmpty() ? null : getPassengers().get(0); }
        @Override public boolean canBeSteered() { return getControllingPassenger() instanceof EntityLivingBase; }
        @Override public void travel(float strafe, float vertical, float forward) {
            Entity r = getControllingPassenger();
            if (r instanceof EntityLivingBase l && getSaddled()) {
                prevRotationYaw = rotationYaw = r.rotationYaw;
                rotationPitch = r.rotationPitch * 0.5F;
                setRotation(rotationYaw, rotationPitch);
                rotationYawHead = renderYawOffset = rotationYaw;
                strafe = l.moveStrafing * 0.5F;
                forward = l.moveForward * 2.0F;
                if (forward > 0.0F) {
                    motionY = 0.1;
                    if (rand.nextInt(4) == 0) playSound(SoundEvents.ENTITY_WITHER_DEATH, 1.0F, getSoundPitch());
                } else if (forward < 0.0F) {
                    motionY = -0.1;
                    if (!onGround) playSound(SoundEvents.ENTITY_WITHER_AMBIENT, 1.0F, getSoundPitch());
                    forward *= -1.0F;
                }
                if (forward == 0.0F) motionY = 0.0;
                stepHeight = 1.0F;
                if (canPassengerSteer()) {
                    setAIMoveSpeed((float) getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() / 1.25F);
                    super.travel(strafe, vertical, forward);
                } else { motionX = 0; motionY = 0; motionZ = 0; }
                prevLimbSwingAmount = limbSwingAmount;
                double d0 = posX - prevPosX, d1 = posZ - prevPosZ;
                float f4 = Math.min(1.0F, MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F);
                limbSwingAmount += (f4 - limbSwingAmount) * 0.4F;
                limbSwing += limbSwingAmount;
            } else {
                stepHeight = 0.5F;
                super.travel(strafe, vertical, forward);
            }
        }
        @Override public boolean isOnLadder() { return false; }
        @Override public void fall(float d, float m) { if (!isBeingRidden()) super.fall(d, m); }
        @Override public boolean getCanSpawnHere() { return true; }
    }

    /** Right-click to take a bite (feeds you); five bites and it is gone. */
    public static class CookiePig extends EntityAnimal {
        private static final DataParameter<Integer> EATEN = key(CookiePig.class, DataSerializers.VARINT);
        private int eatstat = 5;
        public CookiePig(World w) { super(w); setSize(0.9F, 0.9F); }
        @Override protected void initEntityAI() {
            tasks.addTask(0, new EntityAISwimming(this));
            tasks.addTask(1, new EntityAIPanic(this, 1.25));
            tasks.addTask(3, new EntityAIMate(this, 1.0));
            tasks.addTask(4, new EntityAITempt(this, 1.2, SWContent.WhiteChocolate, false));
            tasks.addTask(5, new EntityAIFollowParent(this, 1.1));
            tasks.addTask(6, new EntityAIWander(this, 1.0));
            tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
            tasks.addTask(8, new EntityAILookIdle(this));
        }
        @Override protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0);
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25);
        }
        @Override protected void entityInit() { super.entityInit(); dataManager.register(EATEN, 5); }
        public int getEaten() { return dataManager.get(EATEN); }
        public void setEaten(int i) { dataManager.set(EATEN, i <= 0 ? 5 : eatstat); }
        @Override public void writeEntityToNBT(NBTTagCompound t) { super.writeEntityToNBT(t); t.setInteger("Eating", getEaten()); }
        @Override public void readEntityFromNBT(NBTTagCompound t) {
            super.readEntityFromNBT(t);
            eatstat = t.hasKey("Eating") ? Math.max(1, t.getInteger("Eating")) : 5;
            setEaten(eatstat);
            if (eatstat <= 2) getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0);
        }
        @Override protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_PIG_AMBIENT; }
        @Override protected SoundEvent getHurtSound(DamageSource d) { return SoundEvents.ENTITY_PIG_AMBIENT; }
        @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_PIG_DEATH; }
        @Override protected void playStepSound(BlockPos p, net.minecraft.block.Block b) { playSound(SoundEvents.ENTITY_PIG_STEP, 0.15F, 1.0F); }
        @Override public boolean processInteract(EntityPlayer p, EnumHand h) {
            if (super.processInteract(p, h)) return true;
            if (h != EnumHand.MAIN_HAND || isBeingRidden()) return false;
            if (!world.isRemote) {
                eatstat--;
                world.setEntityState(this, (byte) 7);
                p.getFoodStats().addStats(1, 0.001F);
                playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5F + 0.5F * rand.nextInt(2), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
                setEaten(eatstat);
                if (eatstat <= 0) {
                    attackEntityFrom(DamageSource.GENERIC, 30.0F);
                    playSound(SoundEvents.ENTITY_PLAYER_BURP, 0.5F, rand.nextFloat() * 0.1F + 0.9F);
                    eatstat = 5;
                }
                if (eatstat <= 2) getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0);
            }
            return true;
        }
        @Override protected net.minecraft.util.ResourceLocation getLootTable() { return null; }
        @Override protected void dropFewItems(boolean hit, int looting) {
            int j = rand.nextInt(3) + 1 + rand.nextInt(1 + looting);
            for (int k = 0; k < j; k++) entityDropItem(new ItemStack(Items.COOKIE), 0F);
        }
        @Override public boolean isBreedingItem(ItemStack s) { return s.getItem() == SWContent.WhiteChocolate; }
        @Override public CookiePig createChild(EntityAgeable a) { return new CookiePig(world); }
        @Override public boolean getCanSpawnHere() { return true; }
    }

    /** Small, harmless climbing spider. */
    public static class PassiveSpider extends EntityAnimal {
        private static final DataParameter<Byte> CLIMBING = key(PassiveSpider.class, DataSerializers.BYTE);
        public PassiveSpider(World w) { super(w); setSize(0.5F, 0.5F); }
        @Override protected void initEntityAI() {
            tasks.addTask(0, new EntityAISwimming(this));
            tasks.addTask(1, new EntityAIPanic(this, 1.25));
            tasks.addTask(5, new EntityAIFollowParent(this, 1.1));
            tasks.addTask(6, new EntityAIWander(this, 1.0));
            tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
            tasks.addTask(8, new EntityAILookIdle(this));
        }
        @Override protected net.minecraft.pathfinding.PathNavigate createNavigator(World w) { return new net.minecraft.pathfinding.PathNavigateClimber(this, w); }
        @Override protected void entityInit() { super.entityInit(); dataManager.register(CLIMBING, (byte) 0); }
        @Override public void onUpdate() { super.onUpdate(); if (!world.isRemote) dataManager.set(CLIMBING, (byte) (collidedHorizontally ? 1 : 0)); }
        @Override public boolean isOnLadder() { return dataManager.get(CLIMBING) != 0; }
        @Override public void setInWeb() {}
        @Override public boolean isPotionApplicable(PotionEffect e) { return e.getPotion() != MobEffects.POISON && super.isPotionApplicable(e); }
        @Override protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0);
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2);
        }
        @Nullable @Override public IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance d, @Nullable IEntityLivingData data) { return data; }
        @Override protected Item getDropItem() { return Items.STRING; }
        @Override protected net.minecraft.util.ResourceLocation getLootTable() { return null; }
        @Override protected void dropFewItems(boolean hit, int looting) {
            super.dropFewItems(hit, looting);
            if (hit) entityDropItem(new ItemStack(SWContent.Soul), 0F);
            if (hit && (rand.nextInt(3) == 0 || rand.nextInt(1 + looting) > 0)) entityDropItem(new ItemStack(Items.STRING, 2), 0F);
        }
        @Override protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_SPIDER_AMBIENT; }
        @Override protected SoundEvent getHurtSound(DamageSource d) { return SoundEvents.ENTITY_SPIDER_AMBIENT; }
        @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_SPIDER_DEATH; }
        @Override protected float getSoundPitch() { return super.getSoundPitch() * -1.0F; }
        @Override public PassiveSpider createChild(EntityAgeable a) { return new PassiveSpider(world); }
        @Override public boolean getCanSpawnHere() { return true; }
    }

    /** Shadow wolf: tamed with souls (golden souls always tame, bones still work); hunts Lights. */
    public static class ShadowWolf extends EntityWolf {
        public ShadowWolf(World w) { super(w); setSize(1.0F, 1.2F); }
        @Override protected void initEntityAI() {
            super.initEntityAI();
            targetTasks.addTask(4, new EntityAITargetNonTamed<>(this, Light.class, false, null));
        }
        @Override public boolean canAttackClass(Class<? extends EntityLivingBase> c) {
            return c != SWAngels.Angel.class && c != ShadowWolfKing.class && super.canAttackClass(c);
        }
        @Override protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4);
        }
        /** The 1.7 code swaps these: untamed 40, tamed 20. */
        @Override public void setTamed(boolean t) {
            super.setTamed(t);
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(t ? 20.0 : 40.0);
        }
        @Override protected Item getDropItem() { return null; }
        @Override protected net.minecraft.util.ResourceLocation getLootTable() { return null; }
        @Override public int getMaxSpawnedInChunk() { return 8; }
        @Override protected float getSoundVolume() { return 0.4F; }
        @Override protected float getSoundPitch() { return super.getSoundPitch() * -10.0F; }
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
            return super.processInteract(p, h);
        }
        @Override public boolean canMateWith(EntityAnimal o) {
            return o != this && isTamed() && o instanceof ShadowWolf w && w.isTamed() && !w.isSitting() && isInLove() && w.isInLove();
        }
        @Override public ShadowWolf createChild(EntityAgeable a) {
            ShadowWolf w = new ShadowWolf(world);
            if (getOwnerId() != null) { w.setOwnerId(getOwnerId()); w.setTamed(true); }
            return w;
        }
        @Override public boolean getCanSpawnHere() { return true; }
    }

    /** Glowing jellyfish: squid swimming, 4 skins, 4 sizes. */
    public static class JellyFish extends EntityWaterMob {
        private static final DataParameter<Byte> SKIN = key(JellyFish.class, DataSerializers.BYTE);
        private static final DataParameter<Byte> SCALE = key(JellyFish.class, DataSerializers.BYTE);
        public float squidPitch, prevSquidPitch, squidYaw, prevSquidYaw, squidRotation, prevSquidRotation, tentacleAngle, prevTentacleAngle;
        private float randomMotionSpeed, rotationVelocity, rotateSpeed, randomMotionVecX, randomMotionVecY, randomMotionVecZ;
        public JellyFish(World w) {
            super(w);
            setSize(1.0F, 1.0F);
            rotationVelocity = 1.0F / (rand.nextFloat() + 1.0F) * 0.2F;
        }
        @Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0); }
        @Override protected void entityInit() {
            super.entityInit();
            dataManager.register(SKIN, (byte) (1 + rand.nextInt(3)));
            dataManager.register(SCALE, (byte) rand.nextInt(4));
        }
        public int getSkin() { return dataManager.get(SKIN); }
        public int getScale() { return dataManager.get(SCALE); }
        @Override public void writeEntityToNBT(NBTTagCompound t) { super.writeEntityToNBT(t); t.setInteger("Skin", getSkin()); t.setInteger("Scale", getScale()); }
        @Override public void readEntityFromNBT(NBTTagCompound t) { super.readEntityFromNBT(t); dataManager.set(SKIN, (byte) t.getInteger("Skin")); dataManager.set(SCALE, (byte) t.getInteger("Scale")); }
        @Override protected SoundEvent getAmbientSound() { return null; }
        @Override protected SoundEvent getHurtSound(DamageSource d) { return null; }
        @Override protected SoundEvent getDeathSound() { return null; }
        @Override protected float getSoundVolume() { return 0.4F; }
        @Override protected Item getDropItem() { return SWContent.GlowingInkSac; }
        @Override protected net.minecraft.util.ResourceLocation getLootTable() { return null; }
        @Override protected boolean canTriggerWalking() { return false; }
        @Override protected void dropFewItems(boolean hit, int looting) {
            int j = rand.nextInt(3 + looting) + 1;
            for (int k = 0; k < j; k++) entityDropItem(new ItemStack(SWContent.GlowingInkSac), 0F);
        }
        @Override public boolean isInWater() { return world.handleMaterialAcceleration(getEntityBoundingBox().grow(0.0, -0.6, 0.0), net.minecraft.block.material.Material.WATER, this); }
        @Override public void onLivingUpdate() {
            super.onLivingUpdate();
            prevSquidPitch = squidPitch; prevSquidYaw = squidYaw; prevSquidRotation = squidRotation; prevTentacleAngle = tentacleAngle;
            squidRotation += rotationVelocity;
            if (squidRotation > (float) (Math.PI * 2)) {
                squidRotation -= (float) (Math.PI * 2);
                if (rand.nextInt(10) == 0) rotationVelocity = 1.0F / (rand.nextFloat() + 1.0F) * 0.2F;
            }
            if (isInWater()) {
                if (squidRotation < (float) Math.PI) {
                    float f = squidRotation / (float) Math.PI;
                    tentacleAngle = MathHelper.sin(f * f * (float) Math.PI) * (float) Math.PI * 0.15F;
                    if (f > 0.75) { randomMotionSpeed = 1.0F; rotateSpeed = 1.0F; } else rotateSpeed *= 0.8F;
                } else {
                    tentacleAngle = 0.0F;
                    randomMotionSpeed *= 0.9F;
                    rotateSpeed *= 0.99F;
                }
                if (!world.isRemote) {
                    motionX = randomMotionVecX * randomMotionSpeed;
                    motionY = randomMotionVecY * randomMotionSpeed;
                    motionZ = randomMotionVecZ * randomMotionSpeed;
                }
                float f = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
                renderYawOffset += (-((float) MathHelper.atan2(motionX, motionZ)) * 180.0F / (float) Math.PI - renderYawOffset) * 0.1F;
                rotationYaw = renderYawOffset;
                squidYaw += (float) Math.PI * rotateSpeed * 1.5F;
                squidPitch = (-((float) MathHelper.atan2(f, motionY)) * 180.0F / (float) Math.PI - squidPitch) * 0.001F;
            } else {
                tentacleAngle = MathHelper.abs(MathHelper.sin(squidRotation)) * (float) Math.PI * 0.15F;
                if (!world.isRemote) { motionX = 0.0; motionY -= 0.08; motionY *= 0.98F; motionZ = 0.0; }
                squidPitch = (float) (squidPitch + (-90.0F - squidPitch) * 0.02);
            }
        }
        @Override public void travel(float strafe, float vertical, float forward) { move(net.minecraft.entity.MoverType.SELF, motionX, motionY, motionZ); }
        @Override protected void updateAITasks() {
            idleTime++;
            if (idleTime > 100) randomMotionVecX = randomMotionVecY = randomMotionVecZ = 0.0F;
            else if (rand.nextInt(50) == 0 || !inWater || randomMotionVecX == 0.0F && randomMotionVecY == 0.0F && randomMotionVecZ == 0.0F) {
                float f = rand.nextFloat() * (float) Math.PI;
                randomMotionVecX = MathHelper.cos(f) * 0.1F;
                randomMotionVecY = -0.1F + rand.nextFloat() * 0.2F;
                randomMotionVecZ = MathHelper.sin(f) * 0.1F;
            }
        }
        @Override public boolean getCanSpawnHere() { return posY > 45.0 && posY < 63.0 && super.getCanSpawnHere(); }
        @Override public float getBrightness() { return 1.0F; }
        @Override public int getBrightnessForRender() { return 5728880; }
    }

    /** 1.7 bat-style random flight shared by Light and Dragonfly. */
    public abstract static class Flyer extends EntityAmbientCreature {
        private BlockPos target;
        protected Flyer(World w) { super(w); }
        @Override protected void collideWithEntity(Entity e) {}
        @Override protected void collideWithNearbyEntities() {}
        @Override public void onUpdate() { super.onUpdate(); motionY *= 0.6F; }
        @Override protected void updateAITasks() {
            super.updateAITasks();
            if (target != null && (!world.isAirBlock(target) || target.getY() < 1)) target = null;
            if (target == null || rand.nextInt(30) == 0 || target.distanceSq((int) posX, (int) posY, (int) posZ) < 4.0)
                target = new BlockPos((int) posX + rand.nextInt(7) - rand.nextInt(7), (int) posY + rand.nextInt(6) - 2, (int) posZ + rand.nextInt(7) - rand.nextInt(7));
            double d0 = target.getX() + 0.5 - posX, d1 = target.getY() + 0.1 - posY, d2 = target.getZ() + 0.5 - posZ;
            motionX += (Math.signum(d0) * 0.5 - motionX) * 0.1F;
            motionY += (Math.signum(d1) * 0.7F - motionY) * 0.1F;
            motionZ += (Math.signum(d2) * 0.5 - motionZ) * 0.1F;
            float f = (float) (MathHelper.atan2(motionZ, motionX) * 180.0 / Math.PI) - 90.0F;
            moveForward = 0.5F;
            rotationYaw += MathHelper.wrapDegrees(f - rotationYaw);
        }
        @Override protected boolean canTriggerWalking() { return false; }
        @Override public void fall(float d, float m) {}
        @Override protected void updateFallState(double y, boolean g, net.minecraft.block.state.IBlockState s, BlockPos p) {}
        @Override public boolean doesEntityNotTriggerPressurePlate() { return true; }
        @Override public boolean getCanSpawnHere() { return true; }
    }

    /** Glowing light orb; prey of shadows, wolves and dwarves. Skins 1..7 (4+ use the default texture, like 1.7). */
    public static class Light extends Flyer {
        private static final DataParameter<Byte> SKIN = key(Light.class, DataSerializers.BYTE);
        public Light(World w) { super(w); experienceValue = 10; setSize(0.5F, 0.9F); }
        @Override protected void entityInit() { super.entityInit(); dataManager.register(SKIN, (byte) (1 + rand.nextInt(7))); }
        public int getSkin() { return dataManager.get(SKIN); }
        @Override public void writeEntityToNBT(NBTTagCompound t) { super.writeEntityToNBT(t); t.setInteger("Skin", getSkin()); }
        @Override public void readEntityFromNBT(NBTTagCompound t) { super.readEntityFromNBT(t); dataManager.set(SKIN, (byte) t.getInteger("Skin")); }
        @Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(2.0); }
        @Override protected float getSoundVolume() { return 0.1F; }
        @Override protected SoundEvent getAmbientSound() { return null; }
        @Override protected SoundEvent getHurtSound(DamageSource d) { return null; }
        @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_BAT_DEATH; }
        @Override protected float getSoundPitch() { return super.getSoundPitch() * -10.0F; }
        @Override public boolean canBePushed() { return false; }
        @Override public float getBrightness() { return 1.0F; }
        @Override public int getBrightnessForRender() { return 0xF000F0; }
        @Override protected Item getDropItem() { return Items.GLOWSTONE_DUST; }
        @Override protected void dropFewItems(boolean hit, int looting) {
            super.dropFewItems(hit, looting);
            if (hit) { entityDropItem(new ItemStack(SWContent.Soul), 0F); entityDropItem(new ItemStack(Items.GLOWSTONE_DUST), 0F); }
        }
    }

    /** Dragonfly in 4 sizes. */
    public static class Dragonfly extends Flyer {
        private static final DataParameter<Byte> SCALE = key(Dragonfly.class, DataSerializers.BYTE);
        public Dragonfly(World w) { super(w); setSize(1.0F, 1.0F); }
        @Override protected void entityInit() { super.entityInit(); dataManager.register(SCALE, (byte) (rand.nextInt(4) + 1)); }
        public int getScale() { return dataManager.get(SCALE); }
        @Override public void writeEntityToNBT(NBTTagCompound t) { super.writeEntityToNBT(t); t.setInteger("Scale", getScale()); }
        @Override public void readEntityFromNBT(NBTTagCompound t) { super.readEntityFromNBT(t); dataManager.set(SCALE, (byte) t.getInteger("Scale")); }
        @Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(6.0); }
        @Override protected float getSoundVolume() { return 0.1F; }
        @Override protected float getSoundPitch() { return super.getSoundPitch() * 1.95F; }
        @Override protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_BAT_AMBIENT; }
        @Override protected SoundEvent getHurtSound(DamageSource d) { return SoundEvents.ENTITY_BAT_HURT; }
        @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_BAT_DEATH; }
        @Override public boolean canBePushed() { return true; }
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
