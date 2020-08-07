package com.github.mono83.events.consumers;

import com.github.mono83.events.concurrent.EventPublisher;
import com.github.mono83.events.decorators.DeferredEventDecorator;

import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class LocalDeferredEventScheduler implements Consumer<Object>, Closeable {
    private final ScheduledExecutorService executorService;
    private final Consumer<?> drain;
    private volatile boolean running = true;

    /**
     * Constructs new deferred event scheduler.
     *
     * @param executorService Scheduled executor service to use.
     * @param drain           Event consumer, that will receive deferred events.
     */
    public LocalDeferredEventScheduler(
            final ScheduledExecutorService executorService,
            final Consumer<?> drain
    ) {
        this.executorService = Objects.requireNonNull(executorService, "executorService");
        this.drain = Objects.requireNonNull(drain, "drain");
    }

    @Override
    public void accept(final Object event) {
        if (running && event instanceof DeferredEventDecorator) {
            schedule((DeferredEventDecorator<?>) event);
        }
    }

    /**
     * Schedules execution of given unit.
     *
     * @param event Event to schedule.
     */
    private void schedule(final DeferredEventDecorator<?> event) {
        Instant now = Instant.now();
        Instant until = event.getUntil();
        if (!until.isAfter(now)) {
            // Time has come
            executorService.execute(new EventPublisher(drain, event, true));
        } else {
            Duration between = Duration.between(now, until);
            executorService.schedule(
                    new EventPublisher(drain, event, true),
                    between.toNanos(),
                    TimeUnit.NANOSECONDS
            );
        }
    }

    /**
     * Stops schedules.
     * After this, scheduler will not consume any event, but scheduled tasks remains in executor.
     */
    @Override
    public void close() {
        this.running = false;
    }
}
