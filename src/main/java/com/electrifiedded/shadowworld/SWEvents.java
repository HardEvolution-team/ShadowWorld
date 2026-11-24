package com.electrifiedded.shadowworld;

import com.electrifiedded.shadowworld.entity.SWEntities;
import com.electrifiedded.shadowworld.entity.SWAngels;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

/** Server side of the 1.7 ShadowEventClass (diary entries, armour effects, Holy, soul drop, wolf king weather). */
public class SWEvents {
    static final String ARRIVED = "Day 1\nI finally arrived in the Shadow World. It's very dark and I think I'll need lots of torches here...";

    // ------------------------------------------------------------------ look-at entries (1.7 client raycast -> server raycast here)

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        EntityPlayer p = e.player;
        armourEffects(p);
        if (p.world.isRemote || p.ticksExisted % 10 != 0 || !hasDiary(p)) return;
        lookAt(p);
    }

    static boolean hasDiary(EntityPlayer p) {
        for (ItemStack s : p.inventory.mainInventory) if (s.getItem() == SWContent.Diary) return true;
        for (ItemStack s : p.inventory.offHandInventory) if (s.getItem() == SWContent.Diary) return true;
        return false;
    }

    private void lookAt(EntityPlayer p) {
        double reach = 5.0;
        Vec3d eye = p.getPositionEyes(1.0F), look = p.getLook(1.0F), end = eye.add(look.scale(reach));
        RayTraceResult block = p.world.rayTraceBlocks(eye, end, false, false, true);
        double best = block != null ? block.hitVec.distanceTo(eye) : reach;
        Entity hit = null;
        List<Entity> list = p.world.getEntitiesInAABBexcluding(p, p.getEntityBoundingBox().expand(look.x * reach, look.y * reach, look.z * reach).grow(1.0), EntitySelectors.NOT_SPECTATING);
        for (Entity en : list) {
            AxisAlignedBB bb = en.getEntityBoundingBox().grow(en.getCollisionBorderSize());
            RayTraceResult r = bb.calculateIntercept(eye, end);
            if (bb.contains(eye)) { hit = en; best = 0; }
            else if (r != null && r.hitVec.distanceTo(eye) < best) { hit = en; best = r.hitVec.distanceTo(eye); }
        }
        if (hit instanceof EntityLivingBase el) lookEntity(p, el);
        else if (block != null && block.typeOfHit == RayTraceResult.Type.BLOCK) lookBlock(p, p.world.getBlockState(block.getBlockPos()).getBlock());
    }

    private static void lookBlock(EntityPlayer p, Block b) {
        if (b == SWContent.AzuriteOre) SWDiary.add(p, "This blue ore looks cool. I think it's very useful. I'll call it \"Azurite Ore\".", "Azurite Ore", null, new ItemStack(b));
        else if (b == SWContent.BloodStoneOre) SWDiary.add(p, "This ore seems to be quiet powerful but it looks like frozen blood.", "Blood Stone Ore", null, new ItemStack(b));
        else if (b == SWContent.ShadowFlower) SWDiary.add(p, "It's a glowing flower. It seems to be made of Glowstone. I might use it for some light things...", "Shadow Flower", null, new ItemStack(b));
        else if (b == SWContent.ShadowFlower2) SWDiary.add(p, "It's a very bright, indigo flower. It's a good lightsource, but maybe it has some other uses, too.", "Indigo Shadow Flower", null, new ItemStack(b));
        else if (b == SWContent.ShadowFlower3) SWDiary.add(p, "It's a black flower. It's made of shadows, so it's quite useful", "Black Shadow Flower", null, new ItemStack(b));
        else if (b == SWContent.GlowBean) SWDiary.add(p, "It's a strange glowing bean! I think I can eat it...", "Glow Bean", null, new ItemStack(SWContent.GlowBeanItem));
        else if (b == SWContent.ShadowBush) SWDiary.add(p, "This is a Shadow Bush. I think I can eat it's berries.", "Shadow Bush", null, new ItemStack(b));
        else if (b == SWContent.quicksilverIce) SWDiary.add(p, "I found frozen Quicksilver. It behaves like normal Ice. I think it's harmless.", "Frozen Quicksilver", null, new ItemStack(b));
        else if (b == SWContent.ShadowCactus) SWDiary.add(p, "This is small Cactus is spiky. I have to be careful in this desert.", "Shadow Cactus", null, new ItemStack(b));
        else if (b == SWContent.ShadowGrass) SWDiary.add(p, "This grass grows everywhere. It's blue and sometimes small, white things appear. It's not very interesting...", "Shadow Grass", null, new ItemStack(b));
        else if (b == SWContent.ShadowSand) SWDiary.add(p, "I found strange, blue sand. I'll call it \"Shadow Sand\". I think I can smelt it into glass.", "Shadow Sand", null, new ItemStack(b));
        else if (b == SWContent.Pedestal) SWDiary.add(p, "This Block not only looks like a pedestal it, is also one. It seems to be possible to show of items on there.", "Pedestal", null, new ItemStack(b));
    }

    private static void lookEntity(EntityPlayer p, EntityLivingBase e) {
        if (e instanceof SWEntities.Light) SWDiary.add(p, "I found a strange, living and glowing thing. I'll call it a \"Light\". I think, when I kill it I get some glowing stuff.", "Light", null, e);
        else if (e instanceof SWEntities.WolfSpider) SWDiary.add(p, "This beast is pretty dangerous and it makes some strange sounds, like a wolf. It only lives in these desert biomes. I decided to call it a \"Wolf Spider\".", "Wolf Spider", null, e);
        else if (e instanceof com.electrifiedded.shadowworld.entity.SWDwarves.Dwarf) SWDiary.add(p, "Today I meet some other people! They are a bit small, speak strangely, and live in villages under the ground, but I like them, so I'll try to trade with these \"Dwarfs\".", "Dwarf", null, e);
        else if (e instanceof SWEntities.ShadowPig) SWDiary.add(p, "I found a pig! It's blue and surrounded by shadows, but it's a pig. I'll call it a \"Shadow Pig\". Maybe I can ride it...", "Shadow Pig", null, e);
        else if (e instanceof SWEntities.SkeletonSpider) SWDiary.add(p, "This spider is strange. It's a living skeleton of a spider! I think it's dangerous...", "Skeleton Spider", null, e);
        else if (e instanceof SWEntities.ShadowWolf) SWDiary.add(p, "I found a wolf, a \"Shadow Wolf\". It behaves like a normal wolf, so I think I can tame it, too. I think they like Souls...", "Shadow Wolf", null, e);
        else if (e instanceof SWEntities.Shadow) SWDiary.add(p, "This mob looks like a living shadow, so I'll call it \"Shadow\". But it behaves a bit like an Enderman...", "Shadow", null, e);
        else if (e instanceof SWEntities.ShadowSlime) SWDiary.add(p, "I found this Slime in the plains. When I kill one, two new appear!", "Shadow Slime", null, e);
        else if (e instanceof SWEntities.CookiePig) SWDiary.add(p, "Tasty! It's a \"Cookie Pig\". I think I can eat it...", "Cookie Pig", null, e);
        else if (e instanceof SWEntities.PassiveSpider) SWDiary.add(p, "This spdider seems to be passive. I think it's almost useless for me.", "Passive Spider", null, e);
        else if (e instanceof SWEntities.ShadowWolfKing) SWDiary.add(p, "This is a \"Shadow Wolf King\". It's pretty strong, and it calls other wolfs. I should be prepared for a big fight.", "Shadow Wolf King", null, e);
        else if (e instanceof SWEntities.CookieSlime) SWDiary.add(p, "It's a slime, which looks like a cookie! It seems to like me. I'll call it a \"Cookie Slime\".", "Cookie Slime", null, e);
        else if (e instanceof SWEntities.ShadowChicken) SWDiary.add(p, "This chicken is evil. And dangerous. Even the eggs looks strange...", "Shadow Chicken", null, e);
        else if (e instanceof SWEntities.JellyFish) SWDiary.add(p, "I found a \"Jelly Fish\". It's glowing and swimming in poisonous quicksilver. I think it's useful for something...", "Jelly Fish", null, e);
        else if (e instanceof SWEntities.Dragonfly) SWDiary.add(p, "It's a giant dragonfly! It looks nice, but it doesn't seems to be useful...", "Shadow Dragonfly", null, e);
        else if (e instanceof SWEntities.ExplodingWolf) SWDiary.add(p, "This wolf looks a bit strange. It's eyes have the color of a Creeper...", "Strange Wolf", null, e);
        else if (e instanceof SWAngels.Angel) SWDiary.add(p, "An Angel! But it looks angry. I think he'll attack me...", "Angel", null, e);
        else if (e instanceof SWEntities.ShadowCreeper) SWDiary.add(p, "I found a Creeper. It has an unusual color, but I'm sure it's a Creeper. And all Creepers are dangerous...", "Shadow Creeper", null, e);
    }

    // ------------------------------------------------------------------ armour (onPlayerTick / 2 / 3)

    private static void armourEffects(EntityPlayer p) {
        ItemStack chest = p.getItemStackFromSlot(EntityEquipmentSlot.CHEST), legs = p.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        ItemStack head = p.getItemStackFromSlot(EntityEquipmentSlot.HEAD), feet = p.getItemStackFromSlot(EntityEquipmentSlot.FEET);
        var data = p.getEntityData();
        boolean angelPlate = chest.getItem() == SWContent.plateAngel;
        if (angelPlate && !data.getBoolean("sw_fly")) {
            data.setBoolean("sw_fly", true);
            p.capabilities.allowFlying = true;
            if (p.world.isRemote) p.capabilities.setFlySpeed(0.075F);
            if (!p.world.isRemote) p.sendPlayerAbilities();
            SWDiary.add(p, "Wow flying is cool, now i can explore the world more easily!", "Angel's Chestplate", null, new ItemStack(SWContent.plateAngel));
        } else if (!angelPlate && data.getBoolean("sw_fly")) {
            data.setBoolean("sw_fly", false);
            if (!p.capabilities.isCreativeMode && !p.isSpectator()) {
                p.capabilities.allowFlying = false;
                p.capabilities.isFlying = false;
            }
            if (p.world.isRemote) p.capabilities.setFlySpeed(0.05F);
            if (!p.world.isRemote) p.sendPlayerAbilities();
        }
        boolean angelLegs = legs.getItem() == SWContent.legsAngel;
        if (angelLegs && !data.getBoolean("sw_step")) { data.setBoolean("sw_step", true); p.stepHeight = 1.0F; }
        else if (!angelLegs && data.getBoolean("sw_step")) { data.setBoolean("sw_step", false); p.stepHeight = 0.6F; }
        boolean shadowSet = chest.getItem() == SWContent.plateShadow && feet.getItem() == SWContent.bootsShadow
                && legs.getItem() == SWContent.legsShadow && head.getItem() == SWContent.helmetShadow;
        if (shadowSet) {
            data.setBoolean("sw_invis", true);
            p.setInvisible(true);
            if (p.world.isRemote) ShadowWorld.proxy.shadowParticles(p);
        } else if (data.getBoolean("sw_invis")) {
            data.setBoolean("sw_invis", false);
            if (!p.isPotionActive(net.minecraft.init.MobEffects.INVISIBILITY)) p.setInvisible(false);
        }
    }

    // ------------------------------------------------------------------ damage / death / weather

    @SubscribeEvent
    public void onHurt(LivingHurtEvent e) {
        if (!(e.getEntityLiving() instanceof EntityPlayer p)) return;
        ItemStack feet = p.getItemStackFromSlot(EntityEquipmentSlot.FEET);
        if (feet.isEmpty()) return; // 1.7: everything below only ran with boots on
        if (feet.getItem() == SWContent.bootsAngel && e.getSource() == DamageSource.FALL) {
            p.fallDistance = 0.0F;
            e.setCanceled(true);
            return;
        }
        if (SWContent.holy == null) return;
        for (ItemStack a : p.getArmorInventoryList())
            if (EnchantmentHelper.getEnchantmentLevel(SWContent.holy, a) > 0) e.setAmount(e.getAmount() / 2.0F);
    }

    @SubscribeEvent
    public void onJoin(EntityJoinWorldEvent e) {
        if (!e.getWorld().isRemote && e.getEntity() instanceof SWEntities.ShadowWolfKing) {
            World ow = e.getWorld().getMinecraftServer().getWorld(0);
            ow.getWorldInfo().setRaining(true);
            ow.getWorldInfo().setThundering(true);
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent e) {
        if (e.getEntity().world.isRemote) return;
        if (e.getEntity() instanceof SWEntities.ShadowWolfKing) {
            World ow = e.getEntity().world.getMinecraftServer().getWorld(0);
            ow.getWorldInfo().setThundering(false);
            ow.getWorldInfo().setRaining(false);
        }
        if (e.getEntity() instanceof EntityPlayer p && p.dimension == SWContent.shadowDim()) {
            ItemStack soul = new ItemStack(SWContent.Soul);
            soul.setStackDisplayName(p.getName() + "'s Soul");
            p.dropItem(soul, true, false);
        }
    }

    // ------------------------------------------------------------------ crafting / pickup entries

    @SubscribeEvent
    public void onCrafted(PlayerEvent.ItemCraftedEvent e) {
        Item item = e.crafting.getItem();
        EntityPlayer p = e.player;
        if (p.world.isRemote) return;
        if (item == SWContent.CrossBow)
            SWDiary.add(p, "It's an powerfull weapon, but it doesn't seem to work quite well...", "Crossbow", null, new ItemStack(SWContent.CrossBow));
        if (item == Item.getItemFromBlock(SWContent.GolemEye))
            SWDiary.add(p, "I think with this I should be able to create a golem. I can use different materials for the Golem: Netherrack, Wool, Lapis Lazuli or Shadow Dirt.", "Golem Eye", null, new ItemStack(SWContent.GolemEye));
        if (item == Item.getItemFromBlock(SWContent.ShadowWorkBenchPlus))
            SWDiary.add(p, "This thing is awesome! It's a bigger craftingtable, so I can make more advanced things like an crossbow or better swords.", "Advanced Shadow Craftingtable", null, new ItemStack(SWContent.ShadowWorkBenchPlus));
        if (isSwordPlus(item)) SWDiary.add(p, "The bigger craftingtabel made swords possible I couldn't think of before", "Better Swords", null);
        if (item == SWContent.Diary) {
            SWDiary.initDiary(e.crafting, p, "Day 1\nI've made a new Diary. I hope it'll be better than the last one...");
            SWDiary.toastNewDiary(p);
        }
    }

    static boolean isSwordPlus(Item i) {
        return i == SWContent.BloodStoneSwordPlus || i == SWContent.AzuriteSwordPlus || i == SWContent.SWoodSwordPlus || i == SWContent.SStoneSwordPlus
                || i == SWContent.swordWoodPlus || i == SWContent.swordStonePlus || i == SWContent.swordIronPlus || i == SWContent.swordGoldPlus
                || i == SWContent.swordDiamondPlus || i == SWContent.BasicSwordPlus || i == SWContent.ShadowSwordPlus;
    }

    @SubscribeEvent
    public void onPickup(PlayerEvent.ItemPickupEvent e) {
        EntityPlayer p = e.player;
        if (p.world.isRemote) return;
        Item item = e.getStack().getItem();
        if (item == SWContent.HellFlintAndSteel) SWDiary.add(p, "Hell this thing is HOT. The whole thing is surrounded with power and it says \"Don't use!\" I should be careful.", "Hell Lighter", null, new ItemStack(item));
        if (item == SWContent.DungeonFinder) SWDiary.add(p, "Looks like an old dwarven object. It's behaves a bit like an Ender Eye, but I don't know why...", "Dugeon Finder", null, new ItemStack(item));
        if (item == SWContent.Shadow) SWDiary.add(p, "I found this material best to be described as a shadow. I think it will be useful in the future. They somehow react to craftingtables made of the wood found in the shadows.", "Shadow", null, new ItemStack(item));
        if (item == SWContent.Azurite) SWDiary.add(p, "I have found a strange crystal. I'll call it Azurite. It seems to be very useful for weapons, tools and armor.", "Azurite", null, new ItemStack(item));
        if (item == SWContent.AzuriteShard) SWDiary.add(p, "I do not know how to describe this Shards. They have something of old knowlege and magic. They somehow remind me of books.", "Azurite Shard", null, new ItemStack(item));
        if (item == SWContent.Teleporter) SWDiary.add(p, "This Staff has an strong aura of magic. It does not seem to be unstable but something strong is trapped inside. Looking at the crystal I feel like being somewhere else...", "Staff of Teleporting", null, new ItemStack(item));
        if (item == SWContent.Controller) SWDiary.add(p, "While touchig this Staff I feel a strog power rushing through me, as if I could controll Everyone - MUHAHAHA!!!", "Staff of Controlling", null, new ItemStack(item));
        if (item == SWContent.Confuser) SWDiary.add(p, "What? - Uhm... Forget it.... So Confusing....", "Staff of Confusing", null, new ItemStack(item));
        if (item == SWContent.Soul) SWDiary.add(p, "I found this strange object. It seems to be the Soul of something or someone. It's not very useful, but I can use it as fuel", "Soul", null, new ItemStack(item));
        if (item == Item.getItemFromBlock(SWContent.BloodStoneOre)) SWDiary.add(p, "This ore seems to be quiet powerful but it looks like frozen blood.", "Blood Stone Ore", null, new ItemStack(SWContent.BloodStoneOre));
        if (item == SWContent.BloodStone) SWDiary.add(p, "So it wasn't frozen blood. I think it will make great tools to survive using this ore.", "Blood Stone", "Blood Stone Ore", new ItemStack(item));
        if (item == Item.getItemFromBlock(SWContent.ShadowFlower)) SWDiary.add(p, "It's a glowing flower. It seems to be made of Glowstone. I might use it for some light things...", "Shadow Flower", null, new ItemStack(SWContent.ShadowFlower));
        if (item == Item.getItemFromBlock(SWContent.ShadowFlower2)) SWDiary.add(p, "It's a very bright, indigo flower. It's a good lightsource, but maybe it has some other uses, too.", "Indigo Shadow Flower", null, new ItemStack(SWContent.ShadowFlower2));
        if (item == Item.getItemFromBlock(SWContent.ShadowFlower3)) SWDiary.add(p, "It's a black flower. It's made of shadows, so it's quite useful", "Black Shadow Flower", null, new ItemStack(SWContent.ShadowFlower3));
        if (item == Item.getItemFromBlock(SWContent.ShadowLog)) SWDiary.add(p, "It's the wood I've found here. Looks like a dark wood. I think I can make planks with this wood.", "Shadow Log", null, new ItemStack(SWContent.ShadowLog));
        if (item == SWContent.ShadowCrystal) SWDiary.add(p, "This crystal seemes to be very valueable. I think it would look great on swords or pedestals. It would be very sad if I loose it in a fire...", "Shadow Crystal", "Pedestal", new ItemStack(item));
    }

    // ------------------------------------------------------------------ login / dimension change / respawn

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        EntityPlayer p = e.player;
        for (int i = 0; i < p.inventory.getSizeInventory(); i++) {
            ItemStack d = p.inventory.getStackInSlot(i);
            if (d.getItem() == SWContent.Diary && d.hasTagCompound() && d.getTagCompound().hasKey("day"))
                d.getTagCompound().setTag("day", new NBTTagInt(d.getTagCompound().getInteger("day") + 1));
        }
        if (p instanceof EntityPlayerMP mp)
            com.electrifiedded.shadowworld.net.SWNet.CHANNEL.sendTo(new com.electrifiedded.shadowworld.net.SWNet.Brightness(p.getEntityData().getBoolean("brightness")), mp);
        if (p.dimension == SWContent.shadowDim()) giveArrivalDiary(p);
    }

    @SubscribeEvent
    public void onChangedDim(PlayerEvent.PlayerChangedDimensionEvent e) {
        EntityPlayer p = e.player;
        giveArrivalDiary(p); // 1.7 gave it on ANY first dimension change
        if (e.toDim == SWContent.cookieDim()) {
            if (!p.getEntityData().getBoolean("sw_cookie")) {
                p.getEntityData().setBoolean("sw_cookie", true);
                p.getFoodStats().setFoodLevel(4); // "In the Cookie" achievement side effect
            }
            SWDiary.add(p, "I found a way to the Cookie World. Everything looks sooo tasty! I want to eat everything, for example these Cookie Pigs", "Cookie World", null);
        }
    }

    private static void giveArrivalDiary(EntityPlayer p) {
        if (p.getEntityData().hasKey("shadowworld")) return;
        p.getEntityData().setTag("shadowworld", new NBTTagInt(1));
        ItemStack book = new ItemStack(SWContent.Diary);
        SWDiary.initDiary(book, p, ARRIVED);
        p.addExperience(100);
        if (!p.inventory.addItemStackToInventory(book)) p.dropItem(book, false);
        SWDiary.toastNewDiary(p);
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.player.dimension == SWContent.shadowDim())
            SWDiary.add(e.player, "I don't know what happend. I can't remember, but it must be horrible, because this place is dangerous...", "Death", null);
    }
}
