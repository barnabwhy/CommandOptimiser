package cc.barnab.commandoptimiser.mixin;

import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.BaseCommandBlock.CloseableCommandBlockSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static net.minecraft.commands.Commands.trimOptionalPrefix;

@Mixin(BaseCommandBlock.class)
public abstract class BaseCommandBlockMixin {
    @Shadow private String command;

    @Unique
    private String commandoptimiser$parsedCommand;
    @Unique
    private String commandoptimiser$commandNoSlash;

    @Shadow public abstract CommandSourceStack createCommandSourceStack(ServerLevel serverLevel, CommandSource commandSource);

    @Shadow private int successCount;

    @Shadow @Nullable protected abstract CloseableCommandBlockSource createSource(ServerLevel serverLevel);

    @Unique
    private ParseResults<CommandSourceStack> commandoptimiser$parseResults;

    @Inject(method = "setCommand", at = @At("TAIL"))
    private void parseOnSetCommand(String string, CallbackInfo ci) throws Exception {
        // Parse when the player changes the command (DISABLED, Mojang removed serverLevel from this class)
        // commandoptimiser$parse(serverLevel);
    }

    @Redirect(method = "performCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BaseCommandBlock;createSource(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/level/BaseCommandBlock$CloseableCommandBlockSource;"))
    private CloseableCommandBlockSource skipCommandSource(BaseCommandBlock instance, ServerLevel serverLevel) {
        // Don't create unused command source
        return null;
    }

    @Redirect(method = "performCommand", at = @At(value = "INVOKE", target = "Ljava/util/Objects;requireNonNullElse(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object skipCommandSource2(Object obj, Object defaultObj) {
        // Don't create unused command source
        return null;
    }

    @Redirect(method = "performCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BaseCommandBlock;createCommandSourceStack(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/commands/CommandSource;)Lnet/minecraft/commands/CommandSourceStack;"))
    private CommandSourceStack skipCommandSource3(BaseCommandBlock instance, ServerLevel serverLevel, CommandSource commandSource) {
        // Don't create unused command source
        return null;
    }

    @Redirect(method = "performCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/CommandSourceStack;withCallback(Lnet/minecraft/commands/CommandResultCallback;)Lnet/minecraft/commands/CommandSourceStack;"))
    private CommandSourceStack skipCommandSource4(CommandSourceStack instance, CommandResultCallback commandResultCallback) {
        // Don't call withCallback on null command source
        return null;
    }

    @Redirect(method = "performCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;performPrefixedCommand(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)V"))
    private void performParsed(Commands instance, CommandSourceStack commandSourceStack, String string, ServerLevel serverLevel) throws Exception {
        // Parse if not done by first call
        if (commandoptimiser$parseResults == null || !string.equals(commandoptimiser$parsedCommand))
            commandoptimiser$parse(serverLevel);

        // Perform parsed command
        instance.performCommand(commandoptimiser$parseResults, commandoptimiser$commandNoSlash);
    }


    @Unique
    private void commandoptimiser$parse(ServerLevel serverLevel) throws Exception {
        if (serverLevel == null)
            return;

        // Create command source stack
        CommandSourceStack commandSourceStack;
        try (CloseableCommandBlockSource closeableCommandBlockSource = this.createSource(serverLevel)) {
            CommandSource commandSource = Objects.requireNonNullElse(closeableCommandBlockSource, CommandSource.NULL);
            commandSourceStack = createCommandSourceStack(serverLevel, commandSource).withCallback((bl, i) -> {
                if (bl) {
                    ++successCount;
                }
            });
        }

        // Get server
        MinecraftServer minecraftServer = serverLevel.getServer();

        // Store command string we are parsing
        commandoptimiser$parsedCommand = command;

        // Parse command
        commandoptimiser$commandNoSlash = trimOptionalPrefix(command);
        commandoptimiser$parseResults = minecraftServer.getCommands().getDispatcher().parse(commandoptimiser$commandNoSlash, commandSourceStack);
    }
}
