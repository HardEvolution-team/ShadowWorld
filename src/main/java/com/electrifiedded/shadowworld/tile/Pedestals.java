package com.electrifiedded.shadowworld.tile;

import com.electrifiedded.shadowworld.SWContent;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pedestal crafting of the 1.7 mod: a middle pedestal with a Shadow Crystal and two "active" pedestals
 * (crystal under open sky) two blocks away on one axis holding the two ingredients. 100 ticks of charging,
 * then the result appears - or lightning destroys it (instability %), unless the middle pedestal is
 * surrounded by 8 Azurite Runes. Taking an item while charging blows the pedestal up (power 15).
 */
public final class Pedestals {
    private Pedestals() {}

    // ------------------------------------------------------------------ recipes (Handlers.PedestalCrafting)

    public static final class Recipe {
        public final ItemStack output;
        public final List<ItemStack> inputs;
        public final int instability;

        Recipe(ItemStack out, int instability, List<ItemStack> in) { output = out; this.instability = instability; inputs = in; }

        public boolean matches(ItemStack a, ItemStack b) {
            List<ItemStack> left = new ArrayList<>(inputs);
            for (ItemStack s : new ItemStack[]{a, b}) {
                if (a.isEmpty()) continue; // original checks the first stack each time (bug kept harmlessly)
                boolean found = false;
                for (ItemStack r : left) {
                    if (s.getItem() == r.getItem() && (r.getMetadata() == 32767 || s.getMetadata() == r.getMetadata())) {
                        left.remove(r);
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }
            return left.isEmpty();
        }
    }

    public static final List<Recipe> RECIPES = new ArrayList<>();

    public static void addRecipe(ItemStack out, int instability, Object... in) {
        if (in.length > 2) throw new IllegalArgumentException("A pedestal recipe takes at most two items");
        List<ItemStack> l = new ArrayList<>();
        for (Object o : in) {
            if (o instanceof ItemStack s) l.add(s.copy());
            else if (o instanceof Item i) l.add(new ItemStack(i));
            else if (o instanceof Block b) l.add(new ItemStack(b));
            else throw new IllegalArgumentException("Invalid pedestal recipe input " + o);
        }
        RECIPES.add(new Recipe(out, instability, l));
    }

    public static ItemStack findMatchingRecipe(ItemStack a, ItemStack b) {
        for (Recipe r : RECIPES) if (r.matches(a, b)) return r.output.copy();
        return ItemStack.EMPTY;
    }

    public static int getInstability(ItemStack s) {
        for (Recipe r : RECIPES)
            if (s.getItem() == r.output.getItem() && (r.output.getMetadata() == 32767 || s.getMetadata() == r.output.getMetadata()))
                return r.instability;
        return 100;
    }

    /** ShadowWorld.load() pedestal recipes. */
    public static void registerRecipes() {
        addRecipe(new ItemStack(SWContent.Teleporter), 10, SWContent.GreenCrystal, SWContent.WoodStaff);
        addRecipe(new ItemStack(SWContent.Confuser), 10, SWContent.RedCrystal, SWContent.WoodStaff);
        addRecipe(new ItemStack(SWContent.Controller), 10, SWContent.YellowCrystal, SWContent.WoodStaff);
        addRecipe(new ItemStack(SWContent.Grower), 10, SWContent.WhiteCrystal, SWContent.WoodStaff);
    }

    // ------------------------------------------------------------------ block

    public static class PedestalBlock extends Block implements ITileEntityProvider {
        private static final AxisAlignedBB AABB = new AxisAlignedBB(0.125, 0, 0.125, 0.875, 0.75, 0.875);
        private final Random random = new Random();

        public PedestalBlock() {
            super(Material.ROCK);
            setSoundType(SoundType.STONE);
            setHardness(2F);
            setResistance(10F);
            setLightLevel(1F);
        }

        @Override public boolean isOpaqueCube(IBlockState s) { return false; }
        @Override public boolean isFullCube(IBlockState s) { return false; }
        @Override public AxisAlignedBB getBoundingBox(IBlockState s, net.minecraft.world.IBlockAccess w, BlockPos p) { return AABB; }
        @Override public boolean hasTileEntity(IBlockState s) { return true; }

        @Override public EnumBlockRenderType getRenderType(IBlockState s) { return EnumBlockRenderType.ENTITYBLOCK_ANIMATED; }
        @Override public TileEntity createNewTileEntity(World w, int meta) { return new TilePedestal(); }

        @Override
        public boolean onBlockActivated(World w, BlockPos p, IBlockState s, EntityPlayer pl, EnumHand h, EnumFacing f, float x, float y, float z) {
            if (w.isRemote) return true;
            if (!(w.getTileEntity(p) instanceof TilePedestal te)) return true;
            int k = random.nextInt(4);
            if (te.getStack().isEmpty()) {
                w.playSound(null, p, SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, k > 0 ? 0.6F : 0.5F);
                te.setStack(pl.inventory.decrStackSize(pl.inventory.currentItem, 1));
            } else {
                if (te.isMiddlePedestal && te.craftingticks < 100) {
                    te.result = ItemStack.EMPTY;
                    w.createExplosion(null, p.getX(), p.getY(), p.getZ(), 15.0F, false);
                }
                w.playSound(null, p, SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, k > 0 ? 0.6F : 0.5F);
                dropStack(w, p, te.getStack());
                te.setStack(ItemStack.EMPTY);
            }
            return true;
        }

        @Override
        public void breakBlock(World w, BlockPos p, IBlockState s) {
            if (w.getTileEntity(p) instanceof TilePedestal te) {
                if (te.isMiddlePedestal && te.craftingticks < 100) {
                    te.result = ItemStack.EMPTY;
                    w.createExplosion(null, p.getX(), p.getY(), p.getZ(), 15.0F, true);
                }
                dropStack(w, p, te.getStack());
                w.updateComparatorOutputLevel(p, this);
            }
            super.breakBlock(w, p, s);
        }

        private void dropStack(World w, BlockPos p, ItemStack s) {
            if (s.isEmpty()) return;
            float fx = random.nextFloat() * 0.8F + 0.1F, fy = random.nextFloat() * 0.8F + 0.1F, fz = random.nextFloat() * 0.8F + 0.1F;
            EntityItem ei = new EntityItem(w, p.getX() + fx, p.getY() + fy, p.getZ() + fz, s.copy());
            ei.motionX = random.nextGaussian() * 0.05F;
            ei.motionY = random.nextGaussian() * 0.05F + 0.2F;
            ei.motionZ = random.nextGaussian() * 0.05F;
            w.spawnEntity(ei);
        }
    }

    // ------------------------------------------------------------------ tile entity

    public static class TilePedestal extends TileEntity implements ITickable {
        private ItemStack stack = ItemStack.EMPTY;
        public ItemStack result = ItemStack.EMPTY;
        public int itemage;
        public int craftingticks = 100;
        public boolean isMiddlePedestal, isRunicPedestal, isActivePedestal;
        /** Client-side display entity (1.7 pedestalitem). */
        public EntityItem pedestalitem;

        public ItemStack getStack() { return stack; }

        public void setStack(ItemStack s) {
            craftingticks = 100;
            result = ItemStack.EMPTY;
            stack = s;
            if (!s.isEmpty() && s.getCount() > 1) s.setCount(1);
            pedestalitem = null;
            sync();
        }

        void sync() {
            markDirty();
            if (world != null && !world.isRemote) {
                IBlockState st = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, st, st, 3);
            }
        }

        private TilePedestal at(int dx, int dz) {
            BlockPos q = pos.add(dx, 0, dz);
            if (world.getBlockState(q).getBlock() != SWContent.Pedestal) return null;
            return world.getTileEntity(q) instanceof TilePedestal t ? t : null;
        }

        private boolean isPed(int dx, int dz) { return world.getBlockState(pos.add(dx, 0, dz)).getBlock() == SWContent.Pedestal; }

        private void tryAxis(int dx, int dz) {
            TilePedestal a = at(dx, dz), b = at(-dx, -dz);
            if (!isMiddlePedestal || a == null || b == null) return;
            if (a.isActivePedestal && b.isActivePedestal) return;
            ItemStack s1 = a.stack, s2 = b.stack;
            if (s1.isEmpty() || s2.isEmpty() || findMatchingRecipe(s1, s2).isEmpty()) return;
            if (craftingticks >= 99) {
                result = findMatchingRecipe(s1, s2);
                a.setStack(ItemStack.EMPTY);
                b.setStack(ItemStack.EMPTY);
            }
            craftingticks--;
        }

        @Override
        public void update() {
            isRunicPedestal = false;
            if (!isMiddlePedestal && craftingticks <= 97 && craftingticks >= 5) {
                craftingticks = 100;
                result = ItemStack.EMPTY;
                if (!world.isRemote) world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 15.0F, false);
            }

            tryAxis(0, 2);
            tryAxis(2, 0);

            if (stack.isEmpty()) {
                pedestalitem = null;
                itemage = 0;
                isActivePedestal = false;
                isMiddlePedestal = false;
                craftingticks = 100;
            } else {
                if (itemage >= 360) itemage = 0;
                itemage += 2;
                if (pedestalitem == null && world.isRemote) {
                    pedestalitem = new EntityItem(world);
                    pedestalitem.setItem(stack);
                }
                boolean crystal = stack.getItem() == SWContent.ShadowCrystal;
                boolean ring = isPed(2, 0) && isPed(-2, 0) && isPed(0, 2) && isPed(0, -2);
                if (crystal && world.canBlockSeeSky(pos) && !ring) {
                    isActivePedestal = true;
                    isMiddlePedestal = false;
                } else {
                    boolean xActive = activeAt(2, 0) && activeAt(-2, 0), zActive = activeAt(0, 2) && activeAt(0, -2);
                    isActivePedestal = false;
                    isMiddlePedestal = crystal && ring && (xActive || zActive);
                }
                int runes = 0;
                for (int dx = -1; dx <= 1; dx++)
                    for (int dz = -1; dz <= 1; dz++)
                        if ((dx != 0 || dz != 0) && world.getBlockState(pos.add(dx, 0, dz)).getBlock() == SWContent.AzuriteRune) runes++;
                if (runes == 8) isRunicPedestal = true;
            }

            if (craftingticks < 100) {
                craftingticks--;
                if (isRunicPedestal && world.isRemote) {
                    for (int dx = -1; dx <= 1; dx++)
                        for (int dz = -1; dz <= 1; dz++)
                            if ((dx != 0 || dz != 0) && world.getBlockState(pos.add(dx, 0, dz)).getBlock() == SWContent.AzuriteRune)
                                world.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, pos.getX() + world.rand.nextFloat(), pos.getY(), pos.getZ() + world.rand.nextFloat(), dx, 0.5, dz);
                }
                if (craftingticks <= 0) {
                    if (world.isRemote) return;
                    if (result.isEmpty()) result = new ItemStack(SWContent.ShadowCrystal);
                    if (!isRunicPedestal && getInstability(result) / 100 > world.rand.nextFloat()) {
                        // integer division as in the original: only instability >= 100 can fail
                        world.addWeatherEffect(new EntityLightningBolt(world, pos.getX(), pos.getY() + 1, pos.getZ(), false));
                        craftingticks = 100;
                        result = ItemStack.EMPTY;
                        setStack(ItemStack.EMPTY);
                        return;
                    }
                    ItemStack r = result;
                    setStack(r);
                    world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
                    craftingticks = 100;
                } else if (!world.isRemote && craftingticks % 20 == 0) sync();
            }
        }

        private boolean activeAt(int dx, int dz) {
            TilePedestal t = at(dx, dz);
            return t != null && t.isActivePedestal;
        }

        @Override
        public void readFromNBT(NBTTagCompound c) {
            super.readFromNBT(c);
            itemage = c.getInteger("Age");
            craftingticks = c.getInteger("Ticks");
            NBTTagList l = c.getTagList("Items", 10);
            ItemStack s = l.tagCount() > 0 ? new ItemStack(l.getCompoundTagAt(0)) : ItemStack.EMPTY;
            if (!ItemStack.areItemStacksEqual(s, stack)) pedestalitem = null;
            stack = s;
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound c) {
            super.writeToNBT(c);
            c.setInteger("Age", itemage);
            c.setInteger("Ticks", craftingticks);
            NBTTagList l = new NBTTagList();
            if (!stack.isEmpty()) l.appendTag(stack.writeToNBT(new NBTTagCompound()));
            c.setTag("Items", l);
            return c;
        }

        @Override public NBTTagCompound getUpdateTag() { return writeToNBT(new NBTTagCompound()); }
        @Override public SPacketUpdateTileEntity getUpdatePacket() { return new SPacketUpdateTileEntity(pos, 1, getUpdateTag()); }
        @Override public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) { readFromNBT(pkt.getNbtCompound()); }

        @Override
        @SideOnly(Side.CLIENT)
        public double getMaxRenderDistanceSquared() { return 65536.0; }

        @Override
        @SideOnly(Side.CLIENT)
        public AxisAlignedBB getRenderBoundingBox() {
            return !isActivePedestal && !isMiddlePedestal ? super.getRenderBoundingBox()
                    : new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, 512.0, pos.getZ() + 1.0);
        }
    }
}
