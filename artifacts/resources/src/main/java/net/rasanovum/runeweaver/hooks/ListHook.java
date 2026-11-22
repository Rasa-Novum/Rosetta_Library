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
public class ListHook extends AbstractHook<Map<Identifier, List<Resource>>> {

    public ListHook(Map<Identifier, List<Resource>> attachedResources) {
        super(attachedResources);
    }

    @Override
    public Optional<List<Resource>> captureFiles(Index index, String fileExt) {
        List<Resource> list = attachedResources.get(VersionUtils.withSuffix(index.id(), fileExt));
        return list == null ? Optional.empty() : captureOrdinal(list, index.ordinal());
    }

    @Override
    public List<Map.Entry<Index, Resource>> getMatching(Predicate<Index> predicate) {
        List<Map.Entry<Index, Resource>> matches = new ArrayList<>();
        attachedResources.forEach((id, resources) -> addMatches(matches, id, resources, predicate));
        return matches;
    }

    @Override
    public void insert(Index index, List<Resource> resources, String fileExt, boolean overwrite) {
        Identifier id = VersionUtils.withSuffix(index.id(), fileExt);
        List<Resource> existing = attachedResources.get(id);
        List<Resource> updated = new ArrayList<>(existing == null ? List.of() : existing);
        insertOrdinal(updated, index, resources, overwrite || (existing == null && index.ordinal() == -1));
        if (updated.isEmpty()) attachedResources.remove(id);
        else attachedResources.put(id, updated);
    }

    @Override
    public void delete(Index index, String fileExt) {
        Identifier id = VersionUtils.withSuffix(index.id(), fileExt);
        List<Resource> existing = attachedResources.get(id);
        if (existing == null) return;
        List<Resource> updated = new ArrayList<>(existing);
        deleteOrdinal(updated, index.ordinal());
        if (updated.isEmpty()) attachedResources.remove(id);
        else attachedResources.put(id, updated);
    }
}
