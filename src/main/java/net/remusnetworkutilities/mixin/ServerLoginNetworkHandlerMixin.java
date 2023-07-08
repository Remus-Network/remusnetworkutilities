package net.remusnetworkutilities.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import net.remusnetworkutilities.mixin.accessors.ServerLoginNetworkHandlerAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
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
    private static final String LogFilePath = CONFIG.getProperty("logFilePath");
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Object lock = new Object();
    @Inject(method = "disconnect", at = @At("HEAD"))
    public void onDisconnect(Text reason, CallbackInfo ci) {
        LOGGER.error("onDisconnect method called"); // Log when the method is called

        ServerLoginNetworkHandlerAccessor accessor = (ServerLoginNetworkHandlerAccessor)this;
        GameProfile profile = accessor.getProfile();
        String ipAddress = accessor.getConnection().getAddress().toString();

        // Log the profile and connection
        LOGGER.error("Profile: {}", profile);
        LOGGER.error("Connection: {}", accessor.getConnection());

        if (profile.getId() == null) {
            LOGGER.error("Disconnect without login from IP address {}", ipAddress);
            synchronized (lock) {
                try {
                    String logEntry = String.format("%s %s\n", LocalDateTime.now().format(formatter), ipAddress);
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
            }
        }
    }
}