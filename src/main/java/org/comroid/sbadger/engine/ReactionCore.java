package org.comroid.sbadger.engine;

import de.comroid.javacord.util.commands.Command;
import de.comroid.javacord.util.commands.CommandGroup;

import org.javacord.api.entity.permission.PermissionType;

public final class ReactionCore {
    public static final ReactionCore.Commands COMMANDS = Commands.INSTANCE;

    @CommandGroup(name = "AutoReaction Setup Commands", description = "All commands to set up automated Reactions", ordinal = 1)
    public enum Commands {
        INSTANCE;

        @Command(
                aliases = "setup-reaction",
                description = "Starts an assistant for reaction automation",
                usage = "setup-reaction",
                ordinal = Integer.MIN_VALUE,
                enablePrivateChat = false,
                requiredDiscordPermission = PermissionType.MANAGE_CHANNELS,
                maximumArguments = 0,
                convertStringResultsToEmbed = true,
                useTypingIndicator = true,
                async = true
        )
        public Object assistant() {
            return "unimplemented"; // TODO: 11.01.2020 implement
        }
    }
}
