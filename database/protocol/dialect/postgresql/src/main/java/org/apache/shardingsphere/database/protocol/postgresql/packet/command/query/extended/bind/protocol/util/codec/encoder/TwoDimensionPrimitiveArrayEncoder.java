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
import org.postgresql.util.ByteConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;

/**
 * Decode two-dimensional array.
 *
 * @param <A> baseType
 */
public class TwoDimensionPrimitiveArrayEncoder<A> implements ArrayEncoder<A[][]> {
    
    private final AbstractArrayEncoder<A> support;
    
    /**
     * TwoDimensionPrimitiveArrayEncoder.
     *
     * @param support The instance providing support for the base array type.
     */
    TwoDimensionPrimitiveArrayEncoder(final AbstractArrayEncoder<A> support) {
        this.support = support;
    }
    
    @SneakyThrows(IOException.class)
    @Override
    public void toBinaryRepresentation(final A[][] array, final int oid, final ByteArrayOutputStream bout, final Charset charset) {
        byte[] buffer = new byte[4];
        boolean hasNulls = false;
        for (int i = 0; !hasNulls && i < array.length; i++) {
            if (support.countNulls(array[i]) > 0) {
                hasNulls = true;
            }
        }
        // 2 dimension
        ByteConverter.int4(buffer, 0, 2);
        bout.write(buffer);
        // nulls
        ByteConverter.int4(buffer, 0, hasNulls ? 1 : 0);
        bout.write(buffer);
        // oid
        ByteConverter.int4(buffer, 0, support.getTypeOID(oid));
        bout.write(buffer);
        // length
        ByteConverter.int4(buffer, 0, array.length);
        bout.write(buffer);
        // postgres defaults to 1 based lower bound
        ByteConverter.int4(buffer, 0, 1);
        bout.write(buffer);
        ByteConverter.int4(buffer, 0, array.length > 0 ? Array.getLength(array[0]) : 0);
        bout.write(buffer);
        // postgresql uses 1 base by default
        ByteConverter.int4(buffer, 0, 1);
        bout.write(buffer);
        for (int i = 0; i < array.length; i++) {
            support.toSingleDimensionBinaryRepresentation(array[i], bout, charset);
        }
    }
    
}
