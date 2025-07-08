package de.werwolf2303.tldwr;

import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

public class Events {
    private static Logger logger = PublicValues.newLogger(Events.class.getSimpleName());

    private static class PrivateEvent {
        public final String name;
        public final boolean isProtected;
        public PrivateEvent(String name, boolean isProtected) {
            this.name = name;
            this.isProtected = isProtected;
        }
        public ArrayList<Runnable> subscribers = new ArrayList<>();
        public void trigger() {
            for(Runnable runnable : new ArrayList<>(subscribers)) {
                runnable.run();
            }
        }
    }

    public static class Event {
        private final String name;
        private final boolean isProtected;
        public Event(String name, boolean isProtected, ArrayList<Runnable> subscribers) {
            this.name = name;
            this.isProtected = isProtected;
            this.subscribers = subscribers;
        }
        private final ArrayList<Runnable> subscribers;

        public ArrayList<Runnable> getSubscribers() {
            return subscribers;
        }

        public String getName() {
            return name;
        }

        public boolean isProtected() {
            return isProtected;
        }
    }
    private static final ArrayList<PrivateEvent> registeredEvents = new ArrayList<>();

    private static boolean containsEventWithName(String name) {
        for(PrivateEvent e : registeredEvents) {
            if(e.name.equals(name)) return true;
        }
        return false;
    }

    private static PrivateEvent getEventWithName(String name) {
        for(PrivateEvent e : registeredEvents) {
            if(e.name.equals(name)) return e;
        }
        return null;
    }

    private static int getArrayNumberOfEventWithName(String name) {
        for(int i = 0; i < registeredEvents.size(); i++) {
            if(registeredEvents.get(i).name.equals(name)) return i;
        }
        return -1;
    }

    private static Event convertPrivateToPublic(PrivateEvent event) {
        return new Event(event.name, event.isProtected, event.subscribers);
    }

    public static void register(String name, boolean isProtected) {
        if(containsEventWithName(name)) return;
        registeredEvents.add(new PrivateEvent(name, isProtected));
    }

    public static void unregister(String name) {
        if(Objects.requireNonNull(getEventWithName(name)).isProtected) {
            return;
        }
        int result = getArrayNumberOfEventWithName(name);
        if(result == -1) return;
        registeredEvents.remove(result);
    }

    public static void subscribe(String name, Runnable runnable) {
        logger.info("Subscribing to " + name);
        Objects.requireNonNull(getEventWithName(name)).subscribers.add(runnable);
    }

    public static void unsubscribe(String name, Runnable runnable) {
        logger.info("Unsubscribing from " + name);
       Objects.requireNonNull(getEventWithName(name)).subscribers.remove(runnable);
    }

    public static ArrayList<Event> getEventsList() {
        ArrayList<Event> events = new ArrayList<>();
        for(PrivateEvent e : registeredEvents) {
            events.add(convertPrivateToPublic(e));
        }
        return events;
    }

    public static void triggerEvent(String name) {
        logger.info("Triggering " + name);
        Objects.requireNonNull(getEventWithName(name)).trigger();
    }
}
