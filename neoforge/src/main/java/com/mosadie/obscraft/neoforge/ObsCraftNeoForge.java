package com.mosadie.obscraft.neoforge;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.fml.common.Mod;

import com.mosadie.obscraft.ObsCraft;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ObsCraft.MOD_ID)
public final class ObsCraftNeoForge {
    public ObsCraftNeoForge() {
        // Run our common setup.
        ObsCraft.init();

        NeoForge.EVENT_BUS.addListener(this::onChatMessage);
    }

    public void onChatMessage(ClientChatReceivedEvent event) {
        if (event.getMessage().getContents() instanceof TranslatableContents translatableContents) {
            ObsCraft.handleTranslatableContent(translatableContents);
            event.setCanceled(true);
        }
    }
}
