package org.comroid.botutil.ui.input.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import org.comroid.botutil.ui.output.model.TargetedView;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.api.util.logging.ExceptionLogger;

import static java.util.Objects.requireNonNull;

public abstract class AbstractInput<T, V extends TargetedView<Messageable, CompletableFuture<Message>>> implements UserInput<T, V> {
    private final Class<? extends T> responseType;
    private final V view;
    private final TextChannel channel;

    //region Properties
    protected Predicate<User> userFilter = usr -> !usr.isYourself();
    protected Predicate<TextChannel> channelFilter = textChannel -> channel().equals(textChannel);
    protected Predicate<T> responseFilter = any -> true;
    protected Duration timeout = Duration.ZERO;
    protected boolean deleteOnResponse = false;
    //endregion

    protected AbstractInput(
            Class<? extends T> responseType,
            V view,
            TextChannel channel,
            Predicate<User> userFilter,
            Predicate<TextChannel> channelFilter,
            Predicate<T> responseFilter,
            Duration timeout,
            boolean deleteOnResponse
    ) {
        this(responseType, view, channel);

        this.userFilter = userFilter;
        this.channelFilter = channelFilter;
        this.responseFilter = responseFilter;
        this.timeout = timeout;
        this.deleteOnResponse = deleteOnResponse;
    }

    protected AbstractInput(Class<? extends T> responseType, V view, TextChannel channel) {
        this.responseType = requireNonNull(responseType, "ResponseType cannot be null");
        this.view = requireNonNull(view, "View cannot be null");
        this.channel = requireNonNull(channel, "Channel cannot be null");
    }

    public AbstractInput<T, V> setResponseFilter(Predicate<T> responseFilter) {
        this.responseFilter = responseFilter;

        return this;
    }

    public AbstractInput<T, V> setChannelFilter(Predicate<TextChannel> channelFilter) {
        this.channelFilter = channelFilter;

        return this;
    }

    public AbstractInput<T, V> setUserFilter(Predicate<User> userFilter) {
        this.userFilter = userFilter;

        return this;
    }

    public AbstractInput<T, V> setTimeout(long time, TimeUnit unit) {
        this.timeout = Duration.of(time, unit.toChronoUnit());

        return this;
    }

    public AbstractInput<T, V> setDeleteOnResponse(boolean deleteOnResponse) {
        this.deleteOnResponse = deleteOnResponse;

        return this;
    }

    protected CompletableFuture<Message> showOutput() {
        final CompletableFuture<Message> future = targetedView().view(channel());

        if (!timeout.isZero() && !timeout.isNegative()) channel.getApi()
                .getThreadPool()
                .getScheduler()
                .schedule(() -> {
                    if (!future.isDone())
                        future.completeExceptionally(new TimeoutException("Response timed out"));
                }, timeout.toMillis(), TimeUnit.MILLISECONDS);

        return future;
    }

    @Override
    public final Class<? extends T> responseType() {
        return responseType;
    }

    @Override
    public final V targetedView() {
        return view;
    }

    @Override
    public final TextChannel channel() {
        return channel;
    }

    @Override
    public Predicate<User> getUserFilter() {
        return userFilter;
    }

    @Override
    public Predicate<TextChannel> getChannelFilter() {
        return channelFilter;
    }

    @Override
    public Predicate<T> getResultFilter() {
        return responseFilter;
    }

    @Override public Duration getTimeout() {
        return timeout;
    }

    @Override
    public boolean getDeleteOnResponse() {
        return deleteOnResponse;
    }

    @Override
    public final CompletableFuture<T> read() {
        return read_impl().thenApplyAsync(result -> {
            if (getResultFilter().test(result)) return result;
            else throw new IllegalArgumentException("ResultFilter returned false for result: " + result);
        });
    }

    protected abstract CompletableFuture<T> read_impl();

    protected abstract class EngineBase {
        public final CompletableFuture<T> future = new CompletableFuture<>();

        protected final Message botMessage;
        protected final Collection<ListenerManager<?>> managers;

        protected EngineBase(Message botMessage, int managerCapacity) {
            this.botMessage = botMessage;
            this.managers = new ArrayList<>(managerCapacity);

            future.whenCompleteAsync((val, ex) -> cleanup());
        }

        protected void cleanup() {
            managers.forEach(ListenerManager::remove);

            if (deleteOnResponse)
                botMessage.delete("Input Finished").exceptionally(ExceptionLogger.get());
        }

        protected void manager(ListenerManager<?> manager) {
            managers.add(manager);
        }
    }
}
