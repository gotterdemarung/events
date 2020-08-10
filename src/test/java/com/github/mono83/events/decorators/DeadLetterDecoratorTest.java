package com.github.mono83.events.decorators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DeadLetterDecoratorTest {
    @Test
    public void testStaticConstructor() {
        Object event = new Object();

        DeadLetterDecorator<Object> d1 = DeadLetterDecorator.of(event);
        DeadLetterDecorator<Object> d2 = DeadLetterDecorator.of(d1);

        Assertions.assertSame(event, d1.getEvent());
        Assertions.assertSame(event, d2.getEvent());
    }
}