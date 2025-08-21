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

public abstract class AbstractArrayEncoder<D> implements ArrayEncoder<D[]> {
    
    private final int oid;
    
    public AbstractArrayEncoder(int oid) {
        this.oid = oid;
    }
    
    public abstract void write(D item, ByteArrayOutputStream baos, Charset charset);
    
    public abstract String toString(D item);
    
    public void toSingleDimensionBinaryRepresentation(D[] array, ByteArrayOutputStream baos, Charset charset) {
        writeBytes(array, baos, charset);
    }
    
    @SneakyThrows(IOException.class)
    @Override
    public void toBinaryRepresentation(D[] array, int oid, ByteArrayOutputStream baos, Charset charset) {
        
        final byte[] buffer = new byte[4];
        
        // 1 dimension
        ByteConverter.int4(buffer, 0, 1);
        baos.write(buffer);
        // null
        ByteConverter.int4(buffer, 0, countNulls(array) > 0 ? 1 : 0);
        baos.write(buffer);
        // oid
        ByteConverter.int4(buffer, 0, getTypeOID(oid));
        baos.write(buffer);
        // length
        ByteConverter.int4(buffer, 0, array.length);
        baos.write(buffer);
        
        // postgresql uses 1 base by default
        ByteConverter.int4(buffer, 0, 1);
        baos.write(buffer);
        writeBytes(array, baos, charset);
        
    }
    
    /**
     * @param arrayOid The array oid to get base oid type for.
     * @return The base oid type for the given array oid type given to
     * {@link #toBinaryRepresentation}.
     */
    int getTypeOID(@SuppressWarnings("unused") int arrayOid) {
        return oid;
    }
    
    /**
     * Counts the number of {@code null} elements in <i>array</i>.
     *
     * @param array The array to count {@code null} elements in.
     * @return The number of {@code null} elements in <i>array</i>.
     */
    int countNulls(D[] array) {
        int nulls = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == null) {
                ++nulls;
            }
        }
        return nulls;
    }
    
    @SneakyThrows({IOException.class})
    private void writeBytes(final D[] array, ByteArrayOutputStream baos, Charset charset) {
        
        byte[] buffer = new byte[4];
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            D d = (D) Array.get(array, i);
            if (d == null) {
                ByteConverter.int4(buffer, 0, -1);
                baos.write(buffer);
            } else {
                write(d, baos, charset);
            }
        }
    }
}
