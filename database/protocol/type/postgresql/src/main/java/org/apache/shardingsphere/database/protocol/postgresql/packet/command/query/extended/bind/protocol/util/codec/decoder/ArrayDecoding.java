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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.codec.decoder;

import org.postgresql.core.Oid;
import org.postgresql.util.GT;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for decoding arrays.
 *
 * <p>
 * See {@code ArrayEncoding} for description of the binary format of arrays.
 * </p>
 *
 * @author Brett Okken
 */
public final class ArrayDecoding {
    
    /**
     * Maps from base type oid to {@link ArrayDecoder} capable of processing
     * entries.
     */
    @SuppressWarnings("rawtypes")
    public static final Map<Integer, ArrayDecoder> OID_TO_DECODER = new HashMap<>(
            (int) (21 / .75) + 1);
    
    static {
        
        OID_TO_DECODER.put(Oid.TEXT, StringArrayDecoder.INSTANCE);
        OID_TO_DECODER.put(Oid.VARCHAR, StringArrayDecoder.INSTANCE);
        
    }
    
    @SuppressWarnings("unchecked")
    private static <A extends Object> ArrayDecoder<A> getDecoder(int oid) {
        final Integer key = oid;
        @SuppressWarnings("rawtypes")
        final ArrayDecoder decoder = OID_TO_DECODER.get(key);
        if (decoder != null) {
            return decoder;
        }
        throw new UnsupportedOperationException(GT.tr("Invalid decoder key {0}", key));
    }
    
    @SuppressWarnings("unchecked")
    public static Object readBinaryArray(int index, int count, byte[] bytes, Charset charset) {
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);
        final int dimensions = buffer.getInt();
        @SuppressWarnings("unused")
        final boolean hasNulls = buffer.getInt() != 0;
        final int elementOid = buffer.getInt();
        
        @SuppressWarnings("rawtypes")
        final ArrayDecoder decoder = getDecoder(elementOid);
        
        if (dimensions == 0) {
            return decoder.createArray(0);
        }
        
        final int adjustedSkipIndex = index > 0 ? index - 1 : 0;
        
        // optimize for single dimension array
        if (dimensions == 1) {
            int length = buffer.getInt();
            buffer.position(buffer.position() + 4);
            if (count > 0) {
                length = Math.min(length, count);
            }
            final Object array = decoder.createArray(length);
            decoder.fromBinary(array, adjustedSkipIndex, length, buffer, charset);
            return array;
        }
        
        final int[] dimensionLengths = new int[dimensions];
        for (int i = 0; i < dimensions; i++) {
            dimensionLengths[i] = buffer.getInt();
            buffer.position(buffer.position() + 4);
        }
        
        if (count > 0) {
            dimensionLengths[0] = Math.min(count, dimensionLengths[0]);
        }
        
        final Object[] array = decoder.createMultiDimensionalArray(dimensionLengths);
        
        // TODO: in certain circumstances (no nulls, fixed size data types)
        // if adjustedSkipIndex is > 0, we could advance through the buffer rather than
        // parse our way through throwing away the results
        
        storeValues(array, decoder, buffer, adjustedSkipIndex, dimensionLengths, 0, charset);
        
        return array;
    }
    
    @SuppressWarnings("unchecked")
    private static <A extends Object> void storeValues(Object[] array, ArrayDecoder<A> decoder, ByteBuffer bytes,
                                                       int skip, int[] dimensionLengths, int dim, Charset charset) {
        assert dim <= dimensionLengths.length - 2;
        
        for (int i = 0; i < skip; i++) {
            if (dim == dimensionLengths.length - 2) {
                decoder.fromBinary((A) array[0], 0, dimensionLengths[dim + 1], bytes, charset);
            } else {
                storeValues((Object[]) array[0], decoder, bytes, 0, dimensionLengths, dim + 1, charset);
            }
        }
        
        for (int i = 0; i < dimensionLengths[dim]; i++) {
            if (dim == dimensionLengths.length - 2) {
                decoder.fromBinary((A) array[i], 0, dimensionLengths[dim + 1], bytes, charset);
            } else {
                storeValues((Object[]) array[i], decoder, bytes, 0, dimensionLengths, dim + 1, charset);
            }
        }
    }
    
}
