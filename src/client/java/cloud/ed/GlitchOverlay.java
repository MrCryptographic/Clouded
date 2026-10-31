package cloud.ed; // Make sure this matches where your file is!

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.DeltaTracker;
import java.util.Random;

public class GlitchOverlay {

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.options.hideGui) return;

        float maxHealth = player.getMaxHealth();
        float currentHealth = player.getHealth();
        float healthPct = currentHealth / maxHealth;

        // Start glitching when below 80% health
        if (healthPct > 0.8f) return;

        float intensity = 1.0f - (healthPct / 0.8f);
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // Seed based on exact time to make it jitter wildly but consistently per-frame
        long seed = (long) (player.tickCount * 10 + deltaTracker.getGameTimeDeltaTicks() * 10);
        Random random = new Random(seed);

        // The lower the health, the more glitch lines appear
        int numBlocks = (int) (60 * intensity) + 10;

        for (int i = 0; i < numBlocks; i++) {
            // Cyberpunk / VHS screen-tearing colors
            int colorChoice = random.nextInt(4);
            int color = switch(colorChoice) {
                case 0 -> 0x77FF00FF; // Translucent Magenta
                case 1 -> 0x7700FFFF; // Translucent Cyan
                case 2 -> 0x99000000; // Semi-solid Black
                default -> 0x884B0082; // Dark Purple
            };
            
            // 85% chance to be a long, very thin horizontal scanline
            boolean isScanline = random.nextFloat() < 0.85f;
            int w = isScanline ? random.nextInt(50, width) : random.nextInt(10, 50);
            int h = isScanline ? random.nextInt(1, 3 + (int)(5 * intensity)) : random.nextInt(5, 20);
            
            int x = random.nextInt(width);
            int y = random.nextInt(height);

            // Force 70% of the glitches to stick to the top and bottom edges of the screen
            if (random.nextFloat() < 0.7f) {
                y = random.nextBoolean() ? random.nextInt(height / 4) : height - random.nextInt(height / 4);
            }

            guiGraphics.fill(x, y, x + w, y + h, color);
        }
    }
}