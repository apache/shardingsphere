package org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.upload;

import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

@Getter
public final class FirebirdBlobUpload {

    private final int blobHandle;

    private final long blobId;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private boolean closed;

    public FirebirdBlobUpload(final int blobHandle, final long blobId) {
        this.blobHandle = blobHandle;
        this.blobId = blobId;
    }

    public void append(final byte[] segment) {
        Objects.requireNonNull(segment, "BLOB segment must not be null");
        buffer.write(segment, 0, segment.length);
    }

    public int getSize() {
        return buffer.size();
    }

    public byte[] getBytes() {
        return buffer.toByteArray();
    }

    public boolean isClosed() {
        return closed;
    }

    public void markClosed() {
        closed = true;
    }
}
