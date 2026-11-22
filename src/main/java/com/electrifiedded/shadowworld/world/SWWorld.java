package com.electrifiedded.shadowworld.world;

import com.electrifiedded.archaiclib.original.OriginalModAssets;
import com.electrifiedded.archaiclib.legacy.world.LegacyChunkGenerator;
import com.electrifiedded.archaiclib.legacy.world.LegacyClassStructure;
import com.electrifiedded.archaiclib.legacy.world.LegacyLayerBiomeProvider;
import com.electrifiedded.shadowworld.Reference;
import com.electrifiedded.shadowworld.SWContent;
import com.electrifiedded.shadowworld.ShadowWorld;
import com.electrifiedded.shadowworld.entity.SWEntities;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

/** Shadow World + Cookie World dimensions, biomes and world generation. */
public final class SWWorld {
    private SWWorld() {}

    public static DimensionType SHADOW, COOKIE;
    public static Biome ShadowBiome, ShadowJungle, ShadowDesert, ShadowHills, ShadowPlains, ShadowOcean, ShadowDeadForest, ShadowRiver, CookieBiome;

    // ------------------------------------------------------------------ biomes

    public static class SWBiome extends Biome {
        Supplier<WorldGenAbstractTree> tree;
        Supplier<Block> grass;
        public SWBiome(String name, float base, float var, Supplier<Block> top, Supplier<Block> filler, int trees, int grassCount) {
            super(new BiomeProperties(name).setBaseHeight(base).setHeightVariation(var).setTemperature(0.5F).setRainfall(0.5F).setWaterColor(12653312));
            spawnableMonsterList.clear();
            spawnableCreatureList.clear();
            spawnableWaterCreatureList.clear();
            spawnableCaveCreatureList.clear();
            this.topBlock = top.get().getDefaultState();
            this.fillerBlock = filler.get().getDefaultState();
            decorator.treesPerChunk = trees;
            decorator.grassPerChunk = grassCount;
            decorator.flowersPerChunk = 0;
            decorator.reedsPerChunk = 0;
            decorator.mushroomsPerChunk = 0;
            decorator.generateFalls = false;
            decorator.sandPatchesPerChunk = 0;
            decorator.clayPerChunk = 0;
            decorator.gravelPatchesPerChunk = 0;
        }
        SWBiome tree(Supplier<WorldGenAbstractTree> t) { tree = t; return this; }
        SWBiome grass(Supplier<Block> g) { grass = g; return this; }
        SWBiome spawn(Class<? extends EntityLiving> c, int w, int min, int max) { spawnableCreatureList.add(new SpawnListEntry(c, w, Math.max(1, min), Math.max(1, max))); return this; }
        SWBiome water(Class<? extends EntityLiving> c, int w, int min, int max) { spawnableWaterCreatureList.add(new SpawnListEntry(c, w, min, max)); return this; }
        @Override public WorldGenAbstractTree getRandomTreeFeature(Random r) { return tree != null ? tree.get() : SWTrees.shadow(false); }
        @Override public WorldGenerator getRandomWorldGenForGrass(Random r) { return new SWTrees.Scatter(grass != null ? grass : () -> SWContent.ShadowTallGrass1, 32); }
        @Override public void addDefaultFlowers() {}
        @Override @SideOnly(Side.CLIENT) public int getGrassColorAtPos(BlockPos p) { return 0x2A2A3A; }
        @Override @SideOnly(Side.CLIENT) public int getSkyColorByTemp(float t) { return 0x000000; }
    }

    public static void registerBiomes(net.minecraftforge.registries.IForgeRegistry<Biome> reg) {
        ShadowBiome = new SWBiome("Shadow Forest", 0.05F, 0.46F, () -> SWContent.ShadowGrass, () -> SWContent.ShadowDirt, 10, 10)
                .spawn(SWEntities.SkeletonSpider.class, 5, 4, 4).spawn(SWEntities.Light.class, 1, 1, 1).spawn(SWEntities.ShadowWolf.class, 5, 2, 2)
                .spawn(SWEntities.ShadowPig.class, 5, 2, 2);
        ShadowJungle = new SWBiome("Shadow Jungle", 0.05F, 0.46F, () -> SWContent.ShadowGrass, () -> SWContent.ShadowDirt, 40, 5)
                .tree(() -> new Random().nextInt(3) == 0 ? SWTrees.shadowJungle() : SWTrees.shadow(false))
                .spawn(SWEntities.Light.class, 1, 1, 1).spawn(SWEntities.Shadow.class, 6, 2, 4).spawn(SWEntities.Dragonfly.class, 5, 2, 4);
        ShadowDesert = new SWBiome("Shadow Desert", 0.075F, 0.175F, () -> SWContent.ShadowSand, () -> SWContent.ShadowSand, -999, 0)
                .spawn(SWEntities.Light.class, 1, 1, 1).spawn(SWEntities.WolfSpider.class, 4, 3, 5);
        ShadowHills = new SWBiome("Shadow Hills", 0.45F, 0.57F, () -> SWContent.ShadowGrass, () -> SWContent.ShadowDirt, 2, 3)
                .spawn(SWEntities.Light.class, 1, 1, 1);
        ShadowPlains = new SWBiome("Shadow Plains", 0.05F, 0.15F, () -> SWContent.ShadowGrass, () -> SWContent.ShadowDirt, 0, 50)
                .spawn(SWEntities.Light.class, 1, 1, 3).spawn(SWEntities.Shadow.class, 5, 1, 4).spawn(SWEntities.ShadowSlime.class, 4, 1, 1);
        ShadowOcean = new SWBiome("Shadow Ocean", -1.0F, 0.0F, () -> SWContent.ShadowGrass, () -> SWContent.ShadowDirt, 0, 0)
                .water(SWEntities.JellyFish.class, 3, 4, 8);
        ShadowDeadForest = new SWBiome("Dead Forest", 0.05F, 0.4F, () -> SWContent.ShadowGrass, () -> SWContent.ShadowDirt, 1, 2)
                .tree(SWTrees::dead).grass(() -> SWContent.ShadowTallGrass3)
                .spawn(SWEntities.Light.class, 1, 1, 3).spawn(SWEntities.Shadow.class, 4, 1, 2).spawn(SWEntities.PassiveSpider.class, 5, 1, 4)
                .spawn(SWEntities.ShadowCreeper.class, 5, 2, 2);
        ShadowRiver = new SWBiome("ShadowRiver", -0.5F, 0.0F, () -> SWContent.ShadowDirt, () -> SWContent.ShadowDirt, 0, 0)
                .water(SWEntities.JellyFish.class, 10, 3, 6);
        CookieBiome = new SWBiome("Cookie World", 0.05F, 0.96F, () -> SWContent.CookieBlock, () -> SWContent.CookieBlock, 5, 0)
                .tree(() -> SWTrees.cookie(false)).spawn(SWEntities.CookiePig.class, 10, 4, 8).spawn(SWEntities.CookieSlime.class, 1, 2, 4);
        String[] names = {"shadow_forest", "shadow_jungle", "shadow_desert", "shadow_hills", "shadow_plains", "shadow_ocean", "dead_forest", "shadow_river", "cookie_world"};
        Biome[] all = {ShadowBiome, ShadowJungle, ShadowDesert, ShadowHills, ShadowPlains, ShadowOcean, ShadowDeadForest, ShadowRiver, CookieBiome};
        for (int i = 0; i < all.length; i++) reg.register(all[i].setRegistryName(Reference.MOD_ID, names[i]));
    }

    // ------------------------------------------------------------------ providers

    public static class ShadowProvider extends WorldProvider {
        @Override protected void init() {
            biomeProvider = new LegacyLayerBiomeProvider(world.getSeed(),
                    new Biome[]{ShadowBiome, ShadowDesert, ShadowJungle, ShadowHills, ShadowPlains, ShadowOcean, ShadowDeadForest}, 5);
            hasSkyLight = true;
        }
        @Override public DimensionType getDimensionType() { return SHADOW; }
        @Override public IChunkGenerator createChunkGenerator() {
            return new LegacyChunkGenerator(world, world.getSeed(), null)
                    .substitute(Blocks.STONE, SWContent.ShadowStone.getDefaultState())
                    .substitute(Blocks.WATER, SWContent.quicksilverStill.getDefaultState())
                    .substitute(Blocks.FLOWING_WATER, SWContent.quicksilverStill.getDefaultState())
                    .substitute(Blocks.ICE, SWContent.quicksilverIce.getDefaultState())
                    .substitute(Blocks.GRAVEL, SWContent.ShadowDirt.getDefaultState())
                    .substitute(Blocks.SAND, SWContent.ShadowSand.getDefaultState());
        }
        @Override public boolean isSurfaceWorld() { return false; }
        @Override public boolean canRespawnHere() { return true; }
        @Override public boolean canCoordinateBeSpawn(int x, int z) { return false; }
        @Override public BlockPos getSpawnCoordinate() { return new BlockPos(0, 64, 0); }
        @Override public double getMovementFactor() { return 1.0; }
        @Override public float calculateCelestialAngle(long time, float pt) {
            float f = ((int) (time % 24000L) + pt) / 24000.0F - 0.25F;
            if (f < 0) f++;
            if (f > 1) f--;
            float g = 1.0F - (float) ((Math.cos(f * Math.PI) + 1.0) / 2.0);
            return f + (g - f) / 3.0F;
        }
        @Override @SideOnly(Side.CLIENT) public boolean doesXZShowFog(int x, int z) { return true; }
        @Override @SideOnly(Side.CLIENT) public float getCloudHeight() { return -10.0F; }
        @Override @SideOnly(Side.CLIENT) public float[] calcSunriseSunsetColors(float a, float b) { return null; }
        @Override @SideOnly(Side.CLIENT) public Vec3d getFogColor(float a, float b) { return new Vec3d(0.02, 0.02, 0.05); }
        @Override @SideOnly(Side.CLIENT) public boolean isSkyColored() { return false; }
    }

    public static class CookieProvider extends WorldProvider {
        @Override protected void init() { biomeProvider = new BiomeProviderSingle(CookieBiome); hasSkyLight = true; }
        @Override public DimensionType getDimensionType() { return COOKIE; }
        @Override public IChunkGenerator createChunkGenerator() {
            return new LegacyChunkGenerator(world, world.getSeed(), null)
                    .substitute(Blocks.STONE, SWContent.ChocolateBlock.getDefaultState())
                    .substitute(Blocks.WATER, SWContent.chocoStill.getDefaultState())
                    .substitute(Blocks.FLOWING_WATER, SWContent.chocoStill.getDefaultState())
                    .substitute(Blocks.GRAVEL, SWContent.CookieBlock.getDefaultState())
                    .substitute(Blocks.SAND, SWContent.CookieBlock.getDefaultState());
        }
        @Override public boolean isSurfaceWorld() { return false; }
        @Override public boolean canRespawnHere() { return false; }
        @Override @SideOnly(Side.CLIENT) public float getStarBrightness(float f) { return 1.0F; }
    }

    // ------------------------------------------------------------------ structures (replayed from the original classes)

    private static final String STRUCT = "jajo_11/ShadowWorld/World/Structure/";
    private static final Map<String, LegacyClassStructure> CACHE = new HashMap<>();

    /** GETSTATIC jajo_11/ShadowWorld/ShadowWorld.X -> SWContent.X */
    private static Block resolve(String owner, String name) {
        if (!owner.equals("jajo_11/ShadowWorld/ShadowWorld")) return null;
        try {
            Field f = SWContent.class.getField(name);
            Object o = f.get(null);
            return o instanceof Block b ? b : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    public static synchronized LegacyClassStructure structure(String cls) {
        return CACHE.computeIfAbsent(cls, c -> {
            byte[] bytes = OriginalModAssets.readOriginal(Reference.MOD_ID, STRUCT + c + ".class");
            LegacyClassStructure s = LegacyClassStructure.load(bytes, SWWorld::resolve);
            ShadowWorld.LOG.info("Structure {}: {} blocks ({} unresolved)", c, s.size(), s.unresolved);
            return s;
        });
    }

    /** Place a replayed structure at the surface if the ground under (x,z) is one of {@code ground}. */
    static boolean place(World w, Random r, String cls, int x, int z, int yOff, Block... ground) {
        BlockPos top = w.getHeight(new BlockPos(x, 0, z)).down();
        if (top.getY() < 5) return false;
        Block g = w.getBlockState(top).getBlock();
        boolean ok = ground.length == 0;
        for (Block b : ground) ok |= b == g;
        if (!ok) return false;
        LegacyClassStructure s = structure(cls);
        if (s.size() == 0) return false;
        s.place(w, x, top.getY() + yOff, z, 2, null);
        return true;
    }

    public static class Gen implements IWorldGenerator {
        public static int rarity = 120;

        @Override
        public void generate(Random r, int cx, int cz, World w, IChunkGenerator gen, IChunkProvider prov) {
            int dim = w.provider.getDimension();
            int x = cx * 16 + 8, z = cz * 16 + 8;
            if (dim == SWContent.shadowDim()) shadow(w, r, x, z);
            else if (dim == SWContent.cookieDim()) cookie(w, r, x, z);
        }

        private void ore(World w, Random r, Block ore, int size, int count, int maxY, Block in, int x, int z) {
            WorldGenMinable m = new WorldGenMinable(ore.getDefaultState(), size, s -> s != null && s.getBlock() == in);
            for (int i = 0; i < count; i++) m.generate(w, r, new BlockPos(x + r.nextInt(16), 5 + r.nextInt(maxY), z + r.nextInt(16)));
        }

        private void shadow(World w, Random r, int x, int z) {
            ore(w, r, SWContent.AzuriteOre, 8, 7, 64, SWContent.ShadowStone, x, z);
            ore(w, r, SWContent.BloodStoneOre, 6, 2, 25, SWContent.ShadowStone, x, z);
            ore(w, r, SWContent.Soulstone, 4, 5, 28, SWContent.ShadowStone, x, z);
            Biome b = w.getBiome(new BlockPos(x, 0, z));
            int sx = x + r.nextInt(16), sz = z + r.nextInt(16);
            Block grass = SWContent.ShadowGrass, sand = SWContent.ShadowSand;
            if (b == ShadowPlains) {
                if (r.nextInt(rarity * 3) == 0) place(w, r, "WorldGenCookiePortal", sx, sz, 0, grass);
                else if (r.nextInt(rarity) == 0) place(w, r, "WorldGenHouse", sx, sz, 0, grass);
                else if (r.nextInt(rarity) == 0) place(w, r, "WorldGenPortal", sx, sz, 0, grass);
                if (r.nextInt(8) == 0) SWTrees.big().generate(w, r, w.getHeight(new BlockPos(sx, 0, sz)));
            } else if (b == ShadowBiome) {
                if (r.nextInt(rarity * 2) == 0) place(w, r, "WorldGenTower", sx, sz, 0, grass);
            } else if (b == ShadowHills) {
                int p = r.nextInt(rarity * 3);
                if (p == 0) place(w, r, "WorldGenCastle", sx, sz, 0, grass);
                else if (p == 1) place(w, r, "WorldGenFireTemple", sx, sz, 0, grass);
                else if (p == 2) place(w, r, "WorldGenWaterTemple", sx, sz, 0, grass);
                else if (p == 3) place(w, r, "WorldGenEarthTemple", sx, sz, 0, grass);
                else if (p == 4) place(w, r, "WorldGenAirTemple", sx, sz, 0, grass);
            } else if (b == ShadowDesert) {
                if (r.nextInt(rarity * 2) == 0) place(w, r, "WorldGenPyramide", sx, sz, 0, sand);
                else if (r.nextInt(rarity * 2) == 0 && place(w, r, "WoldGenShadowWolfKing", sx, sz, 0, sand)) {
                    SWEntities.ShadowWolfKing k = new SWEntities.ShadowWolfKing(w);
                    BlockPos p = w.getHeight(new BlockPos(sx + 15, 0, sz + 18));
                    k.setLocationAndAngles(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, 0, 0);
                    k.enablePersistence();
                    w.spawnEntity(k);
                }
                for (int i = 0; i < 4; i++) {
                    BlockPos p = w.getHeight(new BlockPos(x + r.nextInt(16), 0, z + r.nextInt(16)));
                    if (SWContent.ShadowCactus.canPlaceBlockAt(w, p)) w.setBlockState(p, SWContent.ShadowCactus.getDefaultState(), 2);
                }
            } else if (b == ShadowJungle) {
                for (int i = 0; i < 3; i++) {
                    BlockPos p = w.getHeight(new BlockPos(x + r.nextInt(16), 0, z + r.nextInt(16)));
                    if (SWContent.ShadowBush.canPlaceBlockAt(w, p)) w.setBlockState(p, SWContent.ShadowBush.getStateFromMeta(1), 2);
                }
            } else if (b == ShadowDeadForest) {
                if (r.nextInt(rarity) == 0) place(w, r, "WorldGenPedestal", sx, sz, 0, grass);
            }
            if (r.nextInt(rarity / 2) == 0) {
                int y = 10 + r.nextInt(40);
                LegacyClassStructure d = structure("WorldGenShadowDungeon");
                if (d.size() > 0 && w.getBlockState(new BlockPos(sx, y, sz)).getBlock() == SWContent.ShadowStone) d.place(w, sx, y, sz, 2, null);
            }
            for (int i = 0; i < 2; i++) {
                Block f = r.nextInt(3) == 0 ? SWContent.ShadowFlower2 : (b == ShadowDeadForest ? SWContent.ShadowFlower3 : SWContent.ShadowFlower);
                new SWTrees.Scatter(() -> f, 16).generate(w, r, w.getHeight(new BlockPos(x + r.nextInt(16), 0, z + r.nextInt(16))));
            }
        }

        private void cookie(World w, Random r, int x, int z) {
            ore(w, r, SWContent.DarkChocolateBlock, 15, 2, 100, SWContent.ChocolateBlock, x, z);
            ore(w, r, SWContent.WhiteChocolateBlock, 30, 1, 100, SWContent.ChocolateBlock, x, z);
            if (r.nextInt(rarity) == 0) place(w, r, "WorldGenCookieHouse", x + r.nextInt(16), z + r.nextInt(16), 0, SWContent.CookieBlock);
        }
    }

    static int clamp(int v) { return MathHelper.clamp(v, 0, 255); }
    static IBlockState air() { return Blocks.AIR.getDefaultState(); }
    static EnumCreatureType unused() { return EnumCreatureType.CREATURE; }
}
