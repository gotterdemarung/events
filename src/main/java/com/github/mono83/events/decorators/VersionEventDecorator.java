package com.github.mono83.events.decorators;

import com.github.mono83.events.AbstractEventDecorator;
import com.github.mono83.events.Event;

/**
 * Adds versioning to events.
 */
public class VersionEventDecorator<T extends Event> extends AbstractEventDecorator<T> {
    private final int version;

    /**
     * Constructs new decorator instance.
     *
     * @param event   Event to decorate.
     * @param version Version.
     */
    protected VersionEventDecorator(final T event, final int version) {
        super(event);
        this.version = version;
    }

    /**
     * @return Version
     */
    public int getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return String.format(
                "{%s v%d}",
                getEvent().toString(),
                getVersion()
        );
    }
}
