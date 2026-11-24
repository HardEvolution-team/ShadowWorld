package com.electrifiedded.shadowworld.tile;

import com.electrifiedded.archaiclib.legacy.sound.LegacyModSounds;
import com.electrifiedded.shadowworld.SWContent;
import com.electrifiedded.shadowworld.ShadowWorld;
import com.electrifiedded.shadowworld.block.SWBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

/** Diary, Note, Hanging Note and the floating Lamp of the 1.7 mod. */
public final class SWNotes {
    private SWNotes() {}

    static void pageSound(World w, EntityPlayer p) {
        SoundEvent ev = LegacyModSounds.get("shadowworld", "diary.diary_next_page");
        if (ev != null) w.playSound(p, p.posX, p.posY, p.posZ, ev, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    // ------------------------------------------------------------------ Diary (Item.Diary)

    public static class DiaryItem extends Item {
        public DiaryItem() { setMaxStackSize(1); }

        @Override
        public String getItemStackDisplayName(ItemStack s) {
            if (s.hasTagCompound()) {
                String t = s.getTagCompound().getString("title");
                if (!StringUtils.isNullOrEmpty(t)) return t;
            }
            return super.getItemStackDisplayName(s);
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World w, EntityPlayer p, EnumHand hand) {
            ItemStack s = p.getHeldItem(hand);
            if (!s.hasTagCompound()) return new ActionResult<>(EnumActionResult.PASS, s);
            NBTTagCompound t = s.getTagCompound();
            if (t.hasKey("pages")) {
                // pre-1.0 diaries: copy the plain pages into "all"
                NBTTagList all = new NBTTagList(), pages = t.getTagList("pages", 8);
                for (int i = 0; i < pages.tagCount(); i++) all.appendTag(new NBTTagString(pages.getStringTagAt(i)));
                t.setTag("all", all);
                for (String k : new String[]{"items", "blocks", "entities", "other", "entries", "entitydata"}) t.setTag(k, new NBTTagList());
                t.removeTag("pages");
                if (!w.isRemote) p.sendMessage(new TextComponentString("Converted diary!"));
            }
            if (w.isRemote) {
                t.setInteger("open", 0);
                t.setFloat("flip", 1.0F);
                t.setFloat("flip2", 1.0F);
                p.openGui(ShadowWorld.instance, SWGuiIds.DIARY, w, 0, 0, 0);
            }
            pageSound(w, p);
            return new ActionResult<>(EnumActionResult.SUCCESS, s);
        }

        @Override public boolean getShareTag() { return true; }
        @Override @SideOnly(Side.CLIENT) public boolean hasEffect(ItemStack s) { return true; }
    }

    // ------------------------------------------------------------------ Note (Item.Note)

    public static class NoteItem extends Item {
        public NoteItem() { setMaxStackSize(1); }

        @Override
        public String getItemStackDisplayName(ItemStack s) {
            if (s.hasTagCompound()) {
                String t = s.getTagCompound().getString("title");
                if (!StringUtils.isNullOrEmpty(t)) return t;
            }
            return super.getItemStackDisplayName(s);
        }

        static boolean signed(ItemStack s) {
            return s.hasTagCompound() && (s.getTagCompound().hasKey("author") || s.getTagCompound().hasKey("title"));
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World w, EntityPlayer p, EnumHand hand) {
            ItemStack s = p.getHeldItem(hand);
            if (hand != EnumHand.MAIN_HAND) return new ActionResult<>(EnumActionResult.PASS, s); // edit packet works on the main hand
            if (w.isRemote) p.openGui(ShadowWorld.instance, signed(s) ? SWGuiIds.NOTE_READ : SWGuiIds.NOTE_WRITE, w, 0, 0, 0);
            pageSound(w, p);
            return new ActionResult<>(EnumActionResult.SUCCESS, s);
        }

        @Override
        public EnumActionResult onItemUse(EntityPlayer p, World w, BlockPos pos, EnumHand hand, EnumFacing side, float hx, float hy, float hz) {
            ItemStack s = p.getHeldItem(hand);
            if (!p.canPlayerEdit(pos, side, s)) {
                onItemRightClick(w, p, hand);
                return EnumActionResult.SUCCESS;
            }
            if (!w.getBlockState(pos).getMaterial().isSolid() || side.getAxis() == EnumFacing.Axis.Y) return EnumActionResult.PASS;
            BlockPos at = pos.offset(side);
            if (!w.isAirBlock(at)) return EnumActionResult.SUCCESS;
            if (!w.isRemote) {
                int meta = HangingNoteBlock.placedMeta(w, at, side.getIndex(), 0);
                w.setBlockState(at, SWContent.HangingNote.getStateFromMeta(meta), 2);
                ItemStack copy = new ItemStack(SWContent.Note);
                if (s.hasTagCompound()) copy.setTagCompound(s.getTagCompound().copy());
                if (w.getTileEntity(at) instanceof TileHangingNote te) te.note = copy;
                if (!p.capabilities.isCreativeMode) s.shrink(1);
            }
            return EnumActionResult.SUCCESS;
        }

        @Override public boolean getShareTag() { return true; }

        @Override
        @SideOnly(Side.CLIENT)
        public void addInformation(ItemStack s, World w, List<String> tip, ITooltipFlag flag) {
            if (s.hasTagCompound()) {
                String a = s.getTagCompound().getString("author");
                if (!StringUtils.isNullOrEmpty(a)) tip.add(TextFormatting.GRAY + I18n.translateToLocalFormatted("book.byAuthor", a));
            }
        }
    }

    // ------------------------------------------------------------------ Hanging Note (Block.HangingNote, ladder-style render)

    public static class HangingNoteBlock extends SWBlock implements ITileEntityProvider {
        private final Random random = new Random();

        public HangingNoteBlock() {
            super(Material.CIRCUITS, SoundType.CLOTH);
            tex("HangingNote");
            cutout();
            noCollide();
            renderType = RENDER_LADDER;
        }

        private static final float F = 0.125F;

        static AxisAlignedBB box(int meta) {
            return switch (meta) {
                case 2 -> new AxisAlignedBB(0, 0, 1 - F, 1, 1, 1);
                case 3 -> new AxisAlignedBB(0, 0, 0, 1, 1, F);
                case 4 -> new AxisAlignedBB(1 - F, 0, 0, 1, 1, 1);
                case 5 -> new AxisAlignedBB(0, 0, 0, F, 1, 1);
                default -> FULL_BLOCK_AABB;
            };
        }

        @Override public AxisAlignedBB getBoundingBox(IBlockState s, IBlockAccess w, BlockPos p) { return box(s.getValue(META)); }

        static boolean solid(World w, BlockPos p, EnumFacing face) { return w.isSideSolid(p, face, false); }

        @Override
        public boolean canPlaceBlockAt(World w, BlockPos p) {
            return solid(w, p.west(), EnumFacing.EAST) || solid(w, p.east(), EnumFacing.WEST) || solid(w, p.north(), EnumFacing.SOUTH) || solid(w, p.south(), EnumFacing.NORTH);
        }

        /** 1.7 onBlockPlaced. */
        static int placedMeta(World w, BlockPos p, int side, int meta) {
            int j = meta;
            if ((meta == 0 || side == 2) && solid(w, p.south(), EnumFacing.NORTH)) j = 2;
            if ((j == 0 || side == 3) && solid(w, p.north(), EnumFacing.SOUTH)) j = 3;
            if ((j == 0 || side == 4) && solid(w, p.east(), EnumFacing.WEST)) j = 4;
            if ((j == 0 || side == 5) && solid(w, p.west(), EnumFacing.EAST)) j = 5;
            return j;
        }

        @Override
        public void neighborChanged(IBlockState s, World w, BlockPos p, Block b, BlockPos from) {
            int l = s.getValue(META);
            boolean ok = l == 2 && solid(w, p.south(), EnumFacing.NORTH) || l == 3 && solid(w, p.north(), EnumFacing.SOUTH)
                    || l == 4 && solid(w, p.east(), EnumFacing.WEST) || l == 5 && solid(w, p.west(), EnumFacing.EAST);
            if (!ok) {
                dropBlockAsItem(w, p, s, 0);
                w.setBlockToAir(p);
            }
        }

        @Override public int quantityDropped(Random r) { return 0; }
        @Override public TileEntity createNewTileEntity(World w, int meta) { return new TileHangingNote(); }

        @Override
        public void breakBlock(World w, BlockPos p, IBlockState s) {
            if (w.getTileEntity(p) instanceof TileHangingNote te && !te.note.isEmpty()) {
                EntityItem ei = new EntityItem(w, p.getX() + random.nextFloat() * 0.8F + 0.1F, p.getY() + random.nextFloat() * 0.8F + 0.1F,
                        p.getZ() + random.nextFloat() * 0.8F + 0.1F, te.note.copy());
                ei.motionX = random.nextGaussian() * 0.05F;
                ei.motionY = random.nextGaussian() * 0.05F + 0.2F;
                ei.motionZ = random.nextGaussian() * 0.05F;
                w.spawnEntity(ei);
                te.note = ItemStack.EMPTY;
            }
            super.breakBlock(w, p, s);
        }

        @Override
        public ItemStack getPickBlock(IBlockState s, net.minecraft.util.math.RayTraceResult t, World w, BlockPos p, EntityPlayer pl) {
            return new ItemStack(SWContent.Note);
        }
    }

    public static class TileHangingNote extends TileEntity {
        public ItemStack note = ItemStack.EMPTY;

        @Override
        public void readFromNBT(NBTTagCompound c) {
            super.readFromNBT(c);
            NBTTagList l = c.getTagList("Items", 10);
            note = l.tagCount() > 0 ? new ItemStack(l.getCompoundTagAt(0)) : ItemStack.EMPTY;
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound c) {
            super.writeToNBT(c);
            NBTTagList l = new NBTTagList();
            if (!note.isEmpty()) l.appendTag(note.writeToNBT(new NBTTagCompound()));
            c.setTag("Items", l);
            return c;
        }
    }

    // ------------------------------------------------------------------ Lamp (Block.Lamp, TESR-rendered, floats up and down)

    public static class LampBlock extends Block implements ITileEntityProvider {
        public LampBlock() {
            super(Material.CIRCUITS);
            setSoundType(SoundType.GLASS);
            setLightLevel(0.5F);
            setHardness(0.3F);
        }

        @Override public boolean isOpaqueCube(IBlockState s) { return false; }
        @Override public boolean isFullCube(IBlockState s) { return false; }
        @Override public EnumBlockRenderType getRenderType(IBlockState s) { return EnumBlockRenderType.ENTITYBLOCK_ANIMATED; }
        @Override public TileEntity createNewTileEntity(World w, int meta) { return new TileLamp(); }
        @Override public BlockFaceShape getBlockFaceShape(IBlockAccess w, IBlockState s, BlockPos p, EnumFacing f) { return BlockFaceShape.UNDEFINED; }
    }

    public static class TileLamp extends TileEntity implements ITickable {
        public int floattime;

        @Override
        public void update() {
            floattime++;
            if (floattime >= 314.1592653589793) floattime = 0;
        }

        @Override public void readFromNBT(NBTTagCompound c) { super.readFromNBT(c); floattime = c.getInteger("floattime"); }
        @Override public NBTTagCompound writeToNBT(NBTTagCompound c) { super.writeToNBT(c); c.setInteger("floattime", floattime); return c; }
        @Override public NBTTagCompound getUpdateTag() { return writeToNBT(new NBTTagCompound()); }
        @Override public SPacketUpdateTileEntity getUpdatePacket() { return new SPacketUpdateTileEntity(pos, 1, getUpdateTag()); }
        @Override public void onDataPacket(NetworkManager n, SPacketUpdateTileEntity p) { readFromNBT(p.getNbtCompound()); }
    }

}
