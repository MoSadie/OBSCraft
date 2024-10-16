package com.mosadie.obswscraft.fabric.client;

import com.mosadie.obswscraft.ObsWsCraft;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import static com.mosadie.obswscraft.ObsWsCraft.handleTranslatableContent;

public final class ObsWsCraftFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        ObsWsCraft.init();

        ClientReceiveMessageEvents.ALLOW_GAME.register(this::handleGameMessage);
    }

    private boolean handleGameMessage(Component component, boolean b) {
        if (component.getContents() instanceof TranslatableContents translatableContents) {
            return !handleTranslatableContent(translatableContents);
        }

        return true;
    }
}
