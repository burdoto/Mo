package org.comroid.sbadger;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.comroid.botutil.files.FileProvider;
import org.comroid.sbadger.engine.ReactionCore;
import org.comroid.sbadger.engine.ResponseCore;
import de.comroid.eval.EvalCommand;
import de.comroid.javacord.util.commands.CommandHandler;
import de.comroid.javacord.util.server.properties.PropertyGroup;
import de.comroid.javacord.util.server.properties.ServerPropertiesManager;
import de.comroid.javacord.util.ui.embed.DefaultEmbedFactory;
import de.kaleidox.botstats.BotListSettings;
import de.kaleidox.botstats.javacord.JavacordStatsClient;
import de.kaleidox.botstats.model.StatsClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.util.logging.ExceptionLogger;

public final class Bot {
    public final static Logger logger = LogManager.getLogger();
    public final static Color THEME = new Color(0x0f7eb1);

    public static final long BOT_ID = 493055125766537236L;

    public static final DiscordApi API;
    public static final StatsClient STATS;
    public static final CommandHandler CMD;
    public static final ServerPropertiesManager PROP;
    public static final Server SRV;

    public static final List<Long> permitted = new ArrayList<>();

    static {
        try {
            try {
                API = new DiscordApiBuilder()
                        .setToken(FileProvider.readContent("login/discord.cred")[0])
                        .login()
                        .get();
            } catch (Throwable t) {
                throw new RuntimeException("Failed to login to discord servers", t);
            }

            logger.info("Successfully connected to Discord services");

            permitted.add(141476933849448448L); // Kaleidox
            permitted.add(292141393739251714L); // Jay
            permitted.add(232610922298998784L); // Flo

            API.updateStatus(UserStatus.DO_NOT_DISTURB);
            API.updateActivity("Booting up...");

            logger.info("Initializting StatsClient...");
            STATS = new JavacordStatsClient(BotListSettings.builder()
                    .tokenFile(FileProvider.getFile("login/botlists.properties"))
                    .postStatsTester(() -> API.getYourself().getId() == BOT_ID)
                    .build(), API);

            DefaultEmbedFactory.setEmbedSupplier(() -> new EmbedBuilder().setColor(THEME));

            logger.info("Initializing command handlers");
            CMD = new CommandHandler(API);
            CMD.prefixes = new String[]{"badge!", "mo!"};
            logger.info(String.format("Setting command prefixes: '%s'", String.join("', '", CMD.prefixes)));
            CMD.useDefaultHelp(null);
            CMD.registerCommands(ReactionCore.COMMANDS, ResponseCore.COMMANDS);
            CMD.registerCommands(EvalCommand.INSTANCE);

            logger.info("Initialzing server properties");
            PROP = new ServerPropertiesManager(FileProvider.getFile("servers.json"));
            PROP.usePropertyCommand(null, CMD);
            Prop.init();

            logger.info("Registering prefix provider");
            CMD.withCustomPrefixProvider(Prop.PREFIX);

            logger.info("Registering runtime hooks");
            API.getThreadPool()
                    .getScheduler()
                    .scheduleAtFixedRate(Bot::storeAllData, 5, 5, TimeUnit.MINUTES);
            Runtime.getRuntime().addShutdownHook(new Thread(Bot::terminateAll));

            SRV = API.getServerById(625494140427173889L).orElseThrow(IllegalStateException::new);

            logger.info("Initializing Automation Core");

            API.updateActivity(ActivityType.LISTENING, CMD.prefixes[0] + "help");
            API.updateStatus(UserStatus.ONLINE);
            logger.info("Bot ready and listening");
        } catch (Exception e) {
            ExceptionLogger.get().apply(e);
            System.exit(1);
            throw new AssertionError();
        }
    }

    public static void main(String[] args) {
        API.getServerTextChannelById(644220645814566912L)
                .ifPresent(itcrowd -> itcrowd.sendMessage(DefaultEmbedFactory.create()
                        .setDescription("Bot restarted!")).exceptionally(ExceptionLogger.get()));
    }

    private static void terminateAll() {
        logger.info("Trying to shutdown gracefully");
        try {
            PROP.close();
            API.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void storeAllData() {
        logger.info("Trying to save bot properties");
        try {
            PROP.storeData();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final class Prop {
        public static PropertyGroup PREFIX;

        private static void init() {
            PREFIX = PROP.register("bot.customprefix", "t!");
        }
    }
}
