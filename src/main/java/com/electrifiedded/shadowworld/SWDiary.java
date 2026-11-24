package com.electrifiedded.shadowworld;

import com.electrifiedded.shadowworld.net.SWNet;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;

/**
 * The exploration diary of the 1.7 mod (ShadowWorld.addDiaryEntry + the "new diary" NBT layout).
 * Entries are written on the server into every diary of the player that carries his name; the client only
 * gets a toast (SW|Pickup in 1.7).
 */
public final class SWDiary {
    private SWDiary() {}

    public static final int ITEM = 0, ENTITY = 1, OTHER = 2;

    /** NBT of a freshly made diary (ShadowEventClass.onCrafting / onPlayerLogin). */
    public static void initDiary(ItemStack book, EntityPlayer player, String firstEntry) {
        NBTTagList other = new NBTTagList(), all = new NBTTagList();
        other.appendTag(new NBTTagString(firstEntry));
        all.appendTag(new NBTTagString(firstEntry));
        book.setTagInfo("title", new NBTTagString(TextFormatting.ITALIC + player.getName() + "'s Diary"));
        book.setTagInfo("other", other);
        book.setTagInfo("items", new NBTTagList());
        book.setTagInfo("blocks", new NBTTagList());
        book.setTagInfo("entities", new NBTTagList());
        book.setTagInfo("entries", new NBTTagList());
        book.setTagInfo("entitydata", new NBTTagList());
        book.setTagInfo("all", all);
        book.setTagInfo("day", new NBTTagInt(1));
        book.setTagInfo("open", new NBTTagFloat(1.0F));
        book.setTagInfo("flip", new NBTTagFloat(1.0F));
        book.setTagInfo("flip2", new NBTTagFloat(1.0F));
    }

    public static void toastNewDiary(EntityPlayer player) {
        if (player instanceof EntityPlayerMP mp) SWNet.CHANNEL.sendTo(new SWNet.Toast(0, "", ItemStack.EMPTY, null), mp);
    }

    public static void add(EntityPlayer p, String text, String name, String requires) { add(p, text, name, requires, ItemStack.EMPTY, null); }

    public static void add(EntityPlayer p, String text, String name, String requires, ItemStack stack) { add(p, text, name, requires, stack, null); }

    public static void add(EntityPlayer p, String text, String name, String requires, EntityLivingBase entity) { add(p, text, name, requires, ItemStack.EMPTY, entity); }

    private static void add(EntityPlayer player, String text, String name, String requires, ItemStack stack, EntityLivingBase entity) {
        if (player.world.isRemote) return;
        int kind = entity != null ? ENTITY : !stack.isEmpty() ? ITEM : OTHER;
        boolean shown = false;
        for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
            ItemStack diary = player.inventory.getStackInSlot(slot);
            if (diary.getItem() != SWContent.Diary || !diary.hasTagCompound()) continue;
            NBTTagCompound t = diary.getTagCompound();
            String needed = kind == ENTITY ? "entities" : kind == ITEM ? "items" : "other";
            if (!t.hasKey("entries") || !t.hasKey(needed) || !t.hasKey("all") || !t.hasKey("day")) continue;
            if (kind == ITEM && !t.hasKey("blocks")) continue;
            if (!diary.getDisplayName().contains(player.getName())) continue;
            NBTTagList entries = t.getTagList("entries", 8);
            boolean fresh = true, hasReq = false;
            for (int i = 0; i < entries.tagCount(); i++) {
                String e = entries.getStringTagAt(i);
                if (e.equals(name)) fresh = false;
                if (requires != null && e.equals(requires)) hasReq = true;
            }
            if (!fresh || (requires != null && !hasReq)) continue;
            NBTTagString line = new NBTTagString("Day " + t.getTag("day") + "\n-" + name + "\n" + text);
            if (kind == ENTITY) {
                list(t, "entities", 8).appendTag(line.copy());
                list(t, "entitynames", 8).appendTag(new NBTTagString(name));
                NBTTagCompound et = new NBTTagCompound();
                entity.writeToNBTOptional(et);
                list(t, "entitydata", 10).appendTag(et);
            } else if (kind == ITEM) {
                boolean block = Block.getBlockFromItem(stack.getItem()) != Blocks.AIR;
                if (block) list(t, "blocks", 8).appendTag(line.copy());
                else if (stack.getItem() != SWContent.ShadowBucketquicksilver && stack.getItem() != SWContent.GlowBeanItem) {
                    list(t, "itemdata", 10).appendTag(stack.writeToNBT(new NBTTagCompound()));
                    list(t, "items", 8).appendTag(line.copy());
                } else {
                    list(t, "blocks", 8).appendTag(line.copy());
                    list(t, "blockdata", 10).appendTag(stack.writeToNBT(new NBTTagCompound()));
                }
            } else list(t, "other", 8).appendTag(line.copy());
            list(t, "all", 8).appendTag(line);
            entries.appendTag(new NBTTagString(name));
            t.setTag("entries", entries);
            player.inventory.markDirty();
            if (!shown) {
                shown = true;
                player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_NOTE_HAT, SoundCategory.PLAYERS, 1.2F, 2.0F);
                if (player instanceof EntityPlayerMP mp) {
                    NBTTagCompound et = null;
                    if (entity != null) {
                        et = new NBTTagCompound();
                        entity.writeToNBTOptional(et);
                    }
                    SWNet.CHANNEL.sendTo(new SWNet.Toast(kind == ENTITY ? 3 : kind == ITEM ? 2 : 1, name, stack, et), mp);
                }
            }
        }
    }

    private static NBTTagList list(NBTTagCompound t, String key, int type) {
        if (!t.hasKey(key, 9)) t.setTag(key, new NBTTagList());
        return t.getTagList(key, type);
    }
}
