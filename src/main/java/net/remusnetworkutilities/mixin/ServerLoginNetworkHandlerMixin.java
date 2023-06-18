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
        ServerLoginNetworkHandlerAccessor accessor = (ServerLoginNetworkHandlerAccessor)this;
        GameProfile profile = accessor.getProfile();
        String ipAddress = accessor.getConnection().getAddress().toString();
        // Check if the user was not logged in or is not whitelisted and log the IP address so fail2ban can ban it
        // if repeated attempts are made within the window of time specified in the fail2ban config
        // need to have the right filter and jail set up in fail2ban for this to work
        //
        // example jail:
        //
        //  [minecraft]
        //  enabled = true
        //  port = 25565
        //  filter = minecraft
        //  logpath = /config/failed_login_attempts.log (or whatever path you set in the config)
        //  maxretry = 3
        //  findtime = 600
        //  bantime = 3600
        //
        // example filter:
        //
        //  [Definition]
        //  failregex = ^.*\s<HOST>\s.*$
        //  ignoreregex =
        //
        if (profile.getId() == null || reason.equals(Text.translatable("multiplayer.disconnect.not_whitelisted"))) {
            LOGGER.info("Disconnect without login or not whitelisted from IP address {}", ipAddress);
            synchronized (lock) {
                try {
                    String logEntry = String.format("%s %s\n", LocalDateTime.now().format(formatter), ipAddress);
                    Path logFilePath = Paths.get(LogFilePath);
                    if (!Files.exists(logFilePath)) {
                        Files.createFile(logFilePath);
                    }
                    Files.write(logFilePath, logEntry.getBytes(), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    LOGGER.error("An error occurred while writing to the log file", e);
                }
            }
        }
    }
}