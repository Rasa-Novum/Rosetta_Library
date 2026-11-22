package net.rasanovum.runeweaver;


import com.google.gson.JsonElement;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.rasanovum.runeweaver.entries.AbstractEntry;
import net.rasanovum.runeweaver.entries.EventEntry;
import net.rasanovum.runeweaver.entries.ReferenceEntry;
import net.rasanovum.runeweaver.enums.DebugOption;
import net.rasanovum.runeweaver.enums.ErrorPolicy;
import net.rasanovum.runeweaver.enums.Lifetime;
import net.rasanovum.runeweaver.hooks.AbstractHook;
import net.rasanovum.runeweaver.hooks.NamespaceHook;
import net.rasanovum.runeweaver.util.Index;
import net.rasanovum.runeweaver.util.PlatformUtils;
import net.rasanovum.runeweaver.util.VersionUtils;
import net.rasanovum.runeweaver.util.functions.Event;
import net.rasanovum.runeweaver.util.interfaces.ErrorMessageProvider;
import net.rasanovum.runeweaver.util.interfaces.RuneweaverCodec;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

import static net.rasanovum.runeweaver.util.RuneweaverUtil.*;

@SuppressWarnings("unused")
public final class Runeweaver {

    private static final Logger LOGGER = LoggerFactory.getLogger("Runeweaver");
    private static volatile int debugOptionFlags;
    private static boolean debugDirectoryPrepared;
    private static final RuneweaverRegistry<RuneweaverEvent<?>> eventRegistry = new RuneweaverRegistry<>(RuneweaverEvent::uuid, RuneweaverEvent::priority);
    private static final RuneweaverRegistry<ResourceReference<?>> referenceRegistry = new RuneweaverRegistry<>(ResourceReference::getUuid, ResourceReference::getPriority);

    private static final ReadWriteLock lock = new ReentrantReadWriteLock();
    private static final Object REGISTRATION_LOCK = new Object();
    private static volatile Dispatch dispatch = new Dispatch(List.of(), Set.of(), new String[0], new RuneweaverEventBuilder.ResourceFilePredicate[0], false);

    private record Dispatch(List<AbstractEntry> entries, Set<Identifier> exactIds,
                            String[] predicateExtensions, RuneweaverEventBuilder.ResourceFilePredicate[] filePredicates,
                            boolean assertive) {
        boolean mayMatch(Identifier id) {
            if (assertive || exactIds.contains(id)) return true;
            String path = VersionUtils.path(id);
            for (String extension : predicateExtensions) {
                if (path.endsWith(extension)) return true;
            }
            for (var predicate : filePredicates) {
                if (predicate.matches(id)) return true;
            }
            return false;
        }
    }

    private static void rebuildDispatch() {
        List<AbstractEntry> entries = RuneweaverRuntime.merge(eventRegistry, referenceRegistry);
        Set<Identifier> exactIds = new HashSet<>();
        Set<String> predicateExtensions = new HashSet<>();
        List<RuneweaverEventBuilder.ResourceFilePredicate> filePredicates = new ArrayList<>();
        boolean assertive = false;
        for (AbstractEntry entry : entries) {
            if (entry instanceof ReferenceEntry<?> referenceEntry) {
                var reference = referenceEntry.reference();
                exactIds.add(VersionUtils.withSuffix(reference.getIndex().id(), reference.getCodec().extensionAndDot()));
            } else if (entry instanceof EventEntry<?> eventEntry) {
                var event = eventEntry.event();
                assertive |= event.assertive();
                String extension = event.codec().extensionAndDot();
                if (event.resourcePredicate() instanceof RuneweaverEventBuilder.ExactResource exact) {
                    exactIds.add(VersionUtils.withSuffix(exact.id(), extension));
                } else if (event.resourcePredicate() instanceof RuneweaverEventBuilder.ResourceFilePredicate file) {
                    filePredicates.add(file);
                } else {
                    predicateExtensions.add(extension);
                }
            }
        }
        dispatch = new Dispatch(entries, Set.copyOf(exactIds), predicateExtensions.toArray(String[]::new),
                filePredicates.toArray(RuneweaverEventBuilder.ResourceFilePredicate[]::new), assertive);
    }

    public static final int DEFAULT_PRIORITY = 1000;

    private Runeweaver() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    // EVENT REGISTRATION METHODS

    public static UUID registerEvent(int priority, Lifetime lifetime, ErrorPolicy errorPolicy, String eventName, Predicate<Index> resourcePredicate, Event<JsonElement> event) {
        return registerEvent(RuneweaverCodecs.JSON_ELEMENT, priority, lifetime, errorPolicy, eventName, resourcePredicate, event);
    }

    public static <T> UUID registerEvent(RuneweaverCodec<T> codec, int priority, Lifetime lifetime, ErrorPolicy errorPolicy, String eventName, Predicate<Index> resourcePredicate, Event<T> event) {
        return registerEvent(new RuneweaverEventBuilder<T>()
                .setCodec(codec)
                .setPriority(priority)
                .setLifetime(lifetime)
                .setErrorPolicy(errorPolicy)
                .setEventName(eventName)
                .setResourcePredicate(resourcePredicate)
                .setEvent(event)
        );
    }

    public static <T> UUID registerEvent(RuneweaverEventBuilder<T> builder) {
        RuneweaverEvent<T> builtEvent = builder.build();
        if (builtEvent.lifetime() == Lifetime.DEFERRED) {
            return eventRegistry.registerDeferred(builtEvent);
        }
        synchronized (REGISTRATION_LOCK) {
            UUID uuid = eventRegistry.register(builtEvent);
            rebuildDispatch();
            return uuid;
        }
    }

    public static ResourceReference<JsonElement> registerReference(int priority, Index index, String referenceName) {
        return registerReference(RuneweaverCodecs.JSON_ELEMENT, priority, index, referenceName);
    }

    public static <T> ResourceReference<T> registerReference(RuneweaverCodec<T> codec, int priority, Index index, String referenceName) {
        return registerReference(new ResourceReferenceBuilder<T>()
                .setCodec(codec)
                .setIndex(index)
                .setReferenceName(referenceName)
                .setPriority(priority)
        );
    }

    public static <T> ResourceReference<T> registerReference(ResourceReferenceBuilder<T> builder) {
        ResourceReference<T> ref = builder.build();
        synchronized (REGISTRATION_LOCK) {
            referenceRegistry.register(ref);
            rebuildDispatch();
            return ref;
        }
    }

    // EXTERNAL RUN METHODS

    public static boolean hasHandlers() {
        return !dispatch.entries().isEmpty();
    }

    public static boolean hasHandlers(Identifier id) {
        return dispatch.mayMatch(id);
    }

    public static <T> T processHook(AbstractHook<T> hook) {
        Dispatch current = dispatch;
        if (current.entries().isEmpty() || (hook instanceof NamespaceHook namespace
                && !current.mayMatch(namespace.resourceId()))) {
            return hook.getAttachedResources();
        }
        lock.readLock().lock();
        try {
            current = dispatch;
            if (current.entries().isEmpty() || (hook instanceof NamespaceHook namespace
                    && !current.mayMatch(namespace.resourceId()))) {
                return hook.getAttachedResources();
            }
            RuneweaverRuntime<T> runtime = new RuneweaverRuntime<>(hook, current.entries(), LOGGER::error);
            while (runtime.isRunning()) {
                AbstractEntry entry = runtime.pop();
                if (entry instanceof ReferenceEntry<?> referenceEntry) {
                    handleReference(referenceEntry, runtime);
                } else if (entry instanceof EventEntry<?> eventEntry) {
                    handleEvent(eventEntry, runtime);
                } else {
                    throw new IllegalStateException("Unexpected value: " + entry);
                }
            }
            return hook.getAttachedResources();
        } finally {
            lock.readLock().unlock();
        }
    }

    private static <T, R> void handleReference(ReferenceEntry<R> referenceEntry, RuneweaverRuntime<T> runtime) {
        ResourceReference<R> ref = referenceEntry.reference();
        try {
            processReference(ref, runtime);
        } catch (IOException e) {
            runtime.error(e, ref, ref.getResourceId());
        }
    }

    private static <T, R> void processReference(ResourceReference<R> ref, RuneweaverRuntime<T> runtime) throws IOException {
        Optional<List<Resource>> maybeResource = runtime.getHook().captureFiles(ref.getIndex(), ref.getCodec().extensionAndDot());
        if(maybeResource.isEmpty()) return;
        List<Resource> resource = maybeResource.get();
        if(resource.isEmpty()) return;
        if(resource.size() > 1) {
            runtime.error(new RuneweaverException("resource reference cannot match more than 1 resource"), ref, ref.getIndex().id());
            return;
        }
        R file = ref.getCodec().deserialize(resource.get(0));
        ref.fulfill(file);
    }


    private static <T, R> void handleEvent(EventEntry<T> eventEntry, RuneweaverRuntime<R> runtime) {
        RuneweaverEvent<T> event = eventEntry.event();
        List<Map.Entry<Index, Resource>> entries = runtime.getHook().getMatching(event.getWrappedPredicate());
        if(entries.isEmpty() && eventEntry.event().assertive()) throw new RuneweaverException("assertion on event '%s' failed", event.eventName());
        if(entries.isEmpty()) return;
        if (event.lifetime() == Lifetime.ONCE && !removeEvent(event.uuid())) return;
        entries.sort(Comparator
                .comparing((Map.Entry<Index, Resource> o) -> o.getKey().id())
                .thenComparingInt(o -> o.getKey().ordinal())
        );
        Set<Index> markedForDeletion = new HashSet<>();
        logExtra("begun processing event '{}'", event.eventName());
        long fileStartTime = System.nanoTime();
        for(Map.Entry<Index, Resource> resourceEntry : entries) {
            try {
                processEvent(eventEntry, runtime, resourceEntry, markedForDeletion);
            } catch (IOException e) {
                runtime.error(e, event, resourceEntry.getKey().id());
            }
        }
        String ext = eventEntry.event().codec().extensionAndDot();
        for(Index deletionIndex : markedForDeletion.stream().sorted(Comparator.reverseOrder()).toList())
            runtime.getHook().delete(deletionIndex, ext);
        logExtra("successfully finished processing event '{}' in {}", event.eventName(), timestamp(fileStartTime));
    }

    private static <T, R> void processEvent(EventEntry<T> eventEntry, RuneweaverRuntime<R> runtime, Map.Entry<Index, Resource> resourceEntry, Set<Index> markedForDeletion) throws IOException {
        RuneweaverEvent<T> event = eventEntry.event();
        AbstractHook<R> hook = runtime.getHook();
        T file = deserializeFile(event.codec(), resourceEntry.getValue(), error -> runtime.error(error, event, resourceEntry.getKey().id())).orElse(null);
        EventContext<T> context = new EventContext<>(file, resourceEntry.getKey(), eventEntry, runtime, resourceEntry, markedForDeletion.contains(resourceEntry.getKey()));
        if(getDebugFlag(DebugOption.EXPORT_UNPATCHED_FILE))
            exportDebugFile(event.codec(), file, event.eventName(), resourceEntry.getKey().id().toString(), event.codec().extensionAndDot(), false);
        logBasic("Running '{}' on resource '{}'", event.eventName(), resourceEntry.getKey().id());
        long fileStartTime = System.nanoTime();
        event.event().runEvent(context);
        logExtra("Finished running '{}' on resource '{}' in {}", event.eventName(), resourceEntry.getKey().id(), timestamp(fileStartTime));
        Optional<T> debugExport = context.getDebugExport();
        if(debugExport.isPresent() && getDebugFlag(DebugOption.EXPORT_PATCHED_FILE))
            exportDebugFile(event.codec(), debugExport.get(), event.eventName(), resourceEntry.getKey().id().toString(), event.codec().extensionAndDot(), true);
        if(context.isMarkedForDeletion()) markedForDeletion.add(resourceEntry.getKey());
        else markedForDeletion.remove(resourceEntry.getKey());
        for(UUID cancelledFuture : context.getCancelledFutures())
            runtime.cancelEvent(cancelledFuture);
        context.cleanupCapturedFiles();
        for(UUID uuid : context.getPulledFutures()) {
            Optional<RuneweaverEvent<?>> pulledDeferred = eventRegistry.pullDeferred(uuid);
            if(pulledDeferred.isPresent()) {
                runtime.insertEntry(new EventEntry<>(pulledDeferred.get().priority(), pulledDeferred.get()));
                continue;
            }
            Optional<RuneweaverEvent<?>> pulledEvent = eventRegistry.get(uuid);
            if(pulledEvent.isPresent()) {
                runtime.insertEntry(new EventEntry<>(pulledEvent.get().priority(), pulledEvent.get()));
                continue;
            }
            Optional<ResourceReference<?>> pulledRef = referenceRegistry.get(uuid);
            if(pulledRef.isPresent()) {
                runtime.insertEntry(new ReferenceEntry<>(pulledRef.get().getPriority(), pulledRef.get()));
                continue;
            }
            throw new IllegalArgumentException("failed to locate event or reference with uuid of "+uuid);
        }

        hook.insert(resourceEntry.getKey(), event.codec().serialize(resourceEntry.getValue(), context.getFile()), event.codec().extensionAndDot(), true);
        for(Map.Entry<Index, T> createdResource : context.getCreatedResources().entrySet())
            hook.insert(createdResource.getKey(), event.codec().serialize(resourceEntry.getValue(), createdResource.getValue()), event.codec().extensionAndDot(), false);
    }

    // ERRORS

    static void registrationError(Exception e, ErrorMessageProvider errorMessageProvider) {
        if(errorMessageProvider.getErrorPolicy() != ErrorPolicy.THROW) LOGGER.error(errorMessageProvider.getRegistrationErrorMessage(), e);
        else throw new RuneweaverException(errorMessageProvider.getRegistrationErrorMessage(), e);
    }

    // MISC. PUBLICS

    /** @Deprecated Use {@link #removeEvent(UUID)} or {@link #removeReference(UUID)} instead**/
    @Deprecated
    public static boolean remove(UUID uuid) {
        return removeEvent(uuid) || removeReference(uuid);
    }

    public static boolean removeEvent(UUID uuid) {
        synchronized (REGISTRATION_LOCK) {
            if (!eventRegistry.unregister(uuid)) return false;
            rebuildDispatch();
            return true;
        }
    }

    public static boolean removeReference(UUID uuid) {
        synchronized (REGISTRATION_LOCK) {
            if (!referenceRegistry.unregister(uuid)) return false;
            rebuildDispatch();
            return true;
        }
    }

    /** @Deprecated Use {@link #hasEvent(UUID)} or {@link #hasReference(UUID)} instead**/
    @Deprecated
    public static boolean has(UUID uuid) {
        return hasEvent(uuid) || hasReference(uuid);
    }

    public static boolean hasEvent(UUID uuid) {
        return eventRegistry.contains(uuid);
    }

    public static boolean hasReference(UUID uuid) {
        return referenceRegistry.contains(uuid);
    }

    public static String getEventName(UUID uuid) {
        RuneweaverEvent<?> event = eventRegistry.get(uuid).orElseThrow();
        return event.eventName();
    }

    public static Runnable lockEventProcessing() {
        lock.writeLock().lock();
        return () -> lock.writeLock().unlock();
    }

    // DEBUGGING STUFF

    public static synchronized void enableDebugOption(DebugOption option) {
        Runeweaver.debugOptionFlags |= option.getMask();
    }

    private static boolean getDebugFlag(DebugOption option) {
        return (Runeweaver.debugOptionFlags & option.getMask()) > 0;
    }

    static void logBasic(String action, Object... args) {
        if(getDebugFlag(DebugOption.BASIC_LOGGING)) LOGGER.info(action, args);
    }

    private static void logExtra(String action, Object... args) {
        if(getDebugFlag(DebugOption.EXTRA_LOGGING)) LOGGER.info(action, args);
    }

    private static <T> void exportDebugFile(RuneweaverCodec<T> codec, T resource, String eventName, String resourceId, String extension, boolean patched) {
        Path rawDir = prepareDebugDirectory();
        Path patchDir;
        if(patched) patchDir = rawDir.resolve("patched");
        else patchDir = rawDir.resolve("unpatched");
        Path dir = patchDir.resolve(identifierToPathString(resourceId, extension));
        try {
            Files.createDirectories(dir);
            try (FileOutputStream output = new FileOutputStream(dir.resolve(stringToUsablePath(eventName) + extension).toFile())) {
                codec.export(resource).writeTo(output);
            }
        } catch (IOException e) {
            LOGGER.error("failed to export debug file", e);
        }
    }

    private static synchronized Path prepareDebugDirectory() {
        Path path = PlatformUtils.getGameDir().resolve(".runeweaver");
        if (!debugDirectoryPrepared) {
            try {
                FileUtils.deleteDirectory(path.toFile());
            } catch (IOException e) {
                LOGGER.error("Failed to clean Runeweaver debug directory", e);
            }
            debugDirectoryPrepared = true;
        }
        return path;
    }
}
