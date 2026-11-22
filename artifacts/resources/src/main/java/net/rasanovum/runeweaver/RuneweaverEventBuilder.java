package net.rasanovum.runeweaver;

import net.minecraft.resources.Identifier;
import net.rasanovum.runeweaver.enums.ErrorPolicy;
import net.rasanovum.runeweaver.enums.Lifetime;
import net.rasanovum.runeweaver.util.Index;
import net.rasanovum.runeweaver.util.VersionUtils;
import net.rasanovum.runeweaver.util.functions.Event;
import net.rasanovum.runeweaver.util.interfaces.RuneweaverCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

public class RuneweaverEventBuilder<T> {

    private RuneweaverCodec<T> codec;
    private Predicate<Index> resourcePredicate;
    private Predicate<Identifier> filePredicate;
    private String eventName;
    private Event<T> event;
    private boolean assertive = false;
    private Lifetime lifetime;
    private ErrorPolicy errorPolicy;
    private int priority = Runeweaver.DEFAULT_PRIORITY;

    public RuneweaverEventBuilder<T> setErrorPolicy(ErrorPolicy errorPolicy) {
        this.errorPolicy = errorPolicy;
        return this;
    }

    public RuneweaverEventBuilder<T> setCodec(RuneweaverCodec<T> codec) {
        this.codec = codec;
        return this;
    }

    public RuneweaverEventBuilder<T> setResourcePredicate(Predicate<Index> resourcePredicate) {
        this.resourcePredicate = resourcePredicate;
        this.filePredicate = null;
        return this;
    }

    /** Matches all ordinals of a resource ID supplied without the codec extension. */
    public RuneweaverEventBuilder<T> setResourceId(Identifier id) {
        return setResourcePredicate(new ExactResource(Objects.requireNonNull(id, "id")));
    }

    /** Matches a full filename, including its extension. Use a side-effect-free predicate. */
    public RuneweaverEventBuilder<T> setResourceFilePredicate(Predicate<Identifier> predicate) {
        this.filePredicate = Objects.requireNonNull(predicate, "predicate");
        this.resourcePredicate = null;
        return this;
    }

    record ResourceFilePredicate(String extension, Predicate<Identifier> matcher) implements Predicate<Index> {
        boolean matches(Identifier id) {
            return VersionUtils.path(id).endsWith(extension) && matcher.test(id);
        }

        @Override
        public boolean test(Index index) {
            return matcher.test(VersionUtils.withSuffix(index.id(), extension));
        }
    }

    record ExactResource(Identifier id) implements Predicate<Index> {
        @Override
        public boolean test(Index index) {
            return id.equals(index.id());
        }
    }

    public RuneweaverEventBuilder<T> setEventName(String eventName) {
        this.eventName = eventName;
        return this;
    }

    public RuneweaverEventBuilder<T> setEvent(Event<T> event) {
        this.event = event;
        return this;
    }

    public RuneweaverEventBuilder<T> setAssertive(boolean assertive) {
        this.assertive = assertive;
        return this;
    }

    public RuneweaverEventBuilder<T> setLifetime(Lifetime lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    public RuneweaverEventBuilder<T> setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    protected @NotNull RuneweaverEvent<T> build() {
        Objects.requireNonNull(codec, "codec must be set");
        Predicate<Index> predicate = filePredicate == null
                ? Objects.requireNonNull(resourcePredicate, "resource predicate must be set")
                : new ResourceFilePredicate(codec.extensionAndDot(), filePredicate);
        Objects.requireNonNull(event, "event must be set");
        Objects.requireNonNull(eventName, "event name must be set");
        Objects.requireNonNull(lifetime, "lifetime must be set");
        Objects.requireNonNull(errorPolicy, "error policy must be set");

        RuneweaverEvent<T> builtEvent = new RuneweaverEvent<>(codec, priority, lifetime, errorPolicy, eventName, predicate, event, assertive);
        Runeweaver.logBasic(builtEvent.getRegistrationMessage(priority));
        return builtEvent;
    }

    public RuneweaverEventBuilder<T> copy() {
        RuneweaverEventBuilder<T> copy = new RuneweaverEventBuilder<T>()
                .setCodec(codec)
                .setResourcePredicate(resourcePredicate)
                .setEventName(String.valueOf(eventName))
                .setEvent(event)
                .setAssertive(assertive)
                .setErrorPolicy(errorPolicy)
                .setPriority(priority)
                .setLifetime(lifetime);
        return filePredicate == null ? copy : copy.setResourceFilePredicate(filePredicate);
    }
}
