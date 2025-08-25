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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * postgresql array decoder.
 * @param <A> base type for decode
 */
public interface ArrayDecoder<A> {
    
    /**
     * Creates an array of the specified size.
     *
     * @param size The length of the array to be created.
     * @return An array of type A with the specified size. The specific type of array depends on the implementation of this interface.
     */
    A createArray(int size);
    
    /**
     * Creates a multi-dimensional array with the specified sizes for each dimension.
     *
     * @param sizes An array of integers, where each value specifies the size of a corresponding dimension in the multi-dimensional array.
     * @return A multi-dimensional array where each dimension has the specified size. The specific type of array depends on the implementation of this interface.
     */
    Object[] createMultiDimensionalArray(int[] sizes);
    
    /**
     * Populates the provided array with data from a binary source.
     *
     * @param array The array to be populated.
     * @param index The starting index in the array where data should be placed.
     * @param count The number of elements to populate from the binary source.
     * @param bytes A ByteBuffer containing the binary data to be decoded.
     * @param charset The charset to be used for decoding string data from the binary source.
     */
    void fromBinary(A array, int index, int count, ByteBuffer bytes, Charset charset);
}
