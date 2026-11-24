package com.electrifiedded.shadowworld.item;

import com.electrifiedded.shadowworld.SWContent;
import com.electrifiedded.shadowworld.entity.EntityElementStone;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/** Shadow World items re-implemented for 1.12. */
public final class SWItemTypes {
    private SWItemTypes() {}

    private static void tip(List<String> list, String s) { if (s != null) list.add(TextFormatting.ITALIC + s); }

    /** Plain item with optional tooltip / glint / rarity. */
    public static class Simple extends Item {
        String tooltip; boolean glint; EnumRarity rarity = EnumRarity.COMMON;
        public Simple tooltip(String t) { tooltip = t; return this; }
        public Simple glint() { glint = true; return this; }
        public Simple rarity(EnumRarity r) { rarity = r; return this; }
        @Override public boolean hasEffect(ItemStack s) { return glint || super.hasEffect(s); }
        @Override public EnumRarity getRarity(ItemStack s) { return rarity; }
        @Override @SideOnly(Side.CLIENT) public void addInformation(ItemStack s, @Nullable World w, List<String> l, ITooltipFlag f) { tip(l, tooltip); }
    }

    /** Sword with the 1.7 Shadow World damage (base 4 + material, or 7 + material for the "plus" swords). */
    public static class Sword extends ItemSword {
        private final float damage;
        public Sword(ToolMaterial m, float base) { super(m); this.damage = base + m.getAttackDamage(); }
        @Override public float getAttackDamage() { return damage - 4F; }
        @Override
        public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
            Multimap<String, AttributeModifier> m = super.getItemAttributeModifiers(slot);
            if (slot == EntityEquipmentSlot.MAINHAND) {
                m.removeAll(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
                m.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", damage, 0));
            }
            return m;
        }
    }

    public static class Pickaxe extends ItemPickaxe { public Pickaxe(ToolMaterial m) { super(m); } }
    public static class Spade extends ItemSpade { public Spade(ToolMaterial m) { super(m); } }
    public static class Axe extends ItemAxe { public Axe(ToolMaterial m) { super(m, 6.0F + m.getAttackDamage(), -3.1F); } }
    public static class Hoe extends ItemHoe { public Hoe(ToolMaterial m) { super(m); } }

    /** Armor whose layers live in shadowworld:textures/models/<Prefix>_layer_N.png. */
    public static class Armor extends ItemArmor {
        private final String prefix; private final boolean glint;
        public Armor(ArmorMaterial m, EntityEquipmentSlot slot, String prefix, boolean glint) { super(m, 0, slot); this.prefix = prefix; this.glint = glint; }
        @Override public String getArmorTexture(ItemStack s, Entity e, EntityEquipmentSlot slot, String type) {
            return "shadowworld:textures/models/" + prefix + "_layer_" + (slot == EntityEquipmentSlot.LEGS ? 2 : 1) + ".png";
        }
        @Override public boolean hasEffect(ItemStack s) { return glint || super.hasEffect(s); }
    }

    /** Food with an optional effect on eating and tooltip. */
    public static class Food extends ItemFood {
        public interface Eaten { void eaten(ItemStack s, World w, EntityPlayer p); }
        String tooltip; Eaten eaten; boolean glint;
        public Food(int heal, float sat, boolean wolf) { super(heal, sat, wolf); }
        public Food tooltip(String t) { tooltip = t; return this; }
        public Food onEaten(Eaten e) { eaten = e; return this; }
        public Food glint() { glint = true; return this; }
        @Override protected void onFoodEaten(ItemStack s, World w, EntityPlayer p) {
            super.onFoodEaten(s, w, p);
            if (!w.isRemote && eaten != null) eaten.eaten(s, w, p);
        }
        @Override public boolean hasEffect(ItemStack s) { return glint || super.hasEffect(s); }
        @Override @SideOnly(Side.CLIENT) public void addInformation(ItemStack s, @Nullable World w, List<String> l, ITooltipFlag f) { tip(l, tooltip); }
    }

    /** Cookie tools: a working tool that can also be eaten (leaves a stick). */
    public interface Edible {
        int heal();
        default ActionResult<ItemStack> eatStart(World w, EntityPlayer p, EnumHand h) {
            ItemStack s = p.getHeldItem(h);
            if (p.canEat(false)) { p.setActiveHand(h); return new ActionResult<>(EnumActionResult.SUCCESS, s); }
            return new ActionResult<>(EnumActionResult.FAIL, s);
        }
        default ItemStack eatFinish(ItemStack s, World w, EntityLivingBase e) {
            if (e instanceof EntityPlayer p) {
                p.getFoodStats().addStats(heal(), 0.6F);
                w.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, w.rand.nextFloat() * 0.1F + 0.9F);
            }
            return new ItemStack(Items.STICK);
        }
    }

    public static class CookieSword extends ItemSword implements Edible {
        public CookieSword() { super(ToolMaterial.WOOD); }
        public int heal() { return 4; }
        @Override public ActionResult<ItemStack> onItemRightClick(World w, EntityPlayer p, EnumHand h) { return eatStart(w, p, h); }
        @Override public ItemStack onItemUseFinish(ItemStack s, World w, EntityLivingBase e) { return eatFinish(s, w, e); }
        @Override public EnumAction getItemUseAction(ItemStack s) { return EnumAction.EAT; }
        @Override public int getMaxItemUseDuration(ItemStack s) { return 32; }
    }

    public static class CookiePickaxe extends ItemPickaxe implements Edible {
        public CookiePickaxe() { super(ToolMaterial.WOOD); }
        public int heal() { return 4; }
        @Override public ActionResult<ItemStack> onItemRightClick(World w, EntityPlayer p, EnumHand h) { return eatStart(w, p, h); }
        @Override public ItemStack onItemUseFinish(ItemStack s, World w, EntityLivingBase e) { return eatFinish(s, w, e); }
        @Override public EnumAction getItemUseAction(ItemStack s) { return EnumAction.EAT; }
        @Override public int getMaxItemUseDuration(ItemStack s) { return 32; }
    }

    public static class CookieAxe extends ItemAxe implements Edible {
        public CookieAxe() { super(ToolMaterial.WOOD, 6.0F, -3.2F); }
        public int heal() { return 4; }
        @Override public ActionResult<ItemStack> onItemRightClick(World w, EntityPlayer p, EnumHand h) { return eatStart(w, p, h); }
        @Override public ItemStack onItemUseFinish(ItemStack s, World w, EntityLivingBase e) { return eatFinish(s, w, e); }
        @Override public EnumAction getItemUseAction(ItemStack s) { return EnumAction.EAT; }
        @Override public int getMaxItemUseDuration(ItemStack s) { return 32; }
    }

    public static class CookieSpade extends ItemSpade implements Edible {
        public CookieSpade() { super(ToolMaterial.WOOD); }
        public int heal() { return 4; }
        @Override public ActionResult<ItemStack> onItemRightClick(World w, EntityPlayer p, EnumHand h) { return eatStart(w, p, h); }
        @Override public ItemStack onItemUseFinish(ItemStack s, World w, EntityLivingBase e) { return eatFinish(s, w, e); }
        @Override public EnumAction getItemUseAction(ItemStack s) { return EnumAction.EAT; }
        @Override public int getMaxItemUseDuration(ItemStack s) { return 32; }
    }

    /** Fire/Water/Earth/Air stones and the element sphere: thrown like snowballs. */
    public static class ElementStone extends Item {
        private final int type;
        public ElementStone(int type) { this.type = type; setMaxStackSize(1); }
        @Override
        public ActionResult<ItemStack> onItemRightClick(World w, EntityPlayer p, EnumHand h) {
            ItemStack s = p.getHeldItem(h);
            if (!p.capabilities.isCreativeMode) s.shrink(1);
            w.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
            if (!w.isRemote) {
                EntityElementStone e = new EntityElementStone(w, p, type);
                e.shoot(p, p.rotationPitch, p.rotationYaw, 0.0F, 1.5F, 1.0F);
                w.spawnEntity(e);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, s);
        }
    }

    /** Staffs made on the pedestal; break back into their crystal after 384 uses. */
    public abstract static class Staff extends Simple {
        final Supplier<Item> crystal;
        Staff(Supplier<Item> crystal, String tip) { this.crystal = crystal; setMaxStackSize(1); setMaxDamage(384); tooltip(tip).glint().rarity(EnumRarity.UNCOMMON); }
        protected ItemStack use(ItemStack s, EntityPlayer p) {
            if (s.getItemDamage() >= 383 && !p.capabilities.isCreativeMode) return new ItemStack(crystal.get());
            s.damageItem(1, p);
            return s;
        }
        protected static RayTraceResult look(World w, EntityPlayer p) {
            Vec3d eye = p.getPositionEyes(1F), dir = p.getLook(1F), end = eye.add(dir.scale(300));
            return w.rayTraceBlocks(eye, end, false, true, false);
        }
    }

    public static class TeleporterStaff extends Staff {
        public TeleporterStaff() { super(() -> SWContent.GreenCrystal, "Throws EnderPearls"); }
        @Override public ActionResult<ItemStack> onItemRightClick(World w, EntityPlayer p, EnumHand h) {
            ItemStack s = p.getHeldItem(h);
            w.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.ENTITY_ENDERPEARL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F);
            if (!w.isRemote) {
                EntityEnderPearl e = new EntityEnderPearl(w, p);
                e.shoot(p, p.rotationPitch, p.rotationYaw, 0.0F, 1.5F, 1.0F);
                w.spawnEntity(e);
                s = use(s, p);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, s);
        }
    }

    public static class ConfuserStaff extends Staff {
        public ConfuserStaff() { super(() -> SWContent.RedCrystal, "Confuses Mobs"); }
        @Override public ActionResult<ItemStack> onItemRightClick(World w, EntityPlayer p, EnumHand h) {
            ItemStack s = p.getHeldItem(h);
            if (w.isRemote) return new ActionResult<>(EnumActionResult.SUCCESS, s);
            RayTraceResult r = look(w, p);
            if (r == null) return new ActionResult<>(EnumActionResult.PASS, s);
            Vec3d c = r.hitVec;
            w.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.BLOCK_NOTE_HARP, SoundCategory.PLAYERS, 1.2F, 2.0F);
            for (Entity e : w.getEntitiesWithinAABBExcludingEntity(p, new AxisAlignedBB(c.x - 21, c.y - 21, c.z - 21, c.x + 21, c.y + 21, c.z + 21)))
                if (e instanceof EntityCreature cr) {
                    Vec3d t = RandomPositionGenerator.findRandomTarget(cr, 4, 16);
                    if (t != null) cr.getNavigator().tryMoveToXYZ(t.x, t.y, t.z, 1.2);
                }
            return new ActionResult<>(EnumActionResult.SUCCESS, use(s, p));
        }
    }

    public static class ControllerStaff extends Staff {
        public ControllerStaff() { super(() -> SWContent.YellowCrystal, "Controlls nearby mobs"); }
        @Override public boolean onLeftClickEntity(ItemStack s, EntityPlayer p, Entity target) {
            if (!(target instanceof EntityLivingBase tl) || p.world.isRemote) return false;
            p.world.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.BLOCK_NOTE_HARP, SoundCategory.PLAYERS, 1.2F, 2.0F);
            for (Entity e : p.world.getEntitiesWithinAABBExcludingEntity(target, target.getEntityBoundingBox().grow(21)))
                if (e instanceof EntityCreature cr && e != p) { cr.setAttackTarget(tl); cr.setRevengeTarget(tl); }
            ItemStack r = use(s, p);
            if (r != s) p.setHeldItem(EnumHand.MAIN_HAND, r);
            return true;
        }
    }

    public static class GrowerStaff extends Staff {
        public GrowerStaff() { super(() -> SWContent.WhiteCrystal, "Let things grow!"); }
        @Override public EnumActionResult onItemUse(EntityPlayer p, World w, BlockPos pos, EnumHand h, EnumFacing f, float x, float y, float z) {
            for (BlockPos q : BlockPos.getAllInBox(pos.add(-2, -2, -2), pos.add(2, 2, 2))) {
                IBlockState st = w.getBlockState(q);
                if (st.getBlock() instanceof net.minecraft.block.IGrowable g && !w.isRemote) {
                    if (g.canGrow(w, q, st, false) && g.canUseBonemeal(w, w.rand, q, st)) g.grow(w, w.rand, q, st);
                } else if (!w.isRemote && st.getBlock().getTickRandomly()) st.getBlock().updateTick(w, q, st, w.rand);
                if (w.isRemote && st.getMaterial() != net.minecraft.block.material.Material.AIR) w.playEvent(2005, q, 5);
            }
            if (!w.isRemote) { ItemStack r = use(p.getHeldItem(h), p); if (r != p.getHeldItem(h)) p.setHeldItem(h, r); }
            return EnumActionResult.SUCCESS;
        }
    }

    public static class ZombieSlayer extends Simple {
        public ZombieSlayer() { tooltip("Instant kills Zombies").glint().rarity(EnumRarity.RARE); }
        @Override public boolean onLeftClickEntity(ItemStack s, EntityPlayer p, Entity e) {
            if (e instanceof EntityZombie z) { z.setFire(100); z.attackEntityFrom(DamageSource.MAGIC, 100.0F); }
            return false;
        }
    }

    public static class Jumper extends Simple {
        public Jumper() { tooltip("A strange Item found in Doungeons"); }
        @Override public ActionResult<ItemStack> onItemRightClick(World w, EntityPlayer p, EnumHand h) { p.motionY = 1.0; return new ActionResult<>(EnumActionResult.SUCCESS, p.getHeldItem(h)); }
    }

    /** Places a block on the clicked face (azurite rune, glow bean...). */
    public static class Placer extends Simple {
        final Supplier<Block> block; final java.util.function.Predicate<IBlockState> on;
        public Placer(Supplier<Block> block, java.util.function.Predicate<IBlockState> on) { this.block = block; this.on = on; }
        @Override public EnumActionResult onItemUse(EntityPlayer p, World w, BlockPos pos, EnumHand h, EnumFacing f, float x, float y, float z) {
            if (on != null && !on.test(w.getBlockState(pos))) return EnumActionResult.FAIL;
            BlockPos q = w.getBlockState(pos).getBlock().isReplaceable(w, pos) ? pos : pos.offset(f);
            ItemStack s = p.getHeldItem(h);
            if (!p.canPlayerEdit(q, f, s) || !w.isAirBlock(q) || !block.get().canPlaceBlockAt(w, q)) return EnumActionResult.FAIL;
            if (!w.isRemote) w.setBlockState(q, block.get().getDefaultState(), 3);
            if (!p.capabilities.isCreativeMode) s.shrink(1);
            return EnumActionResult.SUCCESS;
        }
    }

    /** Shadow / hell flint and steel: lights the mod fires (Shadow fire lights the lapis portal). */
    public static class Lighter extends Simple {
        final Supplier<Block> fire;
        public Lighter(Supplier<Block> fire) { this.fire = fire; setMaxStackSize(1); setMaxDamage(64); }
        @Override public EnumActionResult onItemUse(EntityPlayer p, World w, BlockPos pos, EnumHand h, EnumFacing f, float x, float y, float z) {
            BlockPos q = pos.offset(f);
            ItemStack s = p.getHeldItem(h);
            if (!p.canPlayerEdit(q, f, s)) return EnumActionResult.FAIL;
            if (w.isAirBlock(q)) {
                w.playSound(p, q, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, itemRand.nextFloat() * 0.4F + 0.8F);
                w.setBlockState(q, fire.get().getDefaultState(), 11);
            }
            s.damageItem(1, p);
            return EnumActionResult.SUCCESS;
        }
    }

    /** Builds a lit lapis portal frame in front of the player (creative helper item of the original). */
    public static class PortalPlacer extends Simple {
        @Override public EnumActionResult onItemUse(EntityPlayer p, World w, BlockPos pos, EnumHand h, EnumFacing f, float x, float y, float z) {
            if (w.isRemote) return EnumActionResult.SUCCESS;
            BlockPos b = pos.up();
            IBlockState lapis = Blocks.LAPIS_BLOCK.getDefaultState();
            for (int l = -1; l <= 2; l++)
                for (int i = -1; i <= 3; i++) {
                    BlockPos q = b.add(l, i, 0);
                    w.setBlockState(q, (l == -1 || l == 2 || i == -1 || i == 3) ? lapis : Blocks.AIR.getDefaultState(), 3);
                }
            SWContent.ShadowPortal.tryToCreatePortal(w, b);
            if (!p.capabilities.isCreativeMode) p.getHeldItem(h).shrink(1);
            return EnumActionResult.SUCCESS;
        }
    }

    /** Crossbow: fires crossbow arrows (1 = normal, 2 = fire, 3 = poison). */
    public static class Crossbow extends Item {
        final int type;
        public Crossbow(int type) { this.type = type; setMaxStackSize(1); setMaxDamage(384); setFull3D(); }
        @Override public net.minecraft.item.EnumRarity getRarity(ItemStack s) { return type == 1 ? net.minecraft.item.EnumRarity.COMMON : net.minecraft.item.EnumRarity.UNCOMMON; }
        @Override public boolean isEnchantable(ItemStack s) { return true; }
        @Override public int getItemEnchantability() { return 1; }
        @Override @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
        public void addInformation(ItemStack s, @javax.annotation.Nullable World w, java.util.List<String> l, net.minecraft.client.util.ITooltipFlag f) {
            if (type == 2) l.add(net.minecraft.util.text.TextFormatting.ITALIC + "Shots burning arrows");
            if (type == 3) l.add(net.minecraft.util.text.TextFormatting.ITALIC + "Shots poisonous arrows");
        }
        @Override public int getMaxItemUseDuration(ItemStack s) { return 72000; }
        @Override public EnumAction getItemUseAction(ItemStack s) { return EnumAction.BOW; }
        @Override public ActionResult<ItemStack> onItemRightClick(World w, EntityPlayer p, EnumHand h) {
            ItemStack s = p.getHeldItem(h);
            boolean ammo = p.capabilities.isCreativeMode || p.inventory.hasItemStack(new ItemStack(SWContent.CrossBowArrow));
            ActionResult<ItemStack> nock = net.minecraftforge.event.ForgeEventFactory.onArrowNock(s, w, p, h, ammo);
            if (nock != null) return nock;
            if (!ammo) return new ActionResult<>(EnumActionResult.FAIL, s);
            p.setActiveHand(h);
            return new ActionResult<>(EnumActionResult.SUCCESS, s);
        }
        @Override public void onPlayerStoppedUsing(ItemStack s, World w, EntityLivingBase e, int left) {
            if (!(e instanceof EntityPlayer p)) return;
            int charge = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(s, w, p, getMaxItemUseDuration(s) - left, true);
            if (charge < 0) return;
            float f = charge / 20.0F;
            f = f * f + f * 2.0F; // 1.7 crossbow: no /3, full power after ~0.4 s
            if (f < 0.1) return;
            if (f > 1.0F) f = 1.0F;
            boolean infinite = p.capabilities.isCreativeMode || net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.INFINITY, s) > 0;
            if (!w.isRemote) {
                com.electrifiedded.shadowworld.entity.SWProjectiles.CrossBowArrow a = new com.electrifiedded.shadowworld.entity.SWProjectiles.CrossBowArrow(w, p, 2.5F);
                if (f == 1.0F) a.setIsCritical(true);
                if (type == 2) a.setFire(100);
                if (type == 3) a.ispoisonous = true;
                if (infinite) a.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
                else {
                    for (int i = 0; i < p.inventory.getSizeInventory(); i++) {
                        ItemStack a2 = p.inventory.getStackInSlot(i);
                        if (a2.getItem() == SWContent.CrossBowArrow) { a2.shrink(1); break; }
                    }
                }
                w.spawnEntity(a);
                s.damageItem(1, p);
            }
            w.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
        }
    }

    /** Teleport cookie: eating sends you to the Cookie World (or back). */
    public static void teleportCookie(ItemStack s, World w, EntityPlayer p) {
        if (!(p instanceof EntityPlayerMP mp)) return;
        int target = w.provider.getDimension() == SWContent.cookieDim() ? 0 : SWContent.cookieDim();
        mp.changeDimension(target, SWContent.CookiePortal.teleporterFor(mp.server.getWorld(target)));
    }

    public static void chatCookie(ItemStack s, World w, EntityPlayer p) {
        p.sendMessage(new TextComponentString("Hello " + p.getName() + "!"));
        p.sendMessage(new TextComponentString("I Like Cookies"));
        p.sendMessage(new TextComponentString("Cookie was slain by " + p.getName()));
    }

    private static final UUID HEALTH_UUID = UUID.fromString("5D6F0BA2-1186-46AC-B896-C61C5CEE99CC");

    /** Extra heart: each one stacks +2 max health (the 1.7 mod reused one UUID, so only the first counted). */
    public static void extraHealth(ItemStack s, World w, EntityPlayer p) {
        net.minecraft.entity.ai.attributes.IAttributeInstance a = p.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        AttributeModifier old = a.getModifier(HEALTH_UUID);
        double v = old == null ? 2.0 : old.getAmount() + 2.0;
        if (old != null) a.removeModifier(old);
        a.applyModifier(new AttributeModifier(HEALTH_UUID, "Shadow World health", v, 0).setSaved(true));
    }
}
