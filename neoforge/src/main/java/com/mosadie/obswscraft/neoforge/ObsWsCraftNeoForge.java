package com.mosadie.obswscraft.neoforge;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.fml.common.Mod;

import com.mosadie.obswscraft.ObsWsCraft;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ObsWsCraft.MOD_ID)
public final class ObsWsCraftNeoForge {
    public ObsWsCraftNeoForge() {
        // Run our common setup.
        ObsWsCraft.init();

        NeoForge.EVENT_BUS.addListener(this::onChatMessage);
        NeoForge.EVENT_BUS.addListener(this::registerClientCommand);
    }

    public void registerClientCommand(RegisterClientCommandsEvent event) {
        // Register our client command.
        event.getDispatcher().register(ObsWsCraft.getNeoForgeCommand());
    }

    public void onChatMessage(ClientChatReceivedEvent event) {
        if (event.getMessage().getContents() instanceof TranslatableContents translatableContents) {
            ObsWsCraft.handleTranslatableContent(translatableContents);
            event.setCanceled(true);
        }
    }
}
