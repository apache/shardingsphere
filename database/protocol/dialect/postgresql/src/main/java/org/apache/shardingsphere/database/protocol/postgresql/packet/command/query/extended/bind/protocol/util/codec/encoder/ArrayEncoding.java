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

import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for using arrays in requests.
 *
 * <p>
 * Binary format:
 * <ul>
 * <li>4 bytes with number of dimensions</li>
 * <li>4 bytes, boolean indicating nulls present or not</li>
 * <li>4 bytes type oid</li>
 * <li>8 bytes describing the length of each dimension (repeated for each dimension)</li>
 * <ul>
 * <li>4 bytes for length</li>
 * <li>4 bytes for lower bound on length to check for overflow (it appears this value can always be 0)</li>
 * </ul>
 * <li>data in depth first element order corresponding number and length of dimensions</li>
 * <ul>
 * <li>4 bytes describing length of element, {@code 0xFFFFFFFF} ({@code -1}) means {@code null}</li>
 * <li>binary representation of element (iff not {@code null}).</li>
 * </ul>
 * </ul>
 * </p>
 *
 * @author Brett Okken
 */
public final class ArrayEncoding {
    
    @SuppressWarnings("rawtypes")
    private static final Map<Class, AbstractArrayEncoder> ARRAY_CLASS_TO_ENCODER = new HashMap<>(
            (int) (14 / .75) + 1);
    
    static {
        // todo override getTypeOID to support more oid type use same encoder
        ARRAY_CLASS_TO_ENCODER.put(Long.class, Int8ArrayEncoder.INSTANCE);
        ARRAY_CLASS_TO_ENCODER.put(Integer.class, Int4ArrayEncoder.INSTANCE);
        ARRAY_CLASS_TO_ENCODER.put(Short.class, Int2ArrayEncoder.INSTANCE);
        ARRAY_CLASS_TO_ENCODER.put(Double.class, Float8ArrayEncoder.INSTANCE);
        ARRAY_CLASS_TO_ENCODER.put(Float.class, Float4ArrayEncoder.INSTANCE);
        ARRAY_CLASS_TO_ENCODER.put(Boolean.class, BooleanArrayEncoder.INSTANCE);
        ARRAY_CLASS_TO_ENCODER.put(String.class, StringArrayEncoder.INSTANCE);
        ARRAY_CLASS_TO_ENCODER.put(Date.class, DateArrayEncoder.INSTANCE);
        ARRAY_CLASS_TO_ENCODER.put(BigDecimal.class, NumericArrayEncoder.INSTANCE);
        ARRAY_CLASS_TO_ENCODER.put(Number.class, NumericArrayEncoder.INSTANCE);
        ARRAY_CLASS_TO_ENCODER.put(Time.class, TimeArrayEncoder.INSTANCE);
        ARRAY_CLASS_TO_ENCODER.put(byte[].class, ByteaArrayEncoder.INSTANCE);
        ARRAY_CLASS_TO_ENCODER.put(Timestamp.class, TimestampArrayEncoder.INSTANCE);
    }
    
    /**
     * Returns support for encoding <i>array</i>.
     *
     * @param array The array to encode. Must not be {@code null}.
     * @param <A> base type
     * @return An instance capable of encoding <i>array</i> as a {@code String} at
     *     minimum. Some types may support binary encoding.
     * @throws PSQLException if <i>array</i> is not a supported type.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <A> ArrayEncoder<A[]> getArrayEncoder(final Object array) throws PSQLException {
        final Class<?> arrayClazz = array.getClass();
        Class<?> subClazz = arrayClazz.getComponentType();
        if (subClazz == null) {
            throw new PSQLException(GT.tr("Invalid elements {0}", array), PSQLState.INVALID_PARAMETER_TYPE);
        }
        AbstractArrayEncoder<A> result = ARRAY_CLASS_TO_ENCODER.get(subClazz);
        if (result != null) {
            return result;
        }
        Class<?> subSubClazz = subClazz.getComponentType();
        if (subSubClazz == null) {
            throw new PSQLException(GT.tr("Invalid elements {0}", array), PSQLState.INVALID_PARAMETER_TYPE);
        }
        subClazz = subSubClazz;
        int dimensions = 2;
        while (subClazz != null) {
            result = ARRAY_CLASS_TO_ENCODER.get(subClazz);
            if (result != null) {
                if (dimensions == 2) {
                    return new TwoDimensionPrimitiveArrayEncoder(result);
                }
                return new RecursiveArrayEncoder(result, dimensions);
            }
            subSubClazz = subClazz.getComponentType();
            ++dimensions;
            subClazz = subSubClazz;
        }
        throw new PSQLException(GT.tr("Invalid elements {0}", array), PSQLState.INVALID_PARAMETER_TYPE);
    }
    
}
