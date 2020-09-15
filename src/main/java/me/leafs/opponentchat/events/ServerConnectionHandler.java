package me.leafs.opponentchat.events;

import me.leafs.opponentchat.OpponentChat;
import me.leafs.opponentchat.utils.server.ServerUtils;
import me.leafs.opponentchat.utils.server.EnabledServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class ServerConnectionHandler {
    @SubscribeEvent
    public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.isSingleplayer()) {
            return;
        }

        OpponentChat instance = OpponentChat.instance;
        ServerData data = mc.getCurrentServerData();
        if (data == null) {
            return;
        }

        String address = data.serverIP;
        EnabledServer server = ServerUtils.findServer(address);

        // set the server regardless of null
        instance.setServer(server);
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        /* when the user disconnects from a server
        doesn't matter the circumstances, remove current server */
        OpponentChat.instance.setServer(null);
    }
}
