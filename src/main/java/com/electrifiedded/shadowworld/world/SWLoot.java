package com.electrifiedded.shadowworld.world;

import com.electrifiedded.archaiclib.legacy.world.LegacyChestContent;
import com.electrifiedded.archaiclib.legacy.world.LegacyClassStructure;
import com.electrifiedded.shadowworld.SWContent;
import com.electrifiedded.shadowworld.entity.SWEntities;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DungeonHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/** Chest loot tables of the 1.7 structures (ShadowLoot, HouseLoot, FurnaceLoot, CookieLoot, temple loot, BoatLoot). */
public final class SWLoot {
    private SWLoot() {}

    private static LegacyChestContent c(Item i, int min, int max, int w) { return new LegacyChestContent(i, 0, min, max, w); }
    private static LegacyChestContent c(ItemStack s, int min, int max, int w) { return new LegacyChestContent(s, min, max, w); }
    private static ItemStack s(net.minecraft.block.Block b) { return new ItemStack(b); }

    /** ShadowWorld.newNote: a signed note with two pages. */
    public static ItemStack newNote(String p1, String p2, String title, String author) {
        ItemStack note = new ItemStack(SWContent.Note);
        NBTTagList pages = new NBTTagList();
        pages.appendTag(new NBTTagString(p1));
        pages.appendTag(new NBTTagString(p2));
        note.setTagInfo("pages", pages);
        note.setTagInfo("title", new NBTTagString(title));
        note.setTagInfo("author", new NBTTagString(author));
        return note;
    }

    public static LegacyChestContent[] SHADOW, HOUSE, FURNACE, COOKIE, FIRE, WATER, EARTH, AIR, BOAT;
    private static final Map<String, LegacyChestContent[]> BY_FIELD = new HashMap<>();

    /** Built after items exist (init). */
    public static void init() {
        String t = "Mysterious Traveller";
        SHADOW = new LegacyChestContent[]{
                c(SWContent.Azurite, 1, 4, 7), c(SWContent.BloodStone, 1, 3, 2), c(SWContent.Shadow, 1, 3, 5), c(SWContent.Soul, 0, 2, 4),
                c(SWContent.helmetShadow, 1, 1, 2), c(SWContent.plateShadow, 1, 1, 2), c(SWContent.legsShadow, 1, 1, 1), c(SWContent.bootsShadow, 1, 1, 3),
                c(SWContent.helmetAzurite, 1, 1, 1), c(SWContent.plateAzurite, 1, 1, 1), c(SWContent.legsAzurite, 1, 1, 1), c(SWContent.bootsAzurite, 1, 1, 2),
                c(SWContent.ChatCookie, 0, 1, 1), c(SWContent.GoldenCookie, 0, 1, 1), c(SWContent.SweetCookie, 0, 1, 1), c(SWContent.DiamondCookie, 0, 1, 1),
                c(SWContent.Chocolate, 0, 1, 1), c(SWContent.BakedCookie, 0, 1, 1), c(SWContent.WoodStaff, 0, 1, 1), c(SWContent.JumperItem, 0, 1, 1),
                c(SWContent.zombieslayer, 0, 1, 1), c(SWContent.DungeonFinder, 0, 1, 1), c(SWContent.Health, 0, 1, 1), c(SWContent.ShadowCrystal, 0, 1, 1),
                c(SWContent.ShadowBucketEmpty, 1, 3, 4), c(SWContent.BasicSword, 0, 1, 2), c(SWContent.BasicSwordPlus, 0, 1, 1), c(SWContent.BloodStoneSword, 0, 1, 1),
                c(SWContent.AzuriteSword, 0, 1, 1), c(SWContent.ShadowSword, 0, 1, 1), c(SWContent.AzuriteAxe, 0, 1, 1),
                c(newNote("The pedastals seem to be part of a magical ritual to craft objects", "The Dwarfs told me that they have stories about 5 mystical pedestals. It's possible that they talk about the pedestals i found... I need to reasearch further", "Day 174", t), 0, 1, 1),
                c(newNote("I finally found an other pedestal it had an Wooden Staff on it.", "After investigating the Staff i still have the impression that it is valuable but i couldn't find a use for it.", "Day 215", t), 0, 1, 1),
                c(newNote("The research on the pedestals is time consuming i had to travel back to the overworld to repair my equipment and get some more food.", "But I finally made progress. Shadow Crystals create light beams when they are placed on a pedestal! Maybe they are required for the ritual.", "Day 352", t), 0, 1, 1),
                c(newNote("I am close to give up. There where days where I thought I could figured it out. But now I think it's impossible", "On my way back to the Overworld I found a travelling Dwarf who told me about the \"magical powers of the + shape\" crazy guy.", "Day 504", t), 0, 1, 1),
                c(newNote("The riddle of the pedestals got me again. I could forget it for over 500 days, but yesterday I couldn't resist any longer I started another expedition into the Shadow World to solve the riddle.", "Today I remembered the traveller I met on the last day of my last expedition. I think I figured out how to arrange the pedestals for the ritual now.", "Day 1024", t), 0, 1, 1),
                c(newNote("\"To force the roots and the magic together, two red eyes have to look to the sky\" - so it's written in the ancient stories of the dwarfs.", "Maybe the stories help me to solve the riddle, the Dwarves back then invented it after all. I think I know what the red eyes are. They would fit into my theory.", "Day 1030", t), 0, 1, 1),
                c(newNote("I explore new areas in the Shadow World again. I found new Villages with other storytellers but their stories aren't helpful yet.", "In a dark and forgotten corner of a library of a small but old village i found a book about a red crystal. But I don't think they are talking about the shadow crystal.", "Day 1062", t), 0, 1, 1),
                c(newNote("After comparing the Book with other texts and storrys I found out that the crystal is made from the \"Star of the Nether\" what ever that is.", "", "Day 1068", t), 0, 1, 1)};
        HOUSE = new LegacyChestContent[]{
                c(SWContent.Azurite, 1, 5, 7), c(SWContent.BloodStone, 1, 3, 2), c(SWContent.Shadow, 1, 4, 5), c(SWContent.Soul, 0, 2, 4),
                c(s(SWContent.ShadowPlanks), 10, 64, 5), c(s(SWContent.ShadowGlass), 10, 64, 4), c(s(SWContent.ShadowCobble), 10, 64, 10), c(s(SWContent.ShadowDirt), 10, 64, 5),
                c(s(SWContent.ShadowFlower), 4, 10, 3), c(s(SWContent.ShadowSapling), 4, 10, 3), c(SWContent.helmetShadow, 1, 1, 1), c(SWContent.plateShadow, 1, 1, 1),
                c(SWContent.legsShadow, 1, 1, 1), c(SWContent.bootsShadow, 1, 1, 1), c(SWContent.SweetCookie, 0, 1, 1), c(SWContent.Chocolate, 0, 1, 1),
                c(SWContent.BakedCookie, 0, 1, 1), c(SWContent.ShadowBucketEmpty, 1, 3, 4), c(Items.STICK, 3, 32, 3), c(SWContent.BasicSword, 0, 1, 2),
                c(SWContent.AzuriteSword, 0, 1, 1), c(SWContent.ShadowSword, 0, 1, 1), c(SWContent.AzuriteAxe, 0, 1, 1), c(SWContent.SWoodAxe, 0, 1, 3),
                c(SWContent.SWoodPickaxe, 0, 1, 3), c(SWContent.SWoodSword, 0, 1, 3), c(SWContent.SWoodShovel, 0, 1, 3)};
        FURNACE = new LegacyChestContent[]{c(SWContent.Soul, 4, 16, 33)};
        COOKIE = new LegacyChestContent[]{
                c(Items.COOKIE, 1, 16, 5), c(SWContent.BakedCookie, 3, 16, 2), c(SWContent.SweetCookie, 1, 16, 2), c(SWContent.GoldenCookie, 0, 1, 1),
                c(SWContent.Chocolate, 2, 5, 3), c(SWContent.WhiteChocolate, 2, 5, 3), c(SWContent.DarkChocolate, 2, 5, 3), c(s(SWContent.CookieSpawner), 4, 10, 2)};
        FIRE = new LegacyChestContent[]{
                c(Items.FIRE_CHARGE, 1, 1, 5), c(Items.FLINT_AND_STEEL, 2, 5, 10), c(s(Blocks.NETHERRACK), 4, 10, 10), c(SWContent.HellFlintAndSteel, 0, 1, 1),
                c(SWContent.ShadowFlintAndSteel, 1, 5, 1), c(Items.LAVA_BUCKET, 2, 5, 10), c(Items.BLAZE_POWDER, 1, 2, 1)};
        WATER = new LegacyChestContent[]{c(Items.WATER_BUCKET, 1, 1, 5), c(Items.BUCKET, 2, 5, 10), c(s(Blocks.WATERLILY), 2, 5, 10)};
        EARTH = new LegacyChestContent[]{c(Items.CLAY_BALL, 1, 5, 7), c(Items.BRICK, 2, 5, 10), c(s(Blocks.DIRT), 2, 10, 10), c(s(Blocks.SANDSTONE), 4, 10, 10)};
        AIR = new LegacyChestContent[]{c(Items.FEATHER, 1, 1, 5), c(Items.BOOK, 2, 5, 10), c(s(Blocks.WOOL), 2, 5, 10)};
        BOAT = new LegacyChestContent[]{
                c(new ItemStack(SWContent.AngelBlock, 1, 0), 5, 15, 5), c(new ItemStack(SWContent.AngelBlock, 1, 1), 5, 15, 5),
                c(new ItemStack(SWContent.AngelBlock, 1, 2), 5, 15, 5), c(SWContent.AngelFeather, 0, 3, 1)};
        for (String[] e : new String[][]{{"ShadowLoot", "S"}, {"HouseLoot", "H"}, {"FurnaceLoot", "F"}, {"CookieLoot", "C"}, {"FireLoot", "1"},
                {"WaterLoot", "2"}, {"EarthLoot", "3"}, {"AirLoot", "4"}, {"BoatLoot", "B"}}) {
            BY_FIELD.put(e[0], switch (e[1]) {
                case "S" -> SHADOW; case "H" -> HOUSE; case "F" -> FURNACE; case "C" -> COOKIE; case "1" -> FIRE;
                case "2" -> WATER; case "3" -> EARTH; case "4" -> AIR; default -> BOAT;
            });
        }
    }

    public static LegacyChestContent[] byField(String ownerDotName) {
        return BY_FIELD.get(ownerDotName.substring(ownerDotName.lastIndexOf('.') + 1));
    }

    /** 1.7 spawner names used by the structures. */
    public static ResourceLocation spawnerMob(String name) {
        if (name.equals("SkeletonSpider")) return EntityList.getKey(SWEntities.SkeletonSpider.class);
        ResourceLocation v = new ResourceLocation(com.electrifiedded.archaiclib.legacy.registry.LegacyEntityNames.toModern(name));
        return v;
    }

    /** Fills chests / sets spawners of replayed structures like the original generate() did. */
    public static final LegacyClassStructure.TileListener TILES = new LegacyClassStructure.TileListener() {
        @Override
        public void chest(World w, BlockPos p, String field) {
            LegacyChestContent[] loot = byField(field);
            TileEntity te = w.getTileEntity(p);
            if (loot != null && te instanceof IInventory inv) LegacyChestContent.generateChestContents(w.rand, loot, inv, 4 + w.rand.nextInt(8));
        }

        @Override
        public void spawner(World w, BlockPos p, String mob) {
            if (w.getTileEntity(p) instanceof TileEntityMobSpawner sp) sp.getSpawnerBaseLogic().setEntityId(spawnerMob(mob));
        }
    };

    /** WorldGenShadowDungeon.pickMobSpawner. */
    public static ResourceLocation dungeonMob(Random r) { return DungeonHooks.getRandomDungeonMob(r); }
}
