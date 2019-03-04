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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.initdb;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchemas;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.MySQLCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.payload.MySQLPacketPayload;

import java.util.Collection;
import java.util.Collections;

/**
 * MySQL COM_INIT_DB command packet.
 *
 * @author zhangliang
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-init-db.html#packet-COM_INIT_DB">COM_INIT_DB</a>
 */
@Slf4j
public final class MySQLComInitDbPacket implements MySQLCommandPacket {
    
    @Getter
    private final String schema;
    
    private final BackendConnection backendConnection;
    
    public MySQLComInitDbPacket(final MySQLPacketPayload payload, final BackendConnection backendConnection) {
        schema = payload.readStringEOF();
        this.backendConnection = backendConnection;
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(MySQLCommandPacketType.COM_INIT_DB.getValue());
        payload.writeStringEOF(schema);
    }
    
    @Override
    public Collection<MySQLPacket> execute() {
        log.debug("Schema name received for Sharding-Proxy: {}", schema);
        if (LogicSchemas.getInstance().schemaExists(schema)) {
            backendConnection.setCurrentSchema(schema);
            return Collections.<MySQLPacket>singletonList(new MySQLOKPacket(1));
        }
        return Collections.<MySQLPacket>singletonList(new MySQLErrPacket(1, MySQLServerErrorCode.ER_BAD_DB_ERROR, schema));
    }
    
    @Override
    public int getSequenceId() {
        return 0;
    }
}
