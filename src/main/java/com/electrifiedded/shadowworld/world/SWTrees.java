package com.electrifiedded.shadowworld.world;

import com.electrifiedded.shadowworld.SWContent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;
import java.util.function.Supplier;

/** Tree / plant generators of the Shadow and Cookie worlds (re-implemented). */
public final class SWTrees {
    private SWTrees() {}

    static boolean soil(Block b) {
        return b == SWContent.ShadowGrass || b == SWContent.ShadowDirt || b == SWContent.CookieBlock || b == SWContent.Shadowfield;
    }

    /** Small round tree: trunk of {@code log}, leaf blob of {@code leaves}; {@code tall} = jungle sized. */
    public static class Tree extends WorldGenAbstractTree {
        final Supplier<Block> log, leaves;
        final int minH, extra;
        final boolean bare;

        public Tree(boolean notify, Supplier<Block> log, Supplier<Block> leaves, int minH, int extra, boolean bare) {
            super(notify);
            this.log = log; this.leaves = leaves; this.minH = minH; this.extra = extra; this.bare = bare;
        }

        @Override
        public boolean generate(World w, Random r, BlockPos pos) {
            int h = minH + r.nextInt(extra + 1);
            if (pos.getY() < 1 || pos.getY() + h + 1 > 255) return false;
            if (!soil(w.getBlockState(pos.down()).getBlock())) return false;
            for (int y = 0; y <= h; y++) if (!w.isAirBlock(pos.up(y)) && !w.getBlockState(pos.up(y)).getBlock().isReplaceable(w, pos.up(y))) return false;
            IBlockState lg = log.get().getDefaultState();
            if (!bare && leaves != null) {
                IBlockState lv = leaves.get().getDefaultState();
                for (int y = h - 3; y <= h; y++) {
                    int rad = y >= h - 1 ? 1 : 2;
                    for (int dx = -rad; dx <= rad; dx++)
                        for (int dz = -rad; dz <= rad; dz++) {
                            if (Math.abs(dx) == rad && Math.abs(dz) == rad && (r.nextInt(2) == 0 || y == h)) continue;
                            BlockPos q = pos.add(dx, y, dz);
                            if (w.isAirBlock(q)) setBlockAndNotifyAdequately(w, q, lv);
                        }
                }
            }
            for (int y = 0; y < h; y++) setBlockAndNotifyAdequately(w, pos.up(y), lg);
            if (bare) {
                for (int i = 0; i < 2 + r.nextInt(3); i++) {
                    int dx = r.nextInt(3) - 1, dz = r.nextInt(3) - 1;
                    BlockPos b = pos.up(h - 1 - r.nextInt(2));
                    for (int l = 1; l <= 2 + r.nextInt(2); l++) {
                        BlockPos q = b.add(dx * l, l / 2, dz * l);
                        if (w.isAirBlock(q)) setBlockAndNotifyAdequately(w, q, log.get().getStateFromMeta(dx != 0 ? 4 : 8));
                    }
                }
            }
            return true;
        }
    }

    public static WorldGenAbstractTree shadow(boolean notify) { return new Tree(notify, () -> SWContent.ShadowLog, () -> SWContent.ShadowLeaf, 4, 3, false); }
    public static WorldGenAbstractTree shadowJungle() { return new Tree(false, () -> SWContent.ShadowLog, () -> SWContent.ShadowLeaf, 8, 6, false); }
    public static WorldGenAbstractTree dead() { return new Tree(false, () -> SWContent.ShadowLog, null, 5, 4, true); }
    public static WorldGenAbstractTree cookie(boolean notify) { return new Tree(notify, () -> SWContent.CookieLog, () -> SWContent.WhiteChocolateBlock, 4, 2, false); }
    public static WorldGenAbstractTree big() { return new Tree(true, () -> SWContent.ShadowLog, () -> SWContent.ShadowLeaf, 10, 6, false); }

    /** Scatter a plant around (vanilla WorldGenFlowers/TallGrass behaviour, searching down to the ground). */
    public static class Scatter extends WorldGenerator {
        final Supplier<Block> plant;
        final int tries;
        public Scatter(Supplier<Block> plant, int tries) { this.plant = plant; this.tries = tries; }

        @Override
        public boolean generate(World w, Random r, BlockPos pos) {
            while (pos.getY() > 1 && (w.isAirBlock(pos) || w.getBlockState(pos).getBlock().isLeaves(w.getBlockState(pos), w, pos))) pos = pos.down();
            Block b = plant.get();
            for (int i = 0; i < tries; i++) {
                BlockPos q = pos.add(r.nextInt(8) - r.nextInt(8), r.nextInt(4) - r.nextInt(4), r.nextInt(8) - r.nextInt(8));
                if (w.isAirBlock(q) && q.getY() < 255 && b.canPlaceBlockAt(w, q)) w.setBlockState(q, b.getDefaultState(), 2);
            }
            return true;
        }
    }
}
