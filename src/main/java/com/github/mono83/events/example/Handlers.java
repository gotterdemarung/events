package com.github.mono83.events.example;

import com.github.mono83.events.spring.EventHandler;

public class Handlers {
    @EventHandler
    public void handleGreeter(final GreeterEvent event) {
        System.out.println("GREETER: " + event);
    }

    @EventHandler
    public void handleGreeterWithTime(final GreeterWithTimeEvent event) {
        System.out.println("WITH TIME: " + event);
    }
}
