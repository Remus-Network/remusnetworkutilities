package net.remusnetworkutilities.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import net.remusnetworkutilities.ExecutorServiceManager;
import net.remusnetworkutilities.mixin.accessors.ServerLoginNetworkHandlerAccessor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static net.remusnetworkutilities.Main.CONFIG;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin {
    @Unique
    private static final String LogFilePath = CONFIG.getProperty("logFilePath");
    @Unique
    private static final Logger LOGGER = LogManager.getLogger();
    @Unique
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Inject(method = "disconnect", at = @At("HEAD"))
    public void onDisconnect(Text reason, CallbackInfo ci) {
        ServerLoginNetworkHandlerAccessor accessor = (ServerLoginNetworkHandlerAccessor)this;
        GameProfile profile = accessor.getProfile();
        String ipAddress = accessor.getConnection().getAddress().toString();
        LOGGER.error("Profile: {}", profile);
        LOGGER.error("Connection: {}", accessor.getConnection());
        if (profile == null) {
            LOGGER.error("Unknown player with IP address {} tried to join the server", ipAddress);
            logToFile("Unknown player", ipAddress, "tried to join the server");
        }
        else if (profile.getId() == null) {
            LOGGER.error("Player {} with IP address {} tried to join the server", profile.getName(), ipAddress);
            logToFile(profile.getName(), ipAddress, "tried to join the server");
        }
        else if (reason.equals(Text.translatable("multiplayer.disconnect.banned"))) {
            LOGGER.error("Player {} with IP address {} tried to join the server", profile.getName(), ipAddress);
            logToFile(profile.getName(), ipAddress, "tried to join the server but is banned");
        }
        else if (reason.equals(Text.translatable("multiplayer.disconnect.not_whitelisted"))) {
            LOGGER.error("Player {} with IP address {} tried to join the server", profile.getName(), ipAddress);
            logToFile(profile.getName(), ipAddress, "tried to join the server but is not whitelisted");
        }

    }
    @Unique
    private void logToFile(String playerName, String ipAddress, String message) {
        ExecutorServiceManager.getExecutor().submit(() -> {
            try {
                String logEntry = String.format("%s [Failed Login] IP: %s Player: %s Message: %s\n",
                        LocalDateTime.now().format(formatter), ipAddress, playerName, message);
                Path logFilePath = Paths.get(LogFilePath, "failed_login_attempts.log");
                if (!Files.exists(logFilePath)) {
                    LOGGER.error("Log file does not exist, creating it");
                    Files.createFile(logFilePath);
                    if (Files.exists(logFilePath)) {
                        LOGGER.error("Log file was created successfully");
                    } else {
                        LOGGER.error("Failed to create log file");
                    }
                }
                Files.write(logFilePath, logEntry.getBytes(), StandardOpenOption.APPEND);
                LOGGER.error("Player {} with IP address {} {} was logged", playerName, ipAddress, message);
            } catch (Exception e) { // Catch all exceptions
                LOGGER.error("An error occurred while writing to the log file", e);
            }
        });
    }
    static {
        LOGGER.error("ServerLoginNetworkHandlerMixin loaded");
        Configurator.setLevel(LogManager.getLogger(ServerLoginNetworkHandlerMixin.class).getName(), Level.ALL);
    }
}


