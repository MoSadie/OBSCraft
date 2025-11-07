package com.mosadie.obscraft.neoforge;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import com.mosadie.obscraft.ObsCraft;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ObsCraft.MOD_ID)
public final class ObsCraftNeoForge {
    public ObsCraftNeoForge(IEventBus modbus) {
        modbus.addListener(this::registerKeyMappingsEvent);
        modbus.addListener(this::onClientSetup);

        NeoForge.EVENT_BUS.addListener(this::onChatMessage);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        // Run our common setup.
        ObsCraft.init();
    }

    public void registerKeyMappingsEvent(RegisterKeyMappingsEvent event) {
        event.registerCategory(ObsCraft.KEY_CATEGORY);

        for (var key : ObsCraft.ACTION_KEYS) {
            event.register(key);
        }
    }

    public void onChatMessage(ClientChatReceivedEvent event) {
        if (event.getMessage().getContents() instanceof TranslatableContents translatableContents) {
            boolean result = ObsCraft.handleTranslatableContent(translatableContents);
            event.setCanceled(result);
        }
    }
}
