package net.rasanovum.runeweaver.util;

//? if <1.19 {
/*import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class LegacyResource implements Resource {
    private final Resource original;
    private final byte[] content;

    public LegacyResource(Resource original, byte[] content) {
        this.original = original;
        this.content = content;
    }

    public Identifier getLocation() { return original.getLocation(); }
    public InputStream getInputStream() { return new ByteArrayInputStream(content); }
    public boolean hasMetadata() { return original.hasMetadata(); }
    public <T> T getMetadata(MetadataSectionSerializer<T> serializer) { return original.getMetadata(serializer); }
    public String getSourceName() { return original.getSourceName(); }
    public void close() throws IOException { original.close(); }
}
*///?}
