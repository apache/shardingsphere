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

package info.avalon566.shardingscaling.sync.mysql.binlog.packet.response;

import info.avalon566.shardingscaling.sync.mysql.binlog.codec.DataTypesCodec;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * @author avalon566
 */
@Data
public class ResultSetHeaderPacket extends AbstractPacket {
    private long columnCount;
    private long extra;
    
    @Override
    public void fromByteBuf(ByteBuf data) {
        columnCount = DataTypesCodec.readLengthCoded(data);
        if (data.readableBytes() > 0) {
            extra = DataTypesCodec.readLengthCoded(data);
        }
    }
}
