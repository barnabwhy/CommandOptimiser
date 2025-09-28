package cc.barnab.commandoptimiser.neoforge;

import cc.barnab.commandoptimiser.Commandoptimiser;
import net.neoforged.fml.common.Mod;

@Mod(Commandoptimiser.MOD_ID)
public final class CommandoptimiserNeoForge {
    public CommandoptimiserNeoForge() {
        // Run our common setup.
        Commandoptimiser.init();
    }
}
