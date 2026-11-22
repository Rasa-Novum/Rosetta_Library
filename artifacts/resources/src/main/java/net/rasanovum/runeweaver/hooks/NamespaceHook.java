package net.rasanovum.runeweaver.hooks;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.rasanovum.runeweaver.util.Index;
import net.rasanovum.runeweaver.util.VersionUtils;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@ApiStatus.Internal
public class NamespaceHook extends AbstractHook<List<Resource>> {

    private final Identifier resourceId;

    public NamespaceHook(List<Resource> attachedResources, Identifier resourceId) {
        super(attachedResources);
        this.resourceId = resourceId;
    }

    public Identifier resourceId() {
        return resourceId;
    }

    @Override
    public Optional<List<Resource>> captureFiles(Index index, String fileExt) {
        if (!VersionUtils.withSuffix(index.id(), fileExt).equals(resourceId)) return Optional.empty();
        return captureOrdinal(attachedResources, index.ordinal());
    }

    @Override
    public List<Map.Entry<Index, Resource>> getMatching(Predicate<Index> predicate) {
        List<Map.Entry<Index, Resource>> matches = new ArrayList<>();
        addMatches(matches, resourceId, attachedResources, predicate);
        return matches;
    }

    @Override
    public void insert(Index index, List<Resource> resources, String fileExt, boolean overwrite) {
        requireResource(index, fileExt);
        insertOrdinal(attachedResources, index, resources, overwrite);
    }

    @Override
    public void delete(Index index, String fileExt) {
        requireResource(index, fileExt);
        deleteOrdinal(attachedResources, index.ordinal());
    }

    private void requireResource(Index index, String fileExt) {
        if (!VersionUtils.withSuffix(index.id(), fileExt).equals(resourceId)) {
            throw new IllegalArgumentException("Namespace hook for " + resourceId + " cannot process " + index);
        }
    }
}
