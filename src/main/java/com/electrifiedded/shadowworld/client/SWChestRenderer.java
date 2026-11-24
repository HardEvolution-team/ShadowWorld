package com.electrifiedded.shadowworld.client;

import com.electrifiedded.shadowworld.tile.SWChests;
import net.minecraft.client.renderer.tileentity.TileEntityChestRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;

import java.util.EnumMap;
import java.util.Map;

/** Vanilla chest renderer with the 1.7 Shadow/Angel chest textures swapped in. */
public class SWChestRenderer extends TileEntityChestRenderer {
    private SWChests.Kind current = SWChests.Kind.SHADOW;

    @Override
    public void render(TileEntityChest te, double x, double y, double z, float pt, int destroy, float alpha) {
        current = te instanceof SWChests.TileSWChest c ? c.kind() : SWChests.Kind.SHADOW;
        super.render(te, x, y, z, pt, destroy, alpha);
    }

    @Override
    protected void bindTexture(ResourceLocation loc) {
        if ("minecraft".equals(loc.getNamespace()) && loc.getPath().startsWith("textures/entity/chest/")) {
            boolean dbl = loc.getPath().endsWith("_double.png");
            loc = new ResourceLocation("shadowworld", "textures/entity/chest/" + (dbl ? current.dbl : current.single) + ".png");
        }
        super.bindTexture(loc);
    }

    /** Inventory/hand render of the chest items (1.7 ShadowChestItemRenderer). */
    public static class Item extends TileEntityItemStackRenderer {
        private final Map<SWChests.Kind, SWChests.TileSWChest> dummies = new EnumMap<>(SWChests.Kind.class);

        @Override
        public void renderByItem(ItemStack stack, float pt) {
            net.minecraft.block.Block b = net.minecraft.block.Block.getBlockFromItem(stack.getItem());
            SWChests.Kind k = b instanceof SWChests.ChestBlock c ? c.kind : SWChests.Kind.SHADOW;
            TileEntityRendererDispatcher.instance.render(dummies.computeIfAbsent(k, SWChests.TileSWChest::new), 0.0, 0.0, 0.0, 0.0F, pt);
        }
    }
}
