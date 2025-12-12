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

package org.apache.shardingsphere.database.protocol.postgresql.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ByteBufTestUtils {
    
    /**
     * Creates a new buffer with a newly allocated byte array, fixed capacity.
     *
     * @param capacity the fixed capacity of the underlying byte array
     * @return byte buffer
     */
    public static ByteBuf createByteBuf(final int capacity) {
        return createByteBuf(capacity, capacity);
    }
    
    /**
     * Creates a new buffer with a newly allocated byte array.
     *
     * @param initialCapacity the initial capacity of the underlying byte array
     * @param maxCapacity the max capacity of the underlying byte array
     * @return byte buffer
     */
    public static ByteBuf createByteBuf(final int initialCapacity, final int maxCapacity) {
        UnpooledByteBufAllocator byteBufAllocator = UnpooledByteBufAllocator.DEFAULT;
        return new UnpooledHeapByteBuf(byteBufAllocator, initialCapacity, maxCapacity);
    }
}
