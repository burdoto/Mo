package org.comroid.botutil.ui.input;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.comroid.botutil.ui.input.model.AbstractInput;
import org.comroid.botutil.ui.output.model.MessageBasedView;
import org.comroid.util.BuilderStruct;
import org.comroid.util.model.HoldingSupplier;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import static java.util.Objects.requireNonNull;

public class TextInput extends AbstractInput<String, MessageBasedView> {
    public TextInput(
            MessageBasedView view,
            TextChannel channel,
            Predicate<User> userFilter,
            Predicate<TextChannel> channelFilter,
            Predicate<String> responseFilter,
            Duration timeout,
            boolean deleteOnResponse
    ) {
        super(String.class, view, channel, userFilter, channelFilter, responseFilter, timeout, deleteOnResponse);
    }

    public TextInput(MessageBasedView view, TextChannel channel) {
        super(String.class, view, channel);
    }

    public TextInput.Builder builder() {
        return new Builder();
    }

    @Override
    protected CompletableFuture<String> read_impl() {
        return showOutput().thenCompose(msg -> new Engine(msg).future);
    }

    private class Engine extends EngineBase implements MessageCreateListener {
        public Engine(Message botMessage) {
            super(botMessage, 1);

            manager(botMessage.getChannel().addMessageCreateListener(this));
        }

        @Override
        public void onMessageCreate(MessageCreateEvent event) {
            if (!getChannelFilter().test(event.getChannel())) return;
            if (!event.getMessageAuthor()
                    .asUser()
                    .map(getUserFilter()::test /* apply user defined filter */)
                    .orElse(true) /* ignore webhooks; negated */) return;

            future.complete(event.getMessageContent());
        }
    }

    public final static class Builder implements BuilderStruct<TextInput> {
        private TextChannel channel;
        private Supplier<MessageBasedView> view;

        @Override
        public TextInput build() {
            return new TextInput(
                    requireNonNull(getViewSupplier(), "No viewer was attached!").get(),
                    requireNonNull(getChannel(), "Channel cannot be unset!"));
        }

        public Supplier<MessageBasedView> getViewSupplier() {
            return view;
        }

        public TextChannel getChannel() {
            return channel;
        }

        public final Builder setChannel(TextChannel channel) {
            this.channel = channel;

            return this;
        }

        public final Builder setView(MessageBasedView view) {
            this.view = new HoldingSupplier<>(view);

            return this;
        }

        public final Builder setView(BuilderStruct<MessageBasedView> viewBuilder) {
            this.view = viewBuilder.supplier();

            return this;
        }
    }
}
