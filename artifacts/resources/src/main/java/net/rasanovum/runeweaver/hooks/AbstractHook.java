package net.rasanovum.runeweaver.hooks;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.rasanovum.runeweaver.util.Index;
import net.rasanovum.runeweaver.util.RuneweaverUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class AbstractHook<T> {

    protected final T attachedResources;

    public AbstractHook(T attachedResources) {
        this.attachedResources = attachedResources;
    }

    public abstract Optional<List<Resource>> captureFiles(Index index, String fileExt);

    public abstract List<Map.Entry<Index, Resource>> getMatching(Predicate<Index> predicate);

    public abstract void insert(Index index, List<Resource> resources, String fileExt, boolean overwrite);

    public void insert(Index index, Resource resource, String fileExt, boolean overwrite) {
        insert(index, List.of(resource), fileExt, overwrite);
    }

    public abstract void delete(Index index, String fileExt);

    static boolean hasOrdinal(int ordinal, int size) {
        return ordinal >= 0 && ordinal < size;
    }

    static Optional<List<Resource>> captureOrdinal(List<Resource> resources, int ordinal) {
        if (ordinal == -1) return Optional.of(resources);
        return hasOrdinal(ordinal, resources.size())
                ? Optional.of(List.of(resources.get(ordinal))) : Optional.empty();
    }

    static void addMatches(List<Map.Entry<Index, Resource>> matches, Identifier id, List<Resource> resources,
                           Predicate<Index> predicate) {
        Identifier trimmed = null;
        for (int ordinal = 0; ordinal < resources.size(); ordinal++) {
            if (!predicate.test(new Index(id, ordinal))) continue;
            if (trimmed == null) trimmed = RuneweaverUtil.removeExtension(id);
            matches.add(Map.entry(new Index(trimmed, ordinal), resources.get(ordinal)));
        }
    }

    static void insertOrdinal(List<Resource> target, Index index, List<Resource> resources, boolean overwrite) {
        int ordinal = index.ordinal();
        if (ordinal == -1) {
            if (!overwrite) throw new IllegalStateException("Missing permission to replace " + index);
            if (target == resources) return;
            target.clear();
            target.addAll(resources);
            return;
        }
        if (resources.size() != 1) throw new IllegalArgumentException("Expected one resource for " + index);
        if (!hasOrdinal(ordinal, target.size() + (overwrite ? 0 : 1))) {
            throw new IllegalStateException("Invalid insertion ordinal for " + index);
        }
        if (overwrite) target.set(ordinal, resources.get(0));
        else target.add(ordinal, resources.get(0));
    }

    static void deleteOrdinal(List<Resource> target, int ordinal) {
        if (ordinal == -1) target.clear();
        else if (hasOrdinal(ordinal, target.size())) target.remove(ordinal);
    }

    public T getAttachedResources() {
        return attachedResources;
    }
}
