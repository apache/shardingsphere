/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BlobFilterNotFoundException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.UnsupportedBlobFilterException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.EncodingFactory;

import java.nio.charset.Charset;
import java.util.Optional;

/**
 * BLOB parameter buffer utility class for Firebird.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdBlobParameterBufferUtils {
    
    private static final int BPB_VERSION1 = 1;
    
    private static final int BPB_SOURCE_TYPE = 1;
    
    private static final int BPB_TARGET_TYPE = 2;
    
    private static final int BPB_SOURCE_INTERP = 4;
    
    private static final int BPB_TARGET_INTERP = 5;
    
    private static final int BLOB_TEXT = 1;
    
    private static final int CHARSET_NONE = 0;
    
    private static final int CHARSET_BINARY = 1;
    
    private static final int CHARSET_DYNAMIC = 127;
    
    private static final int INTERNAL_FILTER_COUNT = 10;
    
    private static final int BLOB_RANGES = 4;
    
    /**
     * Create the transliterator of the BLOB filter that Firebird applies to the data of a BLOB created with the BLOB parameter buffer.
     *
     * <p>Mirrors {@code gds__parse_bpb2} and the filter decision of {@code blb::create2}, which replaces {@code CS_dynamic} with the character set of the connection.
     * Firebird transliterates text between two different character sets other than {@code NONE} and {@code BINARY}; other BLOB filters are not supported.
     * Firebird has internal BLOB filters from its sub types other than ranges to text; other sub type conversions fail as in a database without declared BLOB filters.
     * If the character set of the connection is unknown, it is passed as {@code CS_dynamic}, which has no Java character set.</p>
     *
     * @param bpb BLOB parameter buffer
     * @param connectionCharsetId character set ID of the connection
     * @return transliterator, or empty if Firebird stores the data unchanged
     * @throws BlobFilterNotFoundException if Firebird has no BLOB filter for the sub type conversion
     * @throws UnsupportedBlobFilterException if Firebird converts the data with another BLOB filter or a character set without Java character set
     */
    public static Optional<FirebirdBlobTransliterator> createTransliterator(final byte[] bpb, final int connectionCharsetId) {
        if (0 == bpb.length || BPB_VERSION1 != bpb[0]) {
            return Optional.empty();
        }
        int sourceType = 0;
        int targetType = 0;
        int sourceCharset = 0;
        int targetCharset = 0;
        int index = 1;
        while (index + 1 < bpb.length) {
            int tag = bpb[index];
            int valueLength = Math.min(bpb[index + 1] & 0xFF, bpb.length - index - 2);
            int value = readVaxShort(bpb, index + 2, valueLength);
            if (BPB_SOURCE_TYPE == tag) {
                sourceType = value;
            } else if (BPB_TARGET_TYPE == tag) {
                targetType = value;
            } else if (BPB_SOURCE_INTERP == tag) {
                sourceCharset = value;
            } else if (BPB_TARGET_INTERP == tag) {
                targetCharset = value;
            }
            index += 2 + valueLength;
        }
        if (0 != targetType && sourceType != targetType) {
            checkSubTypeConversion(sourceType, targetType, targetCharset);
            return Optional.empty();
        }
        int actualSourceCharset = getActualCharset(sourceCharset, connectionCharsetId);
        int actualTargetCharset = getActualCharset(targetCharset, connectionCharsetId);
        if (BLOB_TEXT != targetType || actualSourceCharset == actualTargetCharset || isPlainCharset(actualSourceCharset) || isPlainCharset(actualTargetCharset)) {
            return Optional.empty();
        }
        return Optional.of(new FirebirdBlobTransliterator(getJavaCharset(actualSourceCharset), getJavaCharset(actualTargetCharset)));
    }
    
    private static void checkSubTypeConversion(final int sourceType, final int targetType, final int targetCharset) {
        ShardingSpherePreconditions.checkState(BLOB_TEXT == targetType && (0 == sourceType || CHARSET_BINARY == targetCharset), () -> createFilterException(sourceType, targetType));
    }
    
    private static int readVaxShort(final byte[] bpb, final int offset, final int length) {
        if (length <= 0 || length > Integer.BYTES) {
            return 0;
        }
        int result = bpb[offset + length - 1] << 8 * (length - 1);
        for (int i = 0; i < length - 1; i++) {
            result += (bpb[offset + i] & 0xFF) << 8 * i;
        }
        return (short) result;
    }
    
    private static SQLDialectException createFilterException(final int sourceType, final int targetType) {
        boolean internalFilter = BLOB_TEXT == targetType && sourceType >= 0 && sourceType < INTERNAL_FILTER_COUNT && BLOB_RANGES != sourceType;
        return internalFilter ? new UnsupportedBlobFilterException() : new BlobFilterNotFoundException(sourceType, targetType);
    }
    
    private static int getActualCharset(final int charset, final int connectionCharsetId) {
        return CHARSET_DYNAMIC == charset ? connectionCharsetId : charset;
    }
    
    private static boolean isPlainCharset(final int charset) {
        return CHARSET_NONE == charset || CHARSET_BINARY == charset;
    }
    
    private static Charset getJavaCharset(final int charsetId) {
        EncodingDefinition encodingDefinition = EncodingFactory.getPlatformDefault().getEncodingDefinitionByCharacterSetId(charsetId);
        ShardingSpherePreconditions.checkState(CHARSET_DYNAMIC != charsetId && null != encodingDefinition && !encodingDefinition.isInformationOnly(), UnsupportedBlobFilterException::new);
        return encodingDefinition.getJavaCharset();
    }
}
