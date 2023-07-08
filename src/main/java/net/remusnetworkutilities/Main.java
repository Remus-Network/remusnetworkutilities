package net.remusnetworkutilities;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.ServerCommandSource;
import net.remusnetworkutilities.commands.EnderChestCommand;
import net.remusnetworkutilities.commands.SetLogFilePathCommand;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static net.minecraft.server.command.CommandManager.literal;
public class Main implements ModInitializer {
	public static Properties CONFIG;
	private static final String CONFIG_FILE = "config/remusnetworkutilities.properties";
	public static boolean reIntroduceTrapdoorUpdateSkipping = true;

	@Override
	public void onInitialize() {
		CONFIG = new Properties();
		try {
			FileInputStream in = new FileInputStream(CONFIG_FILE);
			CONFIG.load(in);
			in.close();
		} catch (IOException e) {
			System.out.println("Could not load configuration, using defaults");
			String parentDir = new File(CONFIG_FILE).getParent();
			File dir = new File(parentDir);
			if (!dir.exists()) {
				boolean dirCreated = dir.mkdirs();  // Create the directory if it doesn't exist
				if (!dirCreated) {
					System.out.println("Could not create directory: " + dir.getPath());
					System.setErr(System.out);
				}
			}
			String defaultLogFilePath;
			try {
				defaultLogFilePath = new File(".").getCanonicalPath() + "/config/failed_login_attempts.log";
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
			CONFIG.setProperty("logFilePath", defaultLogFilePath);
			saveConfig();
		}
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			CommandDispatcher<ServerCommandSource> dispatcher = server.getCommandManager().getDispatcher();
			LiteralArgumentBuilder<ServerCommandSource> node = literal("enderchest").executes(EnderChestCommand.EnderChestCMD::openEnderChest);
			LiteralArgumentBuilder<ServerCommandSource> nodeAlias = literal("ec").executes(EnderChestCommand.EnderChestCMD::openEnderChest);
			dispatcher.getRoot().addChild(node.build());
			dispatcher.getRoot().addChild(nodeAlias.build());
			SetLogFilePathCommand.register(dispatcher);
		});
	}

	public static void saveConfig() {
		try {
			FileOutputStream out = new FileOutputStream(CONFIG_FILE);
			CONFIG.store(out, null);
			out.close();
		} catch (IOException e) {
			System.out.println("Could not save configuration");
		}
	}
}


