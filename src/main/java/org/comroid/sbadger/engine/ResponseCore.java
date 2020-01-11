package org.comroid.sbadger.engine;

import de.comroid.javacord.util.commands.Command;
import de.comroid.javacord.util.commands.CommandGroup;

import org.javacord.api.entity.permission.PermissionType;

public final class ResponseCore {
    public static final ResponseCore.Commands COMMANDS = Commands.INSTANCE;

    @CommandGroup(name = "AutoResponse Setup Commands", description = "All commands to set up automated Responses", ordinal = 2)
    public enum Commands {
        INSTANCE;

        @Command(
                aliases = "setup-response",
                description = "Starts an assistant for response automation",
                usage = "setup-response",
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
