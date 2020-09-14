package me.leafs.opponentchat;

import com.google.common.eventbus.EventBus;
import lombok.Getter;
import lombok.Setter;
import me.leafs.opponentchat.events.ServerCommandChangeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Mod(modid = OpponentChat.MODID, version = OpponentChat.VERSION)
public class OpponentChat extends DummyModContainer {
    public static final String MODID = "opponentchat";
    public static final String VERSION = "1.0";

    @Mod.Instance
    public static OpponentChat instance;

    @Getter @Setter private boolean enabled;

    public OpponentChat() {
        super(new ModMetadata());

        ModMetadata meta = getMetadata();
        meta.modId = MODID;
        meta.name = "Opponent Focus";
        meta.version = VERSION;

        meta.authorList = Collections.singletonList("leafs");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        enabled = false;

        // register our custom event ;o (fuckin pain in the ass ASM is, you bet I'm using it)
        MinecraftForge.EVENT_BUS.register(new ServerCommandChangeEvent());
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }
}
