package com.electrifiedded.shadowworld.client;

import com.electrifiedded.shadowworld.SWContent;
import com.electrifiedded.shadowworld.entity.EntityFlyingBoat;
import com.electrifiedded.shadowworld.net.SWNet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Random;

/** Client half of the 1.7 ShadowEventClass: dimension music, brightness command, Flying Boat inventory key. */
public class SWClientEvents {
    static boolean brightnessFlag;
    private boolean brightened;
    private final Random random = new Random();

    /** PlaySoundEvent17: in the Shadow World the music is replaced by slowed-down far / wait / mall records. */
    @SubscribeEvent
    public void onMusic(PlaySoundEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (e.getSound() == null || e.getSound().getCategory() != SoundCategory.MUSIC || mc.player == null || mc.player.dimension != SWContent.shadowDim()) return;
        int i = random.nextInt(6);
        SoundEvent rec = i >= 2 && i <= 4 ? SoundEvents.RECORD_FAR : i == 1 ? SoundEvents.RECORD_WAIT : i == 0 ? SoundEvents.RECORD_MALL : null;
        if (rec == null) {
            e.setResultSound(null);
            return;
        }
        float pitch = Math.max(0.5F, -1.0F + (random.nextFloat() - 0.5F) * 2.0F); // 1.7 passed a negative pitch (clamped to 0.5 by the engine)
        e.setResultSound(new PositionedSoundRecord(rec.getSoundName(), SoundCategory.MUSIC, 1.0F, pitch, false, 0, ISound.AttenuationType.NONE, 0.0F, 0.0F, 0.0F));
    }

    /** /shadowworldbrighness: forces gamma 7.5 while the flag is set (1.7 onRenderLiving). */
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;
        if (brightnessFlag) {
            if (mc.gameSettings.gammaSetting != 7.5F) {
                mc.gameSettings.gammaSetting = 7.5F;
                mc.gameSettings.saveOptions();
            }
            brightened = true;
        } else if (brightened || mc.gameSettings.gammaSetting > 1.0F && mc.player.ticksExisted < 5) {
            mc.gameSettings.gammaSetting = 1.0F;
            mc.gameSettings.saveOptions();
            brightened = false;
        }
    }

    /** onInvetoryOpened: the inventory key opens the boat chest / ender chest while riding a Flying Boat. */
    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer p = mc.player;
        if (p == null || !GameSettings.isKeyDown(mc.gameSettings.keyBindInventory)) return;
        if (p.getRidingEntity() instanceof EntityFlyingBoat boat) {
            if (boat.getHasChest()) {
                p.closeScreen();
                SWNet.CHANNEL.sendToServer(new SWNet.BoatInventory(false));
            } else if (boat.getHasEnderChest()) {
                p.closeScreen();
                SWNet.CHANNEL.sendToServer(new SWNet.BoatInventory(true));
            }
        }
    }
}
