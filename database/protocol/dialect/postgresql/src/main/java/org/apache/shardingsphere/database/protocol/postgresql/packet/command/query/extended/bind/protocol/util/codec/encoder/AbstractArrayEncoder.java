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
 * AbstractArrayEncoder.
 *
 * @param <D> data Type
 */
public abstract class AbstractArrayEncoder<D> implements ArrayEncoder<D[]> {
    
    private final int oid;
    
    public AbstractArrayEncoder(final int oid) {
        this.oid = oid;
    }
    
    /**
     * encode item to baut.
     *
     * @param item item
     * @param bout baut
     * @param charset charset
     */
    public abstract void write(D item, ByteArrayOutputStream bout, Charset charset);
    
    /**
     * get item String value.
     *
     * @param item item toString
     * @return string value
     */
    public abstract String toString(D item);
    
    /**
     * encode data without meeta data header.
     *
     * @param array array to encode
     * @param bout output stream
     * @param charset charset
     */
    public void toSingleDimensionBinaryRepresentation(final D[] array, final ByteArrayOutputStream bout, final Charset charset) {
        writeBytes(array, bout, charset);
    }
    
    @SneakyThrows(IOException.class)
    @Override
    public void toBinaryRepresentation(final D[] array, final int oid, final ByteArrayOutputStream bout, final Charset charset) {
        final byte[] buffer = new byte[4];
        // 1 dimension
        ByteConverter.int4(buffer, 0, 1);
        bout.write(buffer);
        // null
        ByteConverter.int4(buffer, 0, countNulls(array) > 0 ? 1 : 0);
        bout.write(buffer);
        // oid
        ByteConverter.int4(buffer, 0, getTypeOID(oid));
        bout.write(buffer);
        // length
        ByteConverter.int4(buffer, 0, array.length);
        bout.write(buffer);
        // postgresql uses 1 base by default
        ByteConverter.int4(buffer, 0, 1);
        bout.write(buffer);
        writeBytes(array, bout, charset);
    }
    
    /**
     * getTypeOID.
     *
     * @param arrayOid The array oid to get base oid type for.
     * @return The base oid type for the given array oid type given to.
     */
    int getTypeOID(@SuppressWarnings("unused") final int arrayOid) {
        return oid;
    }
    
    /**
     * Counts the number of {@code null} elements in <i>array</i>.
     *
     * @param array The array to count {@code null} elements in.
     * @return The number of {@code null} elements in <i>array</i>.
     */
    int countNulls(final D[] array) {
        int result = 0;
        for (D each : array) {
            if (each == null) {
                ++result;
            }
        }
        return result;
    }
    
    /**
     * decode array.
     *
     * @param array array to decode
     * @param bout output stream
     * @param charset charset
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows(IOException.class)
    private void writeBytes(final D[] array, final ByteArrayOutputStream bout, final Charset charset) {
        
        byte[] buffer = new byte[4];
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            D each = (D) Array.get(array, i);
            if (each == null) {
                ByteConverter.int4(buffer, 0, -1);
                bout.write(buffer);
            } else {
                write(each, bout, charset);
            }
        }
    }
}
