package cloud.ed.client; // Matching your folder structure!

import cloud.ed.CloudedSounds;
import cloud.ed.GlitchOverlay;
import cloud.ed.MusicManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

public class CloudedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 1. Initialize sounds
        CloudedSounds.init();

        // 2. Check player health every tick for music updates
        ClientTickEvents.END_CLIENT_TICK.register(client -> MusicManager.tick());

        // 3. Register the procedural glitch rendering
        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath("clouded", "glitches"), 
            GlitchOverlay::render
        );
    }
}