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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.binlog;

import org.apache.shardingsphere.shardingscaling.mysql.binlog.codec.DataTypesCodec;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

/**
 * Rotate event.
 */
@Getter
public final class RotateEventPacket {
    
    private long position;
    
    private String nextFileName;
    
    /**
     * Parse rotate event from {@code ByteBuf}.
     * @param in buffer
     */
    public void parse(final ByteBuf in) {
        position = DataTypesCodec.readInt8LE(in);
        nextFileName = DataTypesCodec.readFixedLengthString(in.readableBytes(), in);
    }
}
