package com.electrifiedded.shadowworld.client;

import com.electrifiedded.shadowworld.tile.SoulFurnaces;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Client screens for the original ShadowGuiHandler ids >= 2. */
final class SWClientGuis {
    private SWClientGuis() {}

    static Object client(int id, EntityPlayer p, World w, BlockPos pos, Object c) {
        if (c instanceof SoulFurnaces.ContainerSoulFurnace f) return new GuiSoulFurnace(f);
        net.minecraft.item.ItemStack held = p.getHeldItemMainhand();
        if (id == com.electrifiedded.shadowworld.tile.SWGuiIds.DIARY) {
            if (held.getItem() != com.electrifiedded.shadowworld.SWContent.Diary) held = p.getHeldItemOffhand();
            return held.getItem() == com.electrifiedded.shadowworld.SWContent.Diary ? new GuiDiary(p, held) : null;
        }
        if (id == com.electrifiedded.shadowworld.tile.SWGuiIds.NOTE_READ || id == com.electrifiedded.shadowworld.tile.SWGuiIds.NOTE_WRITE)
            return held.getItem() == com.electrifiedded.shadowworld.SWContent.Note ? new GuiNote(p, held, id == com.electrifiedded.shadowworld.tile.SWGuiIds.NOTE_WRITE) : null;
        return null;
    }
}
