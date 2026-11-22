package net.rasanovum.runeweaver;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
//? if <1.19 {
/*import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
*///?}
import net.minecraft.server.packs.resources.Resource;
import net.rasanovum.runeweaver.enums.ErrorPolicy;
import net.rasanovum.runeweaver.enums.Lifetime;
import net.rasanovum.runeweaver.hooks.ListHook;
import net.rasanovum.runeweaver.hooks.NamespaceHook;
import net.rasanovum.runeweaver.util.Index;
import net.rasanovum.runeweaver.util.VersionUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class RuneweaverDispatchRegressionTest {
    @Test
    void skipsUnrelatedFilesAndUpdatesWhenHandlersChange() {
        var probeCodec = new net.rasanovum.runeweaver.util.interfaces.RuneweaverCodec<JsonElement>() {
            @Override public String extensionAndDot() { return ".fixture"; }
            @Override public JsonElement deserialize(Resource resource) { return fail("unexpected decode"); }
            @Override public Resource serialize(Resource resource, JsonElement value) { return fail("unexpected encode"); }
            @Override public java.io.ByteArrayOutputStream export(JsonElement value) { return fail("unexpected export"); }
        };
        var target = VersionUtils.id("regression", "exact");
        var targetFile = VersionUtils.withSuffix(target, ".fixture");
        var miss = VersionUtils.id("regression", "unrelated.fixture");
        var initial = builder().setCodec(probeCodec).setResourceId(target).setEvent(context -> fail("unexpected dispatch"));
        var pattern = builder().setCodec(probeCodec).setResourceFilePredicate(file -> VersionUtils.path(file).startsWith("pattern/"))
                .setEvent(context -> fail("unexpected dispatch"));
        var exactId = Runeweaver.registerEvent(initial);
        var patternId = Runeweaver.registerEvent(pattern);
        try {
            assertTrue(Runeweaver.hasHandlers(targetFile));
            assertTrue(Runeweaver.hasHandlers(VersionUtils.id("regression", "pattern/one.fixture")));
            assertFalse(Runeweaver.hasHandlers(miss));
            var original = List.<Resource>of(resource(0));
            var hook = new NamespaceHook(original, miss) {
                @Override
                public List<Map.Entry<Index, Resource>> getMatching(java.util.function.Predicate<Index> predicate) {
                    return fail("unrelated file entered dispatch");
                }
            };
            assertSame(original, Runeweaver.processHook(hook));
        } finally {
            Runeweaver.removeEvent(exactId);
            Runeweaver.removeEvent(patternId);
        }
    }

    @Test
    void deletesMultipleOrdinalsAndKeepsCapturedReplacements() throws Exception {
        Index index = new Index("regression:ordinal");
        var file = VersionUtils.withSuffix(index.id(), ".json");
        var event = Runeweaver.registerEvent(builder().setResourceId(index.id()).setEvent(context -> {
            int ordinal = context.getIndex().ordinal();
            if (ordinal == 0 || ordinal == 2) {
                context.markForDeletion(true);
            } else {
                context.captureFiles(new Index(index.id(), 1)).get(0).setValue(new JsonPrimitive(99));
            }
        }));
        try {
            var resources = new ArrayList<>(List.of(resource(0), resource(1), resource(2)));
            assertTrue(new NamespaceHook(resources, file).captureFiles(new Index(index.id(), 4), ".json").isEmpty());
            var list = new HashMap<>(Map.of(file, List.copyOf(resources)));
            assertTrue(new ListHook(list).captureFiles(new Index(index.id(), 4), ".json").isEmpty());
            var namespaceResult = Runeweaver.processHook(new NamespaceHook(resources, file));
            assertEquals(1, namespaceResult.size());
            assertEquals(99, RuneweaverCodecs.JSON_ELEMENT.deserialize(namespaceResult.get(0)).getAsInt());
            var listResult = Runeweaver.processHook(new ListHook(list)).get(file);
            assertEquals(1, listResult.size());
            assertEquals(99, RuneweaverCodecs.JSON_ELEMENT.deserialize(listResult.get(0)).getAsInt());
        } finally {
            Runeweaver.removeEvent(event);
        }
    }

    @Test
    void exceptionReleasesProcessingLockForOtherThreads() throws Exception {
        Index index = new Index("regression:throw");
        var file = VersionUtils.withSuffix(index.id(), ".json");
        var failure = Runeweaver.registerEvent(builder().setResourceId(index.id())
                .setEvent(context -> { throw new IllegalStateException("dispatch failed"); }));
        try {
            assertThrows(IllegalStateException.class, () ->
                    Runeweaver.processHook(new NamespaceHook(new ArrayList<>(List.of(resource(0))), file)));
        } finally {
            Runeweaver.removeEvent(failure);
        }
        var executor = Executors.newSingleThreadExecutor(task -> {
            Thread worker = new Thread(task, "Runeweaver lock test");
            worker.setDaemon(true);
            return worker;
        });
        try {
            executor.submit(() -> {
                Runnable release = Runeweaver.lockEventProcessing();
                release.run();
            }).get(3, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
    }

    private static RuneweaverEventBuilder<JsonElement> builder() {
        return new RuneweaverEventBuilder<JsonElement>().setCodec(RuneweaverCodecs.JSON_ELEMENT)
                .setEventName("dispatch regression").setErrorPolicy(ErrorPolicy.THROW).setLifetime(Lifetime.PERSISTENT);
    }

    @SuppressWarnings("DataFlowIssue")
    private static Resource resource(int value) {
        byte[] bytes = Integer.toString(value).getBytes(StandardCharsets.UTF_8);
        //? if <1.19 {
        /*return new Resource() {
            @Override public ResourceLocation getLocation() { return VersionUtils.id("regression", "fixture"); }
            @Override public ByteArrayInputStream getInputStream() { return new ByteArrayInputStream(bytes); }
            @Override public boolean hasMetadata() { return false; }
            @Override public <T> T getMetadata(MetadataSectionSerializer<T> serializer) { return null; }
            @Override public String getSourceName() { return "test"; }
            @Override public void close() {}
        };
        *///?} else {
        return new Resource(null, () -> new ByteArrayInputStream(bytes), null);
        //?}
    }
}
