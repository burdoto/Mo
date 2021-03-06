package org.comroid.botutil.ui.output.model;

import java.util.concurrent.CompletableFuture;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;

public interface MessageBasedView extends TargetedView<Messageable, CompletableFuture<Message>> {
    @Override
    CompletableFuture<Message> view(Messageable in);
}
