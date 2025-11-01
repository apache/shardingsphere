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

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public abstract class AbstractObjectArrayDecoder<A> implements ArrayDecoder<A[]> {
    
    private final Class<?> baseClazz;
    
    public AbstractObjectArrayDecoder(final Class<?> baseClazz) {
        this.baseClazz = baseClazz;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public A[] createArray(final int size) {
        return (A[]) Array.newInstance(baseClazz, size);
    }
    
    @Override
    public Object[] createMultiDimensionalArray(final int[] sizes) {
        return (Object[]) Array.newInstance(baseClazz, sizes);
    }
    
    @Override
    public void fromBinary(final A[] array, final int index, final int count, final ByteBuffer bytes, final Charset charset) {
        // skip through to the requested index
        for (int i = 0; i < index; i++) {
            final int length = bytes.getInt();
            if (length > 0) {
                bytes.position(bytes.position() + length);
            }
        }
        for (int i = 0; i < count; i++) {
            final int length = bytes.getInt();
            if (length != -1) {
                array[i] = parseValue(length, bytes, charset);
            } else {
                // explicitly set to null for reader's clarity
                array[i] = null;
            }
        }
    }
    
    /**
     * decode value.
     *
     * @param length params length
     * @param bytes source bytes
     * @param charset charset used
     * @return decode obj
     */
    public abstract A parseValue(int length, ByteBuffer bytes, Charset charset);
}
