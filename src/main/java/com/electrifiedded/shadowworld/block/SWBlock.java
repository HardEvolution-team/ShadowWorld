package com.electrifiedded.shadowworld.block;

import com.electrifiedded.archaiclib.v1710.api.block.IArchaicRenderBlock;
import com.electrifiedded.archaiclib.v1710.api.client.IIcon;
import com.electrifiedded.archaiclib.v1710.api.client.IIconRegister;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import java.util.function.Supplier;

/**
 * Generic Shadow World block: 1.7-style icons (1 = all sides, 3 = bottom/top/side, 6 = per side),
 * optional cross render, non-opaque layers and a replacement drop.
 */
public class SWBlock extends Block implements IArchaicRenderBlock {
    protected String[] textures = new String[0];
    protected int renderType = RENDER_STANDARD;
    protected BlockRenderLayer layer = BlockRenderLayer.SOLID;
    protected boolean opaque = true;
    protected AxisAlignedBB bounds = FULL_BLOCK_AABB;
    protected boolean noCollide;
    protected Supplier<Item> drop;
    protected int dropMin = 1, dropMax = 1;
    public interface Walk { void on(World w, BlockPos p, Entity e); }
    public interface Use { boolean on(World w, BlockPos p, IBlockState s, EntityPlayer pl); }
    protected Walk walk;
    protected Use use;
    protected boolean beacon, fireSource;
    @SideOnly(Side.CLIENT)
    protected IIcon[] icons;

    public SWBlock walk(Walk w) { walk = w; return this; }
    public SWBlock use(Use u) { use = u; return this; }
    public SWBlock beacon() { beacon = true; return this; }
    public SWBlock fireSource() { fireSource = true; return this; }
    public SWBlock unbreakable() { setBlockUnbreakable(); setResistance(6000000F); drop = () -> net.minecraft.init.Items.AIR; dropMin = dropMax = 0; return this; }
    public SWBlock noCollide() { noCollide = true; return this; }

    @Override public void onEntityWalk(World w, BlockPos p, Entity e) { if (walk != null) walk.on(w, p, e); }
    @Override public boolean onBlockActivated(World w, BlockPos p, IBlockState s, EntityPlayer pl, EnumHand h, EnumFacing f, float x, float y, float z) { return use != null ? use.on(w, p, s, pl) : false; }
    @Override public boolean isBeaconBase(IBlockAccess w, BlockPos p, BlockPos b) { return beacon; }
    @Override public boolean isFireSource(World w, BlockPos p, EnumFacing f) { return fireSource || super.isFireSource(w, p, f); }

    public static final net.minecraft.block.properties.PropertyInteger META = net.minecraft.block.properties.PropertyInteger.create("meta", 0, 15);

    @Override protected net.minecraft.block.state.BlockStateContainer createBlockState() { return new net.minecraft.block.state.BlockStateContainer(this, META); }
    @Override public int getMetaFromState(IBlockState s) { return s.getValue(META); }
    @Override public IBlockState getStateFromMeta(int m) { return getDefaultState().withProperty(META, m & 15); }
    public static int meta(IBlockAccess w, BlockPos p) { IBlockState s = w.getBlockState(p); return s.getPropertyKeys().contains(META) ? s.getValue(META) : s.getBlock().getMetaFromState(s); }
    public static void setMeta(World w, BlockPos p, int m, int flags) { IBlockState s = w.getBlockState(p); w.setBlockState(p, s.getBlock().getStateFromMeta(m), flags); }

    public SWBlock(Material m, SoundType s) {
        super(m);
        setSoundType(s);
    }

    public SWBlock tex(String... t) { this.textures = t; return this; }
    public SWBlock cross() { renderType = RENDER_CROSS; layer = BlockRenderLayer.CUTOUT; opaque = false; noCollide = true; bounds = new AxisAlignedBB(0.1, 0, 0.1, 0.9, 0.8, 0.9); return this; }
    public SWBlock cutout() { layer = BlockRenderLayer.CUTOUT; opaque = false; return this; }
    public SWBlock translucent() { layer = BlockRenderLayer.TRANSLUCENT; opaque = false; return this; }
    public SWBlock bounds(double x1, double y1, double z1, double x2, double y2, double z2) { bounds = new AxisAlignedBB(x1, y1, z1, x2, y2, z2); opaque = false; return this; }
    public SWBlock drops(Supplier<Item> d, int min, int max) { drop = d; dropMin = min; dropMax = max; return this; }
    public SWBlock hr(float hardness, float resistance) { setHardness(hardness); setResistance(resistance); return this; }
    public SWBlock light(float l) { setLightLevel(l); return this; }

    @Override public boolean isOpaqueCube(IBlockState s) { return opaque; }
    @Override public boolean isFullCube(IBlockState s) { return opaque; }
    @Override @SideOnly(Side.CLIENT) public BlockRenderLayer getRenderLayer() { return layer; }
    @Override public AxisAlignedBB getBoundingBox(IBlockState s, IBlockAccess w, BlockPos p) { return bounds; }
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState s, IBlockAccess w, BlockPos p) { return noCollide ? NULL_AABB : bounds; }
    @Override public int getArchaicRenderType(int key) { return renderType; }
    @Override public AxisAlignedBB getArchaicRenderBounds(int key) { return renderType == RENDER_CROSS ? FULL_BLOCK_AABB : bounds; }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState s, IBlockAccess w, BlockPos p, EnumFacing f) {
        if (layer == BlockRenderLayer.TRANSLUCENT && w.getBlockState(p.offset(f)).getBlock() == this) return false;
        return super.shouldSideBeRendered(s, w, p, f);
    }

    @Override public Item getItemDropped(IBlockState s, Random r, int fortune) { return drop != null ? drop.get() : super.getItemDropped(s, r, fortune); }
    @Override public int quantityDropped(Random r) { return drop != null ? dropMin + (dropMax > dropMin ? r.nextInt(dropMax - dropMin + 1) : 0) : 1; }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        icons = new IIcon[textures.length];
        for (int i = 0; i < textures.length; i++) icons[i] = reg.registerIcon(textures[i].contains(":") ? textures[i] : "shadowworld:" + textures[i]);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int key) {
        if (icons == null || icons.length == 0) return null;
        if (icons.length == 1) return icons[0];
        if (icons.length == 3) return icons[side == 0 ? 0 : side == 1 ? 1 : 2];
        if (icons.length == 2) return icons[side <= 1 ? 0 : 1];
        return icons[Math.min(side, icons.length - 1)];
    }
}
