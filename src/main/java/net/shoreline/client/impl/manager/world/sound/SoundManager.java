package net.shoreline.client.impl.manager.world.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.shoreline.client.util.Globals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author linus
 * @since 1.0
 */
public class SoundManager implements Globals {
    //
    public static final SoundEvent GUI_CLICK = registerSound("gui_click");
    // PM Sounds
    public static final SoundEvent TWITTER = registerSound("twitter");
    public static final SoundEvent IOS = registerSound("ios");
    public static final SoundEvent DISCORD = registerSound("discord");
    public static final SoundEvent STEAM = registerSound("steam");

    /**
     * @param sound
     */
    public void playSound(final SoundEvent sound) {
        playSound(sound, 1.2f, 0.75f);
    }

    public void playSound(final SoundEvent sound, float volume, float pitch) {
        if (mc.player != null) {
            mc.executeSync(() -> mc.player.playSound(sound, volume, pitch));
        }
    }

    private static SoundEvent registerSound(String name) {
        String assetPath = "assets/ovaqreborn/sounds/" + name + ".ogg";
        if (SoundManager.class.getClassLoader().getResourceAsStream(assetPath) == null) {
            throw new RuntimeException("Sound file not found: " + assetPath);
        }

        Identifier id = new Identifier("ovaqreborn", name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}