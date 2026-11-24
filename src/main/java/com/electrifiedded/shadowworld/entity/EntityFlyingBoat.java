package com.electrifiedded.shadowworld.entity;

import com.electrifiedded.shadowworld.SWContent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * 1.7 EntityFlyingBoat: a hovering boat steered by the rider's look (pitch > 20 deg climbs / dives),
 * mows plants and snow in its way, and can carry an Angel Chest (27 slots) or an Ender Chest.
 * Server-authoritative like the 1.7 boat; the client interpolates.
 */
public class EntityFlyingBoat extends Entity implements IInventory {
    private static final DataParameter<Integer> TIME_SINCE_HIT = EntityDataManager.createKey(EntityFlyingBoat.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> FORWARD = EntityDataManager.createKey(EntityFlyingBoat.class, DataSerializers.VARINT);
    private static final DataParameter<Float> DAMAGE = EntityDataManager.createKey(EntityFlyingBoat.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> HAS_CHEST = EntityDataManager.createKey(EntityFlyingBoat.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HAS_ENDER = EntityDataManager.createKey(EntityFlyingBoat.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> USERS = EntityDataManager.createKey(EntityFlyingBoat.class, DataSerializers.VARINT);

    private double speedMultiplier = 0.035;
    private int lerpSteps;
    private double lerpX, lerpY, lerpZ, lerpYaw, lerpPitch;
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    public float lidAngle, prevLidAngle;
    public int numPlayersUsing;
    private int ticksSinceSync;
    public final BoatEnderChest enderchest = new BoatEnderChest(this);

    public EntityFlyingBoat(World w) {
        super(w);
        preventEntitySpawning = true;
        setSize(2.75F, 0.6F);
    }

    public EntityFlyingBoat(World w, double x, double y, double z) {
        this(w);
        setPosition(x, y, z);
        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;
    }

    @Override
    protected void entityInit() {
        dataManager.register(TIME_SINCE_HIT, 0);
        dataManager.register(FORWARD, 1);
        dataManager.register(DAMAGE, 0.0F);
        dataManager.register(HAS_CHEST, false);
        dataManager.register(HAS_ENDER, false);
        dataManager.register(USERS, 0);
    }

    @Override public ItemStack getPickedResult(RayTraceResult t) { return new ItemStack(SWContent.FlyingBoat); }
    @Override protected boolean canTriggerWalking() { return false; }
    @Override public AxisAlignedBB getCollisionBox(Entity e) { return e.canBePushed() ? e.getEntityBoundingBox() : null; }
    @Override public AxisAlignedBB getCollisionBoundingBox() { return getEntityBoundingBox(); }
    @Override public boolean canBePushed() { return true; }
    @Override public boolean canBeCollidedWith() { return !isDead; }
    @Override public double getMountedYOffset() { return -0.3F + 0.25; }
    @Override public void fall(float d, float m) {}
    @Override protected void updateFallState(double y, boolean onGround, IBlockState s, BlockPos p) {}

    // ------------------------------------------------------------------ damage

    @Override
    public boolean attackEntityFrom(DamageSource src, float amount) {
        if (isEntityInvulnerable(src)) return false;
        if (world.isRemote || isDead) return true;
        setForwardDirection(-getForwardDirection());
        setTimeSinceHit(10);
        setDamageTaken(getDamageTaken() + amount * 10.0F);
        markVelocityChanged();
        boolean creative = src.getTrueSource() instanceof EntityPlayer p && p.capabilities.isCreativeMode;
        if (creative || getDamageTaken() > 40.0F) {
            removePassengers();
            if (!creative) {
                entityDropItem(new ItemStack(SWContent.FlyingBoat), 0.0F);
                if (getHasChest()) {
                    drop(new ItemStack(SWContent.AngelChest));
                    for (ItemStack s : items) if (!s.isEmpty()) drop(s.copy());
                    items.clear();
                } else if (getHasEnderChest()) drop(new ItemStack(Blocks.ENDER_CHEST));
            }
            setDead();
        }
        return true;
    }

    private void drop(ItemStack s) {
        EntityItem ei = new EntityItem(world, posX + rand.nextFloat() * 0.8F + 0.1F, posY + rand.nextFloat() * 0.8F + 0.1F, posZ + rand.nextFloat() * 0.8F + 0.1F, s);
        ei.motionX = rand.nextGaussian() * 0.05F;
        ei.motionY = rand.nextGaussian() * 0.05F + 0.2F;
        ei.motionZ = rand.nextGaussian() * 0.05F;
        world.spawnEntity(ei);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void performHurtAnimation() {
        setForwardDirection(-getForwardDirection());
        setTimeSinceHit(10);
        setDamageTaken(getDamageTaken() * 11.0F);
    }

    // ------------------------------------------------------------------ movement

    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
        lerpX = x;
        lerpY = y;
        lerpZ = z;
        lerpYaw = yaw;
        lerpPitch = pitch;
        lerpSteps = steps + 2;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        ticksSinceSync++;
        if (!world.isRemote && getNumPlayersUsing() != 0 && (ticksSinceSync + posX + posY + posZ) % 200.0 == 0.0) recountUsers();
        prevLidAngle = lidAngle;
        if (getNumPlayersUsing() > 0 && lidAngle == 0.0F)
            world.playSound(null, posX + 0.5, posY + 0.5, posZ + 0.5, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.NEUTRAL, 0.5F, rand.nextFloat() * 0.1F + 0.9F);
        if (getNumPlayersUsing() == 0 && lidAngle > 0.0F || getNumPlayersUsing() > 0 && lidAngle < 1.0F) {
            float old = lidAngle;
            lidAngle = Math.min(1.0F, lidAngle + (getNumPlayersUsing() > 0 ? 0.1F : -0.1F));
            if (lidAngle < 0.5F && old >= 0.5F)
                world.playSound(null, posX + 0.5, posY + 0.5, posZ + 0.5, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.NEUTRAL, 0.5F, rand.nextFloat() * 0.1F + 0.9F);
            if (lidAngle < 0.0F) lidAngle = 0.0F;
        }
        if (getTimeSinceHit() > 0) setTimeSinceHit(getTimeSinceHit() - 1);
        if (getDamageTaken() > 0.0F) setDamageTaken(getDamageTaken() - 1.0F);
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        if (world.isRemote) {
            if (lerpSteps > 0) {
                double x = posX + (lerpX - posX) / lerpSteps, y = posY + (lerpY - posY) / lerpSteps, z = posZ + (lerpZ - posZ) / lerpSteps;
                rotationYaw = (float) (rotationYaw + MathHelper.wrapDegrees(lerpYaw - rotationYaw) / lerpSteps);
                rotationPitch = (float) (rotationPitch + (lerpPitch - rotationPitch) / lerpSteps);
                lerpSteps--;
                setPosition(x, y, z);
                setRotation(rotationYaw, rotationPitch);
            } else {
                setPosition(posX + motionX, posY + motionY, posZ + motionZ);
                motionX *= 0.99F;
                motionZ *= 0.99F;
            }
            return;
        }

        double speedBefore = Math.sqrt(motionX * motionX + motionZ * motionZ);
        Entity rider = getControllingRider();
        if (rider instanceof EntityLivingBase l) {
            float yaw = rider.rotationYaw + -l.moveStrafing * 90.0F;
            motionX += -Math.sin(yaw * (float) Math.PI / 180.0F) * speedMultiplier * l.moveForward * 0.05F;
            motionZ += Math.cos(yaw * (float) Math.PI / 180.0F) * speedMultiplier * l.moveForward * 0.05F;
            if (!(l.rotationPitch > 20.0F) && !(l.rotationPitch < -20.0F)) motionY *= 0.890000000043603;
            else motionY = l.rotationPitch * -0.05F * (speedMultiplier / 4.0) * l.moveForward;
        }
        double speed = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (speed > 0.35) {
            double d = 0.35 / speed;
            motionX *= d;
            motionZ *= d;
            speed = 0.35;
        }
        if (speed > speedBefore && speedMultiplier < 0.95) speedMultiplier = Math.min(0.95, speedMultiplier + (0.95 - speedMultiplier) / 95.0);
        else speedMultiplier = Math.max(0.035, speedMultiplier - (speedMultiplier - 0.035) / 39.0);

        for (int l = 0; l < 25; l++) {
            int bx = MathHelper.floor(posX + (l % 5 - 0.5) * 0.8), bz = MathHelper.floor(posZ + (l / 5 - 0.5) * 0.8);
            for (int dy = 0; dy < 5; dy++) {
                BlockPos bp = new BlockPos(bx, MathHelper.floor(posY) + dy, bz);
                IBlockState st = world.getBlockState(bp);
                Block b = st.getBlock();
                if (b == Blocks.SNOW_LAYER) {
                    world.setBlockToAir(bp);
                    collidedHorizontally = false;
                } else if (st.getMaterial() == Material.PLANTS || b instanceof BlockBush) {
                    b.dropBlockAsItem(world, bp, st, 0);
                    world.setBlockToAir(bp);
                    collidedHorizontally = false;
                }
            }
        }
        move(net.minecraft.entity.MoverType.SELF, motionX, motionY, motionZ);
        if (!collidedHorizontally || !(speedBefore > 0.2)) {
            motionX *= 0.99F;
            motionZ *= 0.99F;
        }
        rotationPitch = 0.0F;
        double yaw = rotationYaw, dx = prevPosX - posX, dz = prevPosZ - posZ;
        if (dx * dx + dz * dz > 0.001) yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI);
        double turn = MathHelper.clamp(MathHelper.wrapDegrees(yaw - rotationYaw), -20.0, 20.0);
        rotationYaw = (float) (rotationYaw + turn);
        setRotation(rotationYaw, rotationPitch);
        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().grow(0.2F, 0.0, 0.2F));
        for (Entity e : list) if (!isPassenger(e) && e.canBePushed() && e instanceof EntityFlyingBoat) e.applyEntityCollision(this);
    }

    private Entity getControllingRider() { return getPassengers().isEmpty() ? null : getPassengers().get(0); }

    @Override
    public void updatePassenger(Entity p) {
        if (!isPassenger(p)) return;
        double dx = Math.cos(rotationYaw * Math.PI / 180.0) * 0.4, dz = Math.sin(rotationYaw * Math.PI / 180.0) * 0.4;
        p.setPosition(posX + dx, posY + getMountedYOffset() + 0.25 + p.getYOffset(), posZ + dz);
    }

    @Override protected boolean canFitPassenger(Entity p) { return getPassengers().isEmpty(); }

    // ------------------------------------------------------------------ interaction

    @Override
    public boolean processInitialInteract(EntityPlayer p, EnumHand hand) {
        ItemStack held = p.getHeldItem(hand);
        if (p.isSneaking()) {
            if (!world.isRemote) openInventory(p, getHasEnderChest() && !getHasChest());
            return true;
        }
        if (held.getItem() == Item.getItemFromBlock(SWContent.AngelChest) && !getHasChest() && !getHasEnderChest()) {
            setHasChest(true);
            if (!p.capabilities.isCreativeMode) held.shrink(1);
            return true;
        }
        if (held.getItem() == Item.getItemFromBlock(Blocks.ENDER_CHEST) && !getHasChest() && !getHasEnderChest()) {
            setHasEnderChest(true);
            if (!p.capabilities.isCreativeMode) held.shrink(1);
            return true;
        }
        if (!getPassengers().isEmpty() && getPassengers().get(0) instanceof EntityPlayer && !isPassenger(p)) return true;
        if (!world.isRemote) p.startRiding(this);
        return true;
    }

    /** SW|FlyingBoat / SW|FlyingBoatEnder and the sneak-click. */
    public static void openInventory(EntityPlayer p, boolean ender) {
        EntityFlyingBoat boat = p.getRidingEntity() instanceof EntityFlyingBoat b ? b : null;
        if (boat == null) {
            List<EntityFlyingBoat> near = p.world.getEntitiesWithinAABB(EntityFlyingBoat.class, p.getEntityBoundingBox().grow(4.0));
            if (near.isEmpty()) return;
            boat = near.get(0);
        }
        if (!ender && boat.getHasChest()) p.displayGUIChest(boat);
        else if (boat.getHasEnderChest()) {
            InventoryEnderChest inv = p.getInventoryEnderChest();
            inv.setChestTileEntity(boat.enderchest);
            p.displayGUIChest(inv);
            boat.openInventory(p);
        }
    }

    private void recountUsers() {
        numPlayersUsing = 0;
        for (EntityPlayer pl : world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(posX - 5, posY - 5, posZ - 5, posX + 6, posY + 6, posZ + 6))) {
            Container c = pl.openContainer;
            if (c instanceof ContainerChest cc && cc.getLowerChestInventory() == this) numPlayersUsing++;
        }
        dataManager.set(USERS, numPlayersUsing);
    }

    // ------------------------------------------------------------------ NBT

    @Override
    protected void writeEntityToNBT(NBTTagCompound c) {
        ItemStackHelper.saveAllItems(c, items);
        c.setBoolean("hasChest", getHasChest());
        c.setBoolean("hasEnderChest", getHasEnderChest());
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound c) {
        setHasChest(c.getBoolean("hasChest"));
        setHasEnderChest(c.getBoolean("hasEnderChest"));
        items = NonNullList.withSize(27, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(c, items);
    }

    // ------------------------------------------------------------------ data

    public void setDamageTaken(float f) { dataManager.set(DAMAGE, f); }
    public float getDamageTaken() { return dataManager.get(DAMAGE); }
    public void setTimeSinceHit(int i) { dataManager.set(TIME_SINCE_HIT, i); }
    public int getTimeSinceHit() { return dataManager.get(TIME_SINCE_HIT); }
    public void setForwardDirection(int i) { dataManager.set(FORWARD, i); }
    public int getForwardDirection() { return dataManager.get(FORWARD); }
    public boolean getHasChest() { return dataManager.get(HAS_CHEST); }
    public void setHasChest(boolean b) { dataManager.set(HAS_CHEST, b); }
    public boolean getHasEnderChest() { return dataManager.get(HAS_ENDER); }
    public void setHasEnderChest(boolean b) { dataManager.set(HAS_ENDER, b); }
    public int getNumPlayersUsing() { return dataManager.get(USERS); }

    // ------------------------------------------------------------------ IInventory

    @Override public int getSizeInventory() { return 27; }
    @Override public boolean isEmpty() { for (ItemStack s : items) if (!s.isEmpty()) return false; return true; }
    @Override public ItemStack getStackInSlot(int i) { return items.get(i); }
    @Override public ItemStack decrStackSize(int i, int n) { return ItemStackHelper.getAndSplit(items, i, n); }
    @Override public ItemStack removeStackFromSlot(int i) { return ItemStackHelper.getAndRemove(items, i); }

    @Override
    public void setInventorySlotContents(int i, ItemStack s) {
        items.set(i, s);
        if (s.getCount() > getInventoryStackLimit()) s.setCount(getInventoryStackLimit());
    }

    @Override public int getInventoryStackLimit() { return 64; }
    @Override public void markDirty() {}
    @Override public boolean isUsableByPlayer(EntityPlayer p) { return !isDead && p.getDistanceSq(this) <= 64.0; }

    @Override
    public void openInventory(EntityPlayer p) {
        if (numPlayersUsing < 0) numPlayersUsing = 0;
        numPlayersUsing++;
        dataManager.set(USERS, numPlayersUsing);
    }

    @Override
    public void closeInventory(EntityPlayer p) {
        numPlayersUsing--;
        dataManager.set(USERS, Math.max(0, numPlayersUsing));
    }

    @Override public boolean isItemValidForSlot(int i, ItemStack s) { return true; }
    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int v) {}
    @Override public int getFieldCount() { return 0; }
    @Override public void clear() { items.clear(); }
    @Override public String getName() { return hasCustomName() ? getCustomNameTag() : "Flying Boat"; }

    /** 1.7 TileEntityFBEnderChest: ender inventory bound to the boat (lid animation, always usable). */
    public static class BoatEnderChest extends TileEntityEnderChest {
        final EntityFlyingBoat boat;

        BoatEnderChest(EntityFlyingBoat b) { boat = b; }

        @Override public void openChest() {}
        @Override public void closeChest() { boat.closeInventory(null); }
        @Override public boolean canBeUsed(EntityPlayer p) { return true; }
    }

    /** Item.FlyingBoat right click: placed on the looked-at block (5 blocks), facing the player. */
    public static boolean place(World w, EntityPlayer p, ItemStack stack) {
        float f1 = p.prevRotationPitch + (p.rotationPitch - p.prevRotationPitch);
        float f2 = p.prevRotationYaw + (p.rotationYaw - p.prevRotationYaw);
        net.minecraft.util.math.Vec3d eye = new net.minecraft.util.math.Vec3d(p.posX, p.posY + p.getEyeHeight(), p.posZ);
        float f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI), f4 = MathHelper.sin(-f2 * 0.017453292F - (float) Math.PI);
        float f5 = -MathHelper.cos(-f1 * 0.017453292F), f6 = MathHelper.sin(-f1 * 0.017453292F);
        net.minecraft.util.math.Vec3d end = eye.add(f4 * f5 * 5.0, f6 * 5.0, f3 * f5 * 5.0);
        RayTraceResult hit = w.rayTraceBlocks(eye, end, true);
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) return false;
        net.minecraft.util.math.Vec3d look = p.getLook(1.0F);
        for (Entity e : w.getEntitiesWithinAABBExcludingEntity(p, p.getEntityBoundingBox().expand(look.x * 5.0, look.y * 5.0, look.z * 5.0).grow(1.0)))
            if (e.canBeCollidedWith() && e.getEntityBoundingBox().grow(e.getCollisionBorderSize()).contains(eye)) return false;
        BlockPos bp = hit.getBlockPos();
        if (w.getBlockState(bp).getBlock() == Blocks.SNOW_LAYER) bp = bp.down();
        EntityFlyingBoat boat = new EntityFlyingBoat(w, bp.getX() + 0.5F, bp.getY() + 1.0F, bp.getZ() + 0.5F);
        boat.rotationYaw = ((MathHelper.floor(p.rotationYaw * 4.0F / 360.0F + 0.5) & 3) - 1) * 90;
        if (!w.getCollisionBoxes(boat, boat.getEntityBoundingBox().grow(-0.1)).isEmpty()) return false;
        if (!w.isRemote) w.spawnEntity(boat);
        if (!p.capabilities.isCreativeMode) stack.shrink(1);
        return true;
    }
}
