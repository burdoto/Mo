package org.comroid.botutil.ui.input.model;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.comroid.botutil.ui.output.model.TargetedView;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.user.User;

public interface UserInput<T, V extends TargetedView<Messageable, CompletableFuture<Message>>> {
    Class<? extends T> responseType();

    V targetedView();

    TextChannel channel();

    Predicate<User> getUserFilter();

    Predicate<TextChannel> getChannelFilter();

    Predicate<T> getResultFilter();

    Duration getTimeout();

    boolean getDeleteOnResponse();

    default T readBlocking() {
        return read().join();
    }

    CompletableFuture<T> read();
}
