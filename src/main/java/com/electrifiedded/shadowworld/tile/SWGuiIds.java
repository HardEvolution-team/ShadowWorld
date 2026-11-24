package com.electrifiedded.shadowworld.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Original ShadowGuiHandler ids: 0/1 workbenches (handled in ShadowWorld.Gui), 2 diary, 3 soul furnace, 4 golden soul furnace, 5 note read, 6 note write. */
public final class SWGuiIds {
    private SWGuiIds() {}
    public static final int DIARY = 2, NOTE_READ = 5, NOTE_WRITE = 6;

    /** Server-side container (null for item-only screens). */
    public static Object server(int id, EntityPlayer p, World w, BlockPos pos) {
        TileEntity te = w.getTileEntity(pos);
        if ((id == SoulFurnaces.GUI_SOUL || id == SoulFurnaces.GUI_GOLDEN) && te instanceof SoulFurnaces.TileSoulFurnace f)
            return new SoulFurnaces.ContainerSoulFurnace(p.inventory, f);
        return null;
    }
}
