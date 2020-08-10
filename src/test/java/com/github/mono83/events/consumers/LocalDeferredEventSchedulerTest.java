package com.github.mono83.events.consumers;

import com.github.mono83.events.decorators.DeferredEventDecorator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class LocalDeferredEventSchedulerTest {
    @Test
    public void testDeferred() throws InterruptedException {
        ArrayList<Object> target = new ArrayList<>();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);
        final LocalDeferredEventScheduler scheduler = new LocalDeferredEventScheduler(executorService, new CollectionConsumer(target));

        scheduler.accept(new Object());
        Assertions.assertEquals(0, target.size()); // Ignored by scheduler

        scheduler.accept(new DeferredEventDecorator<>("foo", Duration.ofMillis(1)));
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        Assertions.assertEquals(1, target.size());
        Assertions.assertEquals("foo", target.get(0));
    }
}