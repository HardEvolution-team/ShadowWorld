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
    /** 1.7 AzuriteRune: redstone-wire-like flat rune (AzuriteRuneRenderingHandler); animated when part of a pedestal circle. */
    public static class Rune extends SWBlock {
        static final net.minecraft.block.properties.PropertyBool W = net.minecraft.block.properties.PropertyBool.create("west"),
                E = net.minecraft.block.properties.PropertyBool.create("east"), N = net.minecraft.block.properties.PropertyBool.create("north"),
                S = net.minecraft.block.properties.PropertyBool.create("south"), ACTIVE = net.minecraft.block.properties.PropertyBool.create("active");

        public Rune() {
            super(Material.CIRCUITS, SoundType.GLASS);
            tex("AzuriteRune_cross", "AzuriteRune_line", "AzuriteRune_cross_animated", "AzuriteRune_line_animated", "AzuriteRune_cross_overlay", "AzuriteRune_line_overlay");
            bounds(0, 0, 0, 1, 0.0625, 1);
            noCollide();
            cutout();
        }

        @Override protected net.minecraft.block.state.BlockStateContainer createBlockState() { return new net.minecraft.block.state.BlockStateContainer(this, META, W, E, N, S, ACTIVE); }

        @Override
        public IBlockState getActualState(IBlockState s, IBlockAccess w, BlockPos p) {
            return s.withProperty(W, w.getBlockState(p.west()).getBlock() == this).withProperty(E, w.getBlockState(p.east()).getBlock() == this)
                    .withProperty(N, w.getBlockState(p.north()).getBlock() == this).withProperty(S, w.getBlockState(p.south()).getBlock() == this)
                    .withProperty(ACTIVE, isPartOfCircle(w, p));
        }

        public static boolean isMiddleOfCircle(IBlockAccess w, BlockPos p) {
            int n = 0;
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++)
                    if ((dx != 0 || dz != 0) && w.getBlockState(p.add(dx, 0, dz)).getBlock() == SWContent.AzuriteRune) n++;
            return n == 8;
        }

        public static boolean isPartOfCircle(IBlockAccess w, BlockPos p) {
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos q = p.add(dx, 0, dz);
                    IBlockState s = w.getBlockState(q);
                    if (!s.isNormalCube() && s.getBlock() != SWContent.AzuriteRune && isMiddleOfCircle(w, q)) return true;
                }
            return false;
        }

        @Override public boolean canPlaceBlockAt(World w, BlockPos p) { return w.getBlockState(p.down()).isTopSolid() || w.getBlockState(p.down()).getBlock() == net.minecraft.init.Blocks.GLOWSTONE; }
        @Override
        public void neighborChanged(IBlockState s, World w, BlockPos p, Block b, BlockPos from) {
            if (!w.isRemote && !canPlaceBlockAt(w, p)) { dropBlockAsItem(w, p, s, 0); w.setBlockToAir(p); }
        }
        @Override public float getEnchantPowerBonus(World w, BlockPos p) { return 3.0F; }

        @Override
        public int getArchaicRenderKey(IBlockState s) {
            return (s.getValue(W) ? 1 : 0) | (s.getValue(E) ? 2 : 0) | (s.getValue(N) ? 4 : 0) | (s.getValue(S) ? 8 : 0) | (s.getValue(ACTIVE) ? 16 : 0);
        }
        @Override public int getArchaicItemRenderKey(int meta) { return 0; }

        @Override
        @SideOnly(Side.CLIENT)
        public List<ArchaicRenderPart> getArchaicRenderParts(int key) {
            if (icons == null || icons.length < 6) return null;
            boolean w = (key & 1) != 0, e = (key & 2) != 0, n = (key & 4) != 0, s = (key & 8) != 0, active = (key & 16) != 0;
            IIcon cross = icons[active ? 2 : 0], line = icons[active ? 3 : 1];
            double y = 0.015625;
            List<ArchaicRenderPart> p = new ArrayList<>();
            int mode = (w || e) && !n && !s ? 1 : (n || s) && !e && !w ? 2 : 0;
            if (mode == 0) {
                double x0 = w ? 0 : 0.3125, x1 = e ? 1 : 0.6875, z0 = n ? 0 : 0.3125, z1 = s ? 1 : 0.6875;
                double u0 = x0 * 16, u1 = x1 * 16, v0 = z0 * 16, v1 = z1 * 16;
                for (IIcon ic : new IIcon[]{cross, icons[4]})
                    p.add(ArchaicRenderPart.quad(ic, new double[]{x1, y, z1, u1, v1}, new double[]{x1, y, z0, u1, v0}, new double[]{x0, y, z0, u0, v0}, new double[]{x0, y, z1, u0, v1}).doubleSided());
            } else if (mode == 1) {
                for (IIcon ic : new IIcon[]{line, icons[5]})
                    p.add(ArchaicRenderPart.quad(ic, new double[]{1, y, 1, 16, 16}, new double[]{1, y, 0, 16, 0}, new double[]{0, y, 0, 0, 0}, new double[]{0, y, 1, 0, 16}).doubleSided());
            } else {
                for (IIcon ic : new IIcon[]{line, icons[5]})
                    p.add(ArchaicRenderPart.quad(ic, new double[]{1, y, 1, 16, 16}, new double[]{1, y, 0, 0, 16}, new double[]{0, y, 0, 0, 0}, new double[]{0, y, 1, 16, 0}).doubleSided());
            }
            return p;
        }
    }

    /** 1.7 AngelBlock = BlockQuartz with angel textures: 0 plain, 1 chiseled, 2/3/4 pillar (Y/X/Z). */
    public static class AngelQuartz extends SWBlock {
        public AngelQuartz() {
            super(Material.ROCK, SoundType.STONE);
            tex("angel_block_bottom", "angel_block_top", "angel_block_side", "angel_block_chiseled_top", "angel_block_chiseled", "angel_block_lines_top", "angel_block_lines");
        }
        @Override
        @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
        public com.electrifiedded.archaiclib.v1710.api.client.IIcon getIcon(int side, int meta) {
            if (icons == null || icons.length < 7) return super.getIcon(side, meta);
            return switch (meta) {
                case 1 -> icons[side <= 1 ? 3 : 4];
                case 2 -> icons[side <= 1 ? 5 : 6];
                case 3 -> icons[side >= 4 ? 5 : 6];
                case 4 -> icons[side == 2 || side == 3 ? 5 : 6];
                default -> icons[side == 0 ? 0 : side == 1 ? 1 : 2];
            };
        }
        @Override public int damageDropped(IBlockState s) { int m = getMetaFromState(s); return m >= 2 ? 2 : m; }
    }

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
            // HellFire: a Shadow Crystal thrown into it summons the Angel (1.7 HellFire.onEntityCollidedWithBlock)
            if (!shadow && e instanceof net.minecraft.entity.item.EntityItem ei && ei.getItem().getItem() == com.electrifiedded.shadowworld.SWContent.ShadowCrystal
                    && w.isAirBlock(p.up()) && !w.isRemote && !e.isDead) {
                e.setDead();
                w.setBlockToAir(p);
                w.playSound(null, e.posX, e.posY, e.posZ, net.minecraft.init.SoundEvents.ENTITY_FIREWORK_LAUNCH, net.minecraft.util.SoundCategory.HOSTILE, 3.0F, 1.0F);
                com.electrifiedded.shadowworld.entity.SWAngels.Angel angel = new com.electrifiedded.shadowworld.entity.SWAngels.Angel(w);
                angel.setPosition(p.getX(), p.getY(), p.getZ());
                w.spawnEntity(angel);
                w.createExplosion(angel, p.getX(), p.getY(), p.getZ(), 15.0F, true);
                return;
            }
            if (e instanceof com.electrifiedded.shadowworld.entity.SWAngels.Angel) return;
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
