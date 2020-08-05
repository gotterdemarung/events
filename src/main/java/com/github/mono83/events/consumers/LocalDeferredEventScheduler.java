package com.github.mono83.events.consumers;

import com.github.mono83.events.Event;
import com.github.mono83.events.EventsConsumer;
import com.github.mono83.events.decorators.DeferredEventDecorator;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class LocalDeferredEventScheduler implements EventsConsumer {
    private final ScheduledExecutorService executorService;
    private final Consumer<Event> drain;

    /**
     * Constructs new deferred event scheduler.
     *
     * @param executorService Scheduled executor service to use.
     * @param drain           Event consumer, that will receive deferred events.
     */
    public LocalDeferredEventScheduler(
            final ScheduledExecutorService executorService,
            final Consumer<Event> drain
    ) {
        this.executorService = Objects.requireNonNull(executorService, "executorService");
        this.drain = Objects.requireNonNull(drain, "drain");
    }

    @Override
    public void consume(final Event... events) {
        if (events != null && events.length > 0) {
            for (Event event : events) {
                if (event instanceof DeferredEventDecorator) {
                    schedule((DeferredEventDecorator<?>) event);
                }
            }
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
            executorService.execute(() -> drain.accept(event.getEvent()));
        } else {
            Duration between = Duration.between(now, until);
            executorService.schedule(
                    () -> drain.accept(event.getEvent()),
                    between.toNanos(),
                    TimeUnit.NANOSECONDS
            );
        }
    }
}
