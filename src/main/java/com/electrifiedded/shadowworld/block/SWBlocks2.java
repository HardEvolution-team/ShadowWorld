package com.electrifiedded.shadowworld.block;

import com.electrifiedded.archaiclib.v1710.api.block.ArchaicRenderPart;
import com.electrifiedded.archaiclib.v1710.api.block.IArchaicRenderBlock;
import com.electrifiedded.archaiclib.v1710.api.client.IIcon;
import com.electrifiedded.archaiclib.v1710.api.client.IIconRegister;
import com.electrifiedded.archaiclib.crafting.LegacyCraftingManager;
import com.electrifiedded.archaiclib.legacy.world.LegacyFrameTeleporter;
import com.electrifiedded.archaiclib.legacy.world.LegacyPortalBlock;
import com.electrifiedded.shadowworld.SWContent;
import com.electrifiedded.shadowworld.ShadowWorld;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/** Stairs, slabs, fire, portals, fluids and workbenches. */
public final class SWBlocks2 {
    private SWBlocks2() {}

    /** Stairs drawn as two archaic boxes (straight shape only). */
    public static class Stairs extends BlockStairs implements IArchaicRenderBlock {
        private final String tex;
        @SideOnly(Side.CLIENT) private IIcon icon;
        public Stairs(Block base, String tex) { super(base.getDefaultState()); this.tex = tex; useNeighborBrightness = true; }
        @Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister r) { icon = r.registerIcon(tex); }
        @Override @SideOnly(Side.CLIENT) public IIcon getIcon(int side, int key) { return icon; }
        @Override public int getArchaicItemRenderKey(int meta) { return 0; }

        @Override
        @SideOnly(Side.CLIENT)
        public List<ArchaicRenderPart> getArchaicRenderParts(int key) {
            List<ArchaicRenderPart> p = new ArrayList<>();
            boolean top = (key & 4) != 0;
            EnumFacing f = EnumFacing.byIndex(5 - (key & 3));
            double y0 = top ? 0.5 : 0, y1 = top ? 1 : 0.5, sy0 = top ? 0 : 0.5, sy1 = top ? 0.5 : 1;
            p.add(ArchaicRenderPart.box(0, y0, 0, 1, y1, 1, icon));
            double x0 = 0, x1 = 1, z0 = 0, z1 = 1;
            switch (f) {
                case EAST -> x0 = 0.5;
                case WEST -> x1 = 0.5;
                case SOUTH -> z0 = 0.5;
                default -> z1 = 0.5;
            }
            p.add(ArchaicRenderPart.box(x0, sy0, z0, x1, sy1, z1, icon));
            return p;
        }
    }

    /** 1.7 slab: meta bit 8 = upper half. Placing a slab on a matching slab makes the double block. */
    public static class Slab extends SWBlock {
        private final Supplier<Block> doubleSlab;
        public Slab(Material m, SoundType s, String tex, Supplier<Block> doubleSlab) { super(m, s); this.doubleSlab = doubleSlab; tex(tex); opaque = false; useNeighborBrightness = true; }
        @Override public AxisAlignedBB getBoundingBox(IBlockState s, IBlockAccess w, BlockPos p) { return (s.getValue(META) & 8) != 0 ? new AxisAlignedBB(0, 0.5, 0, 1, 1, 1) : new AxisAlignedBB(0, 0, 0, 1, 0.5, 1); }
        @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState s, IBlockAccess w, BlockPos p) { return getBoundingBox(s, w, p); }
        @Override public AxisAlignedBB getArchaicRenderBounds(int key) { return (key & 8) != 0 ? new AxisAlignedBB(0, 0.5, 0, 1, 1, 1) : new AxisAlignedBB(0, 0, 0, 1, 0.5, 1); }
        @Override public int damageDropped(IBlockState s) { return 0; }
        @Override public IBlockState getStateForPlacement(World w, BlockPos p, EnumFacing f, float hx, float hy, float hz, int m, EntityLivingBase pl) {
            boolean top = f == EnumFacing.DOWN || (f != EnumFacing.UP && hy > 0.5F);
            return getStateFromMeta(top ? 8 : 0);
        }
        @Override public boolean onBlockActivated(World w, BlockPos p, IBlockState s, EntityPlayer pl, EnumHand h, EnumFacing f, float x, float y, float z) {
            ItemStack st = pl.getHeldItem(h);
            boolean top = (s.getValue(META) & 8) != 0;
            if (st.getItem() == Item.getItemFromBlock(this) && ((f == EnumFacing.UP && !top) || (f == EnumFacing.DOWN && top))) {
                if (!w.isRemote) {
                    w.setBlockState(p, doubleSlab.get().getDefaultState(), 3);
                    if (!pl.capabilities.isCreativeMode) st.shrink(1);
                    w.playSound(null, p, getSoundType().getPlaceSound(), net.minecraft.util.SoundCategory.BLOCKS, 1F, 0.8F);
                }
                return true;
            }
            return false;
        }
    }

    /** Double slab: full block that drops two halves. */
    public static class DoubleSlab extends SWBlock {
        private final Supplier<Block> single;
        public DoubleSlab(Material m, SoundType s, String tex, Supplier<Block> single) { super(m, s); this.single = single; tex(tex); }
        @Override public Item getItemDropped(IBlockState s, Random r, int f) { return Item.getItemFromBlock(single.get()); }
        @Override public int quantityDropped(Random r) { return 2; }
        @Override public ItemStack getPickBlock(IBlockState s, net.minecraft.util.math.RayTraceResult t, World w, BlockPos p, EntityPlayer pl) { return new ItemStack(single.get()); }
    }

    /** Shadow / Hell fire: burns entities, burns out unless it stands on a fire source; shadow fire lights the portal. */
    public static class Fire extends SWBlock {
        private final boolean shadow;
        public Fire(boolean shadow, String tex) {
            super(Material.FIRE, SoundType.CLOTH);
            this.shadow = shadow;
            tex(tex + "_layer_0");
            renderType = RENDER_CROSS;
            layer = BlockRenderLayer.CUTOUT;
            opaque = false;
            noCollide = true;
            setTickRandomly(true);
            setLightLevel(1.0F);
            drops(() -> net.minecraft.init.Items.AIR, 0, 0);
        }
        @Override public boolean isReplaceable(IBlockAccess w, BlockPos p) { return true; }
        @Override public boolean canPlaceBlockAt(World w, BlockPos p) { return w.getBlockState(p.down()).isTopSolid(); }
        @Override public void onBlockAdded(World w, BlockPos p, IBlockState s) {
            if (shadow && SWContent.ShadowPortal.tryToCreatePortal(w, p)) return;
            if (!canPlaceBlockAt(w, p)) w.setBlockToAir(p);
            else w.scheduleUpdate(p, this, 30 + w.rand.nextInt(10));
        }
        @Override public int tickRate(World w) { return 30; }
        @Override public void updateTick(World w, BlockPos p, IBlockState s, Random r) {
            if (w.getBlockState(p.down()).getBlock().isFireSource(w, p.down(), EnumFacing.UP)) return;
            if (!canPlaceBlockAt(w, p) || r.nextInt(4) == 0) w.setBlockToAir(p);
            else w.scheduleUpdate(p, this, 30 + r.nextInt(10));
        }
        @Override public void neighborChanged(IBlockState s, World w, BlockPos p, Block b, BlockPos f) { if (!canPlaceBlockAt(w, p)) w.setBlockToAir(p); }
        @Override public void onEntityCollision(World w, BlockPos p, IBlockState s, Entity e) {
            e.setFire(shadow ? 4 : 8);
            if (!shadow) e.attackEntityFrom(net.minecraft.util.DamageSource.IN_FIRE, 2F);
        }
    }

    /** Lapis-framed portal to the Shadow World. */
    public static class ShadowPortal extends LegacyPortalBlock {
        public ShadowPortal() { super(() -> net.minecraft.init.Blocks.LAPIS_BLOCK, SWContent::shadowDim); }
    }

    /** Gold-framed portal to the Cookie World. */
    public static class CookiePortal extends LegacyPortalBlock {
        public CookiePortal() { super(() -> net.minecraft.init.Blocks.GOLD_BLOCK, SWContent::cookieDim); }
        public ITeleporter teleporterFor(WorldServer dest) { return new LegacyFrameTeleporter(dest, this, frame()); }
    }

    /** Quicksilver / chocolate milk. Quicksilver poisons whatever swims in it. */
    public static class Fluid2 extends BlockFluidClassic {
        private final boolean poison;
        public Fluid2(Fluid f, boolean poison) { super(f, Material.WATER); this.poison = poison; }
        @Override public void onEntityCollision(World w, BlockPos p, IBlockState s, Entity e) {
            if (!w.isRemote && e instanceof EntityLivingBase l) {
                if (poison) l.addPotionEffect(new PotionEffect(MobEffects.POISON, 10, 4));
                else l.addPotionEffect(new PotionEffect(MobEffects.SPEED, 40, 1));
            }
        }
    }

    /** Shadow workbench (3x3 vanilla recipes) or the 5x5 "Plus" one with the shadow recipes. */
    public static class Workbench extends SWBlock {
        public final boolean plus;
        public Workbench(boolean plus) {
            super(Material.WOOD, SoundType.WOOD);
            this.plus = plus;
            String n = plus ? "ShadowWorkBench+" : "ShadowWorkBench";
            tex(n + "_top", n + "_top", n + "_front", n + "_front", n + "_side", n + "_side");
        }
        @Override public boolean onBlockActivated(World w, BlockPos p, IBlockState s, EntityPlayer pl, EnumHand h, EnumFacing f, float x, float y, float z) {
            if (!w.isRemote) pl.openGui(ShadowWorld.instance, plus ? 1 : 0, w, p.getX(), p.getY(), p.getZ());
            return true;
        }
        public LegacyCraftingManager manager() { return plus ? SWContent.SHADOW_CRAFTING : null; }
    }
}
