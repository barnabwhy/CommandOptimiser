package cc.barnab.commandoptimiser.fabric;

import cc.barnab.commandoptimiser.Commandoptimiser;
import net.fabricmc.api.ModInitializer;

public final class CommandoptimiserFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Commandoptimiser.init();
    }
}
