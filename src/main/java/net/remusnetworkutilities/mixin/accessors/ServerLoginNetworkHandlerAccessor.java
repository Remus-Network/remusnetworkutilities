package net.remusnetworkutilities.mixin.accessors;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.server.MinecraftServer;
@Mixin(ServerLoginNetworkHandler.class)
public interface ServerLoginNetworkHandlerAccessor {
    @Accessor("profile")
    GameProfile getProfile();
    @Accessor("connection")
    ClientConnection getConnection();
    @Accessor("server")
    MinecraftServer getServer();

}