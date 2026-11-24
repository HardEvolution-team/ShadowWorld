package com.electrifiedded.shadowworld;

import com.electrifiedded.archaiclib.crafting.LegacyCraftingContainer;

public class CommonProxy {
    public void preInit() {}
    public void init() {}
    public Object gui(int id, net.minecraft.entity.player.EntityPlayer p, net.minecraft.world.World w, net.minecraft.util.math.BlockPos pos, Object container) { return null; }
    public void scheduleClient(Runnable r) {}
    public void openTravellingDwarf(com.electrifiedded.shadowworld.entity.SWDwarves.TravellingDwarf d) {}
    public void showDiaryToast(com.electrifiedded.shadowworld.net.SWNet.Toast t) {}
    public void setBrightnessFlag(boolean on) {}
    public void shadowParticles(net.minecraft.entity.player.EntityPlayer p) {}
    public void spawnParticle(String name, double x, double y, double z, double mx, double my, double mz) {}
    public net.minecraftforge.client.IRenderHandler angelSky() { return null; }
    public Object craftingGui(LegacyCraftingContainer c, boolean plus) { return null; }
}
