package net.rasanovum.runeweaver;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.packs.resources.Resource;
import net.rasanovum.runeweaver.util.interfaces.RuneweaverCodec;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public interface RuneweaverCodecs {

    RuneweaverCodec<JsonElement> JSON_ELEMENT = new RuneweaverCodec<>() {
        @Override
        public String extensionAndDot() {
            return ".json";
        }

        @Override
        public JsonElement deserialize(Resource r) throws IOException {
            //? if <1.19 {
            /*try (Reader reader = new InputStreamReader(open(r), StandardCharsets.UTF_8)) {
            *///?} else {
            try (Reader reader = r.openAsReader()) {
            //?}
                return JsonParser.parseReader(reader);
            }
        }

        @Override
        public Resource serialize(Resource r, JsonElement x) {
            byte[] bytes = x.toString().getBytes(StandardCharsets.UTF_8);
            //? if <1.19 {
            /*return new net.rasanovum.runeweaver.util.LegacyResource(r, bytes);
            *///?} else if <1.19.3 {
            /*return new Resource(r.sourcePackId(), () -> new ByteArrayInputStream(bytes), r::metadata);
            *///?} else {
            return new Resource(r.source(), () -> new ByteArrayInputStream(bytes), r::metadata);
            //?}
        }

        @Override
        public ByteArrayOutputStream export(JsonElement resource) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(new GsonBuilder().setPrettyPrinting().create().toJson(resource).getBytes(StandardCharsets.UTF_8));
            return baos;
        }
    };

    RuneweaverCodec<BufferedImage> PNG = new RuneweaverCodec<>() {
        @Override
        public String extensionAndDot() {
            return ".png";
        }

        @Override
        public BufferedImage deserialize(Resource r) throws IOException {
            try (InputStream input = open(r)) {
                return ImageIO.read(input);
            }
        }

        @Override
        public Resource serialize(Resource r, BufferedImage x) throws IOException {
            byte[] bytes = bufferedImageToStream(x).toByteArray();
            //? if <1.19 {
            /*return new net.rasanovum.runeweaver.util.LegacyResource(r, bytes);
            *///?} else if <1.19.3 {
            /*return new Resource(r.sourcePackId(), () -> new ByteArrayInputStream(bytes), r::metadata);
            *///?} else {
            return new Resource(r.source(), () -> new ByteArrayInputStream(bytes), r::metadata);
            //?}
        }

        @Override
        public ByteArrayOutputStream export(BufferedImage r) throws IOException {
            return bufferedImageToStream(r);
        }
    };

    RuneweaverCodec<CompoundTag> NBT = new RuneweaverCodec<>() {
        @Override
        public String extensionAndDot() {
            return ".nbt";
        }

        @Override
        public CompoundTag deserialize(Resource r) throws IOException {
            try (InputStream input = open(r)) {
                //? if >1.20.1 {
                return NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
                //?} else {
                /*return NbtIo.readCompressed(input);
                *///?}
            }
        }

        @Override
        public Resource serialize(Resource r, CompoundTag x) throws IOException {
            byte[] bytes = nbtToStream(x).toByteArray();
            //? if <1.19 {
            /*return new net.rasanovum.runeweaver.util.LegacyResource(r, bytes);
            *///?} else if <1.19.3 {
            /*return new Resource(r.sourcePackId(), () -> new ByteArrayInputStream(bytes), r::metadata);
            *///?} else {
            return new Resource(r.source(), () -> new ByteArrayInputStream(bytes), r::metadata);
            //?}
        }

        @Override
        public ByteArrayOutputStream export(CompoundTag r) throws IOException {
            return nbtToStream(r);
        }
    };

    private static InputStream open(Resource resource) throws IOException {
        //? if <1.19 {
        /*return resource.getInputStream();
        *///?} else {
        return resource.open();
        //?}
    }

    private static ByteArrayOutputStream bufferedImageToStream(BufferedImage image) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", stream);
        return stream;
    }

    private static ByteArrayOutputStream nbtToStream(CompoundTag compound) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NbtIo.writeCompressed(compound, baos);
        return baos;
    }

}
