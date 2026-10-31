package cloud.ed;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class MusicManager {
    private enum State {
        SILENT, INTRO, PHASE_1, PHASE_2, PHASE_3, OUTRO
    }

    private static State currentState = State.SILENT;
    private static ModMusicInstance currentMusicInstance = null;
    
    // We use this to detect if you left a world and joined a new server
    private static LocalPlayer lastPlayer = null; 

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        // 1. Detect if we left a server or joined a new one
        if (player == null) {
            resetEngine(mc);
            return;
        }
        if (player != lastPlayer) {
            resetEngine(mc);
            lastPlayer = player; // Lock onto the new player object
        }

        if (mc.isPaused()) return;

        float maxHealth = player.getMaxHealth();
        float currentHealth = player.getHealth();
        
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
                    playTrack(mc, CloudedSounds.START, false);
                    currentState = State.INTRO;
                }
                break;
                
            case INTRO:
                if (targetPhaseNumber == 0) {
                    playTrack(mc, CloudedSounds.OUTRO, false);
                    currentState = State.OUTRO;
                } else if (currentMusicInstance == null || !mc.getSoundManager().isActive(currentMusicInstance)) {
                    startPhaseLoop(mc, targetPhaseNumber);
                }
                break;
                
            case PHASE_1:
            case PHASE_2:
            case PHASE_3:
                if (targetPhaseNumber == 0) {
                    playTrack(mc, CloudedSounds.OUTRO, false);
                    currentState = State.OUTRO;
                } else {
                    int currentPhaseNumber = switch (currentState) {
                        case PHASE_1 -> 1;
                        case PHASE_2 -> 2;
                        case PHASE_3 -> 3;
                        default -> 0;
                    };
                    
                    if (targetPhaseNumber != currentPhaseNumber) {
                        startPhaseLoop(mc, targetPhaseNumber);
                    } else if (currentMusicInstance != null && !mc.getSoundManager().isActive(currentMusicInstance)) {
                        // Failsafe: If the server aggressively killed our loop, restart it instantly!
                        startPhaseLoop(mc, targetPhaseNumber);
                    }
                }
                break;
                
            case OUTRO:
                if (targetPhaseNumber > 0) {
                    playTrack(mc, CloudedSounds.START, false);
                    currentState = State.INTRO;
                } else if (currentMusicInstance == null || !mc.getSoundManager().isActive(currentMusicInstance)) {
                    currentState = State.SILENT;
                    currentMusicInstance = null;
                }
                break;
        }
    }

    // Completely resets the audio when world-hopping
    private static void resetEngine(Minecraft mc) {
        currentState = State.SILENT;
        if (currentMusicInstance != null) {
            mc.getSoundManager().stop(currentMusicInstance);
            currentMusicInstance = null;
        }
        lastPlayer = null;
    }

    private static void startPhaseLoop(Minecraft mc, int phase) {
        SoundEvent event = switch (phase) {
            case 1 -> CloudedSounds.PHASE_1;
            case 2 -> CloudedSounds.PHASE_2;
            case 3 -> CloudedSounds.PHASE_3;
            default -> CloudedSounds.PHASE_1;
        };
        
        playTrack(mc, event, true);
        
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

    private static class ModMusicInstance extends AbstractTickableSoundInstance {
        public ModMusicInstance(SoundEvent sound, boolean loop) {
            super(sound, SoundSource.RECORDS, RandomSource.create());
            this.looping = loop;
            this.delay = 0;
            this.volume = 1.0F;
            this.pitch = 1.0F;
            this.relative = true; 
            // Setting position to 0 ensures compatibility with server-based spatial audio
            this.x = 0;
            this.y = 0;
            this.z = 0;
        }

        @Override
        public void tick() {
        }
    }
}