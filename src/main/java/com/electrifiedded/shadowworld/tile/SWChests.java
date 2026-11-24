package com.electrifiedded.shadowworld.tile;

import net.minecraft.block.BlockChest;
import net.minecraft.block.SoundType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * Shadow Chest, Trapped Shadow Chest and Angel Chest. In 1.7 they were copies of the vanilla chest
 * (double chests, trapped redstone, comparator) with their own textures - here they reuse the vanilla
 * chest logic; a chest only joins chests of the same block.
 */
public final class SWChests {
    private SWChests() {}

    public enum Kind {
        SHADOW("normal", "normal_double"), SHADOW_TRAPPED("trapped", "trapped_double"), ANGEL("angel", "angel_double");
        public final String single, dbl;
        Kind(String s, String d) { single = s; dbl = d; }
    }

    public static class ChestBlock extends BlockChest {
        public final Kind kind;

        public ChestBlock(Kind kind) {
            super(kind == Kind.SHADOW_TRAPPED ? Type.TRAP : Type.BASIC);
            this.kind = kind;
            setHardness(2.5F);
            setResistance(12.5F);
            setSoundType(SoundType.WOOD);
        }

        @Override
        public TileEntity createNewTileEntity(World w, int meta) { return new TileSWChest(); }
    }

    public static class TileSWChest extends TileEntityChest {
        /** Set on the dummy tile used for the item render. */
        public Kind forced;

        public TileSWChest() {}
        public TileSWChest(Kind k) { forced = k; }

        public Kind kind() {
            if (forced != null) return forced;
            return getBlockType() instanceof ChestBlock c ? c.kind : Kind.SHADOW;
        }

        @Override
        protected TileEntityChest getAdjacentChest(EnumFacing side) {
            TileEntityChest t = super.getAdjacentChest(side);
            return t instanceof TileSWChest && world.getBlockState(pos.offset(side)).getBlock() == getBlockType() ? t : null;
        }

        @Override
        public String getName() {
            if (hasCustomName()) return super.getName();
            return switch (kind()) {
                case ANGEL -> "Angel Chest";
                default -> "Shadow Chest";
            };
        }
    }
}
