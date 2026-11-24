package com.electrifiedded.shadowworld.world;

import com.electrifiedded.archaiclib.legacy.world.LegacyChunkGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** 1.7 MapGenShadowVillage: underground dwarf villages, 1 per 32x32-chunk region (min spacing 8), in all land shadow biomes. */
public class SWVillage extends MapGenStructure {
    public static final String NAME = "Shadow Village";
    private final int separation = 8, spacing = 32;

    @Override public String getStructureName() { return NAME; }

    @Override
    protected boolean canSpawnStructureAtCoords(int cx, int cz) {
        int k = cx, l = cz;
        if (cx < 0) cx -= spacing - 1;
        if (cz < 0) cz -= spacing - 1;
        int i1 = cx / spacing, j1 = cz / spacing;
        Random r = world.setRandomSeed(i1, j1, 10387312);
        i1 = i1 * spacing + r.nextInt(spacing - separation);
        j1 = j1 * spacing + r.nextInt(spacing - separation);
        if (k != i1 || l != j1) return false;
        List<net.minecraft.world.biome.Biome> ok = Arrays.asList(SWWorld.ShadowBiome, SWWorld.ShadowHills, SWWorld.ShadowJungle,
                SWWorld.ShadowDeadForest, SWWorld.ShadowDesert, SWWorld.ShadowPlains);
        return world.getBiomeProvider().areBiomesViable(k * 16 + 8, l * 16 + 8, 0, ok);
    }

    @Override
    public BlockPos getNearestStructurePos(World w, BlockPos pos, boolean unexplored) {
        this.world = w;
        return findNearestStructurePosBySpacing(w, this, pos, spacing, separation, 10387312, false, 100, unexplored);
    }

    @Override
    protected StructureStart getStructureStart(int cx, int cz) {
        Start s = new Start(world, rand, cx, cz);
        while (s.getComponents().isEmpty() || ((StructureShadowVillagePieces.Stairs2) s.getComponents().get(0)).ShadowVillagePortalRoom == null)
            s = new Start(world, rand, cx, cz);
        return s;
    }

    /** Start + piece ids for the structure NBT (call in preInit). */
    public static void registerIO() {
        net.minecraft.world.gen.structure.MapGenStructureIO.registerStructure(Start.class, "ShadowVillage");
        net.minecraft.world.gen.structure.MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.Corridor.class, "SWVCorridor");
        net.minecraft.world.gen.structure.MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.Crossing.class, "SWVCrossing");
        net.minecraft.world.gen.structure.MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.Farm.class, "SWVFarm");
        net.minecraft.world.gen.structure.MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.Hall.class, "SWVHall");
        net.minecraft.world.gen.structure.MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.LeftTurn.class, "SWVLeftTurn");
        net.minecraft.world.gen.structure.MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.LibaryCorridor.class, "SWVLibaryCorridor");
        net.minecraft.world.gen.structure.MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.Market.class, "SWVMarket");
        net.minecraft.world.gen.structure.MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.RightTurn.class, "SWVRightTurn");
        net.minecraft.world.gen.structure.MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.RoomCrossing.class, "SWVRoomCrossing");
        net.minecraft.world.gen.structure.MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.Stairs.class, "SWVStairs");
        net.minecraft.world.gen.structure.MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.Stairs2.class, "SWVStairs2");
        net.minecraft.world.gen.structure.MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.StairsStraight.class, "SWVStairsStraight");
        net.minecraft.world.gen.structure.MapGenStructureIO.registerStructureComponent(StructureShadowVillagePieces.Straight.class, "SWVStraight");
    }

    /** DungeonFinder target (1.7 findClosestStructure("Dungeon")). */
    public static BlockPos findClosest(World w, BlockPos from) {
        if (!(w instanceof WorldServer ws)) return null;
        if (ws.getChunkProvider().chunkGenerator instanceof LegacyChunkGenerator g)
            return g.getNearestStructurePos(w, NAME, from, false);
        return null;
    }

    public static class Start extends StructureStart {
        public Start() {}

        public Start(World w, Random r, int cx, int cz) {
            super(cx, cz);
            StructureShadowVillagePieces.prepareStructurePieces();
            StructureShadowVillagePieces.Stairs2 st = new StructureShadowVillagePieces.Stairs2(0, r, (cx << 4) + 2, (cz << 4) + 2);
            components.add(st);
            st.buildComponent(st, components, r);
            List<StructureComponent> pending = st.field_75026_c;
            while (!pending.isEmpty()) pending.remove(r.nextInt(pending.size())).buildComponent(st, components, r);
            updateBoundingBox();
            markAvailableHeight(w, r, 10);
        }
    }
}
