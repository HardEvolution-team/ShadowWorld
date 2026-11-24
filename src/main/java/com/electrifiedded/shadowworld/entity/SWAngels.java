package com.electrifiedded.shadowworld.entity;

import com.electrifiedded.shadowworld.SWContent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.BossInfo;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/** 1.7 EntityAngel (boss) and EntityAngelVillager. */
public final class SWAngels {
    private SWAngels() {}

    /** Angel boss: flies at its target, super attack every ~550 ticks (rise, 10 lightnings, explosion), dies into an Angel Portal. */
    public static class Angel extends SWEntities.Boss {
        private static final DataParameter<Integer> SUPER = EntityDataManager.createKey(Angel.class, DataSerializers.VARINT);
        private Entity targetedEntity;
        public int deathTicks;
        private int legacySoundTime;

        public Angel(World w) {
            super(w, BossInfo.Color.WHITE);
            stepHeight = 1.0F;
            setSize(1.5F, 2.5F);
            isImmuneToFire = true;
            experienceValue = 0;
            setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
            armour(this, true);
            for (EntityEquipmentSlot s : EntityEquipmentSlot.values()) setDropChance(s, 0.0F);
        }

        @Override
        protected void initEntityAI() {
            tasks.addTask(1, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
            targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, false));
            targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, SWEntities.WolfSpider.class, false));
        }

        @Override protected void entityInit() { super.entityInit(); dataManager.register(SUPER, 0); }
        public int getSuperAttackTime() { return dataManager.get(SUPER); }
        public void setSuperAttackTime(int t) { dataManager.set(SUPER, t); }

        @Override
        protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(200.0);
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.74);
            getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0);
            getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(8.0);
            getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(10.0);
        }

        @Override public void setInWeb() {}
        @Override public void addPotionEffect(PotionEffect e) {}
        @Override public EnumCreatureAttribute getCreatureAttribute() { return EnumCreatureAttribute.UNDEFINED; }
        @Override protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_ENDERDRAGON_FLAP; }
        @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.ENTITY_ENDERDRAGON_FLAP; }
        @Override @Nullable protected SoundEvent getDeathSound() { return null; }
        @Override protected float getSoundPitch() { return super.getSoundPitch() * 3.5F; }
        @Override public void fall(float d, float m) {}
        @Override protected void updateFallState(double y, boolean g, IBlockState s, BlockPos p) {}
        @Override public boolean isOnLadder() { return false; }
        @Override public int getTotalArmorValue() { return 8; }
        @Override public boolean canAttackClass(Class<? extends EntityLivingBase> c) { return c != SWEntities.ShadowWolfKing.class && super.canAttackClass(c); }

        @Override
        public void onLivingUpdate() {
            super.onLivingUpdate();
            if (!world.isRemote && isEntityAlive()) actionState();
        }

        /** 1.7 updateEntityActionState (ran on top of the AI tasks every tick). */
        private void actionState() {
            if (ticksExisted % 100 == 0 && getHealth() < 100.0F) heal(5.0F);
            if (rand.nextInt(10) < legacySoundTime++) { legacySoundTime = -getTalkInterval(); playLivingSound(); }
            if (targetedEntity != null && targetedEntity.isDead) targetedEntity = null;
            if (targetedEntity == null) {
                targetedEntity = getAttackTarget() != null ? getAttackTarget() : world.getNearestAttackablePlayer(this, 50.0, 50.0);
            } else if (getSuperAttackTime() < 550) faceMotion();
            setSuperAttackTime(getSuperAttackTime() + 1);
            if (getSuperAttackTime() > 500) {
                noClip = true;
                motionX = 0.0; motionY = 0.5; motionZ = 0.0;
                if (getSuperAttackTime() > 550) {
                    for (int i = 0; i < 10; i++)
                        world.addWeatherEffect(new EntityLightningBolt(world, (int) posX + rand.nextInt(50) - 25, (int) posY, (int) posZ + rand.nextInt(50) - 25, false));
                    setSuperAttackTime(-100);
                    world.createExplosion(this, posX, posY, posZ, 15.0F, true);
                    noClip = false;
                }
            } else if (targetedEntity != null) {
                if (getSuperAttackTime() < 550) faceMotion();
                double dx = targetedEntity.posX - posX, dz = targetedEntity.posZ - posZ;
                motionX += dx / 150.0;
                motionZ += dz / 150.0;
                if (dz <= 10.0 && dz >= -10.0 && dx <= 10.0 && dx >= -10.0) motionY += (targetedEntity.posY - posY) / 150.0;
                else motionY = 0.5;
                if (targetedEntity instanceof SWEntities.Golem)
                    world.addWeatherEffect(new EntityLightningBolt(world, targetedEntity.posX, targetedEntity.posY, targetedEntity.posZ, false));
            } else if (motionY == 0.0) motionY -= 0.25;
            if (targetedEntity != null && (onGround || targetedEntity.posY > posY)) motionY += 0.5;
        }

        private void faceMotion() { renderYawOffset = rotationYaw = -((float) Math.atan2(motionX, motionZ)) * 180.0F / (float) Math.PI; }

        @Override public boolean attackEntityAsMob(Entity e) { targetedEntity = e; return super.attackEntityAsMob(e); }

        @Override
        protected void onDeathUpdate() {
            deathTicks++;
            setSuperAttackTime(-999);
            if (!world.isRemote) {
                if (deathTicks >= 180 && deathTicks <= 200) world.addWeatherEffect(new EntityLightningBolt(world, posX, posY, posZ, false));
                if (deathTicks > 150 && deathTicks % 5 == 0) xp(1000);
                if (deathTicks == 1) world.playBroadcastSound(1028, new BlockPos(this), 0);
            }
            motionX = 0.0; motionY = 0.25; motionZ = 0.0;
            move(MoverType.SELF, 0.0, 0.25, 0.0);
            if (deathTicks == 200 && !world.isRemote) {
                xp(2000);
                int n = rand.nextInt(6) + 4;
                for (int i = 0; i < n; i++) world.spawnEntity(new EntityItem(world, posX, posY - 10.0, posZ, new ItemStack(SWContent.AngelFeather)));
                world.setBlockState(new BlockPos((int) posX, (int) posY, (int) posZ), SWContent.AngelPortal.getDefaultState());
                setDead();
            }
        }

        private void xp(int i) {
            while (i > 0) {
                int j = EntityXPOrb.getXPSplit(i);
                i -= j;
                world.spawnEntity(new EntityXPOrb(world, posX, posY, posZ, j));
            }
        }

        @Override public float getBrightness() { return deathTicks > 0 ? 1.0F : super.getBrightness(); }
        @Override @SideOnly(Side.CLIENT) public int getBrightnessForRender() { return deathTicks > 0 ? 0xF000F0 : super.getBrightnessForRender(); }
        @Override public void writeEntityToNBT(NBTTagCompound c) { super.writeEntityToNBT(c); c.setInteger("SuperAttack", getSuperAttackTime()); }
        @Override public void readEntityFromNBT(NBTTagCompound c) { super.readEntityFromNBT(c); setSuperAttackTime(c.getInteger("SuperAttack")); }
    }

    static void armour(EntityLiving e, boolean full) {
        if (full) {
            e.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(SWContent.bootsAngel));
            e.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(SWContent.legsAngel));
        }
        e.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(SWContent.plateAngel));
        e.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(SWContent.helmetAngel));
    }

    /** 1.7 EntityAngelVillager: Dwarf-style merchant (Shadow Coins) living in the Angel Village. */
    public static class AngelVillager extends SWDwarves.Dwarf {
        public static final Map<Object, int[]> villagerStockList = new HashMap<>();
        public static final Map<Item, int[]> blacksmithSellingList = new HashMap<>();

        public AngelVillager(World w) { this(w, 0); }

        public AngelVillager(World w, int profession) {
            super(w, profession);
            tasks.taskEntries.clear();
            targetTasks.taskEntries.clear();
            tasks.addTask(0, new EntityAISwimming(this));
            tasks.addTask(1, new SWDwarves.TradePlayer(this));
            tasks.addTask(1, new SWDwarves.LookAtTradePlayer(this));
            tasks.addTask(2, new EntityAIMoveIndoors(this));
            tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
            tasks.addTask(4, new EntityAIOpenDoor(this, true));
            tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.6));
            tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
            tasks.addTask(9, new EntityAIWatchClosest2(this, AngelVillager.class, 5.0F, 0.02F));
            tasks.addTask(9, new EntityAIWander(this, 0.6));
            tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
            targetTasks.addTask(0, new EntityAIHurtByTarget(this, true));
            targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, SWEntities.Light.class, false));
            targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, SWEntities.WolfSpider.class, false));
            targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, SWEntities.ShadowCreeper.class, false));
            targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, EntityZombie.class, false));
            targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, SWEntities.Shadow.class, false));
            for (EntityEquipmentSlot s : EntityEquipmentSlot.values()) setDropChance(s, 0.0F);
        }

        /** Equipment by profession as in GHouse/DHouse.generateRandomHouse and onSpawnWithEgg (i > 4 = guard). */
        public void equipAngel(int i) {
            for (EntityEquipmentSlot s : EntityEquipmentSlot.values()) setItemStackToSlot(s, ItemStack.EMPTY);
            if (i > 4) {
                setProfession(4);
                setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
                armour(this, true);
            } else {
                setProfession(i);
                armour(this, i == 0);
            }
        }

        @Override public boolean isAngry() { return false; }
        @Override public void setRandomName() {}
        @Override protected float getSoundPitch() { return (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F + 2.0F; }
        @Override public ITextComponent getDisplayName() { return hasCustomName() ? super.getDisplayName() : new TextComponentString("Angel"); }
        @Override public boolean attackEntityAsMob(Entity e) { return e.attackEntityFrom(DamageSource.causeMobDamage(this), 2.0F); }

        @Override
        protected void offers(MerchantRecipeList l) {
            switch (getProfession()) {
                case 0 -> {
                    addBlacksmithItem(l, SWContent.FlyingBoat, rand, adjustProbability(0.15F), blacksmithSellingList);
                    addBlacksmithItem(l, Item.getItemFromBlock(SWContent.Lamp), rand, adjustProbability(0.5F), blacksmithSellingList);
                    addBlacksmithItem(l, Item.getItemFromBlock(SWContent.AngelChest), rand, adjustProbability(0.5F), blacksmithSellingList);
                }
                case 1 -> {
                    addMerchantItem(l, SWContent.Soul, rand, adjustProbability(0.5F), villagerStockList);
                    addMerchantItem(l, SWContent.BloodStone, rand, adjustProbability(0.5F), villagerStockList);
                    addMerchantItem(l, SWContent.Azurite, rand, adjustProbability(0.5F), villagerStockList);
                }
                case 2 -> {
                    addMerchantItem(l, Items.DIAMOND, rand, adjustProbability(0.25F), villagerStockList);
                    addMerchantItem(l, Items.GOLD_INGOT, rand, adjustProbability(0.3F), villagerStockList);
                    addMerchantItem(l, Items.IRON_INGOT, rand, adjustProbability(0.5F), villagerStockList);
                    addMerchantItem(l, Items.EMERALD, rand, adjustProbability(0.1F), villagerStockList);
                }
                case 3 -> {
                    addMerchantItem(l, Items.REDSTONE, rand, adjustProbability(0.5F), villagerStockList);
                    addMerchantItem(l, Items.COAL, rand, adjustProbability(0.5F), villagerStockList);
                }
                default -> {}
            }
            if (l.isEmpty()) addMerchantItem(l, SWContent.AngelFeather, rand, 1.0F, villagerStockList);
        }

        @Override
        @Nullable
        public IEntityLivingData onInitialSpawn(DifficultyInstance d, @Nullable IEntityLivingData data) {
            data = super.onInitialSpawn(d, data);
            equipAngel(rand.nextInt(6));
            return data;
        }

        @Override
        public SWDwarves.Dwarf func_90012_b(EntityAgeable other) {
            AngelVillager v = new AngelVillager(world);
            v.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(v)), null);
            return v;
        }

        static {
            villagerStockList.put(Blocks.QUARTZ_BLOCK, new int[]{1, 3});
            villagerStockList.put(Items.COAL, new int[]{1, 3});
            villagerStockList.put(Items.REDSTONE, new int[]{10, 30});
            blacksmithSellingList.put(SWContent.FlyingBoat, new int[]{10, 11});
            blacksmithSellingList.put(Item.getItemFromBlock(SWContent.Lamp), new int[]{5, 8});
            blacksmithSellingList.put(Item.getItemFromBlock(SWContent.AngelChest), new int[]{4, 6});
            villagerStockList.put(Items.DIAMOND, new int[]{-9, -6});
            villagerStockList.put(Items.GOLD_INGOT, new int[]{-6, -4});
            villagerStockList.put(Items.IRON_INGOT, new int[]{-3, -1});
            villagerStockList.put(Items.EMERALD, new int[]{-6, -4});
            villagerStockList.put(SWContent.Azurite, new int[]{-4, -2});
            villagerStockList.put(SWContent.BloodStone, new int[]{-6, -4});
            villagerStockList.put(SWContent.Soul, new int[]{-2, -1});
            villagerStockList.put(SWContent.AngelFeather, new int[]{-10, -5});
        }
    }
}
