package com.electrifiedded.shadowworld.tile;

import com.electrifiedded.shadowworld.SWContent;
import com.electrifiedded.shadowworld.ShadowWorld;
import com.electrifiedded.shadowworld.block.SWBlock;
import com.electrifiedded.archaiclib.v1710.api.client.IIcon;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

/**
 * Soul Furnace (soul-fuelled, 50-tick smelting, lit/unlit block swap) and Golden Soul Furnace
 * (no fuel, 150 ticks, doubles output after the first item) - 1:1 logic of the 1.7 TileEntitySoulFurnace /
 * TileEntityGoldenSoulFurnace. GUI ids 3 / 4 as in the original ShadowGuiHandler.
 */
public final class SoulFurnaces {
    private SoulFurnaces() {}

    public static final int GUI_SOUL = 3, GUI_GOLDEN = 4;
    private static boolean keepInventory;

    // ------------------------------------------------------------------ block

    public static class FurnaceBlock extends SWBlock implements ITileEntityProvider {
        public final boolean lit, golden;

        public FurnaceBlock(boolean lit, boolean golden) {
            super(Material.ROCK, SoundType.STONE);
            this.lit = lit;
            this.golden = golden;
            tex("SoulFurnace_side", golden ? "GoldenSoulFurnace_front" : lit ? "SoulFurnace_front_on" : "SoulFurnace_front_off", "SoulFurnace_top");
            hr(3.5F, 17.5F);
            if (lit) light(0.875F);
            if (lit) drops(() -> net.minecraft.item.Item.getItemFromBlock(SWContent.SoulFurnace), 1, 1);
        }

        @Override public TileEntity createNewTileEntity(World w, int meta) { return new TileSoulFurnace(golden); }

        @Override
        @SideOnly(Side.CLIENT)
        public IIcon getIcon(int side, int meta) {
            if (icons == null || icons.length < 3) return null;
            if (meta == 0) meta = 3; // item: show the front like the 1.7 inventory render
            return side <= 1 ? icons[2] : side == meta ? icons[1] : icons[0];
        }

        @Override
        public void onBlockAdded(World w, BlockPos p, IBlockState s) {
            super.onBlockAdded(w, p, s);
            if (w.isRemote || meta(w, p) >= 2) return;
            boolean n = w.getBlockState(p.north()).isFullBlock(), so = w.getBlockState(p.south()).isFullBlock();
            boolean we = w.getBlockState(p.west()).isFullBlock(), e = w.getBlockState(p.east()).isFullBlock();
            int f = 3;
            if (n && !so) f = 3;
            if (so && !n) f = 2;
            if (we && !e) f = 5;
            if (e && !we) f = 4;
            setMeta(w, p, f, 2);
        }

        @Override
        public void onBlockPlacedBy(World w, BlockPos p, IBlockState s, EntityLivingBase placer, ItemStack stack) {
            int l = MathHelper.floor(placer.rotationYaw * 4.0F / 360.0F + 0.5) & 3;
            setMeta(w, p, new int[]{2, 5, 3, 4}[l], 2);
            TileEntity te = w.getTileEntity(p);
            if (stack.hasDisplayName() && te instanceof TileSoulFurnace f) f.customName = stack.getDisplayName();
        }

        @Override
        public boolean onBlockActivated(World w, BlockPos p, IBlockState s, EntityPlayer pl, EnumHand h, EnumFacing f, float x, float y, float z) {
            if (!w.isRemote && w.getTileEntity(p) instanceof TileSoulFurnace)
                pl.openGui(ShadowWorld.instance, golden ? GUI_GOLDEN : GUI_SOUL, w, p.getX(), p.getY(), p.getZ());
            return true;
        }

        @Override
        public void breakBlock(World w, BlockPos p, IBlockState s) {
            if (!keepInventory) {
                TileEntity te = w.getTileEntity(p);
                if (te instanceof IInventory inv) {
                    InventoryHelper.dropInventoryItems(w, p, inv);
                    w.updateComparatorOutputLevel(p, this);
                }
            }
            super.breakBlock(w, p, s);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void randomDisplayTick(IBlockState s, World w, BlockPos p, Random r) {
            if (!lit) return;
            int l = s.getValue(META);
            float x = p.getX() + 0.5F, y = p.getY() + r.nextFloat() * 6.0F / 16.0F, z = p.getZ() + 0.5F;
            float a = 0.52F, b = r.nextFloat() * 0.6F - 0.3F;
            double px = x, pz = z;
            if (l == 4) px = x - a; else if (l == 5) px = x + a;
            if (l == 4 || l == 5) pz = z + b;
            if (l == 2) pz = z - a; else if (l == 3) pz = z + a;
            if (l == 2 || l == 3) px = x + b;
            if (l >= 2) {
                w.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, px, y, pz, 0, 0, 0);
                w.spawnParticle(EnumParticleTypes.FLAME, px, y, pz, 0, 0, 0);
            }
        }

        public static void setLit(boolean lit, World w, BlockPos p) {
            int meta = meta(w, p);
            TileEntity te = w.getTileEntity(p);
            keepInventory = true;
            Block b = lit ? SWContent.LitSoulFurnace : SWContent.SoulFurnace;
            w.setBlockState(p, b.getStateFromMeta(meta), 3);
            keepInventory = false;
            if (te != null) {
                te.validate();
                w.setTileEntity(p, te);
            }
        }
    }

    // ------------------------------------------------------------------ tile entity

    public static class TileSoulFurnace extends TileEntity implements ISidedInventory, ITickable {
        private static final int[] TOP = {0}, BOTTOM = {2, 1}, SIDES = {1};
        private static final int[] G_TOP = {0}, G_BOTTOM = {1}, G_SIDES = {0};
        public boolean golden;
        private NonNullList<ItemStack> items;
        public int burnTime, currentItemBurnTime, cookTime;
        String customName;

        public TileSoulFurnace() { this(false); }

        public TileSoulFurnace(boolean golden) {
            this.golden = golden;
            items = NonNullList.withSize(golden ? 2 : 3, ItemStack.EMPTY);
        }

        int out() { return golden ? 1 : 2; }
        public int cookLength() { return golden ? 150 : 50; }

        @Override public int getSizeInventory() { return items.size(); }
        @Override public boolean isEmpty() { for (ItemStack s : items) if (!s.isEmpty()) return false; return true; }
        @Override public ItemStack getStackInSlot(int i) { return items.get(i); }
        @Override public ItemStack decrStackSize(int i, int n) { return ItemStackHelper.getAndSplit(items, i, n); }
        @Override public ItemStack removeStackFromSlot(int i) { return ItemStackHelper.getAndRemove(items, i); }

        @Override
        public void setInventorySlotContents(int i, ItemStack s) {
            items.set(i, s);
            if (s.getCount() > getInventoryStackLimit()) s.setCount(getInventoryStackLimit());
        }

        @Override public String getName() { return hasCustomName() ? customName : golden ? "Golden Soul Furnace" : "Soul Furnace"; }
        @Override public boolean hasCustomName() { return customName != null && !customName.isEmpty(); }
        @Override public ITextComponent getDisplayName() { return new TextComponentString(getName()); }

        @Override
        public void readFromNBT(NBTTagCompound c) {
            super.readFromNBT(c);
            golden = c.getBoolean("Golden");
            items = NonNullList.withSize(golden ? 2 : 3, ItemStack.EMPTY);
            ItemStackHelper.loadAllItems(c, items);
            burnTime = c.getShort("BurnTime");
            cookTime = c.getShort("CookTime");
            currentItemBurnTime = golden ? 0 : getItemBurnTime(items.get(1));
            if (c.hasKey("CustomName", 8)) customName = c.getString("CustomName");
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound c) {
            super.writeToNBT(c);
            c.setBoolean("Golden", golden);
            c.setShort("BurnTime", (short) burnTime);
            c.setShort("CookTime", (short) cookTime);
            ItemStackHelper.saveAllItems(c, items);
            if (hasCustomName()) c.setString("CustomName", customName);
            return c;
        }

        @Override public int getInventoryStackLimit() { return 64; }

        public int getCookProgressScaled(int n) { return cookTime * n / cookLength(); }

        public int getBurnTimeRemainingScaled(int n) {
            if (currentItemBurnTime == 0) currentItemBurnTime = 50;
            return burnTime * n / currentItemBurnTime;
        }

        public boolean isBurning() { return burnTime > 0; }

        @Override
        public void update() {
            if (golden) { updateGolden(); return; }
            boolean wasBurning = burnTime > 0, dirty = false;
            if (burnTime > 0) burnTime--;
            if (!world.isRemote) {
                if (burnTime == 0 && canSmelt()) {
                    currentItemBurnTime = burnTime = getItemBurnTime(items.get(1));
                    if (burnTime > 0) {
                        dirty = true;
                        ItemStack fuel = items.get(1);
                        if (!fuel.isEmpty()) {
                            net.minecraft.item.Item it = fuel.getItem();
                            fuel.shrink(1);
                            if (fuel.isEmpty()) items.set(1, it.getContainerItem(fuel));
                        }
                    }
                }
                if (isBurning() && canSmelt()) {
                    if (++cookTime == 50) {
                        cookTime = 0;
                        smeltItem();
                        dirty = true;
                    }
                } else cookTime = 0;
                if (wasBurning != burnTime > 0) {
                    dirty = true;
                    FurnaceBlock.setLit(burnTime > 0, world, pos);
                }
            }
            if (dirty) markDirty();
        }

        private void updateGolden() {
            if (!world.isRemote) {
                if (canSmelt()) {
                    if (++cookTime == 150) {
                        cookTime = 0;
                        smeltItem();
                    }
                } else cookTime = 0;
            }
            markDirty();
        }

        private boolean canSmelt() {
            ItemStack in = items.get(0);
            if (in.isEmpty()) return false;
            ItemStack r = FurnaceRecipes.instance().getSmeltingResult(in);
            if (r.isEmpty()) return false;
            ItemStack o = items.get(out());
            if (o.isEmpty()) return true;
            if (!o.isItemEqual(r)) return false;
            int n = o.getCount() + r.getCount();
            return n <= getInventoryStackLimit() && n <= o.getMaxStackSize();
        }

        public void smeltItem() {
            if (!canSmelt()) return;
            ItemStack r = FurnaceRecipes.instance().getSmeltingResult(items.get(0));
            ItemStack o = items.get(out());
            if (o.isEmpty()) items.set(out(), r.copy());
            else if (o.getItem() == r.getItem()) o.grow(golden ? r.getCount() * 2 : r.getCount()); // original quirk: golden doubles only when stacking
            items.get(0).shrink(1);
        }

        public static int getItemBurnTime(ItemStack s) {
            return !s.isEmpty() && s.getItem() == SWContent.Soul ? 5000 : 0;
        }

        public static boolean isItemFuel(ItemStack s) { return s.getItem() == SWContent.Soul; }

        @Override
        public boolean isUsableByPlayer(EntityPlayer p) {
            return world.getTileEntity(pos) == this && p.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
        }

        @Override public void openInventory(EntityPlayer p) {}
        @Override public void closeInventory(EntityPlayer p) {}

        @Override
        public boolean isItemValidForSlot(int i, ItemStack s) {
            if (golden) return i != 2; // original check (slot 2 doesn't exist) -> everything goes
            return i != 2 && (i != 1 || isItemFuel(s));
        }

        @Override
        public int[] getSlotsForFace(EnumFacing f) {
            if (golden) return f == EnumFacing.DOWN ? G_BOTTOM : f == EnumFacing.UP ? G_TOP : G_SIDES;
            return f == EnumFacing.DOWN ? BOTTOM : f == EnumFacing.UP ? TOP : SIDES;
        }

        @Override public boolean canInsertItem(int i, ItemStack s, EnumFacing f) { return isItemValidForSlot(i, s); }
        @Override public boolean canExtractItem(int i, ItemStack s, EnumFacing f) { return f != EnumFacing.DOWN || i != 1 || golden || s.getItem() == Items.BUCKET; }

        @Override public int getField(int id) { return id == 0 ? burnTime : id == 1 ? currentItemBurnTime : id == 2 ? cookTime : 0; }

        @Override
        public void setField(int id, int v) {
            if (id == 0) burnTime = v; else if (id == 1) currentItemBurnTime = v; else if (id == 2) cookTime = v;
        }

        @Override public int getFieldCount() { return 3; }
        @Override public void clear() { items.clear(); }

        @Override
        public boolean shouldRefresh(World w, BlockPos p, IBlockState o, IBlockState n) {
            return !(o.getBlock() instanceof FurnaceBlock && n.getBlock() instanceof FurnaceBlock);
        }
    }

    // ------------------------------------------------------------------ container

    public static class ContainerSoulFurnace extends Container {
        public final TileSoulFurnace tile;
        private int lastCook, lastBurn, lastItemBurn;

        public ContainerSoulFurnace(InventoryPlayer inv, TileSoulFurnace te) {
            tile = te;
            if (te.golden) {
                addSlotToContainer(new Slot(te, 0, 56, 35));
                addSlotToContainer(new SlotFurnaceOutput(inv.player, te, 1, 116, 35));
            } else {
                addSlotToContainer(new Slot(te, 0, 56, 17));
                addSlotToContainer(new Slot(te, 1, 56, 53));
                addSlotToContainer(new SlotFurnaceOutput(inv.player, te, 2, 116, 35));
            }
            for (int i = 0; i < 3; i++) for (int j = 0; j < 9; j++) addSlotToContainer(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            for (int k = 0; k < 9; k++) addSlotToContainer(new Slot(inv, k, 8 + k * 18, 142));
        }

        @Override
        public void addListener(IContainerListener l) {
            super.addListener(l);
            l.sendAllWindowProperties(this, tile);
        }

        @Override
        public void detectAndSendChanges() {
            super.detectAndSendChanges();
            for (IContainerListener l : listeners) {
                if (lastCook != tile.cookTime) l.sendWindowProperty(this, 2, tile.cookTime);
                if (lastBurn != tile.burnTime) l.sendWindowProperty(this, 0, tile.burnTime);
                if (lastItemBurn != tile.currentItemBurnTime) l.sendWindowProperty(this, 1, tile.currentItemBurnTime);
            }
            lastCook = tile.cookTime;
            lastBurn = tile.burnTime;
            lastItemBurn = tile.currentItemBurnTime;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void updateProgressBar(int id, int v) { tile.setField(id, v); }

        @Override public boolean canInteractWith(EntityPlayer p) { return tile.isUsableByPlayer(p); }

        @Override
        public ItemStack transferStackInSlot(EntityPlayer p, int index) {
            int n = tile.getSizeInventory(), out = n - 1;
            ItemStack copy = ItemStack.EMPTY;
            Slot slot = inventorySlots.get(index);
            if (slot == null || !slot.getHasStack()) return copy;
            ItemStack s = slot.getStack();
            copy = s.copy();
            if (index == out) {
                if (!mergeItemStack(s, n, n + 36, true)) return ItemStack.EMPTY;
                slot.onSlotChange(s, copy);
            } else if (index >= n) {
                if (!FurnaceRecipes.instance().getSmeltingResult(s).isEmpty()) {
                    if (!mergeItemStack(s, 0, 1, false)) return ItemStack.EMPTY;
                } else if (!tile.golden && TileSoulFurnace.isItemFuel(s)) {
                    if (!mergeItemStack(s, 1, 2, false)) return ItemStack.EMPTY;
                } else if (index < n + 27) {
                    if (!mergeItemStack(s, n + 27, n + 36, false)) return ItemStack.EMPTY;
                } else if (!mergeItemStack(s, n, n + 27, false)) return ItemStack.EMPTY;
            } else if (!mergeItemStack(s, n, n + 36, false)) return ItemStack.EMPTY;
            if (s.isEmpty()) slot.putStack(ItemStack.EMPTY); else slot.onSlotChanged();
            if (s.getCount() == copy.getCount()) return ItemStack.EMPTY;
            slot.onTake(p, s);
            return copy;
        }
    }
}
