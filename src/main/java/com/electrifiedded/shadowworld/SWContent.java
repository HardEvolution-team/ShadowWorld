package com.electrifiedded.shadowworld;

import com.electrifiedded.archaiclib.legacy.fluid.LegacyBucketHandler;
import com.electrifiedded.archaiclib.legacy.fluid.LegacyFluids;
import com.electrifiedded.archaiclib.legacy.registry.LegacyGameRegistry;
import com.electrifiedded.archaiclib.v1710.api.item.ArchaicItem;
import com.electrifiedded.archaiclib.crafting.LegacyCraftingManager;
import com.electrifiedded.shadowworld.block.SWBlock;
import com.electrifiedded.shadowworld.block.SWBlockTypes.*;
import com.electrifiedded.shadowworld.block.SWBlocks2.*;
import com.electrifiedded.shadowworld.item.SWItemTypes;
import com.electrifiedded.shadowworld.item.SWItemTypes.*;
import com.electrifiedded.shadowworld.world.SWTrees;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fluids.Fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** All Shadow World blocks and items (field names = the 1.7.10 ShadowWorld fields, so recipes/structures map 1:1). */
public final class SWContent {
    private SWContent() {}

    public static int DimID = -30;
    public static int shadowDim() { return DimID; }
    public static int cookieDim() { return DimID + 1; }

    public static final LegacyCraftingManager SHADOW_CRAFTING = new LegacyCraftingManager("shadowworld:shadow_crafting");
    public static final List<Block> BLOCKS = new ArrayList<>();

    public static final CreativeTabs ShadowBlocks = new CreativeTabs("ShadowBlocks") {
        @Override public ItemStack createIcon() { return new ItemStack(ShadowGrass); }
    };
    public static final CreativeTabs ShadowItems = new CreativeTabs("ShadowItems") {
        @Override public ItemStack createIcon() { return new ItemStack(AzuriteSword); }
    };

    static final Item.ToolMaterial MaterialAzurite = EnumHelper.addToolMaterial("SW_AZURITE", 2, 500, 8.0F, 2.5F, 14);
    static final Item.ToolMaterial MaterialShadowWood = EnumHelper.addToolMaterial("SW_SWOOD", 0, 50, 4.0F, 0.0F, 5);
    static final Item.ToolMaterial MaterialShadowStone = EnumHelper.addToolMaterial("SW_SSTONE", 1, 150, 5.0F, 1.0F, 2);
    static final Item.ToolMaterial MaterialShadow = EnumHelper.addToolMaterial("SW_SHADOW", 1, 100, 5.0F, 1.0F, 10);
    static final Item.ToolMaterial MaterialBloodStone = EnumHelper.addToolMaterial("SW_BLOOD", 3, 1500, 13.0F, 2.5F, 16);
    static final Item.ToolMaterial MaterialAncient = EnumHelper.addToolMaterial("SW_ANCIENT", 4, 2000, 24.0F, 3.5F, 100);
    static final ItemArmor.ArmorMaterial ArmorAzurite = EnumHelper.addArmorMaterial("SW_AZURITE", "shadowworld:azurite", 25, new int[]{2, 6, 7, 2}, 11, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 0F);
    static final ItemArmor.ArmorMaterial ArmorShadow = EnumHelper.addArmorMaterial("SW_SHADOW", "shadowworld:shadow", 20, new int[]{2, 4, 5, 2}, 8, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0F);
    static final ItemArmor.ArmorMaterial ArmorBloodStone = EnumHelper.addArmorMaterial("SW_BLOOD", "shadowworld:bloodstone", 30, new int[]{3, 7, 8, 3}, 15, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, 1F);
    static final ItemArmor.ArmorMaterial ArmorAncient = EnumHelper.addArmorMaterial("SW_ANCIENT", "shadowworld:ancient", 45, new int[]{4, 5, 10, 4}, 100, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, 2F);
    static final ItemArmor.ArmorMaterial ArmorAngel = EnumHelper.addArmorMaterial("SW_ANGEL", "shadowworld:angel", 20, new int[]{5, 6, 10, 5}, 100, SoundEvents.ITEM_ARMOR_EQUIP_GOLD, 2F);

    public static Fluid quicksilverFluid, chocoFluid;

    public static Block ShadowFire, HellFire, ShadowGrass, ShadowDirt, ShadowStone, ShadowLog, ShadowLeaf, ShadowCobble, AzuriteOre,
            BloodStoneOre, Soulstone, Shadowfield, ShadowFlower, ShadowFlower2, ShadowFlower3, ShadowSapling, SoulCrop, ShadowTallGrass1,
            ShadowTallGrass2, ShadowTallGrass3, ShadowCactus, GlowBean, Glowshroom, ShadowBush, ShadowPlanks, ShadowSand, ShadowGlass,
            ShadowStoneBricks, MossyShadowStoneBricks, CrackedShadowStoneBricks, ShadowStoneBricksBurnable, BloodStoneBlock, AzuriteBlock,
            GlowStoneTorch, ShadowFence, GolemEye, ShadowPlanksDoubleSlab, ShadowPlanksSingleSlab, ShadowCobbleDoubleSlab,
            ShadowCobbleSingleSlab, ShadowStoneBricksDoubleSlab, ShadowStoneBricksSingleSlab, LapisLazuliDoubleSlab, LapisLazuliSingleSlab,
            stairsShadowWood, stairsShadowCobble, stairsShadowStoneBricks, stairsLapisLazuli, Jumper, Trap, HealthStone, InvisibleStone,
            WheaterBlock, ShadowWorkBench, ShadowWorkBenchPlus, SoulFurnace, LitSoulFurnace, GoldenSoulFurnace, ShadowChest,
            ShadowChestTrapped, Pedestal, AzuriteRune, DwarvenLamp, PyramideStone, PyramideStoneTrap, MossyShadowPlanks, MossyShadowCobble,
            quicksilverStill, quicksilverIce, CookieBlock, CookieLog, CookieSpawner, ChocolateBlock, DarkChocolateBlock, WhiteChocolateBlock,
            CookieSapling, chocoStill, ShadowAir, HangingNote, Lamp, AngelBlock, AngelStairs, AngelDoubleSlab, AngelSingleSlab, MysticalLeaf,
            AngelChest, AngelPortal;
    public static ShadowPortal ShadowPortal;
    public static net.minecraft.enchantment.Enchantment holy;
    public static CookiePortal CookiePortal;

    public static Item Azurite, AzuriteShard, Shadow, BloodStone, Soul, ShadowCrystal, AzuriteSword, AzuritePickaxe, AzuriteAxe, AzuriteShovel,
            SWoodSword, SWoodPickaxe, SWoodAxe, SWoodShovel, SStoneSword, SStonePickaxe, SStoneAxe, SStoneShovel, AncientSword, AncientPickaxe,
            AncientAxe, AncientShovel, BloodStoneSword, BloodStonePickaxe, BloodStoneAxe, BloodStoneShovel, ShadowSword, ShadowPickaxe, ShadowAxe,
            ShadowShovel, AzuriteHoe, SWoodHoe, SStoneHoe, AncientHoe, BloodStoneHoe, ShadowHoe, helmetAzurite, plateAzurite, legsAzurite,
            bootsAzurite, helmetAncient, plateAncient, legsAncient, bootsAncient, helmetBloodStone, plateBloodStone, legsBloodStone,
            bootsBloodStone, helmetShadow, plateShadow, legsShadow, bootsShadow, helmetAngel, plateAngel, legsAngel, bootsAngel, ShadowBucketEmpty,
            ShadowBucketquicksilver, ShadowBucketlava, ShadowBucketwater, ShadowBucketchoco, Bucketquicksilver, Bucketchoco, ShadowPortalPlacer,
            ShadowFlintAndSteel, Health, JumperItem, WoodStaff, GreenCrystal, YellowCrystal, RedCrystal, WhiteCrystal, Teleporter, Confuser,
            Controller, Grower, zombieslayer, DungeonFinder, HellFlintAndSteel, ShadowCompass, CrossBow, FireCrossBow, PoisonousCrossBow,
            CrossBowArrow, BasicSword, BasicSwordPlus, swordWoodPlus, swordStonePlus, swordIronPlus, swordGoldPlus, swordDiamondPlus,
            AzuriteSwordPlus, SWoodSwordPlus, SStoneSwordPlus, BloodStoneSwordPlus, ShadowSwordPlus, AncientSwordPlus, GoldenSoul, BlackFeather,
            GlowingInkSac, MagicalPaper, AngelFeather, ShadowCoin, ShadowBerry, shadowpotion, GlowBeanItem, Diary, Note, BakedCookie, SweetCookie,
            GoldenCookie, DiamondCookie, ChatCookie, TeleportCookie, CookieSword, CookiePickaxe, CookieAxe, CookieShovel, Chocolate, DarkChocolate,
            WhiteChocolate, FireStone, WaterStone, EarthStone, AirStone, ElementSphere, FlyingBoat;

    // ------------------------------------------------------------------ helpers

    private static String reg(String name) { return name.replace(' ', '_').replace("+", "_plus").toLowerCase(Locale.ROOT); }

    private static <T extends Block> T b(String regName, String key, T block, CreativeTabs tab) {
        block.setTranslationKey("shadowworld:" + key);
        if (tab != null) block.setCreativeTab(tab);
        LegacyGameRegistry.registerBlock(block, reg(regName));
        LegacyGameRegistry.registerItemModel(block, 0, "shadowworld:" + reg(regName), "inventory");
        BLOCKS.add(block);
        return block;
    }

    private static <T extends Block> T b(String name, T block) { return b(name, name, block, ShadowBlocks); }

    private static SWBlock basic(String name, Material m, SoundType s, float hard, float res, String tex) {
        return b(name, (SWBlock) new SWBlock(m, s).tex(tex).hr(hard, res));
    }

    private static <T extends Item> T i(String regName, String key, String tex, T item, CreativeTabs tab) {
        item.setTranslationKey("shadowworld:" + key);
        if (tab != null) item.setCreativeTab(tab);
        LegacyGameRegistry.registerItem(item, reg(regName));
        ArchaicItem.registerMetaTexture(item, 0, "shadowworld:" + tex);
        return item;
    }

    private static <T extends Item> T i(String name, T item) { return i(name, name, name, item, ShadowItems); }

    // ------------------------------------------------------------------ registration

    public static void registerBlocks() {
        quicksilverFluid = LegacyFluids.create("sw_quicksilver", new net.minecraft.util.ResourceLocation("shadowworld", "blocks/quicksilver_still"),
                new net.minecraft.util.ResourceLocation("shadowworld", "blocks/quicksilver_flow"), 0xFFFFFFFF).setDensity(3000).setViscosity(3000);
        chocoFluid = LegacyFluids.create("sw_chocomilk", new net.minecraft.util.ResourceLocation("shadowworld", "blocks/ChocoStill"),
                new net.minecraft.util.ResourceLocation("shadowworld", "blocks/ChocoStill"), 0xFFFFFFFF).setViscosity(1500);

        ShadowPortal = b("ShadowPortal", "ShadowPortal", new ShadowPortal(), null);
        ShadowFire = b("ShadowFire", "ShadowFire", new Fire(true, "ShadowFire"), null);
        HellFire = b("HellFire", "HellFire", new Fire(false, "HellFire"), null);
        ShadowStone = b("ShadowStone", (SWBlock) new SWBlock(Material.ROCK, SoundType.STONE).tex("ShadowStone").hr(1.5F, 10F).drops(() -> Item.getItemFromBlock(ShadowCobble), 1, 1));
        ShadowGrass = b("ShadowGrass", new Grass().hr(1.0F, 5F));
        ShadowDirt = basic("ShadowDirt", Material.GROUND, SoundType.GROUND, 1.0F, 2.5F, "ShadowDirt");
        ShadowSand = b("ShadowSand", (SWBlock) new SWBlock(Material.SAND, SoundType.SAND).tex("ShadowSand").hr(1.0F, 2.5F));
        ShadowLog = b("ShadowLog", new Log("ShadowLogTop", "ShadowLogSide").hr(1F, 1F));
        ShadowSapling = b("ShadowSapling", new Plant(SWContent::shadowSoil).tree(() -> SWTrees.shadow(true)).tex("ShadowSapling"));
        ShadowLeaf = b("ShadowLeaf", new Leaves("ShadowLeaf", () -> ShadowSapling).hr(1F, 1F));
        ShadowGlass = b("ShadowGlass", (SWBlock) new SWBlock(Material.GLASS, SoundType.GLASS).tex("ShadowGlass").cutout().drops(() -> Items.AIR, 0, 0).hr(0.3F, 1.5F));
        ShadowCobble = basic("ShadowCobble", Material.ROCK, SoundType.STONE, 5F, 2.5F, "ShadowCobble");
        ShadowPlanks = basic("ShadowPlanks", Material.WOOD, SoundType.WOOD, 5F, 1F, "ShadowPlanks");
        PyramideStone = b("PyramideStone", (SWBlock) new SWBlock(Material.ROCK, SoundType.STONE).tex("PyramideStone").unbreakable());
        ShadowStoneBricks = basic("ShadowStoneBricks", Material.ROCK, SoundType.STONE, 10F, 10F, "ShadowStoneBricks");
        MossyShadowStoneBricks = basic("MossyShadowStoneBricks", Material.ROCK, SoundType.STONE, 10F, 10F, "MossyShadowStoneBricks");
        CrackedShadowStoneBricks = basic("CrackedShadowStoneBricks", Material.ROCK, SoundType.STONE, 10F, 10F, "CrackedShadowStoneBricks");
        ShadowStoneBricksBurnable = b("ShadowStoneBricksBurnable", "ShadowStoneBricksBurnable",
                new SWBlock(Material.ROCK, SoundType.STONE).tex("ShadowStoneBricks").hr(10F, 10F).fireSource(), CreativeTabs.SEARCH);
        MossyShadowCobble = basic("MossyShadowCobble", Material.ROCK, SoundType.STONE, 10F, 10F, "MossyShadowCobble");
        MossyShadowPlanks = basic("MossyShadowPlanks", Material.WOOD, SoundType.WOOD, 5F, 1F, "MossyShadowPlanks");

        stairsShadowWood = b("stairsShadowWood", "stairsWoodShadow", new Stairs(ShadowPlanks, "shadowworld:ShadowPlanks"), ShadowBlocks);
        stairsShadowCobble = b("stairsShadowCobble", new Stairs(ShadowCobble, "shadowworld:ShadowCobble"));
        stairsShadowStoneBricks = b("stairsShadowStoneBricks", new Stairs(ShadowStoneBricks, "shadowworld:ShadowStoneBricks"));
        stairsLapisLazuli = b("stairsLapisLazuli", "stairsLapislazuli", new Stairs(Blocks.LAPIS_BLOCK, "minecraft:blocks/lapis_block"), ShadowBlocks);
        ShadowPlanksDoubleSlab = b("ShadowPlanksDoubleSlab", "ShadowWoodDoubleSlab", new DoubleSlab(Material.WOOD, SoundType.WOOD, "ShadowPlanks", () -> ShadowPlanksSingleSlab).hr(2F, 5F), null);
        ShadowPlanksSingleSlab = b("ShadowPlanksSingleSlab", "ShadowWoodSingleSlab", new Slab(Material.WOOD, SoundType.WOOD, "ShadowPlanks", () -> ShadowPlanksDoubleSlab).hr(2F, 5F), ShadowBlocks);
        ShadowCobbleDoubleSlab = b("ShadowCobbleDoubleSlab", "ShadowCobbleDoubleSlab", new DoubleSlab(Material.ROCK, SoundType.STONE, "ShadowCobble", () -> ShadowCobbleSingleSlab).hr(2F, 10F), null);
        ShadowCobbleSingleSlab = b("ShadowCobbleSingleSlab", new Slab(Material.ROCK, SoundType.STONE, "ShadowCobble", () -> ShadowCobbleDoubleSlab).hr(2F, 10F));
        ShadowStoneBricksDoubleSlab = b("ShadowStoneBricksDoubleSlab", "ShadowStoneBricksDoubleSlab", new DoubleSlab(Material.ROCK, SoundType.STONE, "ShadowStoneBricks", () -> ShadowStoneBricksSingleSlab).hr(2F, 10F), null);
        ShadowStoneBricksSingleSlab = b("ShadowStoneBricksSingleSlab", new Slab(Material.ROCK, SoundType.STONE, "ShadowStoneBricks", () -> ShadowStoneBricksDoubleSlab).hr(2F, 10F));
        LapisLazuliDoubleSlab = b("LapisLazuliDoubleSlab", "LapisLazuliDoubleSlab", new DoubleSlab(Material.ROCK, SoundType.STONE, "minecraft:blocks/lapis_block", () -> LapisLazuliSingleSlab).hr(2F, 10F), null);
        LapisLazuliSingleSlab = b("LapisLazuliSingleSlab", new Slab(Material.ROCK, SoundType.STONE, "minecraft:blocks/lapis_block", () -> LapisLazuliDoubleSlab).hr(2F, 10F));

        ShadowWorkBench = b("ShadowWorkBench", new Workbench(false).hr(5F, 1F));
        ShadowWorkBenchPlus = b("ShadowWorkBenchPlus", new Workbench(true).hr(5F, 1F));
        SoulFurnace = b("SoulFurnace", new com.electrifiedded.shadowworld.tile.SoulFurnaces.FurnaceBlock(false, false));
        LitSoulFurnace = b("LitSoulFurnace", "SoulFurnace", new com.electrifiedded.shadowworld.tile.SoulFurnaces.FurnaceBlock(true, false), null);
        GoldenSoulFurnace = b("GoldenSoulFurnace", new com.electrifiedded.shadowworld.tile.SoulFurnaces.FurnaceBlock(false, true));
        net.minecraftforge.fml.common.registry.GameRegistry.registerTileEntity(com.electrifiedded.shadowworld.tile.SoulFurnaces.TileSoulFurnace.class, new net.minecraft.util.ResourceLocation("shadowworld", "soul_furnace"));
        ShadowChest = b("ShadowChest", new com.electrifiedded.shadowworld.tile.SWChests.ChestBlock(com.electrifiedded.shadowworld.tile.SWChests.Kind.SHADOW));
        ShadowChestTrapped = b("ShadowChestTrapped", new com.electrifiedded.shadowworld.tile.SWChests.ChestBlock(com.electrifiedded.shadowworld.tile.SWChests.Kind.SHADOW_TRAPPED));
        Pedestal = b("Pedestal", new com.electrifiedded.shadowworld.tile.Pedestals.PedestalBlock());
        net.minecraftforge.fml.common.registry.GameRegistry.registerTileEntity(com.electrifiedded.shadowworld.tile.Pedestals.TilePedestal.class, new net.minecraft.util.ResourceLocation("shadowworld", "pedestal"));
        AzuriteRune = b("AzuriteRune", "AzuriteRune", new SWBlock(Material.CIRCUITS, SoundType.GLASS).tex("AzuriteRune_cross").bounds(0, 0, 0, 1, 0.0625, 1).noCollide().cutout().light(0.25F).hr(0F, 0F).drops(() -> AzuriteShard, 1, 1), null);
        GlowStoneTorch = b("GlowStoneTorch", (SWBlock) new SWBlock(Material.CIRCUITS, SoundType.WOOD).tex("GlowStoneTorch").cross().light(1.0F).hr(0F, 0F));
        Shadowfield = b("Shadowfield", "Shadowfield", new Field().hr(1F, 10F), null);
        ShadowFence = b("ShadowFence", (SWBlock) new SWBlock(Material.WOOD, SoundType.WOOD).tex("ShadowPlanks").bounds(0.375, 0, 0.375, 0.625, 1, 0.625).hr(5F, 1F));
        DwarvenLamp = basic("DwarvenLamp", Material.GLASS, SoundType.GLASS, 0.3F, 1.5F, "DwarvenLamp").light(1.0F);
        Soulstone = b("SoulStone", "Soulstone", new SWBlock(Material.ROCK, SoundType.STONE).tex("Soulstone").hr(5F, 1F).drops(() -> Soul, 1, 1), ShadowBlocks);
        AzuriteOre = b("AzuriteOre", "AzuriteOre", new SWBlock(Material.ROCK, SoundType.STONE).tex("Azurite").hr(5F, 1F).light(0.75F).drops(() -> Azurite, 1, 1), CreativeTabs.SEARCH);
        BloodStoneOre = b("BloodStoneOre", "BloodStoneOre", new SWBlock(Material.ROCK, SoundType.STONE).tex("BloodStoneOre").hr(5F, 1F).light(0.25F), CreativeTabs.SEARCH);
        BloodStoneBlock = b("BloodStoneBlock", (SWBlock) new SWBlock(Material.ROCK, SoundType.STONE).tex("BloodStoneBlock").hr(5F, 1F).light(0.5F).beacon());
        AzuriteBlock = b("AzuriteBlock", (SWBlock) new SWBlock(Material.ROCK, SoundType.STONE).tex("AzuriteBlock").hr(5F, 1F).light(0.75F).beacon());
        Jumper = b("Jumper", (SWBlock) new SWBlock(Material.GROUND, SoundType.STONE).tex("Jumper").hr(2F, 10F).walk((w, p, e) -> e.motionY = 5.0));
        Trap = b("Trap", (SWBlock) new SWBlock(Material.GROUND, SoundType.STONE).tex("Trap").hr(2F, 10F).walk((w, p, e) -> {
            e.setFire(100);
            e.attackEntityFrom(DamageSource.IN_FIRE, 3.0F);
            w.playSound(null, p, SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 0.6F);
        }));
        HealthStone = b("HealthStone", (SWBlock) new SWBlock(Material.ROCK, SoundType.STONE).tex("HealthStone").hr(2F, 10F).use((w, p, s, pl) -> {
            pl.heal(20.0F);
            w.playSound(null, p, SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 0.6F);
            return true;
        }));
        InvisibleStone = b("InvisibleStone", (SWBlock) new SWBlock(Material.ROCK, SoundType.STONE).tex("InvisibleStone").hr(2F, 10F).walk((w, p, e) -> {
            if (w.isRemote || e.ticksExisted % 20 != 0) return;
            e.setInvisible(!e.isInvisible());
            w.playSound(null, p, SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 0.6F);
        }));
        PyramideStoneTrap = b("PyramideStoneTrap", (SWBlock) new SWBlock(Material.ROCK, SoundType.STONE).tex("PyramideStone").unbreakable().noCollide());
        WheaterBlock = b("WheaterBlock", new Weather().hr(2F, 10F));
        quicksilverStill = b("quicksilverStill", "quicksilver", new Fluid2(quicksilverFluid, true).setHardness(100F).setLightOpacity(2), null);
        LegacyFluids.ignoreLevel(quicksilverStill);
        quicksilverIce = b("quicksilverIce", "quicksilverice", new SWBlock(Material.ICE, SoundType.GLASS).tex("quicksilverice").translucent().hr(0.75F, 3.75F), ShadowBlocks);
        quicksilverIce.setDefaultSlipperiness(0.98F);

        ShadowFlower = b("ShadowFlower", new Plant(SWContent::shadowSoil).tex("ShadowFlower").light(0.75F));
        ShadowFlower2 = b("ShadowFlower2", new Plant(SWContent::shadowSoil).tex("ShadowFlower2").light(0.9F));
        ShadowFlower3 = b("ShadowFlower3", new Plant(SWContent::shadowSoil).tex("ShadowFlower3"));
        ShadowTallGrass1 = b("ShadowTallGrass1", new TallGrass(SWContent::shadowSoil).tex("ShadowTallGrass1"));
        ShadowTallGrass2 = b("ShadowTallGrass2", new TallGrass(SWContent::shadowSoil).tex("ShadowTallGrass2"));
        ShadowTallGrass3 = b("ShadowTallGrass3", new TallGrass(SWContent::shadowSoil).tex("ShadowTallGrass3"));
        SoulCrop = b("SoulCrop", "SoulCrop", new Crop(), null);
        ShadowCactus = b("ShadowCactus", new Cactus().hr(0.4F, 2F));
        ShadowBush = b("ShadowBush", new Bush().hr(0.2F, 1F));
        GlowBean = b("GlowBean", "GlowBean", new Plant(bl -> bl == ShadowLog || shadowSoil(bl)).tex("GlowBean_stage_2").light(0.75F), null);
        Glowshroom = b("Glowshroom", new Plant(bl -> bl != Blocks.AIR).tex("Glowshroom").light(0.75F));

        CookiePortal = b("CookiePortal", "CookiePortal", new CookiePortal(), null);
        CookieBlock = b("CookieBlock", new Cookie().hr(1F, 1F).light(1.0F));
        CookieLog = b("CookieLog", new Log("CookieLogTop", "CookieLogSide").hr(1F, 1F).light(0.25F));
        CookieSpawner = b("CookieBlockSpawner", "CookieSpawner", new CookieSpawner().hr(1F, 1F).light(0.5F), ShadowBlocks);
        ChocolateBlock = basic("ChocolateBlock", Material.CAKE, SoundType.CLOTH, 2F, 10F, "ChocolateBlock");
        DarkChocolateBlock = basic("DarkChocolateBlock", Material.CAKE, SoundType.CLOTH, 2F, 10F, "DarkChocolateBlock");
        CookieSapling = b("Cookie Sapling", "CookieSapling", new Plant(bl -> bl == CookieBlock || shadowSoil(bl)).tree(() -> SWTrees.cookie(true)).tex("CookieSapling"), ShadowBlocks);
        WhiteChocolateBlock = b("WhiteChocolateBlock", (SWBlock) new SWBlock(Material.CAKE, SoundType.CLOTH).tex("WhiteChocolateBlock").hr(2F, 10F)
                .drops(() -> Item.getItemFromBlock(WhiteChocolateBlock), 1, 1));
        chocoStill = b("chocomilkStill", "chocomilkstill", new Fluid2(chocoFluid, false).setHardness(100F).setLightOpacity(2), null);
        LegacyFluids.ignoreLevel(chocoStill);
        ShadowAir = b("Air", "ShadowAir", new SWBlock(Material.AIR, SoundType.STONE) {
            @Override public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState s) { return net.minecraft.util.EnumBlockRenderType.INVISIBLE; }
            @Override public boolean isReplaceable(net.minecraft.world.IBlockAccess w, net.minecraft.util.math.BlockPos p) { return true; }
        }.noCollide().light(0.5F).bounds(0, 0, 0, 0, 0, 0), null);
        HangingNote = b("HangingNote", "HangingNote", new com.electrifiedded.shadowworld.tile.SWNotes.HangingNoteBlock().hr(0F, 0F), null);
        net.minecraftforge.fml.common.registry.GameRegistry.registerTileEntity(com.electrifiedded.shadowworld.tile.SWNotes.TileHangingNote.class, new net.minecraft.util.ResourceLocation("shadowworld", "hanging_note"));
        Lamp = b("Lamp", new com.electrifiedded.shadowworld.tile.SWNotes.LampBlock());
        net.minecraftforge.fml.common.registry.GameRegistry.registerTileEntity(com.electrifiedded.shadowworld.tile.SWNotes.TileLamp.class, new net.minecraft.util.ResourceLocation("shadowworld", "lamp"));
        GolemEye = b("GolemEye", (SWBlock) new SWBlock(Material.ROCK, SoundType.STONE).tex("GolemEye").hr(1F, 1F).light(0.1F));
        AngelBlock = b("AngelBock", "AngelBlock", new com.electrifiedded.shadowworld.block.SWBlocks2.AngelQuartz().hr(0.8F, 4F), ShadowBlocks);
        AngelStairs = b("AngelStairs", new Stairs(AngelBlock, "shadowworld:angel_block_side"));
        AngelDoubleSlab = b("AngelDoubleSlab", "AngelDoubleSlab", new DoubleSlab(Material.ROCK, SoundType.STONE, "angel_block_side", () -> AngelSingleSlab).hr(0.8F, 4F), null);
        AngelSingleSlab = b("AngelSingleSlab", new Slab(Material.ROCK, SoundType.STONE, "angel_block_side", () -> AngelDoubleSlab).hr(0.8F, 4F));
        MysticalLeaf = b("MysticalLeaf", new Leaves("MysticalLeaf", null).hr(1F, 1F));
        AngelChest = b("AngelChest", new com.electrifiedded.shadowworld.tile.SWChests.ChestBlock(com.electrifiedded.shadowworld.tile.SWChests.Kind.ANGEL));
        net.minecraftforge.fml.common.registry.GameRegistry.registerTileEntity(com.electrifiedded.shadowworld.tile.SWChests.TileSWChest.class, new net.minecraft.util.ResourceLocation("shadowworld", "sw_chest"));
        AngelPortal = b("AngelPortal", "AngelPortal", new com.electrifiedded.shadowworld.tile.AngelPortals.PortalBlock(), null);
        net.minecraftforge.fml.common.registry.GameRegistry.registerTileEntity(com.electrifiedded.shadowworld.tile.AngelPortals.TilePortal.class, new net.minecraft.util.ResourceLocation("shadowworld", "angel_portal"));
    }

    public static boolean shadowSoil(Block b) { return b == ShadowGrass || b == ShadowDirt || b == Shadowfield; }

    public static void registerItems() {
        Azurite = i("Azurite", new Item());
        AzuriteShard = i("AzuriteShard", new Placer(() -> AzuriteRune, null));
        Shadow = i("Shadow", new ItemFood(4, 0.6F, true).setAlwaysEdible().setPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 100 * 20, 0), 1.0F));
        BloodStone = i("BloodStone", new Item());
        Soul = i("Soul", new ItemSeeds(SoulCrop, Shadowfield) {
            @Override public int getItemBurnTime(ItemStack s) { return 5000; }
        });
        ShadowCrystal = i("ShadowCrystal", new Item());

        AzuriteSword = i("AzuriteSword", new Sword(MaterialAzurite, 4F));
        AzuritePickaxe = i("AzuritePickaxe", new Pickaxe(MaterialAzurite));
        AzuriteAxe = i("AzuriteAxe", new Axe(MaterialAzurite));
        AzuriteShovel = i("AzuriteShovel", new Spade(MaterialAzurite));
        SWoodSword = i("SWoodSword", new Sword(MaterialShadowWood, 4F));
        SWoodPickaxe = i("SWoodPickaxe", new Pickaxe(MaterialShadowWood));
        SWoodAxe = i("SWoodAxe", new Axe(MaterialShadowWood));
        SWoodShovel = i("SWoodShovel", new Spade(MaterialShadowWood));
        SStoneSword = i("SStoneSword", new Sword(MaterialShadowStone, 4F));
        SStonePickaxe = i("SStonePickaxe", new Pickaxe(MaterialShadowStone));
        SStoneAxe = i("SStoneAxe", new Axe(MaterialShadowStone));
        SStoneShovel = i("SStoneShovel", new Spade(MaterialShadowStone));
        AncientSword = i("AncientSword", new Sword(MaterialAncient, 4F));
        AncientPickaxe = i("AncientPickaxe", new Pickaxe(MaterialAncient));
        AncientAxe = i("AncientAxe", new Axe(MaterialAncient));
        AncientShovel = i("AncientShovel", new Spade(MaterialAncient));
        BloodStoneSword = i("BloodStoneSword", new Sword(MaterialBloodStone, 4F));
        BloodStonePickaxe = i("BloodStonePickaxe", new Pickaxe(MaterialBloodStone));
        BloodStoneAxe = i("BloodStoneAxe", new Axe(MaterialBloodStone));
        BloodStoneShovel = i("BloodStoneShovel", new Spade(MaterialBloodStone));
        ShadowSword = i("ShadowSword", new Sword(MaterialShadow, 4F));
        ShadowPickaxe = i("ShadowPickaxe", new Pickaxe(MaterialShadow));
        ShadowAxe = i("ShadowAxe", new Axe(MaterialShadow));
        ShadowShovel = i("ShadowShovel", new Spade(MaterialShadow));
        AzuriteHoe = i("AzuriteHoe", new Hoe(MaterialAzurite));
        SWoodHoe = i("SWoodHoe", new Hoe(MaterialAzurite));
        SStoneHoe = i("SStoneHoe", new Hoe(MaterialAzurite));
        AncientHoe = i("AncientHoe", new Hoe(MaterialAncient));
        BloodStoneHoe = i("BloodStoneHoe", new Hoe(MaterialBloodStone));
        ShadowHoe = i("ShadowHoe", new Hoe(MaterialAzurite));

        String[][] armor = {{"Azurite"}, {"Ancient"}, {"BloodStone"}, {"Shadow"}, {"Angel"}};
        ItemArmor.ArmorMaterial[] mats = {ArmorAzurite, ArmorAncient, ArmorBloodStone, ArmorShadow, ArmorAngel};
        EntityEquipmentSlot[] slots = {EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};
        String[] regPre = {"helmet", "plate", "legs", "boots"}, keyPre = {"helmet", "chestplate", "leggings", "boots"}, texSuf = {"Helmet", "Chestplate", "Leggings", "Boots"};
        Item[][] out = new Item[5][4];
        for (int m = 0; m < 5; m++)
            for (int s = 0; s < 4; s++) {
                String n = armor[m][0];
                boolean glint = n.equals("Ancient") || n.equals("Angel");
                out[m][s] = i(regPre[s] + n, keyPre[s] + n, n + texSuf[s], new Armor(mats[m], slots[s], n, glint), ShadowItems);
            }
        helmetAzurite = out[0][0]; plateAzurite = out[0][1]; legsAzurite = out[0][2]; bootsAzurite = out[0][3];
        helmetAncient = out[1][0]; plateAncient = out[1][1]; legsAncient = out[1][2]; bootsAncient = out[1][3];
        helmetBloodStone = out[2][0]; plateBloodStone = out[2][1]; legsBloodStone = out[2][2]; bootsBloodStone = out[2][3];
        helmetShadow = out[3][0]; plateShadow = out[3][1]; legsShadow = out[3][2]; bootsShadow = out[3][3];
        helmetAngel = out[4][0]; plateAngel = out[4][1]; legsAngel = out[4][2]; bootsAngel = out[4][3];

        ShadowBucketEmpty = i("ShadowBucket", "ShadowBucket", "ShadowBucket_empty", new ItemBucket(Blocks.AIR).setMaxStackSize(16), ShadowItems);
        ShadowBucketquicksilver = i("ShadowBucketquicksilver", "QuicksilverSBucket", "ShadowBucket_quicksilver", new ItemBucket(quicksilverStill).setContainerItem(ShadowBucketEmpty), ShadowItems);
        ShadowBucketlava = i("ShadowBucketlava", "LavaSBucket", "ShadowBucket_lava", new ItemBucket(Blocks.FLOWING_LAVA).setContainerItem(ShadowBucketEmpty), ShadowItems);
        ShadowBucketwater = i("ShadowBucketwater", "WaterSBucket", "ShadowBucket_water", new ItemBucket(Blocks.FLOWING_WATER).setContainerItem(ShadowBucketEmpty), ShadowItems);
        ShadowBucketchoco = i("ShadowBucketchoco", "ChocoSBucket", "ShadowBucket_choco", new ItemBucket(chocoStill).setContainerItem(ShadowBucketEmpty), ShadowItems);
        Bucketquicksilver = i("Bucketquicksilver", "QuicksilverBucket", "Bucket_quicksilver", new ItemBucket(quicksilverStill).setContainerItem(Items.BUCKET), ShadowItems);
        Bucketchoco = i("Bucketchoco", "ChocoBucket", "Bucket_choco", new ItemBucket(chocoStill).setContainerItem(Items.BUCKET), ShadowItems);
        LegacyBucketHandler.register(quicksilverStill, Bucketquicksilver);
        LegacyBucketHandler.register(chocoStill, Bucketchoco);

        ShadowPortalPlacer = i("ShadowPortalPlacer", "ShadowPortalPlacer", "PortalPlacer", new PortalPlacer(), null);
        ShadowFlintAndSteel = i("ShadowFlintAndSteel", new Lighter(() -> ShadowFire));
        Health = i("Health", new Food(0, 0F, false).tooltip("An extra Health").onEaten(SWItemTypes::extraHealth).setAlwaysEdible().setMaxStackSize(10));
        JumperItem = i("JumperItem", new Jumper());
        WoodStaff = i("WoodStaff", new Item().setMaxStackSize(1));
        GreenCrystal = i("GreenCrystal", new Item());
        YellowCrystal = i("YellowCrystal", new Item());
        RedCrystal = i("RedCrystal", new Item());
        WhiteCrystal = i("WhiteCrystal", new Item());
        Teleporter = i("Teleporter", new TeleporterStaff());
        Confuser = i("Confuser", new ConfuserStaff());
        Controller = i("Controller", new ControllerStaff());
        Grower = i("Grower", new GrowerStaff());
        zombieslayer = i("zombieslayer", "zombieslayer", "ZombieSlayer", new ZombieSlayer(), ShadowItems);
        DungeonFinder = i("DungeonFinder", new com.electrifiedded.shadowworld.item.SWFinders.DungeonFinderItem());
        HellFlintAndSteel = i("HellFlintAndSteel", new Lighter(() -> HellFire));
        ShadowCompass = i("ShadowCompass", "ShadowCompass", "ShadowCompass", new com.electrifiedded.shadowworld.item.SWFinders.ShadowCompassItem(), ShadowItems);
        CrossBow = i("CrossBow", "Crossbow", "Crossbow", new Crossbow(1), ShadowItems);
        FireCrossBow = i("FireCrossBow", "FireCrossbow", "FireCrossbow", new Crossbow(2), ShadowItems);
        PoisonousCrossBow = i("PoisonousCrossBow", "PoisonousCrossbow", "PoisonousCrossbow", new Crossbow(3), ShadowItems);
        CrossBowArrow = i("CrossBowArrow", new Item());
        BasicSword = i("BasicSword", new Sword(Item.ToolMaterial.IRON, 4F));
        BasicSwordPlus = i("BasicSword+", "BasicSwordPlus", "BasicSwordPlus", new Sword(Item.ToolMaterial.DIAMOND, 4F), ShadowItems);
        swordWoodPlus = i("swordWoodPlus", "swordWoodPlus", "WoodSwordPlus", new Sword(Item.ToolMaterial.WOOD, 7F), ShadowItems);
        swordStonePlus = i("swordStonePlus", "swordStonePlus", "StoneSwordPlus", new Sword(Item.ToolMaterial.STONE, 7F), ShadowItems);
        swordIronPlus = i("swordIronPlus", "swordIronPlus", "IronSwordPlus", new Sword(Item.ToolMaterial.IRON, 7F), ShadowItems);
        swordGoldPlus = i("swordGoldPlus", "swordGoldPlus", "GoldSwordPlus", new Sword(Item.ToolMaterial.GOLD, 7F), ShadowItems);
        swordDiamondPlus = i("swordDiamondPlus", "swordDiamondPlus", "DiamondSwordPlus", new Sword(Item.ToolMaterial.DIAMOND, 7F), ShadowItems);
        AzuriteSwordPlus = i("AzuriteSwordPlus", new Sword(MaterialAzurite, 7F));
        SWoodSwordPlus = i("SWoodSwordPlus", new Sword(MaterialShadowWood, 7F));
        SStoneSwordPlus = i("SStoneSwordPlus", new Sword(MaterialShadowStone, 7F));
        BloodStoneSwordPlus = i("BloodStoneSwordPlus", new Sword(MaterialBloodStone, 7F));
        ShadowSwordPlus = i("ShadowSwordPlus", new Sword(MaterialShadow, 7F));
        AncientSwordPlus = i("AncientSwordPlus", new Sword(MaterialAncient, 7F));
        GoldenSoul = i("GoldenSoul", new Simple().glint());
        BlackFeather = i("BlackFeather", new Item());
        GlowingInkSac = i("GlowingInkSac", new Item());
        MagicalPaper = i("MagicalPaper", new Item());
        AngelFeather = i("AngelFeather", new Item());
        ShadowCoin = i("ShadowCoin", new Item().setMaxStackSize(32));
        ShadowBerry = i("ShadowBerry", new ItemFood(1, 0.3F, false).setAlwaysEdible().setMaxStackSize(16));
        shadowpotion = i("shadowpotion", "shadowpotion", "ShadowPotion", new Food(0, 0F, false).onEaten((s, w, p) -> {
            p.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 3600, 0));
            p.addPotionEffect(new PotionEffect(MobEffects.SPEED, 3600, 1));
        }).setAlwaysEdible().setMaxStackSize(1), ShadowItems);
        GlowBeanItem = i("GlowBeanItem", new Placer(() -> GlowBean, st -> st.getBlock() == ShadowLog || shadowSoil(st.getBlock())));
        Diary = i("Diary", new com.electrifiedded.shadowworld.tile.SWNotes.DiaryItem());
        Note = i("Note", new com.electrifiedded.shadowworld.tile.SWNotes.NoteItem());
        BakedCookie = i("BakedCookie", new ItemFood(4, 0.5F, false).setMaxStackSize(16));
        SweetCookie = i("SweetCookie", new Food(2, 0.5F, false).onEaten((s, w, p) -> {
            p.addPotionEffect(new PotionEffect(MobEffects.SPEED, 600, 6));
            p.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 600, 4));
        }).setMaxStackSize(16));
        GoldenCookie = i("GoldenCookie", new ItemFood(10, 0.5F, false).setAlwaysEdible().setPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 100 * 20, 9), 1.0F).setMaxStackSize(16));
        DiamondCookie = i("DiamondCookie", new ItemFood(10, 0.5F, false).setAlwaysEdible().setPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 100 * 20, 9), 1.0F).setMaxStackSize(16));
        ChatCookie = i("ChatCookie", new Food(0, 0.1F, false).tooltip("A strange Cookie").onEaten(SWItemTypes::chatCookie).setAlwaysEdible().setMaxStackSize(16));
        TeleportCookie = i("TeleportCookie", new Food(0, 0.1F, false).tooltip("Teleports you to the Cookie World").onEaten(SWItemTypes::teleportCookie).setAlwaysEdible().setMaxStackSize(1));
        CookieSword = i("CookieSword", new SWItemTypes.CookieSword());
        CookiePickaxe = i("CookiePickaxe", new SWItemTypes.CookiePickaxe());
        CookieAxe = i("CookieAxe", new SWItemTypes.CookieAxe());
        CookieShovel = i("CookieShovel", new CookieSpade());
        Chocolate = i("Chocolate", new ItemFood(2, 0.5F, false).setMaxStackSize(16));
        DarkChocolate = i("DarkChocolate", new ItemFood(2, 0.5F, false).setMaxStackSize(16));
        WhiteChocolate = i("WhiteChocolate", new ItemFood(2, 0.6F, false).setMaxStackSize(16));
        FireStone = i("FireStone", new ElementStone(0));
        WaterStone = i("WaterStone", new ElementStone(1));
        EarthStone = i("EarthStone", new ElementStone(2));
        AirStone = i("AirStone", new ElementStone(3));
        ElementSphere = i("ElementSphere", new ElementStone(4));
        FlyingBoat = i("FlyingBoat", new Simple() {
            @Override
            public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(net.minecraft.world.World w, net.minecraft.entity.player.EntityPlayer p, net.minecraft.util.EnumHand h) {
                ItemStack s = p.getHeldItem(h);
                com.electrifiedded.shadowworld.entity.EntityFlyingBoat.place(w, p, s);
                return new net.minecraft.util.ActionResult<>(net.minecraft.util.EnumActionResult.SUCCESS, s);
            }
        }.rarity(EnumRarity.EPIC).setMaxStackSize(1));
    }

    static Entity unused(EntityLivingBase e) { return e; }
}
