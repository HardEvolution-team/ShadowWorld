package com.electrifiedded.shadowworld.net;

import com.electrifiedded.shadowworld.SWContent;
import com.electrifiedded.shadowworld.ShadowWorld;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Replaces the 1.7 raw channels: SW|Pickup (diary toast), SW|Brightness, SW|NEdit / SW|NSign (note writing),
 * SW|FlyingBoat / SW|FlyingBoatEnder (boat inventories). SW|DOpen/DFlip/DEntry are not needed: diary entries are
 * written on the server, the book animation state stays on the client stack.
 */
public final class SWNet {
    private SWNet() {}

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("shadowworld");

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(Toast.Handler.class, Toast.class, id++, Side.CLIENT);
        CHANNEL.registerMessage(Brightness.Handler.class, Brightness.class, id++, Side.CLIENT);
        CHANNEL.registerMessage(NoteEdit.Handler.class, NoteEdit.class, id++, Side.SERVER);
        CHANNEL.registerMessage(BoatInventory.Handler.class, BoatInventory.class, id++, Side.SERVER);
    }

    /** Diary toast: kind 0 = new diary, 1 = text only, 2 = with item, 3 = with entity (NBT incl. "id"). */
    public static class Toast implements IMessage {
        public int kind;
        public String name = "";
        public ItemStack stack = ItemStack.EMPTY;
        public NBTTagCompound entity;

        public Toast() {}

        public Toast(int kind, String name, ItemStack stack, NBTTagCompound entity) {
            this.kind = kind;
            this.name = name == null ? "" : name;
            this.stack = stack == null ? ItemStack.EMPTY : stack;
            this.entity = entity;
        }

        @Override
        public void fromBytes(ByteBuf b) {
            kind = b.readByte();
            name = ByteBufUtils.readUTF8String(b);
            stack = ByteBufUtils.readItemStack(b);
            entity = ByteBufUtils.readTag(b);
        }

        @Override
        public void toBytes(ByteBuf b) {
            b.writeByte(kind);
            ByteBufUtils.writeUTF8String(b, name);
            ByteBufUtils.writeItemStack(b, stack);
            ByteBufUtils.writeTag(b, entity);
        }

        public static class Handler implements IMessageHandler<Toast, IMessage> {
            @Override
            public IMessage onMessage(Toast m, MessageContext ctx) {
                ShadowWorld.proxy.scheduleClient(() -> ShadowWorld.proxy.showDiaryToast(m));
                return null;
            }
        }
    }

    public static class Brightness implements IMessage {
        public boolean on;

        public Brightness() {}
        public Brightness(boolean on) { this.on = on; }

        @Override public void fromBytes(ByteBuf b) { on = b.readBoolean(); }
        @Override public void toBytes(ByteBuf b) { b.writeBoolean(on); }

        public static class Handler implements IMessageHandler<Brightness, IMessage> {
            @Override
            public IMessage onMessage(Brightness m, MessageContext ctx) {
                ShadowWorld.proxy.scheduleClient(() -> ShadowWorld.proxy.setBrightnessFlag(m.on));
                return null;
            }
        }
    }

    /** Note pages (and title when signing) from GuiNote. */
    public static class NoteEdit implements IMessage {
        public NBTTagList pages = new NBTTagList();
        public String title = "";
        public boolean sign;

        public NoteEdit() {}
        public NoteEdit(NBTTagList pages, String title, boolean sign) { this.pages = pages; this.title = title; this.sign = sign; }

        @Override
        public void fromBytes(ByteBuf b) {
            NBTTagCompound c = ByteBufUtils.readTag(b);
            pages = c == null ? new NBTTagList() : c.getTagList("pages", 8);
            title = ByteBufUtils.readUTF8String(b);
            sign = b.readBoolean();
        }

        @Override
        public void toBytes(ByteBuf b) {
            NBTTagCompound c = new NBTTagCompound();
            c.setTag("pages", pages);
            ByteBufUtils.writeTag(b, c);
            ByteBufUtils.writeUTF8String(b, title);
            b.writeBoolean(sign);
        }

        public static class Handler implements IMessageHandler<NoteEdit, IMessage> {
            @Override
            public IMessage onMessage(NoteEdit m, MessageContext ctx) {
                EntityPlayerMP p = ctx.getServerHandler().player;
                p.getServerWorld().addScheduledTask(() -> {
                    ItemStack held = p.getHeldItem(EnumHand.MAIN_HAND);
                    if (held.getItem() != SWContent.Note) return;
                    if (held.hasTagCompound() && held.getTagCompound().hasKey("author")) return; // already signed
                    NBTTagList pages = new NBTTagList();
                    for (int i = 0; i < Math.min(2, m.pages.tagCount()); i++) {
                        String s = m.pages.getStringTagAt(i);
                        pages.appendTag(new NBTTagString(s.length() > 256 ? s.substring(0, 256) : s));
                    }
                    held.setTagInfo("pages", pages);
                    if (m.sign) {
                        String t = m.title.trim();
                        if (t.isEmpty() || t.length() > 16) return;
                        held.setTagInfo("author", new NBTTagString(p.getName()));
                        held.setTagInfo("title", new NBTTagString(t));
                    }
                });
                return null;
            }
        }
    }

    /** Inventory key while riding a Flying Boat with a chest (false) or ender chest (true). */
    public static class BoatInventory implements IMessage {
        public boolean ender;

        public BoatInventory() {}
        public BoatInventory(boolean ender) { this.ender = ender; }

        @Override public void fromBytes(ByteBuf b) { ender = b.readBoolean(); }
        @Override public void toBytes(ByteBuf b) { b.writeBoolean(ender); }

        public static class Handler implements IMessageHandler<BoatInventory, IMessage> {
            @Override
            public IMessage onMessage(BoatInventory m, MessageContext ctx) {
                EntityPlayerMP p = ctx.getServerHandler().player;
                p.getServerWorld().addScheduledTask(() -> com.electrifiedded.shadowworld.entity.EntityFlyingBoat.openInventory(p, m.ender));
                return null;
            }
        }
    }
}
