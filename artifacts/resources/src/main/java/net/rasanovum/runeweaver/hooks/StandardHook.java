package net.rasanovum.runeweaver.hooks;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.rasanovum.runeweaver.util.Index;
import net.rasanovum.runeweaver.util.RuneweaverUtil;
import net.rasanovum.runeweaver.util.VersionUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class StandardHook extends AbstractHook<Map<Identifier, Resource>> {

    public StandardHook(Map<Identifier, Resource> attachedResources) {
        super(attachedResources);
    }

    @Override
    public Optional<List<Resource>> captureFiles(@NotNull Index index, String fileExt) {
        if (index.ordinal() != -1 && !hasOrdinal(index.ordinal(), 1))
            return Optional.empty();
        Resource resource = this.attachedResources.get(VersionUtils.withSuffix(index.id(), fileExt));
        if(resource == null) return Optional.empty();
        return Optional.of(List.of(resource));
    }

    @Override
    public List<Map.Entry<Index, Resource>> getMatching(Predicate<Index> predicate) {
        return this.attachedResources
                .entrySet()
                .stream()
                .filter((entry) -> predicate.test(new Index(entry.getKey(), 0)))
                .map(entry -> Map.entry(new Index(RuneweaverUtil.removeExtension(entry.getKey()), 0), entry.getValue()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void insert(@NotNull Index index, List<Resource> resources, String fileExt, boolean overwrite) {
        if (resources.size() != 1) {
            throw new IllegalArgumentException("Expected one resource for " + index);
        }
        if (index.ordinal() != -1 && !hasOrdinal(index.ordinal(), 1)) {
            throw new IllegalArgumentException("Resource type does not support ordinal " + index.ordinal());
        }
        Identifier id = VersionUtils.withSuffix(index.id(), fileExt);
        if (overwrite && !attachedResources.containsKey(id)) {
            throw new IllegalStateException("Cannot overwrite missing resource " + index);
        }
        attachedResources.put(id, resources.get(0));
    }

    @Override
    public void delete(Index index, String fileExt) {
        if (index.ordinal() == -1 || hasOrdinal(index.ordinal(), 1))
            this.attachedResources.remove(VersionUtils.withSuffix(index.id(), fileExt));
    }
}
