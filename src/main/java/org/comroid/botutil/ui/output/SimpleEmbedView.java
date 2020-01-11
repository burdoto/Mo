package org.comroid.botutil.ui.output;

import java.util.concurrent.CompletableFuture;

import org.comroid.botutil.ui.output.model.MessageBasedView;
import de.comroid.javacord.util.ui.embed.DefaultEmbedFactory;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public class SimpleEmbedView implements MessageBasedView {
    private final EmbedBuilder embedBuilder;

    public SimpleEmbedView(EmbedBuilder embedBuilder) {
        this.embedBuilder = embedBuilder;
    }

    public SimpleEmbedView(String text) {
        this.embedBuilder = DefaultEmbedFactory.create()
                .setDescription(text);
    }

    @Override
    public CompletableFuture<Message> view(Messageable in) {
        return in.sendMessage(embedBuilder);
    }
}
