package com.electrifiedded.shadowworld;

import com.electrifiedded.archaiclib.legacy.registry.LegacyGameRegistry;
import com.electrifiedded.archaiclib.original.OriginalModAssets;
import com.electrifiedded.archaiclib.crafting.LegacyCraftingContainer;
import com.electrifiedded.shadowworld.block.SWBlocks2;
import com.electrifiedded.shadowworld.entity.EntityElementStone;
import com.electrifiedded.shadowworld.entity.SWEntities;
import com.electrifiedded.shadowworld.entity.SWAngels;
import com.electrifiedded.shadowworld.world.SWWorld;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, dependencies = "required-after:archaiclib@[1.40.0,)")
public class ShadowWorld {
    public static final Logger LOG = LogManager.getLogger("ShadowWorld");
    public static final String ORIGINAL_NAME = "shadow_world_mod_1.7.10.jar - Shadow World 1.2.1 for 1.7.10 (by JaJo_11)";
    public static final String ORIGINAL_URL = "http://www.minecraftforum.net/topic/2537659-shadow-world-101/";

    @Mod.Instance(Reference.MOD_ID)
    public static ShadowWorld instance;

    @SidedProxy(clientSide = "com.electrifiedded.shadowworld.client.ClientProxy", serverSide = "com.electrifiedded.shadowworld.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        OriginalModAssets.require(Reference.MOD_ID, "shadowworld", ORIGINAL_NAME, ORIGINAL_URL, Reference.MOD_ID);
        Configuration cfg = new Configuration(e.getSuggestedConfigurationFile());
        cfg.load();
        SWContent.DimID = cfg.getInt("shadowDimension", "general", -30, -1000, 1000, "Shadow World dimension id; the Cookie World uses id + 1");
        SWWorld.Gen.rarity = cfg.getInt("structureRarity", "general", 120, 4, 10000, "1 in N chunks tries each structure (higher = rarer)");
        if (cfg.hasChanged()) cfg.save();

        com.electrifiedded.archaiclib.legacy.sound.LegacyModSounds.autoRegister(Reference.MOD_ID);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new SWMisc.Registrar());
        com.electrifiedded.shadowworld.net.SWNet.register();
        SWContent.registerBlocks();
        SWContent.registerItems();
        SWWorld.registerBiomes(ForgeRegistries.BIOMES);
        entities();
        com.electrifiedded.shadowworld.world.SWVillage.registerIO();
        SWWorld.SHADOW = DimensionType.register("shadow_world", "_shadowworld", SWContent.shadowDim(), SWWorld.ShadowProvider.class, false);
        SWWorld.COOKIE = DimensionType.register("cookie_world", "_cookieworld", SWContent.cookieDim(), SWWorld.CookieProvider.class, false);
        DimensionManager.registerDimension(SWContent.shadowDim(), SWWorld.SHADOW);
        DimensionManager.registerDimension(SWContent.cookieDim(), SWWorld.COOKIE);
        com.electrifiedded.shadowworld.world.SWAngelWorld.ANGEL = DimensionType.register("angel_world", "_angelworld", com.electrifiedded.shadowworld.world.SWAngelWorld.angelDim(), com.electrifiedded.shadowworld.world.SWAngelWorld.AngelProvider.class, false);
        DimensionManager.registerDimension(com.electrifiedded.shadowworld.world.SWAngelWorld.angelDim(), com.electrifiedded.shadowworld.world.SWAngelWorld.ANGEL);
        GameRegistry.registerWorldGenerator(new SWWorld.Gen(), 100);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new Gui());
        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        com.electrifiedded.shadowworld.world.SWLoot.init();
        SWRecipes.register();
        com.electrifiedded.shadowworld.tile.Pedestals.registerRecipes();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new SWEvents());
        proxy.init();
    }

    /** Parse the replayed structures once when a server starts (logs block counts, avoids a hitch on first generation). */
    @Mod.EventHandler
    public void serverStarting(net.minecraftforge.fml.common.event.FMLServerStartingEvent e) {
        e.registerServerCommand(new SWMisc.BrightnessCommand());
        for (String c : new String[]{"WorldGenTower", "WorldGenCastle", "WorldGenPyramide", "WoldGenShadowWolfKing", "WorldGenFireTemple",
                "WorldGenWaterTemple", "WorldGenEarthTemple", "WorldGenAirTemple", "WorldGenHouse", "WorldGenPortal", "WorldGenCookiePortal",
                "WorldGenPedestal", "WorldGenCookieHouse"}) SWWorld.structure(c);
    }

    private void entities() {
        int id = 0;
        LegacyGameRegistry.registerModEntity(SWEntities.Light.class, "Light", id++, this, 64, 3, true, 15720064, 16775294);
        LegacyGameRegistry.registerModEntity(SWEntities.SkeletonSpider.class, "SkeletonSpider", id++, this, 64, 3, true, 16382458, 12369083);
        LegacyGameRegistry.registerModEntity(SWEntities.Shadow.class, "Shadow", id++, this, 64, 3, true, 0, 0x333333);
        LegacyGameRegistry.registerModEntity(SWEntities.WolfSpider.class, "WolfSpider", id++, this, 64, 3, true, 0x3E8C39, 11961171);
        LegacyGameRegistry.registerModEntity(SWEntities.ShadowWolf.class, "ShadowWolf", id++, this, 64, 3, true, 10000001, 13545366);
        LegacyGameRegistry.registerModEntity(SWEntities.ShadowSlime.class, "ShadowSlime", id++, this, 64, 3, true, 191960, 181960);
        LegacyGameRegistry.registerModEntity(SWEntities.CookiePig.class, "CookiePig", id++, this, 64, 3, true, 0xC87F40, 0x5A3A1A);
        LegacyGameRegistry.registerModEntity(SWEntities.PassiveSpider.class, "PassiveSpider", id++, this, 64, 3, true, 3014592, 3014451);
        LegacyGameRegistry.registerModEntity(SWEntities.ShadowWolfKing.class, "ShadowWolfKing", id++, this, 80, 3, true, 4144959, 16382458);
        LegacyGameRegistry.registerModEntity(SWEntities.ShadowChicken.class, "ShadowChicken", id++, this, 64, 3, true, 0, 12369083);
        LegacyGameRegistry.registerModEntity(SWEntities.JellyFish.class, "JellyFish", id++, this, 64, 3, true, 13615741, 14534269);
        LegacyGameRegistry.registerModEntity(SWEntities.Dragonfly.class, "ShadowDragonfly", id++, this, 64, 3, true, 3014451, 0);
        LegacyGameRegistry.registerModEntity(com.electrifiedded.shadowworld.entity.SWDwarves.Dwarf.class, "Dwarf", id++, this, 64, 3, true, 11035942, 14200196);
        LegacyGameRegistry.registerModEntity(SWAngels.Angel.class, "Angel", id++, this, 80, 3, true, 12771552, 14869218);
        LegacyGameRegistry.registerModEntity(SWEntities.ShadowPig.class, "ShadowPig", id++, this, 64, 3, true, 1977932, 1450300);
        LegacyGameRegistry.registerModEntity(SWEntities.CookieSlime.class, "CookieSlime", id++, this, 64, 3, true, 15175746, 5713690);
        LegacyGameRegistry.registerModEntity(SWEntities.ShadowCreeper.class, "ShadowCreeper", id++, this, 64, 3, true, 3546880, 3288620);
        LegacyGameRegistry.registerModEntity(SWEntities.ExplodingWolf.class, "ExplodingWolf", id++, this, 64, 3, true, 10000001, 6881082);
        LegacyGameRegistry.registerModEntity(com.electrifiedded.shadowworld.entity.SWDwarves.TravellingDwarf.class, "TravellingDwarf", id++, this, 64, 3, true, 11035942, 14200196);
        LegacyGameRegistry.registerModEntity(SWAngels.AngelVillager.class, "AngelVillager", id++, this, 64, 3, true, 12771552, 14869218);
        LegacyGameRegistry.registerModEntity(SWEntities.FireGolem.class, "FireGolem", id++, this, 64, 3, true, 0xC03000, 0x401000);
        LegacyGameRegistry.registerModEntity(SWEntities.EarthGolem.class, "EarthGolem", id++, this, 64, 3, true, 0x6A4A2A, 0x303030);
        LegacyGameRegistry.registerModEntity(SWEntities.WaterGolem.class, "WaterGolem", id++, this, 64, 3, true, 0x2040C0, 0x102060);
        LegacyGameRegistry.registerModEntity(SWEntities.AirGolem.class, "AirGolem", id++, this, 64, 3, true, 0xE0E0E0, 0xA0A0A0);
        LegacyGameRegistry.registerModEntity(EntityElementStone.class, "ElementStone", id++, this, 64, 5, true);
        LegacyGameRegistry.registerModEntity(com.electrifiedded.shadowworld.entity.EntityFlyingBoat.class, "FlyingBoat", id++, this, 128, 1, true);
        LegacyGameRegistry.registerModEntity(com.electrifiedded.shadowworld.item.SWFinders.EntityDungeonFinder.class, "DungoenFinder", id++, this, 64, 5, true);
        LegacyGameRegistry.registerModEntity(com.electrifiedded.shadowworld.entity.SWProjectiles.CrossBowArrow.class, "CrossBowArrow", id++, this, 64, 20, false);
        LegacyGameRegistry.registerModEntity(com.electrifiedded.shadowworld.entity.SWProjectiles.ExplodingItem.class, "ExplodingItem", id++, this, 64, 20, true);
        LegacyGameRegistry.registerModEntity(com.electrifiedded.shadowworld.tile.AngelPortals.EntityFalling.class, "FallingAngelPortal", id++, this, 128, 1, true);
    }

    /** 0 = Shadow Workbench (3x3, vanilla recipes), 1 = Shadow Workbench+ (5x5, shadow recipes). */
    public static class Gui implements IGuiHandler {
        @Override
        public Object getServerGuiElement(int id, EntityPlayer p, World w, int x, int y, int z) {
            if (id >= 2) return com.electrifiedded.shadowworld.tile.SWGuiIds.server(id, p, w, new BlockPos(x, y, z));
            return container(id, p, w, new BlockPos(x, y, z));
        }

        @Override
        public Object getClientGuiElement(int id, EntityPlayer p, World w, int x, int y, int z) {
            if (id >= 2) return proxy.gui(id, p, w, new BlockPos(x, y, z), com.electrifiedded.shadowworld.tile.SWGuiIds.server(id, p, w, new BlockPos(x, y, z)));
            LegacyCraftingContainer c = container(id, p, w, new BlockPos(x, y, z));
            return c == null ? null : proxy.craftingGui(c, id == 1);
        }

        static LegacyCraftingContainer container(int id, EntityPlayer p, World w, BlockPos pos) {
            Block b = w.getBlockState(pos).getBlock();
            if (!(b instanceof SWBlocks2.Workbench wb)) return null;
            return id == 1
                    ? new LegacyCraftingContainer(p.inventory, w, pos, b, wb.manager(), 5, 131, 36, 4, 3, 8, 94, 148)
                    : new LegacyCraftingContainer(p.inventory, w, pos, b, null, 3, 124, 35, 30, 17, 8, 84);
        }
    }
}
