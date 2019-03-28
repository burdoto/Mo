package de.kaleidox;

import java.awt.Color;

import de.kaleidox.mo.Commands;

import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JavacordHandler;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.util.logging.ExceptionLogger;

public final class Mo {
    public final static Color THEME = new Color(0x34A6FF);

    public static final DiscordApi API;
    public static final CommandHandler CMD;

    static {
        API = new DiscordApiBuilder()
                .setToken(System.getenv("token"))
                .login()
                .exceptionally(ExceptionLogger.get())
                .join();

        API.updateStatus(UserStatus.DO_NOT_DISTURB);
        API.updateActivity("Booting up...");

        CMD = new JavacordHandler(API);
        CMD.registerCommand(Commands.INSTANCE);
        CMD.setDefaultPrefix("!mo ");
    }

    public static void main(String[] args) {

    }

    public static EmbedBuilder defaultEmbed(Object... args) {
        User usr = null;

        for (Object arg : args)
            if (arg instanceof User) usr = (User) arg;

        EmbedBuilder builder = new EmbedBuilder()
                .setColor(THEME);

        if (usr != null) builder.setAuthor(usr);

        return builder;
    }
}
