/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.proxy.transport.mysql.packet;

import io.shardingjdbc.proxy.transport.common.packet.DatabaseProtocolPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * MySQL packet.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public abstract class MySQLPacket implements DatabaseProtocolPacket {
    
    public static final int PAYLOAD_LENGTH = 3;
    
    public static final int SEQUENCE_LENGTH = 1;
    
    private final int sequenceId;
    
    /**
     * Write packet to byte buffer.
     *
     * @param mysqlPacketPayload packet payload to be write
     */
    public abstract void write(MySQLPacketPayload mysqlPacketPayload);
}
