package cc.barnab.commandoptimiser.mixin;

import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseCommandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseCommandBlock.class)
public abstract class BaseCommandBlockMixin {
    @Shadow public abstract ServerLevel getLevel();

    @Shadow private String command;

    @Unique
    private String commandoptimiser$parsedCommand;
    @Unique
    private String commandoptimiser$commandNoSlash;

    @Shadow public abstract CommandSourceStack createCommandSourceStack();

    @Shadow private int successCount;
    @Unique
    private ParseResults<CommandSourceStack> commandoptimiser$parseResults;

    @Inject(method = "setCommand", at = @At("TAIL"))
    private void parseOnSetCommand(String string, CallbackInfo ci) {
        // Parse when the player changes the command
        if (getLevel() != null)
            commandoptimiser$parse();
    }

    @Redirect(method = "performCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BaseCommandBlock;createCommandSourceStack()Lnet/minecraft/commands/CommandSourceStack;"))
    private CommandSourceStack skipCommandSource(BaseCommandBlock instance) {
        // Don't create unused command source
        return null;
    }

    @Redirect(method = "performCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/CommandSourceStack;withCallback(Lnet/minecraft/commands/CommandResultCallback;)Lnet/minecraft/commands/CommandSourceStack;"))
    private CommandSourceStack skipCommandSource2(CommandSourceStack instance, CommandResultCallback commandResultCallback) {
        // Don't call withCallback on null command source
        return null;
    }

    @Redirect(method = "performCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;performPrefixedCommand(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)V"))
    private void performParsed(Commands instance, CommandSourceStack commandSourceStack, String string) {
        // Parse if not done by first call
        if (commandoptimiser$parseResults == null || !string.equals(commandoptimiser$parsedCommand))
            commandoptimiser$parse();

        // Perform parsed command
        instance.performCommand(commandoptimiser$parseResults, commandoptimiser$commandNoSlash);
    }


    @Unique
    private void commandoptimiser$parse() {
        // Create command source stack
        CommandSourceStack commandSourceStack = createCommandSourceStack().withCallback((bl, i) -> {
            if (bl) {
                ++successCount;
            }
        });

        // Get server
        MinecraftServer minecraftServer = getLevel().getServer();

        // Store command string we are parsing
        commandoptimiser$parsedCommand = command;

        // Parse command
        commandoptimiser$commandNoSlash = command.startsWith("/") ? command.substring(1) : command;
        commandoptimiser$parseResults = minecraftServer.getCommands().getDispatcher().parse(commandoptimiser$commandNoSlash, commandSourceStack);
    }
}
