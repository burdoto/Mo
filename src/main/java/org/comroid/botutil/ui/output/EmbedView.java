package org.comroid.botutil.ui.output;

import java.util.concurrent.CompletableFuture;

import org.comroid.botutil.ui.output.model.MessageBasedView;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;

public class EmbedView implements MessageBasedView {
    @Override
    public CompletableFuture<Message> view(Messageable in) {
        return null;
    }
}
