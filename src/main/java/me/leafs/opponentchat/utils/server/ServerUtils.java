package me.leafs.opponentchat.utils.server;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import lombok.SneakyThrows;
import me.leafs.opponentchat.OpponentChat;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ServerUtils {
    @Getter private static final List<EnabledServer> servers = new ArrayList<>();

    @SneakyThrows
    public static JsonObject getJson(String strUrl) {
        URL url = new URL(strUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        // make sure the request is OK
        if (con.getResponseCode() != 200) {
            return null;
        }

        try {
            JsonReader reader = new JsonReader(new InputStreamReader(con.getInputStream()));

            // return the parsed data
            return OpponentChat.JSON_PARSER.parse(reader).getAsJsonObject();
        } catch (JsonIOException e) {
            return null;
        }
    }

    public static EnabledServer findServer(String address) {
        return servers.stream().filter(server -> server.getIpPattern().matcher(address).matches())
                .findAny().orElse(null);
    }
}
