package net.remusnetworkutilities.carpetedition.commands;

import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.remusnetworkutilities.carpetedition.RemusNetworkUtilitiesSettings;

public class EnderChestCommand {
    public static void openEnderChest(CommandContext<ServerCommandSource> context) {
        if (RemusNetworkUtilitiesSettings.EnderChestCommand)
            try {
            ServerPlayerEntity player = context.getSource().getPlayer();
            if (player != null) {
                if (hasEnderChestInInventory(player)) {
                    player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) ->
                            new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, i, player.getInventory(), player.getEnderChestInventory(), 3),
                            Text.of("Ender Chest")
                    ));
                } else {
                    player.sendMessage(Text.of("You must have an Ender Chest in your inventory to use this command."), false);
                }
            } else {
                context.getSource().sendError(Text.of("This command can only be executed by a player."));
            }
        } catch (Exception e) {
            System.out.println("An error occurred while trying to open the Ender Chest: " + e.getMessage());
        }
    }
    private static boolean hasEnderChestInInventory(ServerPlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack itemStack = player.getInventory().getStack(i);
            if (itemStack.getItem() == Items.ENDER_CHEST) {
                return true;
            }
        }
        return false;
    }
    public static class EnderChestCMD {
        public static int openEnderChest(CommandContext<ServerCommandSource> context) {
            EnderChestCommand.openEnderChest(context);
            return 1;
        }
    }
}