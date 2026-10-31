package cloud.ed;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class MusicManager {
    // These states tell the game what part of the song is currently playing
    private enum State {
        SILENT, INTRO, PHASE_1, PHASE_2, PHASE_3, OUTRO
    }

    private static State currentState = State.SILENT;
    private static ModMusicInstance currentMusicInstance = null;

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.isPaused()) return;

        float maxHealth = player.getMaxHealth();
        float currentHealth = player.getHealth();
        
        // Target Phase (0 = Full Health, 1-3 = Danger Levels)
        int targetPhaseNumber = 0; 
        
        if (currentHealth < maxHealth && currentHealth > 0) {
            if (currentHealth > maxHealth * 0.66f) {
                targetPhaseNumber = 1;
            } else if (currentHealth > maxHealth * 0.33f) {
                targetPhaseNumber = 2;
            } else {
                targetPhaseNumber = 3;
            }
        }

        // --- The Audio State Machine ---
        switch (currentState) {
            case SILENT:
                if (targetPhaseNumber > 0) {
                    playTrack(mc, CloudedSounds.START, false); // Play Intro (No loop)
                    currentState = State.INTRO;
                }
                break;
                
            case INTRO:
                if (targetPhaseNumber == 0) {
                    // Player healed to full before intro even finished
                    playTrack(mc, CloudedSounds.OUTRO, false);
                    currentState = State.OUTRO;
                } else if (currentMusicInstance == null || !mc.getSoundManager().isActive(currentMusicInstance)) {
                    // The intro track finished! Seamlessly start looping the correct phase.
                    startPhaseLoop(mc, targetPhaseNumber);
                }
                break;
                
            case PHASE_1:
            case PHASE_2:
            case PHASE_3:
                if (targetPhaseNumber == 0) {
                    // Player healed back to full! Stop looping and play Outro.
                    playTrack(mc, CloudedSounds.OUTRO, false); // Play Outro (No loop)
                    currentState = State.OUTRO;
                } else {
                    // Check if player took more damage and shifted to a worse phase
                    int currentPhaseNumber = switch (currentState) {
                        case PHASE_1 -> 1;
                        case PHASE_2 -> 2;
                        case PHASE_3 -> 3;
                        default -> 0;
                    };
                    
                    if (targetPhaseNumber != currentPhaseNumber) {
                        startPhaseLoop(mc, targetPhaseNumber);
                    }
                }
                break;
                
            case OUTRO:
                if (targetPhaseNumber > 0) {
                    // Player took damage while outro was playing! Cut back to Intro.
                    playTrack(mc, CloudedSounds.START, false);
                    currentState = State.INTRO;
                } else if (currentMusicInstance == null || !mc.getSoundManager().isActive(currentMusicInstance)) {
                    // Outro finished naturally. Return to peace and quiet.
                    currentState = State.SILENT;
                    currentMusicInstance = null;
                }
                break;
        }
    }

    private static void startPhaseLoop(Minecraft mc, int phase) {
        SoundEvent event = switch (phase) {
            case 1 -> CloudedSounds.PHASE_1;
            case 2 -> CloudedSounds.PHASE_2;
            case 3 -> CloudedSounds.PHASE_3;
            default -> CloudedSounds.PHASE_1;
        };
        
        playTrack(mc, event, true); // True = Loop forever
        
        currentState = switch (phase) {
            case 1 -> State.PHASE_1;
            case 2 -> State.PHASE_2;
            case 3 -> State.PHASE_3;
            default -> State.PHASE_1;
        };
    }

    private static void playTrack(Minecraft mc, SoundEvent sound, boolean looping) {
        if (currentMusicInstance != null) {
            mc.getSoundManager().stop(currentMusicInstance);
        }
        currentMusicInstance = new ModMusicInstance(sound, looping);
        mc.getSoundManager().play(currentMusicInstance);
    }

    // --- Custom Sound Instance Class ---
    // This allows us to forcefully tell Minecraft to loop the track forever!
    private static class ModMusicInstance extends AbstractTickableSoundInstance {
        public ModMusicInstance(SoundEvent sound, boolean loop) {
            super(sound, SoundSource.RECORDS, RandomSource.create());
            this.looping = loop;
            this.delay = 0;
            this.volume = 1.0F;
            this.pitch = 1.0F;
            this.relative = true; // Plays exactly in your ears regardless of where you look
        }

        @Override
        public void tick() {
            // Keep ticking in the background
        }
    }
}