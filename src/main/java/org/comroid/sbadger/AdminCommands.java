package org.comroid.sbadger;

import java.io.IOException;
import java.io.InputStream;

import de.comroid.javacord.util.commands.Command;
import de.comroid.javacord.util.commands.CommandGroup;
import de.comroid.javacord.util.ui.embed.DefaultEmbedFactory;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.logging.ExceptionLogger;

@CommandGroup(name = "Administration Commands", description = "Commands for handling the Server")
public enum AdminCommands {
    INSTANCE;

    @Command(usage = "shutdown", description = "Only the owner of the bot can use this", shownInHelpCommand = false)
    public void shutdown(User user, String[] args, Message command, TextChannel channel) {
        if (Bot.permitted.contains(user.getId()))
            System.exit(1);

        command.delete("Unauthorized").exceptionally(ExceptionLogger.get());
        channel.sendMessage("User " + user.getDiscriminatedName() + " not authorized.");
    }

    @Command(shownInHelpCommand = false)
    public String say(String[] args, User executor) {
        if (!Bot.permitted.contains(executor.getId()))
            return null;

        return String.join(" ", args);
    }

    @Command(description = "Experimental", shownInHelpCommand = false)
    public EmbedBuilder ssh(String[] args, User executor) throws IOException, InterruptedException {
        if (!Bot.permitted.contains(executor.getId()))
            return null;

        final String cmd = String.join(" ", args);

        final Process exec = Runtime.getRuntime().exec(cmd);

        while (exec.isAlive()) {
            // sleep shortly
            Thread.sleep(200);
        }

        final InputStream out = exec.getInputStream();
        final InputStream err = exec.getErrorStream();
        StringBuilder str = new StringBuilder();
        StringBuilder serr = new StringBuilder();

        int r;
        while ((r = out.read()) != -1)
            str.append((char) r);
        while ((r = err.read()) != -1)
            serr.append((char) r);

        final EmbedBuilder embedBuilder = DefaultEmbedFactory.create()
                .addField(String.format("Program finished with exit code %d", exec.exitValue()), "```\n" + str.toString() + "\n```");

        if (serr.length() > 1)
            embedBuilder.addField("`stderr`:", "```\n" + serr.toString() + "\n```");

        return embedBuilder;
    }
}
