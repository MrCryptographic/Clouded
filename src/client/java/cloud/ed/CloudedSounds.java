package cloud.ed;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class CloudedSounds {
    public static final SoundEvent START = register("start");
    public static final SoundEvent PHASE_1 = register("phase_1");
    public static final SoundEvent PHASE_2 = register("phase_2");
    public static final SoundEvent PHASE_3 = register("phase_3");
    public static final SoundEvent OUTRO = register("outro");

    private static SoundEvent register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath("clouded", name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {
    }
}