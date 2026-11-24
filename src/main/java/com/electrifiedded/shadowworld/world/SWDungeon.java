package com.electrifiedded.shadowworld.world;

import com.electrifiedded.archaiclib.legacy.world.LegacyChestContent;
import com.electrifiedded.shadowworld.SWContent;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

/** 1.7 WorldGenShadowDungeon: vanilla-style dungeon of shadow cobble with Shadow Chests (ShadowLoot) and a dungeon spawner. */
public class SWDungeon extends WorldGenerator {
    private static Material mat(World w, int x, int y, int z) { return w.getBlockState(new BlockPos(x, y, z)).getMaterial(); }

    @Override
    public boolean generate(World w, Random r, BlockPos pos) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        int h = 3, l = r.nextInt(2) + 2, i1 = r.nextInt(2) + 2, holes = 0;
        for (int a = x - l - 1; a <= x + l + 1; a++)
            for (int b = y - 1; b <= y + h + 1; b++)
                for (int c = z - i1 - 1; c <= z + i1 + 1; c++) {
                    Material m = mat(w, a, b, c);
                    if (b == y - 1 && !m.isSolid()) return false;
                    if (b == y + h + 1 && !m.isSolid()) return false;
                    if ((a == x - l - 1 || a == x + l + 1 || c == z - i1 - 1 || c == z + i1 + 1) && b == y
                            && w.isAirBlock(new BlockPos(a, b, c)) && w.isAirBlock(new BlockPos(a, b + 1, c))) holes++;
                }
        if (holes < 1 || holes > 5) return false;
        for (int a = x - l - 1; a <= x + l + 1; a++)
            for (int b = y + h; b >= y - 1; b--)
                for (int c = z - i1 - 1; c <= z + i1 + 1; c++) {
                    BlockPos p = new BlockPos(a, b, c);
                    if (a != x - l - 1 && b != y - 1 && c != z - i1 - 1 && a != x + l + 1 && b != y + h + 1 && c != z + i1 + 1) w.setBlockToAir(p);
                    else if (b >= 0 && !mat(w, a, b - 1, c).isSolid()) w.setBlockToAir(p);
                    else if (mat(w, a, b, c).isSolid()) {
                        if (b == y - 1 && r.nextInt(4) != 0) w.setBlockState(p, SWContent.MossyShadowCobble.getDefaultState(), 2);
                        else w.setBlockState(p, SWContent.ShadowCobble.getDefaultState(), 2);
                    }
                }
        for (int n = 0; n < 2; n++) {
            for (int tries = 0; tries < 3; tries++) {
                int cx = x + r.nextInt(l * 2 + 1) - l, cz = z + r.nextInt(i1 * 2 + 1) - i1;
                if (!w.isAirBlock(new BlockPos(cx, y, cz))) continue;
                int walls = 0;
                if (mat(w, cx - 1, y, cz).isSolid()) walls++;
                if (mat(w, cx + 1, y, cz).isSolid()) walls++;
                if (mat(w, cx, y, cz - 1).isSolid()) walls++;
                if (mat(w, cx, y, cz + 1).isSolid()) walls++;
                if (walls == 1) {
                    BlockPos cp = new BlockPos(cx, y, cz);
                    w.setBlockState(cp, SWContent.ShadowChest.getDefaultState(), 2);
                    TileEntity te = w.getTileEntity(cp);
                    if (te instanceof IInventory inv && !w.isRemote) LegacyChestContent.generateChestContents(r, SWLoot.SHADOW, inv, 4 + r.nextInt(8));
                    break;
                }
            }
        }
        BlockPos sp = new BlockPos(x, y, z);
        w.setBlockState(sp, Blocks.MOB_SPAWNER.getDefaultState(), 2);
        if (w.getTileEntity(sp) instanceof TileEntityMobSpawner s) s.getSpawnerBaseLogic().setEntityId(SWLoot.dungeonMob(r));
        return true;
    }
}
