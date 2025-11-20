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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.codec.encoder;

import lombok.SneakyThrows;
import org.checkerframework.checker.index.qual.Positive;
import org.postgresql.util.ByteConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;

/**
 * Decode multi-level nested array.
 *
 * @param <A> base type
 */
@SuppressWarnings("rawtypes")
public class RecursiveArrayEncoder<A> implements ArrayEncoder {
    
    private final AbstractArrayEncoder<A> support;
    
    private final @Positive int dimensions;
    
    /**
     * RecursiveArrayEncoder.
     *
     * @param support The instance providing support for the base array type.
     */
    RecursiveArrayEncoder(final AbstractArrayEncoder<A> support, final @Positive int dimensions) {
        this.support = support;
        this.dimensions = dimensions;
        assert dimensions >= 2;
    }
    
    private boolean hasNulls(final Object array, final int depth) {
        if (depth > 1) {
            for (int i = 0, j = Array.getLength(array); i < j; i++) {
                if (hasNulls(Array.get(array, i), depth - 1)) {
                    return true;
                }
            }
            return false;
        }
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Object item = Array.get(array, i);
            if (item == null) {
                return true;
            }
        }
        return false;
    }
    
    @SneakyThrows(IOException.class)
    @Override
    public void toBinaryRepresentation(final Object array, final int oid, final ByteArrayOutputStream bout, final Charset charset) {
        boolean hasNulls = hasNulls(array, dimensions);
        byte[] buffer = new byte[4];
        // dimensions
        ByteConverter.int4(buffer, 0, dimensions);
        bout.write(buffer);
        // nulls
        ByteConverter.int4(buffer, 0, hasNulls ? 1 : 0);
        bout.write(buffer);
        // oid
        ByteConverter.int4(buffer, 0, support.getTypeOID(oid));
        bout.write(buffer);
        // length
        ByteConverter.int4(buffer, 0, Array.getLength(array));
        bout.write(buffer);
        // postgresql uses 1 base by default
        ByteConverter.int4(buffer, 0, 1);
        bout.write(buffer);
        writeArray(buffer, bout, array, dimensions, true, charset);
    }
    
    @SneakyThrows(IOException.class)
    @SuppressWarnings("unchecked")
    private void writeArray(final byte[] buffer, final ByteArrayOutputStream bout, final Object array, final int depth, final boolean first, final Charset charset) {
        int length = Array.getLength(array);
        if (first) {
            ByteConverter.int4(buffer, 0, length > 0 ? Array.getLength(Array.get(array, 0)) : 0);
            bout.write(buffer);
            // postgresql uses 1 base by default
            ByteConverter.int4(buffer, 0, 1);
            bout.write(buffer);
        }
        for (int i = 0; i < length; i++) {
            Object subArray = Array.get(array, i);
            if (depth > 2) {
                writeArray(buffer, bout, subArray, depth - 1, i == 0, charset);
            } else {
                support.toSingleDimensionBinaryRepresentation((A[]) subArray, bout, charset);
            }
        }
    }
    
}
