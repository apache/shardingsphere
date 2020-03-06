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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.response;

import org.apache.shardingsphere.shardingscaling.mysql.binlog.codec.DataTypesCodec;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

/**
 * MySQL result set packet header.
 *
 * <p>
 *     There is no define for result set packet header.
 *
 *     MySQL Internals Manual  /  MySQL Client/Server Protocol  /  Prepared Statements  /  Binary Protocol ResultSet
 *     https://dev.mysql.com/doc/internals/en/binary-protocol-resultset.html
 * </p>
 */
@Getter
public final class ResultSetHeaderPacket extends AbstractPacket {
    
    private long columnCount;
    
    private long extra;
    
    @Override
    public void fromByteBuf(final ByteBuf data) {
        columnCount = DataTypesCodec.readLengthCodedIntLE(data);
        if (data.readableBytes() > 0) {
            extra = DataTypesCodec.readLengthCodedIntLE(data);
        }
    }
}
