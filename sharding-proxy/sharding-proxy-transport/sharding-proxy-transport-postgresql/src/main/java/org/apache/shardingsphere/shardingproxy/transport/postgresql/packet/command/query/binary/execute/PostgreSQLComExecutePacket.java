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

package org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.execute;

import com.google.common.base.Optional;
import lombok.Getter;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.PostgreSQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandPacketType;

/**
 * PostgreSQL command execute packet.
 *
 * @author zhangyonglun
 */
@Getter
public final class PostgreSQLComExecutePacket implements PostgreSQLCommandPacket {
    
    private final char messageType = PostgreSQLCommandPacketType.EXECUTE.getValue();
    
    public PostgreSQLComExecutePacket(final PostgreSQLPacketPayload payload) {
        payload.readInt4();
        payload.readStringNul();
        payload.readInt4();
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
    }
    
    @Override
    public Optional<CommandResponsePackets> execute() {
        return Optional.absent();
    }
}
