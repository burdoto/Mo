package de.kaleidox.mo;

import de.kaleidox.Mo;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import static de.kaleidox.Mo.defaultEmbed;

public enum Commands implements CommandExecutor {
    INSTANCE;

    @Command(aliases = "help", usage = "!mo help [Command]")
    public EmbedBuilder help(User author, TextChannel channel, String[] args) {
        EmbedBuilder builder = defaultEmbed(author);

        if (args.length == 0) Mo.CMD.getCommands()
                .forEach(cmd -> {
                    Command annot = cmd.getCommandAnnotation();
                    builder.addField(
                            "__" + annot.aliases()[0] + "__ - Usage: " + annot.usage(),
                            annot.description()
                    );
                });
        else Mo.CMD.getCommands()
                .stream()
                .map(CommandHandler.SimpleCommand::getCommandAnnotation)
                .filter(cmd -> {
                    for (var alias : cmd.aliases()) if (alias.equalsIgnoreCase(args[0])) return true;
                    return false;
                })
                .findAny()
                .ifPresentOrElse(
                        cmd -> builder.addField("__" + cmd.aliases()[0] + "__ - Usage: " + cmd.usage(), cmd.description()),
                        () -> builder.addField("Error!", "Unknown command: " + args[0])
                );

        return builder;
    }
}
