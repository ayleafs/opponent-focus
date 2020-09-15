package me.leafs.opponentchat.events;

import me.leafs.opponentchat.OpponentChat;
import me.leafs.opponentchat.utils.server.EnabledServer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChatMessageHandler {
    private final Map<String, Long> seenEntities = new HashMap<>();

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
        if (displayName == null) {
            return; // return, nothing will be done (it's not a chat message)
        }

        // if any visible entity's name isn't in the display, then cancel the chat message
        if (seenEntities.keySet().stream().noneMatch(displayName::contains)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.ClientTickEvent event) {
        // save resources
        if (!OpponentChat.instance.isEnabled()) {
            seenEntities.clear();
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) {
            return;
        }

        final long currentTime = System.currentTimeMillis();
        List<Entity> entities = mc.theWorld.loadedEntityList;
        // find all players
        List<EntityPlayer> players = entities.stream().filter(entity -> entity instanceof EntityPlayer)
                .map(entity -> (EntityPlayer) entity).collect(Collectors.toList());

        // only allow 2 players (thePlayer & opponent)
        if (players.size() > 2) {
            return;
        }

        // add all player names with their time last seen
        for (EntityPlayer player : players) {
            seenEntities.put(player.getName(), currentTime);
        }

        // remove all entities that have surpassed their time (of 1 minute)
        Iterator<Map.Entry<String, Long>> iterator = seenEntities.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            long lastSeen = entry.getValue();
            if (currentTime - lastSeen < 60 * 1000L) {
                continue;
            }

            iterator.remove();
        }
    }
}
