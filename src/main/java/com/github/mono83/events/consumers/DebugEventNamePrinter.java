package com.github.mono83.events.consumers;

import com.github.mono83.events.Event;
import com.github.mono83.events.EventsConsumer;

import java.io.PrintStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Events consumer, that prints events into given print stream.
 * Can be useful for some debugs.
 * Not for production use.
 */
public class DebugEventNamePrinter implements EventsConsumer {
    private final PrintStream out;
    private final boolean showEventClassName;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.of("UTC"));

    /**
     * Constructor
     *
     * @param stream             Print stream to write data into.
     * @param showEventClassName If true, will also output event class name.
     */
    public DebugEventNamePrinter(final PrintStream stream, final boolean showEventClassName) {
        this.out = Objects.requireNonNull(stream, "stream");
        this.showEventClassName = showEventClassName;
    }

    /**
     * Constructor.
     * Will build debug event printer that will output to STDERR with event class names
     */
    public DebugEventNamePrinter() {
        this(System.err, true);
    }

    @Override
    public void consume(final Event... events) {
        if (events != null && events.length > 0) {
            for (Event event : events) {
                if (showEventClassName) {
                    out.printf(
                            "%s - %s - %s\n",
                            formatter.format(Instant.now()),
                            event.getClass().getName(),
                            event.toString()
                    );
                } else {
                    out.printf(
                            "%s - %s\n",
                            formatter.format(Instant.now()),
                            event.toString()
                    );
                }
            }
        }
    }
}
