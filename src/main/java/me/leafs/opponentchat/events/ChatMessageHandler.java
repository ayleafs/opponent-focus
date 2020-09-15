package me.leafs.opponentchat.events;

import me.leafs.opponentchat.OpponentChat;
import me.leafs.opponentchat.utils.server.EnabledServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatMessageHandler {
    private final List<String> visibleEntities = new ArrayList<>();

    @SubscribeEvent
    public void onMessage(ClientChatReceivedEvent event) {
        OpponentChat opponentChat = OpponentChat.instance;
        if (!opponentChat.isEnabled()) {
            return;
        }

        // convert message to raw message
        String message = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());
        EnabledServer server = opponentChat.getServer();

        // get the display name and check for opponent name(s)
        String displayName = server.getChatUserDisplay(message);
        if (displayName == null || visibleEntities.isEmpty()) {
            return; // return, nothing will be done (it's not a chat message)
        }

        // if any visible entity's name isn't in the display, then cancel the chat message
        if (visibleEntities.stream().noneMatch(displayName::contains)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) {
            return;
        }

        List<Entity> entities = mc.theWorld.loadedEntityList;
        // find all players
        List<EntityPlayer> players = entities.stream().filter(entity -> entity instanceof EntityPlayer)
                .map(entity -> (EntityPlayer) entity).collect(Collectors.toList());

        visibleEntities.clear();

        // only allow 2 players (thePlayer & opponent)
        if (players.size() > 2) {
            return;
        }

        // add all player names
        players.stream().map(EntityPlayer::getName).forEach(visibleEntities::add);
    }
}
