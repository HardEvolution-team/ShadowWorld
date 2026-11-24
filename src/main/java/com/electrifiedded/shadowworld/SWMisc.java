package com.electrifiedded.shadowworld;

import com.electrifiedded.shadowworld.net.SWNet;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/** Holy enchantment (EnchantmentHoly) and /shadowworldbrighness (BrightnessCommand) of the 1.7 mod. */
public final class SWMisc {
    private SWMisc() {}

    /** Armour enchantment for Angel / Shadow armour: halves damage per piece (see SWEvents.onHurt). */
    public static class EnchantmentHoly extends Enchantment {
        public EnchantmentHoly() {
            super(Rarity.VERY_RARE, EnumEnchantmentType.ARMOR, new EntityEquipmentSlot[]{EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET});
            setName("shadowworld:enchantment.holy");
            setRegistryName("shadowworld", "holy");
        }

        @Override public int getMinEnchantability(int l) { return 80; }
        @Override public int getMaxEnchantability(int l) { return 100; }
        @Override public int getMaxLevel() { return 1; }

        @Override
        public boolean canApply(ItemStack s) {
            if (!(s.getItem() instanceof ItemArmor)) return false;
            net.minecraft.item.Item i = s.getItem();
            return i == SWContent.helmetAngel || i == SWContent.plateAngel || i == SWContent.legsAngel || i == SWContent.bootsAngel
                    || i == SWContent.helmetShadow || i == SWContent.plateShadow || i == SWContent.legsShadow || i == SWContent.bootsShadow;
        }

        @Override public boolean canApplyAtEnchantingTable(ItemStack s) { return canApply(s); }
    }

    public static class Registrar {
        @SubscribeEvent
        public void onEnchant(RegistryEvent.Register<Enchantment> e) {
            SWContent.holy = new EnchantmentHoly();
            e.getRegistry().register(SWContent.holy);
        }
    }

    public static class BrightnessCommand extends CommandBase {
        @Override public String getName() { return "shadowworldbrighness"; }
        @Override public String getUsage(ICommandSender s) { return "shadowworldbrighness <player> <true|false>"; }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            if (args.length < 2 || !args[1].equals("true") && !args[1].equals("false")) throw new WrongUsageException(getUsage(sender));
            EntityPlayerMP p = getPlayer(server, sender, args[0]);
            boolean on = args[1].equals("true");
            p.getEntityData().setBoolean("brightness", on);
            SWNet.CHANNEL.sendTo(new SWNet.Brightness(on), p);
        }

        @Override
        public List<String> getTabCompletions(MinecraftServer server, ICommandSender s, String[] args, @Nullable BlockPos pos) {
            if (args.length == 1) return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
            if (args.length == 2) return getListOfStringsMatchingLastWord(args, "true", "false");
            return Collections.emptyList();
        }
    }
}
