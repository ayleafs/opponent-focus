package me.leafs.opponentchat;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import lombok.Setter;
import me.leafs.opponentchat.events.ChatMessageHandler;
import me.leafs.opponentchat.events.ServerCommandChangeEvent;
import me.leafs.opponentchat.events.ServerConnectionHandler;
import me.leafs.opponentchat.utils.server.EnabledServer;
import me.leafs.opponentchat.utils.server.ServerUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

@Mod(modid = OpponentChat.MODID, version = OpponentChat.VERSION)
public class OpponentChat {
    public static final String MODID = "opponentchat";
    public static final String VERSION = "1.0";

    // hard-coded to avoid easily exploitable mechanics (e.g. modification of internal patterns.json's URL)
    // NOTE: "EASILY" exploitable, still very exploitable LMAO
    public static final String PATTERNS_UPDATE = "https://raw.githubusercontent.com/ayleafs/opponent-focus/master/src/main/resources/patterns.json";
    public static final JsonParser JSON_PARSER = new JsonParser();

    @Mod.Instance
    public static OpponentChat instance;

    @Setter @Getter private boolean toggled;
    @Getter @Setter private EnabledServer server;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        toggled = false;
        server = null;

        setupChatPatterns();
        registerEvents();
    }

    private void setupChatPatterns() {
        JsonObject json = ServerUtils.getJson(PATTERNS_UPDATE);

        // if unable to get patterns, use internal one
        if (json == null) {
            InputStream patternsIn = getClass().getResourceAsStream("/patterns.json");
            JsonReader reader = new JsonReader(new InputStreamReader(patternsIn));

            try {
                // try to parse the internal file
                json = JSON_PARSER.parse(reader).getAsJsonObject();
            } catch (JsonIOException e) {
                System.out.println("Failed to parse patterns");
                return;
            }
        }

        try {
            // go through each server and add it to the ServerUtils server list
            Set<Map.Entry<String, JsonElement>> serverEntries = json.getAsJsonObject("servers").entrySet();

            for (Map.Entry<String, JsonElement> entry : serverEntries) {
                EnabledServer server = new EnabledServer(entry.getKey());
                // add all patterns to server chat patterns
                for (JsonElement jsonElement : entry.getValue().getAsJsonArray()) {
                    server.add(jsonElement.getAsString());
                }

                // add the server
                ServerUtils.getServers().add(server);
            }
        } catch (JsonIOException | PatternSyntaxException ignored) { }

        System.out.printf("Loaded a total of %d server configs%n", ServerUtils.getServers().size());
    }

    private void registerEvents() {
        EventBus bus = MinecraftForge.EVENT_BUS;

        bus.register(new ChatMessageHandler());
        bus.register(new ServerConnectionHandler());
        // register our custom event ;o (fuckin pain in the ass ASM is, you bet I'm using it)
        bus.register(new ServerCommandChangeEvent());
    }

    public boolean isEnabled() {
        return toggled && ServerUtils.getServers().size() > 0 && server != null && !Minecraft.getMinecraft().isSingleplayer();
    }
}
