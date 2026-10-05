package net.rasanovum.rosetta.attachment;

enum AttachmentKind {
    LEVEL("level"), ENTITY("entity"), PLAYER("player"), CHUNK("chunk"), BLOCK_ENTITY("block_entity");

    final String path;
    AttachmentKind(String path) { this.path = path; }
}
