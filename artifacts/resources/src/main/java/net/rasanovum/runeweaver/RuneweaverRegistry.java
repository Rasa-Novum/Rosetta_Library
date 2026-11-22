package net.rasanovum.runeweaver;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class RuneweaverRegistry<T> {
    
    private final Map<UUID, T> valuesById = new HashMap<>();
    private final Map<UUID, T> deferredValuesById = new HashMap<>();
    private final SortedMap<Integer, List<T>> valuesByPriority = new TreeMap<>();
    private final Function<T, UUID> idFunction;
    private final Function<T, Integer> priorityFunction;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    public RuneweaverRegistry(Function<T, UUID> idFunction, Function<T, Integer> priorityFunction) {
        this.idFunction = idFunction;
        this.priorityFunction = priorityFunction;
    }
    
    public UUID register(T value) {
        UUID uuid = idFunction.apply(value);
        int priority = priorityFunction.apply(value);
        lock.writeLock().lock();
        try {
            T previous = valuesById.get(uuid);
            if (previous != null) removeFromPriority(previous);
            valuesById.put(uuid, value);
            valuesByPriority.computeIfAbsent(priority, ignored -> new ArrayList<>()).add(value);
            return uuid;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public UUID registerDeferred(T value) {
        UUID uuid = idFunction.apply(value);
        lock.writeLock().lock();
        try {
            deferredValuesById.put(uuid, value);
            return uuid;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<T> pullDeferred(UUID uuid) {
        lock.writeLock().lock();
        try {
            return Optional.ofNullable(deferredValuesById.remove(uuid));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<T> get(UUID uuid) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(valuesById.get(uuid));
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean contains(UUID uuid) {
        lock.readLock().lock();
        try {
            return valuesById.containsKey(uuid);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean unregister(UUID uuid) {
        lock.writeLock().lock();
        try {
            T value = valuesById.get(uuid);
            if (value == null) return false;
            removeFromPriority(value);
            valuesById.remove(uuid);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void removeFromPriority(T value) {
        int priority = priorityFunction.apply(value);
        List<T> values = valuesByPriority.get(priority);
        if (values == null || !values.remove(value)) {
            throw new IllegalStateException("Found component by UUID, but not present by priority");
        }
        if (values.isEmpty()) valuesByPriority.remove(priority);
    }

    public SortedMap<Integer, List<T>> captureSnapshot() {
        lock.readLock().lock();
        try {
            SortedMap<Integer, List<T>> snapshot = new TreeMap<>();
            valuesByPriority.forEach((priority, values) -> snapshot.put(priority, List.copyOf(values)));
            return snapshot;
        } finally {
            lock.readLock().unlock();
        }
    }
}
