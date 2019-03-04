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

package org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.text;

import lombok.Getter;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.PostgreSQLQueryCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.payload.PostgreSQLPacketPayload;

import java.util.Collection;
import java.util.Collections;

/**
 * PostgreSQL command query packet.
 *
 * @author zhangyonglun
 */
@Getter
public final class PostgreSQLComQueryPacket implements PostgreSQLQueryCommandPacket {
    
    private final String sql;
    
    public PostgreSQLComQueryPacket(final PostgreSQLPacketPayload payload) {
        payload.readInt4();
        sql = payload.readStringNul();
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
    }
    
    @Override
    public Collection<PostgreSQLPacket> execute() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean isQuery() {
        return true;
    }
    
    @Override
    public boolean next() {
        return true;
    }
    
    @Override
    public PostgreSQLPacket getQueryData() {
        return null;
    }
    
    @Override
    public char getMessageType() {
        return PostgreSQLCommandPacketType.QUERY.getValue();
    }
}
