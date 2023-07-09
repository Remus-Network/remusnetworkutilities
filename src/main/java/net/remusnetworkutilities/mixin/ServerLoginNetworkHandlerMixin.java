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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static net.remusnetworkutilities.Main.CONFIG;
@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin {


    // add listener to the disconnect method
    @Inject(method = "disconnect", at = @At("HEAD"))
    public void onDisconnect(Text reason, CallbackInfo ci) {
        // get the accessor
        ServerLoginNetworkHandlerAccessor accessor = (ServerLoginNetworkHandlerAccessor)this;
        // get the profile and connection
        GameProfile profile = accessor.getProfile();
        String ipAddress = accessor.getConnection().getAddress().toString();
        // log the profile and connection
        LOGGER.error("Profile: {}", profile);
        LOGGER.error("Connection: {}", accessor.getConnection());
        // if the profile id is null, then the player is not premium
        if (profile.getId() == null) {
            // log the player's name and ip address
            LOGGER.error("Player {} with IP address {} tried to join the server with a cracked account", profile.getName(), ipAddress);
            // log the player's name and ip address to the file
            logToFile("Player " + profile.getName() + " with IP address " + ipAddress + " tried to join the server with a cracked account");
        }
    }

    @Unique
    private void logToFile(String s) {
        ExecutorServiceManager.getExecutor().submit(() -> {
            try {
                String logEntry = String.format("%s %s\n", LocalDateTime.now().format(formatter), s);
                Path logFilePath = Paths.get(LogFilePath);
                if (!Files.exists(logFilePath)) {
                    LOGGER.error("Log file does not exist, creating it"); // Log when the file is created
                    Files.createFile(logFilePath);
                    // Check if the file was created successfully
                    if (Files.exists(logFilePath)) {
                        LOGGER.error("Log file was created successfully");
                    } else {
                        LOGGER.error("Failed to create log file");
                    }
                }
                Files.write(logFilePath, logEntry.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                LOGGER.error("An error occurred while writing to the log file", e);
            }
        });
    }


    @Unique
    private static final String LogFilePath = CONFIG.getProperty("logFilePath");
    @Unique
    private static final Logger LOGGER = LogManager.getLogger();
    @Unique
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Static block
    static {
        LOGGER.error("ServerLoginNetworkHandlerMixin loaded");
        // Set the log level to ERROR
        Configurator.setLevel(LogManager.getLogger(ServerLoginNetworkHandlerMixin.class).getName(), Level.ERROR);
    }
}