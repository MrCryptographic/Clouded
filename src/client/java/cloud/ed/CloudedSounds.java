package cloud.ed;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class CloudedSounds {
    public static final SoundEvent START = create("start");
    public static final SoundEvent PHASE_1 = create("phase_1");
    public static final SoundEvent PHASE_2 = create("phase_2");
    public static final SoundEvent PHASE_3 = create("phase_3");
    public static final SoundEvent OUTRO = create("outro");

    private static SoundEvent create(String name) {
        Identifier id = Identifier.fromNamespaceAndPath("clouded", name);
        // We skip Registry.register entirely so the server can't delete our sounds!
        return SoundEvent.createVariableRangeEvent(id);
    }

    public static void init() {
    }
}