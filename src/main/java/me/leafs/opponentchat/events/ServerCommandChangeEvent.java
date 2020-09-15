package me.leafs.opponentchat.events;

import me.leafs.opponentchat.OpponentChat;
import me.leafs.opponentchat.utils.ChatUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ServerCommandChangeEvent {
    @SubscribeEvent(receiveCanceled = true)
    public void onMessageSend(ClientPreChatEvent event) {
        OpponentChat opponentChat = OpponentChat.instance;
        if (opponentChat.getServer() == null) {
            return;
        }

        // to avoid overriding the togglechat command on servers completely
        // dw, I'm mad I did it like this too <3
        if (event.message.equals("/togglechat focus")) {
            boolean newToggled = !opponentChat.isToggled();
            ChatUtils.printChat(String.format("§eOpponent Focus mode was %s§e.", newToggled ? "§aenabled" : "§cdisabled"));

            event.setCanceled(true);
            opponentChat.setToggled(newToggled);
        }
    }
}
