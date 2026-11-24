package com.electrifiedded.shadowworld.world;

import com.electrifiedded.archaiclib.legacy.world.LegacyChestContent;
import com.electrifiedded.archaiclib.legacy.world.LegacyChunkedStructure;
import com.electrifiedded.archaiclib.legacy.world.LegacyClassStructure;
import com.electrifiedded.archaiclib.original.OriginalModAssets;
import com.electrifiedded.shadowworld.Reference;
import com.electrifiedded.shadowworld.SWContent;
import com.electrifiedded.shadowworld.ShadowWorld;
import com.electrifiedded.shadowworld.entity.EntityFlyingBoat;
import com.electrifiedded.shadowworld.entity.SWAngels;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Angel World (1.7 WorldProviderAngel / ChunkProviderAngel / AngelTeleporter / BiomeGenAngel): an empty sky dimension
 * holding the giant Angel Village (WorldGenAngelVillage1..21, replayed from the original classes). The original placed
 * the whole ~450k-block village from chunk 0,0; here every chunk writes its own part into its primer.
 */
public final class SWAngelWorld {
    private SWAngelWorld() {}

    public static DimensionType ANGEL;
    public static Biome AngelBiome;
    /** Village origin = ChunkProviderAngel.populate: generate(world, rand, 0, 20, 0). */
    static final int VX = 0, VY = 20, VZ = 0;
    private static final String PKG = "jajo_11/ShadowWorld/World/Structure/AngelVillage/";
    private static final String GEN_DESC = "(Lnet/minecraft/world/World;Ljava/util/Random;III)Z";

    public static int angelDim() { return SWContent.DimID + 2; }

    // ------------------------------------------------------------------ biome

    public static class BiomeAngel extends Biome {
        public BiomeAngel() {
            super(new BiomeProperties("Angel Village").setBaseHeight(0.05F).setHeightVariation(0.46F).setTemperature(0.5F).setRainDisabled().setWaterColor(2540249));
            spawnableMonsterList.clear();
            spawnableCreatureList.clear();
            spawnableWaterCreatureList.clear();
            spawnableCaveCreatureList.clear();
            topBlock = SWContent.ShadowGrass.getDefaultState();
            fillerBlock = SWContent.ShadowDirt.getDefaultState();
            decorator.treesPerChunk = -999;
            decorator.flowersPerChunk = -999;
            decorator.grassPerChunk = 0;
        }
        @Override public WorldGenAbstractTree getRandomTreeFeature(Random r) { return SWTrees.shadow(false); }
        @Override public void addDefaultFlowers() {}
        @Override @SideOnly(Side.CLIENT) public int getGrassColorAtPos(BlockPos p) { return 5164354; }
        @Override @SideOnly(Side.CLIENT) public int getFoliageColorAtPos(BlockPos p) { return 5164354; }
    }

    public static void registerBiome(net.minecraftforge.registries.IForgeRegistry<Biome> reg) {
        AngelBiome = new BiomeAngel();
        reg.register(AngelBiome.setRegistryName(Reference.MOD_ID, "angel_village"));
    }

    // ------------------------------------------------------------------ provider

    public static class AngelProvider extends WorldProvider {
        @Override protected void init() { biomeProvider = new BiomeProviderSingle(AngelBiome); hasSkyLight = true; }
        @Override public DimensionType getDimensionType() { return ANGEL; }
        @Override public IChunkGenerator createChunkGenerator() { return new AngelGenerator(world); }
        @Override public boolean canRespawnHere() { return false; }
        @Override public boolean isSurfaceWorld() { return false; }
        @Override public boolean canDoLightning(Chunk c) { return false; }
        @Override public boolean canDoRainSnowIce(Chunk c) { return false; }
        @Override public boolean isDaytime() { return false; }
        @Override public double getHorizon() { return 256.0; }
        @Override public BlockPos getSpawnCoordinate() { return new BlockPos(168, 142, 94); }
        @Override public boolean canCoordinateBeSpawn(int x, int z) { return false; }
        @Override public float calculateCelestialAngle(long time, float pt) { return 0.0F; }

        @Override
        protected void generateLightBrightnessTable() {
            for (int i = 0; i <= 15; i++) {
                float f1 = 1.0F - i / 15.0F - 0.2F;
                lightBrightnessTable[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F);
            }
        }

        @Override @SideOnly(Side.CLIENT) public float getStarBrightness(float pt) { return 1.0F; }
        @Override @SideOnly(Side.CLIENT) public float[] calcSunriseSunsetColors(float a, float b) { return null; }
        @Override @SideOnly(Side.CLIENT) public Vec3d getFogColor(float a, float b) { return Vec3d.ZERO; }
        @Override @SideOnly(Side.CLIENT) public boolean isSkyColored() { return false; }
        @Override @SideOnly(Side.CLIENT) public float getCloudHeight() { return -10.0F; }
        @Override @SideOnly(Side.CLIENT) public Vec3d getSkyColor(Entity e, float pt) { return Vec3d.ZERO; }

        @SideOnly(Side.CLIENT) private IRenderHandler sky;
        @Override @SideOnly(Side.CLIENT) @Nullable
        public IRenderHandler getSkyRenderer() {
            if (sky == null) sky = ShadowWorld.proxy.angelSky();
            return sky;
        }
    }

    // ------------------------------------------------------------------ chunk generator

    public static class AngelGenerator implements IChunkGenerator {
        private final World world;
        private final Random rand;

        public AngelGenerator(World w) { world = w; rand = new Random(w.getSeed()); }

        @Override
        public Chunk generateChunk(int x, int z) {
            ChunkPrimer primer = new ChunkPrimer();
            village().fill(x, z, primer);
            Chunk c = new Chunk(world, primer, x, z);
            byte[] biomes = c.getBiomeArray();
            byte id = (byte) Biome.getIdForBiome(AngelBiome);
            java.util.Arrays.fill(biomes, id);
            c.generateSkylightMap();
            return c;
        }

        @Override
        public void populate(int cx, int cz) {
            BlockFalling.fallInstantly = true;
            int x0 = cx * 16 + 8, z0 = cz * 16 + 8;
            for (int[] h : HOUSES) {
                int hx = VX + h[1], hy = VY + h[2], hz = VZ + h[3];
                if (hx >= x0 && hx < x0 + 16 && hz >= z0 && hz < z0 + 16) {
                    rand.setSeed(world.getSeed() ^ (hx * 341873128712L + hz * 132897987541L + hy));
                    house(world, rand, h[0] == 0 ? "GHouse" : "DHouse", hx, hy, hz);
                }
            }
            for (int i = 0; i < BOATS.length; i++) {
                int[] b = BOATS[i];
                int bx = VX + b[0], by = VY + b[1], bz = VZ + b[2];
                if (bx >= x0 && bx < x0 + 16 && bz >= z0 && bz < z0 + 16) {
                    rand.setSeed(world.getSeed() ^ (bx * 341873128712L + bz * 132897987541L));
                    EntityFlyingBoat boat = new EntityFlyingBoat(world, bx, by, bz);
                    if (i > 0) {
                        boat.rotationYaw = -90.0F;
                        if (rand.nextInt(5) == 0) {
                            if (rand.nextInt(3) == 0) boat.setHasEnderChest(true);
                            else {
                                boat.setHasChest(true);
                                LegacyChestContent.generateChestContents(rand, SWLoot.BOAT, boat, 8 + rand.nextInt(8));
                            }
                        }
                    }
                    world.spawnEntity(boat);
                }
            }
            BlockFalling.fallInstantly = false;
        }

        @Override public boolean generateStructures(Chunk c, int x, int z) { return false; }
        @Override public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType t, BlockPos p) { return Collections.emptyList(); }
        @Override @Nullable public BlockPos getNearestStructurePos(World w, String n, BlockPos p, boolean f) { return null; }
        @Override public void recreateStructures(Chunk c, int x, int z) {}
        @Override public boolean isInsideStructure(World w, String n, BlockPos p) { return false; }
    }

    // ------------------------------------------------------------------ village data

    /** Positions of GHouse(0)/DHouse(1).generateRandomHouse calls in WorldGenAngelVillage21 (relative to the village origin). */
    static final int[][] HOUSES = {
            {0,205,119,108},{0,161,121,108},{0,191,121,108},{0,170,121,118},{0,181,121,118},{0,191,121,125},{0,84,131,95},{0,89,131,95},{0,94,131,95},
            {0,105,131,95},{0,110,131,95},{0,115,131,95},{0,73,131,95},{0,105,131,111},{0,110,131,111},{0,73,131,111},{0,94,131,111},{0,244,57,156},
            {0,249,57,156},{0,260,57,156},{0,265,57,156},{0,270,57,156},{0,265,57,172},{0,270,57,172},{0,275,57,172},{0,244,57,172},{0,249,57,172},
            {0,260,57,172},{0,260,57,188},{0,265,57,188},{0,249,57,188},{1,181,121,87},{1,161,121,97},{1,191,121,97},{1,73,131,84},{1,84,131,84},
            {1,89,131,84},{1,94,131,84},{1,105,131,84},{1,110,131,84},{1,115,131,84},{1,105,131,100},{1,110,131,100},{1,115,131,100},{1,84,131,100},
            {1,89,131,100},{1,94,131,100},{1,73,131,100},{1,239,57,161},{1,244,57,161},{1,249,57,161},{1,260,57,161},{1,265,57,161},{1,270,57,161},
            {1,275,57,161},{1,260,57,177},{1,265,57,177},{1,270,57,177},{1,244,57,177},{1,249,57,177}};
    /** EntityFlyingBoat spawns in WorldGenAngelVillage21 (the first one never has cargo). */
    static final int[][] BOATS = {{191, 166, 211}, {166, 118, 158}, {169, 118, 159}, {173, 118, 160}};

    private static volatile LegacyChunkedStructure village;

    public static LegacyChunkedStructure village() {
        LegacyChunkedStructure v = village;
        if (v != null) return v;
        synchronized (SWAngelWorld.class) {
            if (village != null) return village;
            long t = System.currentTimeMillis();
            LegacyClassStructure[] parts = new LegacyClassStructure[21];
            int unresolved = 0;
            for (int i = 0; i < 21; i++) {
                byte[] bytes = OriginalModAssets.readOriginal(Reference.MOD_ID, PKG + "WorldGenAngelVillage" + (i + 1) + ".class");
                parts[i] = LegacyClassStructure.load(bytes, SWWorld::resolveBlock);
                unresolved += parts[i].unresolved;
            }
            village = new LegacyChunkedStructure(VX, VY, VZ, parts);
            ShadowWorld.LOG.info("Angel Village: {} blocks ({} unresolved) in {} ms", village.size(), unresolved, System.currentTimeMillis() - t);
            return village;
        }
    }

    private static final java.util.Map<String, LegacyClassStructure> HOUSE_PARTS = new java.util.HashMap<>();

    static synchronized LegacyClassStructure housePart(String cls, String method) {
        return HOUSE_PARTS.computeIfAbsent(cls + "." + method, k -> {
            byte[] bytes = OriginalModAssets.readOriginal(Reference.MOD_ID, PKG + cls + ".class");
            return LegacyClassStructure.load(bytes, method, GEN_DESC, SWWorld::resolveBlock);
        });
    }

    /** GHouse/DHouse.generateRandomHouse. */
    static void house(World w, Random rand, String cls, int x, int y, int z) {
        int i = rand.nextInt(5);
        int hx = x - 2, hz = z - 2;
        switch (i) {
            case 0 -> housePart(cls, "generate1").place(w, hx, y, hz, 2, null);
            case 1 -> housePart(cls, "generate2").place(w, hx, y, hz, 2, null);
            case 2 -> housePart(cls, "generate3").place(w, hx, y, hz, 2, null);
            case 3 -> housePart(cls, rand.nextInt(1) == 0 ? "generate4" : "generate5").place(w, hx, y, hz, 2, null);
            default -> housePart(cls, "generate6").place(w, hx, y, hz, 2, null);
        }
        if (i != 3 && rand.nextInt(3) == 0) {
            int c = rand.nextInt(4);
            int[] at = switch (c) { case 0, 1 -> new int[]{x - 1, z - 1}; case 2 -> new int[]{x, z}; default -> new int[]{x - 1, z + 1}; };
            housePart(cls, "generateChest" + (c + 1)).place(w, at[0], y, at[1], 2, null, new LegacyClassStructure.TileListener() {
                @Override public void chest(World world, BlockPos p, String field) { fillHouseChest(world, rand, p, field); }
            });
        }
        int prof = rand.nextInt(4);
        SWAngels.AngelVillager v = new SWAngels.AngelVillager(w, prof);
        v.equipAngel(prof);
        v.setLocationAndAngles(x, y + 1.0, z, 0.0F, 0.0F);
        w.spawnEntity(v);
    }

    static LegacyChestContent[] LOOT2, LOOT3, LOOT4;

    static void fillHouseChest(World w, Random rand, BlockPos p, String field) {
        if (LOOT2 == null) {
            Item feather = SWContent.AngelFeather;
            LOOT2 = new LegacyChestContent[]{new LegacyChestContent(new ItemStack(SWContent.AngelBlock, 1, 0), 5, 15, 5),
                    new LegacyChestContent(new ItemStack(SWContent.AngelBlock, 1, 1), 5, 15, 5),
                    new LegacyChestContent(new ItemStack(SWContent.AngelBlock, 1, 2), 5, 15, 5), new LegacyChestContent(feather, 0, 0, 3, 1)};
            LOOT3 = new LegacyChestContent[]{new LegacyChestContent(SWContent.helmetAngel, 0, 0, 1, 1), new LegacyChestContent(SWContent.plateAngel, 0, 0, 1, 1),
                    new LegacyChestContent(SWContent.legsAngel, 0, 0, 1, 1), new LegacyChestContent(SWContent.bootsAngel, 0, 0, 1, 1),
                    new LegacyChestContent(feather, 0, 0, 3, 1)};
            LOOT4 = new LegacyChestContent[]{new LegacyChestContent(SWContent.WoodStaff, 0, 0, 1, 1), new LegacyChestContent(feather, 0, 0, 3, 1),
                    new LegacyChestContent(SWContent.FlyingBoat, 0, 0, 1, 1)};
        }
        String n = field.substring(field.lastIndexOf('.') + 1);
        LegacyChestContent[] loot = switch (n) { case "Loot3" -> LOOT3; case "Loot4" -> LOOT4; default -> LOOT2; };
        TileEntity te = w.getTileEntity(p);
        if (te instanceof IInventory inv) LegacyChestContent.generateChestContents(rand, loot, inv, 2 + rand.nextInt(n.equals("Loot3") ? 4 : 8));
    }

    // ------------------------------------------------------------------ teleporter

    /** 1.7 AngelTeleporter: into the Angel World at the village portal (168,142,94); back to the overworld spawn at y 128. */
    public static class AngelTeleporter implements ITeleporter {
        @Override
        public void placeEntity(World world, Entity e, float yaw) {
            if (world.provider.getDimension() != 0) {
                BlockPos p = new BlockPos(168, 142, 94);
                world.setBlockState(p, SWContent.AngelPortal.getDefaultState());
                e.setLocationAndAngles(168.0, 142.0, 94.0, e.rotationYaw, e.rotationPitch);
            } else {
                BlockPos s = world.getSpawnPoint();
                int y = Math.max(128, world.getHeight(s).getY() + 1);
                e.setLocationAndAngles(s.getX() + 0.5, y, s.getZ() + 0.5, e.rotationYaw, e.rotationPitch);
            }
            e.motionX = e.motionY = e.motionZ = 0.0;
        }
    }

}
