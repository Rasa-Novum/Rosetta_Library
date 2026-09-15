package net.rasanovum.rosetta.attachment;

import java.util.Optional;

/** Compatibility seam for importing attachment formats owned by older libraries. */
final class AttachmentMigration {
    private AttachmentMigration() {}

    static <O, T> Optional<T> tryMigrate(AttachmentKey<O, T> key, O owner) {
        // Data Anchor import will be implemented once representative world data is available.
        return Optional.empty();
    }
}
