package com.electrifiedded.shadowworld.block;

import com.electrifiedded.archaiclib.v1710.api.client.IIcon;
import com.electrifiedded.shadowworld.SWContent;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Behaviour blocks of Shadow World (re-implemented for 1.12). */
public final class SWBlockTypes {
    private SWBlockTypes() {}

    /** Flowers, tall grass, mushrooms, saplings: cross plant that needs a soil block below. */
    public static class Plant extends SWBlock implements IPlantable {
        protected Predicate<Block> soil;
        protected Supplier<WorldGenerator> tree;

        public Plant(Predicate<Block> soil) {
            super(Material.PLANTS, SoundType.PLANT);
            this.soil = soil;
            cross();
            setHardness(0F);
            setTickRandomly(true);
        }

        public Plant tree(Supplier<WorldGenerator> t) { tree = t; return this; }

        public boolean canStay(World w, BlockPos p) { return soil.test(w.getBlockState(p.down()).getBlock()); }
        @Override public boolean canPlaceBlockAt(World w, BlockPos p) { return super.canPlaceBlockAt(w, p) && canStay(w, p); }
        @Override public boolean isReplaceable(IBlockAccess w, BlockPos p) { return tree == null && renderType == RENDER_CROSS && this instanceof TallGrass; }

        @Override
        public void neighborChanged(IBlockState s, World w, BlockPos p, Block b, BlockPos from) {
            if (!canStay(w, p)) { dropBlockAsItem(w, p, s, 0); w.setBlockToAir(p); }
        }

        @Override
        public void updateTick(World w, BlockPos p, IBlockState s, Random r) {
            if (!canStay(w, p)) { dropBlockAsItem(w, p, s, 0); w.setBlockToAir(p); return; }
            if (tree != null && !w.isRemote && w.getLightFromNeighbors(p.up()) >= 9 && r.nextInt(7) == 0) {
                int m = meta(w, p);
                if ((m & 8) == 0) setMeta(w, p, m | 8, 4);
                else grow(w, p, r);
            }
        }

        public boolean grow(World w, BlockPos p, Random r) {
            if (tree == null) return false;
            IBlockState old = w.getBlockState(p);
            w.setBlockState(p, Blocks.AIR.getDefaultState(), 4);
            if (!tree.get().generate(w, r, p)) { w.setBlockState(p, old, 4); return false; }
            return true;
        }

        @Override public int damageDropped(IBlockState s) { return 0; }
        @Override public EnumPlantType getPlantType(IBlockAccess w, BlockPos p) { return EnumPlantType.Plains; }
        @Override public IBlockState getPlant(IBlockAccess w, BlockPos p) { return getDefaultState(); }
    }

    public static class TallGrass extends Plant {
        public TallGrass(Predicate<Block> soil) { super(soil); }
        @Override public Item getItemDropped(IBlockState s, Random r, int f) { return r.nextInt(8) == 0 ? SWContent.Soul : Items.AIR; }
    }

    /** Shadow grass: spreads onto shadow dirt, drops shadow dirt, bonemeal spawns shadow plants. */
    public static class Grass extends SWBlock implements net.minecraft.block.IGrowable {
        public Grass() { super(Material.GRASS, SoundType.PLANT); setTickRandomly(true); tex("grassBottom", "grassTop", "grassSide"); }

        @Override
        public void updateTick(World w, BlockPos p, IBlockState s, Random r) {
            if (w.isRemote || w.getLightFromNeighbors(p.up()) < 3) return;
            for (int l = 0; l < 4; l++) {
                BlockPos q = p.add(r.nextInt(3) - 1, r.nextInt(5) - 3, r.nextInt(3) - 1);
                if (w.isBlockLoaded(q) && w.getBlockState(q).getBlock() == SWContent.ShadowDirt && w.getLightFromNeighbors(q.up()) >= 3
                        && w.getBlockState(q.up()).getLightOpacity(w, q.up()) <= 1)
                    w.setBlockState(q, getDefaultState());
            }
        }

        @Override public Item getItemDropped(IBlockState s, Random r, int f) { return Item.getItemFromBlock(SWContent.ShadowDirt); }
        @Override public boolean canGrow(World w, BlockPos p, IBlockState s, boolean c) { return true; }
        @Override public boolean canUseBonemeal(World w, Random r, BlockPos p, IBlockState s) { return true; }

        @Override
        public void grow(World w, Random r, BlockPos pos, IBlockState s) {
            outer:
            for (int l = 0; l < 128; l++) {
                BlockPos p = pos.up();
                for (int k = 0; k < l / 16; k++) {
                    p = p.add(r.nextInt(3) - 1, (r.nextInt(3) - 1) * r.nextInt(3) / 2, r.nextInt(3) - 1);
                    if (w.getBlockState(p.down()).getBlock() != this || w.getBlockState(p).isNormalCube()) continue outer;
                }
                if (w.isAirBlock(p)) {
                    Block b = r.nextInt(8) != 0 ? SWContent.ShadowTallGrass1 : (r.nextBoolean() ? SWContent.ShadowFlower : SWContent.ShadowFlower2);
                    w.setBlockState(p, b.getDefaultState(), 3);
                }
            }
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void randomDisplayTick(IBlockState s, World w, BlockPos p, Random r) {
            if (r.nextInt(75) == 0)
                w.spawnParticle(EnumParticleTypes.SPELL_INSTANT, p.getX() + r.nextFloat(), p.getY() + 1.1, p.getZ() + r.nextFloat(), 0, -0.05, 0);
        }
    }

    /** Shadow farmland (meta = moisture). */
    public static class Field extends SWBlock {
        public Field() {
            super(Material.GROUND, SoundType.GROUND);
            setTickRandomly(true);
            bounds(0, 0, 0, 1, 0.9375, 1);
            setLightOpacity(255);
            tex("ShadowDirt", "field_dry", "ShadowDirt", "field_wet");
        }

        @Override
        @SideOnly(Side.CLIENT)
        public IIcon getIcon(int side, int key) {
            if (icons == null) return null;
            return side == 1 ? icons[key > 0 ? 3 : 1] : icons[0];
        }

        @Override
        public void updateTick(World w, BlockPos p, IBlockState s, Random r) {
            boolean water = false;
            for (BlockPos q : BlockPos.getAllInBoxMutable(p.add(-4, 0, -4), p.add(4, 1, 4)))
                if (w.getBlockState(q).getMaterial() == Material.WATER) { water = true; break; }
            if (water || w.isRainingAt(p.up())) setMeta(w, p, 7, 2);
            else {
                int m = meta(w, p);
                if (m > 0) setMeta(w, p, m - 1, 2);
                else if (!(w.getBlockState(p.up()).getBlock() instanceof IPlantable)) w.setBlockState(p, SWContent.ShadowDirt.getDefaultState());
            }
        }

        @Override
        public void onFallenUpon(World w, BlockPos p, Entity e, float dist) {
            if (!w.isRemote && w.rand.nextFloat() < dist - 0.5F && (e instanceof EntityPlayer || w.getGameRules().getBoolean("mobGriefing")))
                w.setBlockState(p, SWContent.ShadowDirt.getDefaultState());
            super.onFallenUpon(w, p, e, dist);
        }

        @Override
        public void neighborChanged(IBlockState s, World w, BlockPos p, Block b, BlockPos from) {
            if (w.getBlockState(p.up()).getMaterial().isSolid()) w.setBlockState(p, SWContent.ShadowDirt.getDefaultState());
        }

        @Override public Item getItemDropped(IBlockState s, Random r, int f) { return Item.getItemFromBlock(SWContent.ShadowDirt); }
        @Override public int damageDropped(IBlockState s) { return 0; }
        @Override public boolean canSustainPlant(IBlockState s, IBlockAccess w, BlockPos p, EnumFacing d, IPlantable pl) { return true; }
    }

    /** Soul crop: 8 growth stages, grows on shadow farmland, drops souls. */
    public static class Crop extends Plant implements net.minecraft.block.IGrowable {
        public Crop() {
            super(b -> b == SWContent.Shadowfield);
            renderType = RENDER_CROPS;
            tex("Soul_stage_0", "Soul_stage_1", "Soul_stage_2", "Soul_stage_3");
        }

        @Override
        @SideOnly(Side.CLIENT)
        public IIcon getIcon(int side, int key) {
            if (icons == null) return null;
            int m = key < 7 ? (key == 6 ? 5 : key) >> 1 : 3;
            return icons[m];
        }

        @Override
        public void updateTick(World w, BlockPos p, IBlockState s, Random r) {
            if (!canStay(w, p)) { dropBlockAsItem(w, p, s, 0); w.setBlockToAir(p); return; }
            int m = meta(w, p);
            if (m < 7 && w.getLightFromNeighbors(p.up()) >= 9 && r.nextInt(meta(w, p.down()) > 0 ? 6 : 12) == 0) setMeta(w, p, m + 1, 2);
        }

        @Override
        public void getDrops(net.minecraft.util.NonNullList<ItemStack> drops, IBlockAccess w, BlockPos p, IBlockState s, int fortune) {
            int m = s.getValue(META);
            drops.add(new ItemStack(SWContent.Soul));
            if (m >= 7) {
                Random r = w instanceof World ? ((World) w).rand : new Random();
                for (int i = 0; i < 3 + fortune; i++) if (r.nextInt(15) <= m) drops.add(new ItemStack(SWContent.Soul));
            }
        }

        @Override public boolean canGrow(World w, BlockPos p, IBlockState s, boolean c) { return s.getValue(META) < 7; }
        @Override public boolean canUseBonemeal(World w, Random r, BlockPos p, IBlockState s) { return true; }
        @Override public void grow(World w, Random r, BlockPos p, IBlockState s) { setMeta(w, p, Math.min(7, s.getValue(META) + 2 + r.nextInt(4)), 2); }
    }

    /** Berry bush (meta 1 = berries). */
    public static class Bush extends SWBlock {
        public Bush() {
            super(Material.LEAVES, SoundType.PLANT);
            setTickRandomly(true);
            cutout();
            tex("ShadowBush1", "ShadowBush2");
        }

        @Override
        @SideOnly(Side.CLIENT)
        public IIcon getIcon(int side, int key) { return icons == null ? null : icons[key == 0 ? 0 : 1]; }

        private boolean canStay(World w, BlockPos p) {
            Block b = w.getBlockState(p.down()).getBlock();
            return b == SWContent.ShadowGrass || b == SWContent.ShadowDirt || b == this || b == SWContent.ShadowLeaf;
        }

        @Override public boolean canPlaceBlockAt(World w, BlockPos p) { return super.canPlaceBlockAt(w, p) && canStay(w, p); }
        @Override public void neighborChanged(IBlockState s, World w, BlockPos p, Block b, BlockPos f) { if (!canStay(w, p)) w.destroyBlock(p, true); }
        @Override public int damageDropped(IBlockState s) { return 0; }

        @Override
        public boolean onBlockActivated(World w, BlockPos p, IBlockState s, EntityPlayer pl, net.minecraft.util.EnumHand h, EnumFacing f, float x, float y, float z) {
            if (s.getValue(META) != 1) return false;
            if (!w.isRemote) {
                if (!pl.inventory.addItemStackToInventory(new ItemStack(SWContent.ShadowBerry)))
                    w.spawnEntity(new EntityItem(w, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, new ItemStack(SWContent.ShadowBerry)));
                w.setBlockState(p, getDefaultState(), 3);
            }
            w.playSound(null, p, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.2F, ((w.rand.nextFloat() - w.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            return true;
        }

        @Override
        public void updateTick(World w, BlockPos p, IBlockState s, Random r) {
            if (s.getValue(META) == 0) w.setBlockState(p, getStateFromMeta(1), 3);
            int ran = r.nextInt(10);
            if (ran < 5) {
                BlockPos q = ran == 0 ? p.up() : p.offset(EnumFacing.HORIZONTALS[ran - 1]);
                if (w.isAirBlock(q) && canStay(w, q)) w.setBlockState(q, getStateFromMeta(1), 3);
            }
        }
    }

    /** Cactus that hurts like the vanilla one. */
    public static class Cactus extends SWBlock implements IPlantable {
        private static final AxisAlignedBB COLL = new AxisAlignedBB(0.0625, 0, 0.0625, 0.9375, 0.9375, 0.9375);
        public Cactus() {
            super(Material.CACTUS, SoundType.CLOTH);
            setTickRandomly(true);
            bounds(0.0625, 0, 0.0625, 0.9375, 1, 0.9375);
            cutout();
            tex("ShadowCactus");
        }

        @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState s, IBlockAccess w, BlockPos p) { return COLL; }
        @Override public void onEntityCollision(World w, BlockPos p, IBlockState s, Entity e) { e.attackEntityFrom(DamageSource.CACTUS, 1.0F); }
        @Override public int damageDropped(IBlockState s) { return 0; }

        private boolean canStay(World w, BlockPos p) {
            for (EnumFacing f : EnumFacing.HORIZONTALS) if (w.getBlockState(p.offset(f)).getMaterial().isSolid()) return false;
            Block b = w.getBlockState(p.down()).getBlock();
            return b == this || b == SWContent.ShadowSand || b == Blocks.SAND;
        }

        @Override public boolean canPlaceBlockAt(World w, BlockPos p) { return super.canPlaceBlockAt(w, p) && canStay(w, p); }
        @Override public void neighborChanged(IBlockState s, World w, BlockPos p, Block b, BlockPos f) { if (!canStay(w, p)) w.destroyBlock(p, true); }

        @Override
        public void updateTick(World w, BlockPos p, IBlockState s, Random r) {
            if (!w.isAirBlock(p.up())) return;
            int h = 1;
            while (w.getBlockState(p.down(h)).getBlock() == this) h++;
            if (h >= 3) return;
            int m = s.getValue(META);
            if (m == 15) { w.setBlockState(p.up(), getDefaultState()); w.setBlockState(p, getStateFromMeta(0), 4); }
            else w.setBlockState(p, getStateFromMeta(m + 1), 4);
        }

        @Override public EnumPlantType getPlantType(IBlockAccess w, BlockPos p) { return EnumPlantType.Desert; }
        @Override public IBlockState getPlant(IBlockAccess w, BlockPos p) { return getDefaultState(); }
    }

    /** Logs: meta 0..3 = vertical, 4..7 = along X, 8..11 = along Z (1.7 BlockLog). */
    public static class Log extends SWBlock {
        public Log(String top, String side) { super(Material.WOOD, SoundType.WOOD); tex(top, side); }

        @Override
        @SideOnly(Side.CLIENT)
        public IIcon getIcon(int side, int key) {
            if (icons == null) return null;
            int axis = key & 12;
            boolean end = axis == 0 ? side <= 1 : axis == 4 ? side >= 4 : side == 2 || side == 3;
            return icons[end ? 0 : 1];
        }

        @Override
        public IBlockState getStateForPlacement(World w, BlockPos p, EnumFacing f, float hx, float hy, float hz, int m, net.minecraft.entity.EntityLivingBase pl) {
            int a = f.getAxis() == EnumFacing.Axis.Y ? 0 : f.getAxis() == EnumFacing.Axis.X ? 4 : 8;
            return getStateFromMeta(a);
        }

        @Override public int damageDropped(IBlockState s) { return 0; }
        @Override public boolean canSustainLeaves(IBlockState s, IBlockAccess w, BlockPos p) { return true; }
        @Override public boolean isWood(IBlockAccess w, BlockPos p) { return true; }
    }

    /** Leaves: decay when no log within 4 blocks (unless placed by a player: meta bit 4). */
    public static class Leaves extends SWBlock {
        private final Supplier<Block> sapling;
        public Leaves(String tex, Supplier<Block> sapling) {
            super(Material.LEAVES, SoundType.PLANT);
            this.sapling = sapling;
            setTickRandomly(true);
            setLightOpacity(1);
            cutout();
            tex(tex);
        }

        @Override @SideOnly(Side.CLIENT) public boolean shouldSideBeRendered(IBlockState s, IBlockAccess w, BlockPos p, EnumFacing f) { return true; }
        @Override public IBlockState getStateForPlacement(World w, BlockPos p, EnumFacing f, float hx, float hy, float hz, int m, net.minecraft.entity.EntityLivingBase pl) { return getStateFromMeta(4); }
        @Override public boolean isLeaves(IBlockState s, IBlockAccess w, BlockPos p) { return true; }
        @Override public int damageDropped(IBlockState s) { return 0; }

        @Override
        public void updateTick(World w, BlockPos p, IBlockState s, Random r) {
            if (w.isRemote || (s.getValue(META) & 4) != 0 || r.nextInt(4) != 0) return;
            for (BlockPos q : BlockPos.getAllInBoxMutable(p.add(-4, -4, -4), p.add(4, 4, 4)))
                if (w.getBlockState(q).getBlock() instanceof Log) return;
            dropBlockAsItem(w, p, s, 0);
            w.setBlockToAir(p);
        }

        @Override public Item getItemDropped(IBlockState s, Random r, int f) { return r.nextInt(20) == 0 && sapling != null ? Item.getItemFromBlock(sapling.get()) : Items.AIR; }
        @Override public int quantityDropped(Random r) { return 1; }
        @Override public boolean canSilkHarvest(World w, BlockPos p, IBlockState s, EntityPlayer pl) { return true; }
    }

    /** Cookie block: each click eats a slice (meta 0..9). */
    public static class Cookie extends SWBlock {
        public Cookie() { super(Material.CAKE, SoundType.CLOTH); tex("CookieBlock"); }

        @Override public AxisAlignedBB getBoundingBox(IBlockState s, IBlockAccess w, BlockPos p) { return new AxisAlignedBB(0, 0, 0, 1, 1 - s.getValue(META) / 10.0, 1); }
        @Override public AxisAlignedBB getArchaicRenderBounds(int key) { return new AxisAlignedBB(0, 0, 0, 1, 1 - Math.min(key, 9) / 10.0, 1); }
        @Override public boolean isOpaqueCube(IBlockState s) { return s.getValue(META) == 0; }
        @Override public boolean isFullCube(IBlockState s) { return s.getValue(META) == 0; }
        @Override public Item getItemDropped(IBlockState s, Random r, int f) { return Items.COOKIE; }
        @Override public int quantityDropped(IBlockState s, int fortune, Random r) { return 9 - s.getValue(META); }

        @Override
        public boolean onBlockActivated(World w, BlockPos p, IBlockState s, EntityPlayer pl, net.minecraft.util.EnumHand h, EnumFacing f, float x, float y, float z) {
            if (!pl.canEat(false) && !pl.capabilities.isCreativeMode) return false;
            pl.getFoodStats().addStats(1, 0.001F);
            w.playSound(null, p, SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.PLAYERS, 0.5F, 1F);
            int l = s.getValue(META) + 1;
            if (l > 9) { w.setBlockToAir(p); w.playSound(null, p, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, 1F); }
            else w.setBlockState(p, getStateFromMeta(l), 2);
            return true;
        }
    }

    /** Cookie spawner: every click pops out cookies, breaks after 10 uses. */
    public static class CookieSpawner extends SWBlock {
        public CookieSpawner() { super(Material.CAKE, SoundType.CLOTH); tex("CookieSpawner"); }
        @Override public Item getItemDropped(IBlockState s, Random r, int f) { return Items.COOKIE; }

        @Override
        public boolean onBlockActivated(World w, BlockPos p, IBlockState s, EntityPlayer pl, net.minecraft.util.EnumHand h, EnumFacing f, float x, float y, float z) {
            if (w.isRemote) return true;
            int m = s.getValue(META) + 1;
            if (m >= 10) w.setBlockToAir(p); else w.setBlockState(p, getStateFromMeta(m), 3);
            int n = w.rand.nextInt(4);
            if (n > 0) {
                EntityItem it = new EntityItem(w, p.getX() + 0.5, p.getY() + 1, p.getZ() + 0.5, new ItemStack(Items.COOKIE, n));
                it.motionY = 0.5;
                w.spawnEntity(it);
            }
            w.playSound(null, p, SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 0.6F);
            return true;
        }
    }

    /** Weather block (overworld only): toggles rain. */
    public static class Weather extends SWBlock {
        public Weather() { super(Material.ROCK, SoundType.STONE); tex("WheaterBlock1", "WheaterBlock2"); }
        @Override @SideOnly(Side.CLIENT) public IIcon getIcon(int side, int key) { return icons == null ? null : icons[key == 0 ? 0 : 1]; }
        @Override public int damageDropped(IBlockState s) { return 0; }

        @Override
        public boolean onBlockActivated(World w, BlockPos p, IBlockState s, EntityPlayer pl, net.minecraft.util.EnumHand h, EnumFacing f, float x, float y, float z) {
            if (w.isRemote) return true;
            if (w.provider.getDimension() != 0) {
                pl.sendMessage(new net.minecraft.util.text.TextComponentString("Only works in the normal world!"));
                return true;
            }
            w.setBlockState(p, getStateFromMeta(s.getValue(META) == 0 ? 1 : 0), 3);
            boolean rain = !w.getWorldInfo().isRaining();
            w.getWorldInfo().setRaining(rain);
            w.getWorldInfo().setRainTime(rain ? 12000 : 0);
            w.playSound(null, p, SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 0.6F);
            return true;
        }
    }
}
