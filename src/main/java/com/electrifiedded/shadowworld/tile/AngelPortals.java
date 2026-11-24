package com.electrifiedded.shadowworld.tile;

import com.electrifiedded.shadowworld.SWContent;
import com.electrifiedded.shadowworld.world.SWAngelWorld;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

/** 1.7 AngelPortal (falling floating sphere left by the dead Angel), TileEntityAngelPortal and EntityFallingAngelPortal. */
public final class AngelPortals {
    private AngelPortals() {}

    private static final AxisAlignedBB BOX = new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);

    public static class PortalBlock extends Block implements ITileEntityProvider {
        public PortalBlock() {
            super(Material.PORTAL);
            setTickRandomly(true);
            setResistance(6000000.0F);
            setBlockUnbreakable();
            setSoundType(SoundType.GLASS);
            setLightLevel(0.75F);
        }

        @Override public AxisAlignedBB getBoundingBox(IBlockState s, IBlockAccess w, BlockPos p) { return BOX; }
        @Override @Nullable public AxisAlignedBB getCollisionBoundingBox(IBlockState s, IBlockAccess w, BlockPos p) { return NULL_AABB; }
        @Override public boolean isOpaqueCube(IBlockState s) { return false; }
        @Override public boolean isFullCube(IBlockState s) { return false; }
        @Override public EnumBlockRenderType getRenderType(IBlockState s) { return EnumBlockRenderType.INVISIBLE; }
        @Override public BlockFaceShape getBlockFaceShape(IBlockAccess w, IBlockState s, BlockPos p, EnumFacing f) { return BlockFaceShape.UNDEFINED; }
        @Override public int quantityDropped(Random r) { return 0; }
        @Override public ItemStack getPickBlock(IBlockState s, RayTraceResult t, World w, BlockPos p, net.minecraft.entity.player.EntityPlayer pl) { return ItemStack.EMPTY; }
        @Override public TileEntity createNewTileEntity(World w, int meta) { return new TilePortal(); }

        @Override
        public void onEntityCollision(World w, BlockPos p, IBlockState s, Entity e) {
            if (w.isRemote || e.isRiding() || e.isBeingRidden() || !(e instanceof EntityPlayerMP pl)) return;
            if (pl.timeUntilPortal > 0) { pl.timeUntilPortal = 10; return; }
            pl.timeUntilPortal = 10;
            int dest = pl.dimension != SWAngelWorld.angelDim() ? SWAngelWorld.angelDim() : 0;
            pl.changeDimension(dest, new SWAngelWorld.AngelTeleporter());
        }

        // ---- BlockFalling behaviour (1.7 func_149830_m), tick rate 2
        @Override public int tickRate(World w) { return 2; }
        @Override public void onBlockAdded(World w, BlockPos p, IBlockState s) { w.scheduleUpdate(p, this, tickRate(w)); }
        @Override public void neighborChanged(IBlockState s, World w, BlockPos p, Block b, BlockPos from) { w.scheduleUpdate(p, this, tickRate(w)); }

        @Override
        public void updateTick(World w, BlockPos p, IBlockState s, Random r) {
            if (w.isRemote || !canFallBelow(w, p.down()) || p.getY() < 0) return;
            if (net.minecraft.block.BlockFalling.fallInstantly || !w.isAreaLoaded(p.add(-32, -32, -32), p.add(32, 32, 32))) {
                w.setBlockToAir(p);
                BlockPos q = p;
                while (canFallBelow(w, q.down()) && q.getY() > 0) q = q.down();
                if (q.getY() > 0) w.setBlockState(q, getDefaultState());
            } else w.spawnEntity(new EntityFalling(w, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5));
        }

        public static boolean canFallBelow(World w, BlockPos p) {
            IBlockState s = w.getBlockState(p);
            Material m = s.getMaterial();
            return s.getBlock().isAir(s, w, p) || s.getBlock() == Blocks.FIRE || m == Material.WATER || m == Material.LAVA;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void randomDisplayTick(IBlockState s, World w, BlockPos p, Random r) {
            if (r.nextInt(100) == 0)
                w.playSound(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, com.electrifiedded.archaiclib.legacy.sound.LegacyModSounds.get("shadowworld", "AngelPortal"),
                        SoundCategory.BLOCKS, 0.5F, r.nextFloat() * 0.4F + 0.8F, false);
        }
    }

    public static class TilePortal extends TileEntity implements ITickable {
        public int floattime, rotation;

        @Override
        public void update() {
            if (++floattime >= 314.1592653589793) floattime = 0;
            if (++rotation >= 360) rotation = 0;
        }

        @Override public void readFromNBT(NBTTagCompound c) { super.readFromNBT(c); floattime = c.getInteger("floattime"); rotation = c.getInteger("rotation"); }
        @Override public NBTTagCompound writeToNBT(NBTTagCompound c) { super.writeToNBT(c); c.setInteger("floattime", floattime); c.setInteger("rotation", rotation); return c; }
        @Override public NBTTagCompound getUpdateTag() { return writeToNBT(new NBTTagCompound()); }
        @Override public SPacketUpdateTileEntity getUpdatePacket() { return new SPacketUpdateTileEntity(pos, 1, getUpdateTag()); }
        @Override public void onDataPacket(NetworkManager n, SPacketUpdateTileEntity p) { readFromNBT(p.getNbtCompound()); }
        @Override @SideOnly(Side.CLIENT) public AxisAlignedBB getRenderBoundingBox() { return new AxisAlignedBB(pos).grow(2.0); }
    }

    /** EntityFallingAngelPortal: the falling sphere; re-places the portal where it lands. */
    public static class EntityFalling extends Entity {
        public int fallTime;

        public EntityFalling(World w) { super(w); preventEntitySpawning = true; setSize(0.98F, 0.98F); }

        public EntityFalling(World w, double x, double y, double z) {
            this(w);
            setPosition(x, y, z);
            motionX = motionY = motionZ = 0.0;
            prevPosX = x; prevPosY = y; prevPosZ = z;
        }

        @Override protected boolean canTriggerWalking() { return false; }
        @Override protected void entityInit() {}
        @Override public boolean canBeCollidedWith() { return !isDead; }

        @Override
        public void onUpdate() {
            prevPosX = posX; prevPosY = posY; prevPosZ = posZ;
            fallTime++;
            motionY -= 0.04F;
            move(MoverType.SELF, motionX, motionY, motionZ);
            motionX *= 0.98F; motionY *= 0.98F; motionZ *= 0.98F;
            if (world.isRemote) return;
            BlockPos p = new BlockPos(MathHelper.floor(posX), MathHelper.floor(posY), MathHelper.floor(posZ));
            if (fallTime == 1) {
                if (world.getBlockState(p).getBlock() != SWContent.AngelPortal) { setDead(); return; }
                world.setBlockToAir(p);
            }
            if (onGround) {
                motionX *= 0.7F; motionZ *= 0.7F; motionY *= -0.5;
                if (!world.getBlockState(p).getBlock().isReplaceable(world, p) && !PortalBlock.canFallBelow(world, p.down())) p = p.up();
                if (world.getBlockState(p).getBlock() != Blocks.PISTON_EXTENSION) {
                    setDead();
                    world.setBlockState(p, SWContent.AngelPortal.getDefaultState(), 3);
                }
            } else if (fallTime > 100 && (p.getY() < 1 || p.getY() > 256) || fallTime > 600) {
                entityDropItem(new ItemStack(Item.getItemFromBlock(SWContent.AngelPortal)), 0.0F);
                setDead();
            }
        }

        @Override public void fall(float d, float m) {}
        @Override protected void writeEntityToNBT(NBTTagCompound c) { c.setByte("Time", (byte) fallTime); }
        @Override protected void readEntityFromNBT(NBTTagCompound c) { fallTime = c.getByte("Time") & 255; }
        @Override @SideOnly(Side.CLIENT) public boolean canRenderOnFire() { return false; }
    }
}
