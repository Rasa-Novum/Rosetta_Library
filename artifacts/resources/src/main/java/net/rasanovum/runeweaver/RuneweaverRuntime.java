package net.rasanovum.runeweaver;

import net.minecraft.resources.Identifier;
import net.rasanovum.runeweaver.entries.AbstractEntry;
import net.rasanovum.runeweaver.entries.EventEntry;
import net.rasanovum.runeweaver.entries.ReferenceEntry;
import net.rasanovum.runeweaver.hooks.AbstractHook;
import net.rasanovum.runeweaver.util.interfaces.ErrorMessageProvider;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.BiConsumer;

@ApiStatus.Internal
public class RuneweaverRuntime<T> {

    private final AbstractHook<T> hook;
    private final BiConsumer<String, Exception> errorCallback;
    private final List<AbstractEntry> queuedEvents;
    private int nextEntry;

    protected RuneweaverRuntime(AbstractHook<T> hook, RuneweaverRegistry<RuneweaverEvent<?>> eventRegistry, RuneweaverRegistry<ResourceReference<?>> referenceRegistry, BiConsumer<String, Exception> logCallback) {
        this(hook, merge(eventRegistry, referenceRegistry), logCallback);
    }

    RuneweaverRuntime(AbstractHook<T> hook, List<AbstractEntry> entries, BiConsumer<String, Exception> logCallback) {
        this.hook = hook;
        this.errorCallback = logCallback;
        this.queuedEvents = new ArrayList<>(entries);
    }

    static List<AbstractEntry> merge(RuneweaverRegistry<RuneweaverEvent<?>> events,
                                     RuneweaverRegistry<ResourceReference<?>> references) {
        List<AbstractEntry> entries = new ArrayList<>();
        references.captureSnapshot().forEach((priority, values) ->
                values.forEach(reference -> entries.add(new ReferenceEntry<>(priority, reference))));
        events.captureSnapshot().forEach((priority, values) ->
                values.forEach(event -> entries.add(new EventEntry<>(priority, event))));
        entries.sort(Comparator.comparingInt(AbstractEntry::priority));
        return List.copyOf(entries);
    }

    public AbstractHook<T> getHook() {
        return hook;
    }

    protected AbstractEntry pop() {
        return queuedEvents.get(nextEntry++);
    }

    protected boolean isRunning() {
        return nextEntry < queuedEvents.size();
    }

    protected void insertEntry(AbstractEntry entry) {
        int priority = entry.priority();
        for (int i = nextEntry; i < queuedEvents.size(); i++) {
            AbstractEntry abstractEntry = queuedEvents.get(i);
            if(abstractEntry.priority() <= priority) continue;
            queuedEvents.add(i, entry);
            return;
        }
        queuedEvents.add(entry);
    }

    protected void cancelEvent(UUID uuid) {
        for (int i = nextEntry; i < queuedEvents.size(); i++) {
            AbstractEntry abstractEntry = queuedEvents.get(i);
            if(abstractEntry instanceof EventEntry<?> eventEntry) if(eventEntry.event().uuid().equals(uuid)) {
                queuedEvents.remove(i);
                return;
            }
        }
    }

    protected void error(Exception e, ErrorMessageProvider errorProvider, Identifier resourceId) {
        switch(errorProvider.getErrorPolicy()) {
            case LOG -> errorCallback.accept(errorProvider.getRuntimeErrorMessage(resourceId), e);
            case THROW -> throw new RuneweaverException(errorProvider.getRuntimeErrorMessage(resourceId), e);
        }
    }
}
