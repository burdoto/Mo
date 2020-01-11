package org.comroid.botutil.ui.input;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.comroid.botutil.Find;
import org.comroid.botutil.ui.input.model.AbstractInput;
import org.comroid.botutil.ui.output.model.MessageBasedView;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.event.message.reaction.SingleReactionEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.util.logging.ExceptionLogger;

public class BooleanInput extends AbstractInput<Boolean, MessageBasedView> {
    private String yesEmoji = "✅";
    private String noEmoji = "❌";

    public BooleanInput(
            MessageBasedView view,
            TextChannel channel,
            Predicate<User> userFilter,
            Predicate<TextChannel> channelFilter,
            Predicate<Boolean> responseFilter,
            Duration timeout,
            boolean deleteOnResponse
    ) {
        super(Boolean.class, view, channel, userFilter, channelFilter, responseFilter, timeout, deleteOnResponse);
    }

    public BooleanInput(MessageBasedView view, TextChannel channel) {
        super(Boolean.class, view, channel);
    }

    public String getYesEmoji() {
        return yesEmoji;
    }

    public BooleanInput setYesEmoji(Emoji emoji) {
        return setYesEmoji(emoji.getMentionTag());
    }

    public BooleanInput setYesEmoji(String emoji) {
        this.yesEmoji = emoji;

        return this;
    }

    public String getNoEmoji() {
        return noEmoji;
    }

    public BooleanInput setNoEmoji(Emoji emoji) {
        return setNoEmoji(emoji.getMentionTag());
    }

    public BooleanInput setNoEmoji(String emoji) {
        this.noEmoji = emoji;

        return this;
    }

    @Override
    public AbstractInput<Boolean, MessageBasedView> setResponseFilter(Predicate<Boolean> responseFilter) {
        throw new UnsupportedOperationException("Response Filters are not allowed for BooleanInput");
    }

    @Override
    protected CompletableFuture<Boolean> read_impl() {
        return showOutput().thenCompose(msg -> new Engine(msg).future);
    }

    private class Engine extends EngineBase implements MessageCreateListener, ReactionAddListener, ReactionRemoveListener {
        protected Engine(Message botMessage) {
            super(botMessage, 3);

            CompletableFuture.allOf(botMessage.addReaction(yesEmoji), botMessage.addReaction(noEmoji))
                    .exceptionally(ExceptionLogger.get());

            manager(botMessage.getChannel().addMessageCreateListener(this));
            manager(botMessage.addReactionAddListener(this));
            manager(botMessage.addReactionRemoveListener(this));
        }

        @Override
        public void onMessageCreate(MessageCreateEvent event) {
            if (!getChannelFilter().test(event.getChannel())) return;
            if (!event.getMessageAuthor()
                    .asUser()
                    .map(getUserFilter()::test /* apply user defined filter */)
                    .orElse(true) /* ignore webhooks; negated */) return;

            try {
                future.complete(Find.bool(event.getMessageContent()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        @Override
        public void onReactionAdd(ReactionAddEvent event) {
            onReaction(event);
        }

        private void onReaction(SingleReactionEvent event) {
            if (event.getUser().isYourself() || !getUserFilter().test(event.getUser())) return;

            final String emoji = event.getEmoji().getMentionTag();

            if (emoji.equals(yesEmoji)) future.complete(true);
            else if (emoji.equals(noEmoji)) future.complete(false);
        }

        @Override
        public void onReactionRemove(ReactionRemoveEvent event) {
            onReaction(event);
        }

        @Override
        protected void cleanup() {
            super.cleanup();

            botMessage.removeAllReactions().exceptionally(ExceptionLogger.get());
        }
    }
}
