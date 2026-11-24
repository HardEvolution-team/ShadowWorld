package com.electrifiedded.shadowworld.entity;

import com.electrifiedded.archaiclib.original.OriginalModAssets;
import com.electrifiedded.shadowworld.SWContent;
import com.electrifiedded.shadowworld.ShadowWorld;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.village.Village;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** EntityDwarf / EntityTravellingDwarf of the 1.7 mod with their AI (trade, mate, play, travel) and Shadow Coin trades. */
public final class SWDwarves {
    private SWDwarves() {}

    /** IDwarf. */
    public interface IDwarf extends INpc, IMob {
        boolean isTrading();
        @Nullable EntityPlayer getCustomer();
    }

    static void setRandomName(EntityLiving e) {
        switch (e.getRNG().nextInt(500)) {
            case 1 -> e.setCustomNameTag("jajo_11");
            case 2 -> e.setCustomNameTag("Thorin");
            case 3 -> e.setCustomNameTag("Honeydew");
            case 4 -> e.setCustomNameTag("nicolas963");
            case 5 -> e.setCustomNameTag("You");
            default -> {}
        }
    }

    /** Shared 1.7 attackEntityAsMob (4 damage + enchantments). */
    static boolean attackAsMob(EntityLivingBase self, Entity target) {
        float f = 4.0F;
        int kb = 0;
        if (target instanceof EntityLivingBase l) {
            f += EnchantmentHelper.getModifierForCreature(self.getHeldItemMainhand(), l.getCreatureAttribute());
            kb += EnchantmentHelper.getKnockbackModifier(self);
        }
        boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(self), f);
        if (hit) {
            if (kb > 0) {
                target.addVelocity(-MathHelper.sin(self.rotationYaw * (float) Math.PI / 180.0F) * kb * 0.5F, 0.1,
                        MathHelper.cos(self.rotationYaw * (float) Math.PI / 180.0F) * kb * 0.5F);
                self.motionX *= 0.6;
                self.motionZ *= 0.6;
            }
            int fire = EnchantmentHelper.getFireAspectModifier(self);
            if (fire > 0) target.setFire(fire * 4);
            if (target instanceof EntityLivingBase l) EnchantmentHelper.applyThornEnchantments(l, self);
            EnchantmentHelper.applyArthropodEnchantments(self, target);
        }
        return hit;
    }

    static void baseTasks(EntityCreature e) {
        PathNavigateGround nav = (PathNavigateGround) e.getNavigator();
        nav.setBreakDoors(true);
        nav.setAvoidSun(true);
    }

    static void targets(EntityCreature e) {
        e.targetTasks.addTask(0, new EntityAIHurtByTarget(e, true));
        e.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(e, SWEntities.Light.class, false));
        e.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(e, SWEntities.WolfSpider.class, false));
        e.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(e, SWEntities.ShadowCreeper.class, false));
        e.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(e, EntityZombie.class, false));
        e.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(e, SWEntities.Shadow.class, false));
    }

    /** 1.7 old-AI attackEntity (walk at the target, hit every 20 ticks within 2 blocks) for angry fighters. */
    static class LegacyMelee extends EntityAIBase {
        final EntityCreature e;
        final java.util.function.BooleanSupplier active;
        int attackTime;

        LegacyMelee(EntityCreature e, java.util.function.BooleanSupplier active) {
            this.e = e;
            this.active = active;
            setMutexBits(3);
        }

        @Override public boolean shouldExecute() { return active.getAsBoolean() && e.getAttackTarget() != null && e.getAttackTarget().isEntityAlive(); }

        @Override
        public void updateTask() {
            EntityLivingBase t = e.getAttackTarget();
            if (t == null) return;
            if (attackTime > 0) attackTime--;
            e.getLookHelper().setLookPositionWithEntity(t, 30.0F, 30.0F);
            double dx = t.posX - e.posX, dz = t.posZ - e.posZ;
            float d = MathHelper.sqrt(dx * dx + dz * dz);
            if (d > 0.01F) {
                e.motionX = dx / d * 0.25 * 0.8F + e.motionX * 0.2F;
                e.motionZ = dz / d * 0.25 * 0.8F + e.motionZ * 0.2F;
            }
            e.getNavigator().tryMoveToEntityLiving(t, 0.6);
            if (attackTime <= 0 && e.getDistance(t) < 2.0F && t.getEntityBoundingBox().maxY > e.getEntityBoundingBox().minY
                    && t.getEntityBoundingBox().minY < e.getEntityBoundingBox().maxY) {
                attackTime = 20;
                e.swingArm(EnumHand.MAIN_HAND);
                e.attackEntityAsMob(t);
            }
        }
    }

    // ------------------------------------------------------------------ Dwarf

    public static class Dwarf extends EntityAgeable implements IMerchant, IDwarf {
        private static final DataParameter<Integer> PROFESSION = EntityDataManager.createKey(Dwarf.class, DataSerializers.VARINT);
        private static final DataParameter<Byte> FLAGS = EntityDataManager.createKey(Dwarf.class, DataSerializers.BYTE);
        public static final Map<Object, int[]> villagerStockList = new HashMap<>();
        public static final Map<Item, int[]> blacksmithSellingList = new HashMap<>();
        private int randomTickDivider;
        private boolean isMating, isPlaying, needsInitilization, isLookingForHome;
        Village villageObj;
        private EntityPlayer buyingPlayer;
        private MerchantRecipeList buyingList;
        private int timeUntilReset, wealth;
        private String lastBuyingPlayer;
        private float field_82191_bN;

        public Dwarf(World w) { this(w, 0); }

        public Dwarf(World w, int profession) {
            super(w);
            setProfession(profession);
            setSize(0.6F, 1.8F);
            baseTasks(this);
            tasks.addTask(0, new EntityAISwimming(this));
            tasks.addTask(1, new TradePlayer(this));
            tasks.addTask(1, new LookAtTradePlayer(this));
            tasks.addTask(1, new LegacyMelee(this, () -> isAngry() && getProfession() == 3));
            tasks.addTask(2, new EntityAIMoveIndoors(this));
            tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
            tasks.addTask(4, new EntityAIOpenDoor(this, true));
            tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.6));
            tasks.addTask(6, new Mate(this));
            tasks.addTask(8, new Play(this, 0.32));
            tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
            tasks.addTask(9, new EntityAIWatchClosest2(this, Dwarf.class, 5.0F, 0.02F));
            tasks.addTask(9, new EntityAIWander(this, 0.6));
            tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
            targets(this);
        }

        public void setRandomName() { SWDwarves.setRandomName(this); }

        @Override
        protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5);
        }

        @Override
        protected void entityInit() {
            super.entityInit();
            dataManager.register(PROFESSION, 0);
            dataManager.register(FLAGS, (byte) 0);
        }

        @Override
        protected void updateAITasks() {
            if (!(isAngry() && getProfession() == 3)) {
                if (--randomTickDivider <= 0) {
                    BlockPos bp = new BlockPos(this);
                    world.getVillageCollection().addToVillagerPositionList(bp);
                    randomTickDivider = 70 + rand.nextInt(50);
                    villageObj = world.getVillageCollection().getNearestVillage(bp, 32);
                    if (villageObj == null) detachHome();
                    else {
                        setHomePosAndDistance(villageObj.getCenter(), (int) (villageObj.getVillageRadius() * 2.0F));
                        if (isLookingForHome) {
                            isLookingForHome = false;
                            villageObj.setDefaultPlayerReputation(5);
                        }
                    }
                }
                if (!isTrading() && timeUntilReset > 0 && --timeUntilReset <= 0) {
                    if (needsInitilization) {
                        if (buyingList.size() > 1)
                            for (MerchantRecipe r : buyingList) if (r.isRecipeDisabled()) r.increaseMaxTradeUses(rand.nextInt(8) + rand.nextInt(8) + 4);
                        addDefaultEquipmentAndRecipies(1);
                        needsInitilization = false;
                        if (villageObj != null && lastBuyingPlayer != null) {
                            world.setEntityState(this, (byte) 14);
                            villageObj.modifyPlayerReputation(lastBuyingPlayer, 1);
                        }
                    }
                    addPotionEffect(new PotionEffect(MobEffects.HASTE, 200, 0));
                    heal(4.0F);
                }
            }
            super.updateAITasks();
        }

        @Override
        public boolean processInteract(EntityPlayer p, EnumHand hand) {
            ItemStack held = p.getHeldItem(hand);
            boolean egg = held.getItem() == Items.SPAWN_EGG;
            if (!egg && isEntityAlive() && !isTrading() && !isChild() && !p.isSneaking()) {
                if (!world.isRemote && getAttackTarget() != p) {
                    setCustomer(p);
                    p.displayVillagerTradeGui(this);
                }
                return true;
            }
            return super.processInteract(p, hand);
        }

        @Override
        public void writeEntityToNBT(NBTTagCompound c) {
            super.writeEntityToNBT(c);
            c.setInteger("Profession", getProfession());
            c.setInteger("Riches", wealth);
            c.setBoolean("Angry", isAngry());
            if (buyingList != null) c.setTag("Offers", buyingList.getRecipiesAsTags());
        }

        @Override
        public void readEntityFromNBT(NBTTagCompound c) {
            super.readEntityFromNBT(c);
            setProfession(c.getInteger("Profession"));
            wealth = c.getInteger("Riches");
            setAngry(c.getBoolean("Angry"));
            if (c.hasKey("Offers")) buyingList = new MerchantRecipeList(c.getCompoundTag("Offers"));
        }

        @Override protected boolean canDespawn() { return false; }
        @Override protected SoundEvent getAmbientSound() { return isTrading() ? SoundEvents.ENTITY_VILLAGER_TRADING : SoundEvents.ENTITY_VILLAGER_AMBIENT; }
        @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.ENTITY_VILLAGER_HURT; }
        @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_VILLAGER_DEATH; }
        @Override protected float getSoundPitch() { return super.getSoundPitch() * -1.0F; }

        public void setProfession(int p) { dataManager.set(PROFESSION, p); }
        public int getProfession() { return dataManager.get(PROFESSION); }
        public boolean isMating() { return isMating; }
        public void setMating(boolean b) { isMating = b; }
        public void setPlaying(boolean b) { isPlaying = b; }
        public boolean isPlaying() { return isPlaying; }
        public void setLookingForHome() { isLookingForHome = true; }

        @Override
        public void setRevengeTarget(@Nullable EntityLivingBase e) {
            super.setRevengeTarget(e);
            if (villageObj != null && e != null) {
                villageObj.addOrRenewAgressor(e);
                if (e instanceof EntityPlayer p) {
                    villageObj.modifyPlayerReputation(p.getName(), isChild() ? -3 : -1);
                    setAngry(true);
                    setAttackTarget(e);
                    if (isEntityAlive()) world.setEntityState(this, (byte) 13);
                }
            }
        }

        @Override
        public void onDeath(DamageSource src) {
            if (villageObj != null) {
                Entity e = src.getTrueSource();
                if (e instanceof EntityPlayer p) villageObj.modifyPlayerReputation(p.getName(), -2);
                else if (e instanceof IMob) villageObj.endMatingSeason();
                else if (e == null && world.getClosestPlayerToEntity(this, 16.0) != null) villageObj.endMatingSeason();
            }
            super.onDeath(src);
        }

        @Override public void setCustomer(@Nullable EntityPlayer p) { buyingPlayer = p; }
        @Override @Nullable public EntityPlayer getCustomer() { return buyingPlayer; }
        @Override public boolean isTrading() { return buyingPlayer != null; }

        @Override
        public void useRecipe(MerchantRecipe r) {
            r.incrementToolUses();
            livingSoundTime = -getTalkInterval();
            playSound(SoundEvents.ENTITY_VILLAGER_YES, getSoundVolume(), getSoundPitch());
            if (sameIds(r, buyingList.get(buyingList.size() - 1))) {
                timeUntilReset = 40;
                needsInitilization = true;
                lastBuyingPlayer = buyingPlayer != null ? buyingPlayer.getName() : null;
            }
            if (r.getItemToBuy().getItem() == SWContent.ShadowCoin) wealth += r.getItemToBuy().getCount();
        }

        @Override
        public void verifySellingItem(ItemStack s) {
            if (!world.isRemote && livingSoundTime > -getTalkInterval() + 20) {
                livingSoundTime = -getTalkInterval();
                playSound(s.isEmpty() ? SoundEvents.ENTITY_VILLAGER_NO : SoundEvents.ENTITY_VILLAGER_YES, getSoundVolume(), getSoundPitch());
            }
        }

        @Override
        @Nullable
        public MerchantRecipeList getRecipes(EntityPlayer p) {
            if (buyingList == null) addDefaultEquipmentAndRecipies(1);
            return buyingList;
        }

        @Override @SideOnly(Side.CLIENT) public void setRecipes(@Nullable MerchantRecipeList l) {}
        @Override public World getWorld() { return world; }
        @Override public BlockPos getPos() { return new BlockPos(this); }
        @Override public ITextComponent getDisplayName() { return hasCustomName() ? super.getDisplayName() : new TextComponentString("Dwarf"); }

        protected float adjustProbability(float p) {
            float f = p + field_82191_bN;
            return f > 0.9F ? 0.9F - (f - 0.9F) : f;
        }

        private void addDefaultEquipmentAndRecipies(int count) {
            field_82191_bN = buyingList != null ? MathHelper.sqrt(buyingList.size()) * 0.2F : 0.0F;
            MerchantRecipeList l = new MerchantRecipeList();
            offers(l);
            Collections.shuffle(l);
            if (buyingList == null) buyingList = new MerchantRecipeList();
            for (int i = 0; i < count && i < l.size(); i++) addToListWithCheck(buyingList, l.get(i));
        }

        /** Profession offers (overridden by the Angel villager). */
        protected void offers(MerchantRecipeList l) {
            switch (getProfession()) {
                case 0 -> {
                    addMerchantItem(l, SWContent.Soul, rand, adjustProbability(0.5F));
                    addBlacksmithItem(l, Item.getItemFromBlock(Blocks.BROWN_MUSHROOM), rand, adjustProbability(0.7F));
                    addBlacksmithItem(l, Item.getItemFromBlock(Blocks.RED_MUSHROOM), rand, adjustProbability(0.7F));
                    addBlacksmithItem(l, SWContent.SWoodHoe, rand, adjustProbability(0.7F));
                    addBlacksmithItem(l, SWContent.AncientHoe, rand, adjustProbability(0.1F));
                }
                case 1 -> {
                    addMerchantItem(l, Items.PAPER, rand, adjustProbability(0.8F));
                    addBlacksmithItem(l, Item.getItemFromBlock(Blocks.BOOKSHELF), rand, adjustProbability(0.8F));
                    addBlacksmithItem(l, SWContent.BlackFeather, rand, adjustProbability(0.2F));
                    addBlacksmithItem(l, SWContent.GlowingInkSac, rand, adjustProbability(0.3F));
                }
                case 2 -> {
                    addBlacksmithItem(l, SWContent.GlowingInkSac, rand, adjustProbability(0.3F));
                    addBlacksmithItem(l, SWContent.ChatCookie, rand, adjustProbability(0.2F));
                    addBlacksmithItem(l, SWContent.Health, rand, adjustProbability(0.1F));
                    addMerchantItem(l, SWContent.FireStone, rand, adjustProbability(0.2F));
                    addMerchantItem(l, SWContent.WaterStone, rand, adjustProbability(0.2F));
                    addMerchantItem(l, SWContent.EarthStone, rand, adjustProbability(0.2F));
                    addMerchantItem(l, SWContent.AirStone, rand, adjustProbability(0.2F));
                    addBlacksmithItem(l, SWContent.shadowpotion, rand, adjustProbability(0.1F));
                }
                case 3 -> {
                    addMerchantItem(l, SWContent.Azurite, rand, adjustProbability(0.4F));
                    addMerchantItem(l, SWContent.BloodStone, rand, adjustProbability(0.2F));
                    addMerchantItem(l, SWContent.GoldenSoul, rand, adjustProbability(0.5F));
                    addBlacksmithItem(l, SWContent.Azurite, rand, adjustProbability(0.4F));
                    addBlacksmithItem(l, SWContent.BloodStone, rand, adjustProbability(0.2F));
                    for (Item i : new Item[]{SWContent.helmetAncient, SWContent.plateAncient, SWContent.legsAncient, SWContent.bootsAncient, SWContent.AncientAxe,
                            SWContent.AncientPickaxe, SWContent.AncientSword, SWContent.AncientShovel, SWContent.ShadowCrystal, SWContent.AncientSwordPlus})
                        addBlacksmithItem(l, i, rand, adjustProbability(0.1F));
                }
                case 4 -> {
                    addMerchantItem(l, SWContent.BlackFeather, rand, adjustProbability(0.7F));
                    addMerchantItem(l, Items.BONE, rand, adjustProbability(0.5F));
                    addMerchantItem(l, Items.STRING, rand, adjustProbability(0.5F));
                    addMerchantItem(l, SWContent.Shadow, rand, adjustProbability(0.6F));
                }
                default -> {}
            }
            if (l.isEmpty()) addBlacksmithItem(l, SWContent.Azurite, rand, 1.0F);
        }

        /** 1.7 MerchantRecipe.hasSameIDsAs / hasSameItemsAs. */
        static boolean sameIds(MerchantRecipe a, MerchantRecipe b) {
            return a.getItemToBuy().getItem() == b.getItemToBuy().getItem() && a.getItemToSell().getItem() == b.getItemToSell().getItem()
                    && (a.getSecondItemToBuy().isEmpty() && b.getSecondItemToBuy().isEmpty()
                        || !a.getSecondItemToBuy().isEmpty() && a.getSecondItemToBuy().getItem() == b.getSecondItemToBuy().getItem());
        }

        static boolean sameItems(MerchantRecipe a, MerchantRecipe b) {
            return sameIds(a, b) && (a.getItemToBuy().getCount() < b.getItemToBuy().getCount()
                    || !a.getSecondItemToBuy().isEmpty() && a.getSecondItemToBuy().getCount() < b.getSecondItemToBuy().getCount());
        }

        /** 1.7 MerchantRecipeList.addToListWithCheck. */
        static void addToListWithCheck(MerchantRecipeList list, MerchantRecipe r) {
            for (int i = 0; i < list.size(); i++) {
                MerchantRecipe o = list.get(i);
                if (sameIds(r, o)) {
                    if (sameItems(r, o)) list.set(i, r);
                    return;
                }
            }
            list.add(r);
        }

        public static void addMerchantItem(MerchantRecipeList list, Item item, Random r, float chance) { addMerchantItem(list, item, r, chance, villagerStockList); }

        public static void addMerchantItem(MerchantRecipeList list, Item item, Random r, float chance, Map<Object, int[]> stock) {
            if (r.nextFloat() >= chance) return;
            int j = count(stock.get(item), r);
            ItemStack coin, goods;
            if (j < 0) {
                coin = new ItemStack(SWContent.ShadowCoin, -j);
                goods = new ItemStack(item);
            } else {
                coin = new ItemStack(SWContent.ShadowCoin);
                goods = new ItemStack(item, j);
            }
            list.add(new MerchantRecipe(goods, coin));
        }

        public static void addBlacksmithItem(MerchantRecipeList list, Item item, Random r, float chance) { addBlacksmithItem(list, item, r, chance, blacksmithSellingList); }

        public static void addBlacksmithItem(MerchantRecipeList list, Item item, Random r, float chance, Map<Item, int[]> sell) {
            if (r.nextFloat() >= chance) return;
            int j = count(sell.get(item), r);
            ItemStack coin, goods;
            if (j < 0) {
                coin = new ItemStack(SWContent.ShadowCoin);
                goods = new ItemStack(item, -j);
            } else {
                coin = new ItemStack(SWContent.ShadowCoin, j);
                goods = new ItemStack(item);
            }
            list.add(new MerchantRecipe(coin, goods));
        }

        private static int count(int[] t, Random r) {
            if (t == null) return 1;
            return t[0] >= t[1] ? t[0] : t[0] + r.nextInt(t[1] - t[0]);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void handleStatusUpdate(byte id) {
            if (id == 12) particles(EnumParticleTypes.HEART);
            else if (id == 13) particles(EnumParticleTypes.VILLAGER_ANGRY);
            else if (id == 14) particles(EnumParticleTypes.VILLAGER_HAPPY);
            else super.handleStatusUpdate(id);
        }

        @SideOnly(Side.CLIENT)
        private void particles(EnumParticleTypes t) {
            for (int i = 0; i < 5; i++)
                world.spawnParticle(t, posX + rand.nextFloat() * width * 2.0F - width, posY + 1.0 + rand.nextFloat() * height,
                        posZ + rand.nextFloat() * width * 2.0F - width, rand.nextGaussian() * 0.02, rand.nextGaussian() * 0.02, rand.nextGaussian() * 0.02);
        }

        /** onSpawnWithEgg: random profession, its tools/armour and a rare name. */
        @Override
        @Nullable
        public IEntityLivingData onInitialSpawn(DifficultyInstance d, @Nullable IEntityLivingData data) {
            data = super.onInitialSpawn(d, data);
            int i = rand.nextInt(6);
            setProfession(Math.min(i, 4));
            equip(this, i, false);
            setRandomName();
            return data;
        }

        /** Equipment by profession (also used by the village pieces). */
        public static void equip(EntityLiving e, int i, boolean villageGuardHelmet) {
            if (i == 0) e.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(SWContent.AncientHoe));
            else if (i == 1) e.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOOK));
            else if (i == 2) e.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(SWContent.MagicalPaper));
            else if (i == 3) e.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(SWContent.AncientSword));
            else e.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(SWContent.AncientPickaxe));
            if (i != 2) {
                e.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(SWContent.plateAncient));
                e.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(SWContent.legsAncient));
                e.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(SWContent.bootsAncient));
            }
            if (i == 3 || i >= 4 && villageGuardHelmet) e.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(SWContent.helmetAncient));
        }

        public Dwarf func_90012_b(EntityAgeable other) {
            Dwarf d = new Dwarf(world);
            d.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(d)), null);
            return d;
        }

        @Override public boolean canBeLeashedTo(EntityPlayer p) { return false; }
        @Override public EntityAgeable createChild(EntityAgeable a) { return func_90012_b(a); }

        @Override
        public void setAttackTarget(@Nullable EntityLivingBase e) {
            super.setAttackTarget(e);
            setAngry(true); // the 1.7 code sets angry even for null (it never calms down)
        }

        public boolean isAngry() { return (dataManager.get(FLAGS) & 2) != 0; }
        public void setAngry(boolean b) { byte f = dataManager.get(FLAGS); dataManager.set(FLAGS, (byte) (b ? f | 2 : f & -3)); }
        @Override public boolean attackEntityAsMob(Entity e) { return attackAsMob(this, e); }

        static {
            villagerStockList.put(SWContent.BlackFeather, new int[]{4, 7});
            villagerStockList.put(Blocks.BROWN_MUSHROOM, new int[]{-4, -1}); // keyed by Block in 1.7 too (never matched)
            villagerStockList.put(Blocks.RED_MUSHROOM, new int[]{-4, -1});
            villagerStockList.put(Items.STRING, new int[]{5, 10});
            villagerStockList.put(Items.BONE, new int[]{5, 10});
            villagerStockList.put(SWContent.Shadow, new int[]{6, 9});
            blacksmithSellingList.put(SWContent.Health, new int[]{10, 11});
            blacksmithSellingList.put(SWContent.ChatCookie, new int[]{5, 9});
            blacksmithSellingList.put(SWContent.GlowingInkSac, new int[]{7, 10});
            blacksmithSellingList.put(SWContent.Azurite, new int[]{5, 7});
            blacksmithSellingList.put(SWContent.BloodStone, new int[]{2, 3});
            for (Item i : new Item[]{SWContent.helmetAncient, SWContent.plateAncient, SWContent.legsAncient, SWContent.bootsAncient, SWContent.AncientSword})
                blacksmithSellingList.put(i, new int[]{8, 16});
            for (Item i : new Item[]{SWContent.AncientAxe, SWContent.AncientHoe, SWContent.AncientPickaxe, SWContent.AncientShovel})
                blacksmithSellingList.put(i, new int[]{6, 13});
            blacksmithSellingList.put(SWContent.ShadowCrystal, new int[]{12, 16});
            blacksmithSellingList.put(SWContent.AncientSwordPlus, new int[]{31, 32});
            blacksmithSellingList.put(SWContent.shadowpotion, new int[]{10, 11});
            villagerStockList.put(SWContent.GoldenSoul, new int[]{-9, -6});
            villagerStockList.put(SWContent.Azurite, new int[]{-3, -1});
            villagerStockList.put(SWContent.BloodStone, new int[]{-3, -1});
            for (Item i : new Item[]{SWContent.FireStone, SWContent.WaterStone, SWContent.AirStone, SWContent.EarthStone})
                villagerStockList.put(i, new int[]{-9, -3});
            villagerStockList.put(SWContent.Soul, new int[]{-10, -5});
            villagerStockList.put(SWContent.SWoodHoe, new int[]{-6, -5});
        }
    }

    // ------------------------------------------------------------------ Travelling Dwarf

    public static class TravellingDwarf extends EntityAgeable implements IDwarf {
        private static final DataParameter<Byte> FLAGS = EntityDataManager.createKey(TravellingDwarf.class, DataSerializers.BYTE);
        private EntityPlayer listeningPlayer;
        private int direction;

        public TravellingDwarf(World w) {
            super(w);
            setSize(0.6F, 1.8F);
            baseTasks(this);
            tasks.addTask(0, new EntityAISwimming(this));
            tasks.addTask(1, new LookAtTradePlayer(this));
            tasks.addTask(1, new LegacyMelee(this, this::isAngry));
            tasks.addTask(2, new EntityAIMoveIndoors(this));
            tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
            tasks.addTask(4, new EntityAIOpenDoor(this, true));
            tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.6));
            tasks.addTask(6, new Travelling(this, 0.6));
            tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
            tasks.addTask(9, new EntityAIWatchClosest2(this, TravellingDwarf.class, 5.0F, 0.02F));
            tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
            targets(this);
        }

        @Override
        protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5);
        }

        @Override protected void entityInit() { super.entityInit(); dataManager.register(FLAGS, (byte) 0); }

        @Override
        protected void updateAITasks() {
            if (listeningPlayer != null && getDistance(listeningPlayer) > 10.0F) listeningPlayer = null;
            super.updateAITasks();
        }

        /** Story lines (assets/shadowworld/stories/storyN.story of the original jar), one per page. */
        public List<String> story() {
            int n = 0;
            while (n < 64 && OriginalModAssets.readOriginal("shadowworld", "assets/shadowworld/stories/story" + n + ".story") != null) n++;
            List<String> out = new ArrayList<>();
            if (n == 0) return out;
            byte[] b = OriginalModAssets.readOriginal("shadowworld", "assets/shadowworld/stories/story" + Math.floorMod(getEntityId() * 31 + getUniqueID().hashCode(), n) + ".story");
            if (b != null) for (String l : new String(b, StandardCharsets.UTF_8).split("\\r?\\n")) out.add(l);
            return out;
        }

        @Override
        public boolean processInteract(EntityPlayer p, EnumHand hand) {
            ItemStack held = p.getHeldItem(hand);
            if (held.getItem() != Items.SPAWN_EGG && isEntityAlive() && !isChild() && !p.isSneaking()) {
                if (world.isRemote && getAttackTarget() != p) {
                    listeningPlayer = p;
                    ShadowWorld.proxy.openTravellingDwarf(this);
                }
                return true;
            }
            return super.processInteract(p, hand);
        }

        @Override
        public void writeEntityToNBT(NBTTagCompound c) {
            super.writeEntityToNBT(c);
            c.setBoolean("Angry", isAngry());
            c.setInteger("Direction", direction);
        }

        @Override
        public void readEntityFromNBT(NBTTagCompound c) {
            super.readEntityFromNBT(c);
            setAngry(c.getBoolean("Angry"));
            direction = c.getInteger("Direction");
        }

        @Override protected boolean canDespawn() { return false; }
        @Override protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_VILLAGER_AMBIENT; }
        @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.ENTITY_VILLAGER_HURT; }
        @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_VILLAGER_DEATH; }
        @Override protected float getSoundPitch() { return super.getSoundPitch() * -1.0F; }

        @Override
        public EntityAgeable createChild(EntityAgeable a) {
            TravellingDwarf d = new TravellingDwarf(world);
            d.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(d)), null);
            return d;
        }

        @Override public boolean canBeLeashedTo(EntityPlayer p) { return false; }

        @Override
        public void setAttackTarget(@Nullable EntityLivingBase e) {
            super.setAttackTarget(e);
            setAngry(true);
        }

        public boolean isAngry() { return (dataManager.get(FLAGS) & 2) != 0; }
        public void setAngry(boolean b) { byte f = dataManager.get(FLAGS); dataManager.set(FLAGS, (byte) (b ? f | 2 : f & -3)); }
        @Override public boolean attackEntityAsMob(Entity e) { return attackAsMob(this, e); }
        public void setCustomer(@Nullable EntityPlayer p) { listeningPlayer = p; }
        @Override @Nullable public EntityPlayer getCustomer() { return listeningPlayer; }
        @Override public boolean isTrading() { return listeningPlayer != null; }
        public int getDirection() { return direction; }
        public void setDirection(int d) { direction = d; }

        @Override
        @Nullable
        public IEntityLivingData onInitialSpawn(DifficultyInstance d, @Nullable IEntityLivingData data) {
            data = super.onInitialSpawn(d, data);
            int i = rand.nextInt(6);
            setDirection(rand.nextInt(4));
            setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(SWContent.Note));
            if (i == 0) {
                setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(SWContent.plateAncient));
                setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(SWContent.legsAncient));
                setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(SWContent.bootsAncient));
            } else if (i == 1) {
                setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
                setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
                setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
            } else if (i == 2) setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(SWContent.helmetAncient));
            setRandomName(this);
            return data;
        }
    }

    // ------------------------------------------------------------------ AI

    /** EntityAITradePlayerDwarf. */
    static class TradePlayer extends EntityAIBase {
        final Dwarf v;
        TradePlayer(Dwarf v) { this.v = v; setMutexBits(5); }

        @Override
        public boolean shouldExecute() {
            if (!v.isEntityAlive() || v.isInWater() || !v.onGround || v.velocityChanged) return false;
            EntityPlayer p = v.getCustomer();
            return p != null && v.getDistanceSq(p) <= 16.0 && p.openContainer instanceof Container;
        }

        @Override public void startExecuting() { v.getNavigator().clearPath(); }
        @Override public void resetTask() { v.setCustomer(null); }
    }

    /** EntityAILookAtTradePlayerDwarf. */
    static class LookAtTradePlayer extends EntityAIWatchClosest {
        final IDwarf m;
        LookAtTradePlayer(EntityLiving e) { super(e, EntityPlayer.class, 8.0F); m = (IDwarf) e; }

        @Override
        public boolean shouldExecute() {
            if (!m.isTrading()) return false;
            closestEntity = m.getCustomer();
            return true;
        }
    }

    /** EntityAIDwarfMate. */
    static class Mate extends EntityAIBase {
        final Dwarf v;
        Dwarf mate;
        int matingTimeout;
        Village village;

        Mate(Dwarf v) { this.v = v; setMutexBits(3); }

        @Override
        public boolean shouldExecute() {
            if (v.getGrowingAge() != 0 || v.getRNG().nextInt(500) != 0) return false;
            village = v.world.getVillageCollection().getNearestVillage(new BlockPos(v), 0);
            if (village == null || !doors()) return false;
            Entity e = v.world.findNearestEntityWithinAABB(Dwarf.class, v.getEntityBoundingBox().grow(8.0, 3.0, 8.0), v);
            if (e == null) return false;
            mate = (Dwarf) e;
            return mate.getGrowingAge() == 0;
        }

        @Override public void startExecuting() { matingTimeout = 300; v.setMating(true); }
        @Override public void resetTask() { village = null; mate = null; v.setMating(false); }
        @Override public boolean shouldContinueExecuting() { return matingTimeout >= 0 && doors() && v.getGrowingAge() == 0; }

        @Override
        public void updateTask() {
            matingTimeout--;
            v.getLookHelper().setLookPositionWithEntity(mate, 10.0F, 30.0F);
            if (v.getDistanceSq(mate) > 2.25) v.getNavigator().tryMoveToEntityLiving(mate, 0.25);
            else if (matingTimeout == 0 && mate.isMating()) {
                Dwarf child = v.func_90012_b(mate);
                mate.setGrowingAge(6000);
                v.setGrowingAge(6000);
                child.setGrowingAge(-24000);
                child.setLocationAndAngles(v.posX, v.posY, v.posZ, 0.0F, 0.0F);
                v.world.spawnEntity(child);
                v.world.setEntityState(child, (byte) 12);
            }
            if (v.getRNG().nextInt(35) == 0) v.world.setEntityState(v, (byte) 12);
        }

        private boolean doors() {
            if (village == null || !village.isMatingSeason()) return false;
            return village.getNumVillagers() < (int) (village.getNumVillageDoors() * 0.35);
        }
    }

    /** EntityAIPlayDwarf. */
    static class Play extends EntityAIBase {
        final Dwarf v;
        final double speed;
        EntityLivingBase target;
        int playTime;

        Play(Dwarf v, double speed) { this.v = v; this.speed = speed; setMutexBits(1); }

        @Override
        public boolean shouldExecute() {
            if (v.getGrowingAge() >= 0 || v.getRNG().nextInt(400) != 0) return false;
            double best = Double.MAX_VALUE;
            for (Dwarf d : v.world.getEntitiesWithinAABB(Dwarf.class, v.getEntityBoundingBox().grow(6.0, 3.0, 6.0))) {
                if (d != v && !d.isPlaying() && d.getGrowingAge() < 0) {
                    double ds = d.getDistanceSq(v);
                    if (ds <= best) { best = ds; target = d; }
                }
            }
            return target != null || RandomPositionGenerator.findRandomTarget(v, 16, 3) != null;
        }

        @Override public boolean shouldContinueExecuting() { return playTime > 0; }
        @Override public void startExecuting() { if (target != null) v.setPlaying(true); playTime = 1000; }
        @Override public void resetTask() { v.setPlaying(false); target = null; }

        @Override
        public void updateTask() {
            playTime--;
            if (target != null) {
                if (v.getDistanceSq(target) > 4.0) v.getNavigator().tryMoveToEntityLiving(target, speed);
            } else if (v.getNavigator().noPath()) {
                Vec3d p = RandomPositionGenerator.findRandomTarget(v, 16, 3);
                if (p != null) v.getNavigator().tryMoveToXYZ(p.x, p.y, p.z, speed);
            }
        }
    }

    /** EntityAITravelling: walks 20 blocks further in its fixed direction each time. */
    static class Travelling extends EntityAIBase {
        final TravellingDwarf e;
        final double speed;
        double x, y, z;

        Travelling(TravellingDwarf e, double speed) { this.e = e; this.speed = speed; setMutexBits(0); }

        @Override
        public boolean shouldExecute() {
            Vec3d t = RandomPositionGenerator.findRandomTarget(e, 40, 7);
            if (t != null) {
                t = switch (e.getDirection()) {
                    case 0 -> t.add(20, 0, 0);
                    case 1 -> t.add(0, 0, 20);
                    case 2 -> t.add(-20, 0, 0);
                    default -> t.add(0, 0, -20);
                };
                Vec3d p = RandomPositionGenerator.findRandomTargetBlockTowards(e, 40, 7, t);
                if (p != null) { x = p.x; y = p.y; z = p.z; }
            }
            return true;
        }

        @Override public boolean shouldContinueExecuting() { return !e.getNavigator().noPath(); }
        @Override public void startExecuting() { e.getNavigator().tryMoveToXYZ(x, y, z, speed); }
    }
}
