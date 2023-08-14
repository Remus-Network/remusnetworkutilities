package net.remusnetworkutilities.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.remusnetworkutilities.Main;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
public class SetLogFilePathCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("logpath")
                .requires(source -> source.hasPermissionLevel(4))
                .then(argument("path", StringArgumentType.greedyString())
                        .executes(SetLogFilePathCommand::setLogFilePath)));
    }
    private static int setLogFilePath(CommandContext<ServerCommandSource> context) {
        String newPath = StringArgumentType.getString(context, "path");
        Main.CONFIG.setProperty("logFilePath", newPath);
        Main.saveConfig();
        context.getSource().sendFeedback(() -> Text.of("Log file path set to " + newPath), true);
        return 1;
    }
}