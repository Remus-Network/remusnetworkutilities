package net.remusnetworkutilities;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.ServerCommandSource;
import net.remusnetworkutilities.commands.EnderChestCommand;
import net.remusnetworkutilities.commands.SetLogFilePathCommand;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import static net.minecraft.data.DataProvider.LOGGER;
import static net.minecraft.server.command.CommandManager.literal;

public class Main implements ModInitializer {
	public static Properties CONFIG;
	private static final String CONFIG_FILE = "config/remusnetworkutilities.properties";
	public static boolean reIntroduceTrapdoorUpdateSkipping = true;


	@Override
	public void onInitialize() {
		Configurator.setLevel(LogManager.getLogger().getName(), Level.ALL);
		LOGGER.info("RemusNetwork is initializing");

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			LOGGER.info("Server is stopping, shutting down ExecutorService");
			ExecutorServiceManager.shutdownExecutor();
		});

		CONFIG = new Properties();
		loadConfig();
		try {
			FileInputStream in = new FileInputStream(CONFIG_FILE);
			CONFIG.load(in);
			in.close();
		} catch (IOException e) {
			System.out.println("Could not load configuration, using defaults");
			String parentDir = new File(CONFIG_FILE).getParent();
			File dir = new File(parentDir);
			if (!dir.exists()) {
				boolean dirCreated = dir.mkdirs();
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
			LiteralArgumentBuilder<ServerCommandSource> node2 = literal("ENDERCHEST").executes(EnderChestCommand.EnderChestCMD::openEnderChest);
			LiteralArgumentBuilder<ServerCommandSource> nodeAlias2 = literal("EC").executes(EnderChestCommand.EnderChestCMD::openEnderChest);
			dispatcher.getRoot().addChild(node2.build());
			dispatcher.getRoot().addChild(nodeAlias2.build());
			dispatcher.getRoot().addChild(node.build());
			dispatcher.getRoot().addChild(nodeAlias.build());
			SetLogFilePathCommand.register(dispatcher);
		});
	}
	private void loadConfig() {
		CONFIG.setProperty("logFilePath", "config/failed_login_attempts.log");
		CONFIG.setProperty("enableEnderChestCommand", "true");
		CONFIG.setProperty("enableSetLogFilePathCommand", "true");
		CONFIG.setProperty("enableFailedLoginAttemptsLogging", "true");
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


