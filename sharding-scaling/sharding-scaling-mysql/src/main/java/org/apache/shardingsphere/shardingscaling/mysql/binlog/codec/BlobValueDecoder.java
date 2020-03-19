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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.codec;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;

/**
 * Blob value decoder.
 */
public final class BlobValueDecoder {
    
    /**
     * decode blob value.
     *
     * @param meta meta data
     * @param in input byte buffer
     * @return blob value
     */
    public static Serializable decodeBlob(final int meta, final ByteBuf in) {
        switch (meta) {
            case 1:
                return DataTypesCodec.readBytes(DataTypesCodec.readUnsignedInt1(in), in);
            case 2:
                return DataTypesCodec.readBytes(DataTypesCodec.readUnsignedInt2LE(in), in);
            case 3:
                return DataTypesCodec.readBytes(DataTypesCodec.readUnsignedInt3LE(in), in);
            case 4:
                return DataTypesCodec.readBytes((int) DataTypesCodec.readUnsignedInt4LE(in), in);
            default:
                throw new UnsupportedOperationException();
        }
    }
}
