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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.fieldlist;

import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.response.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.shardingproxy.transport.api.packet.CommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.CommandPacketExecutor;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLColumnType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacketFactory;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * MySQL COM_FIELD_LIST packet executor.
 *
 * @author zhangliang 
 */
public final class MySQLComFieldListPacketExecutor implements CommandPacketExecutor<MySQLPacket> {
    
    private static final String SQL = "SHOW COLUMNS FROM %s FROM %s";
    
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Override
    public Collection<MySQLPacket> execute(final BackendConnection backendConnection, final CommandPacket commandPacket) throws SQLException {
        MySQLComFieldListPacket comFieldListPacket = (MySQLComFieldListPacket) commandPacket;
        databaseCommunicationEngine = DatabaseCommunicationEngineFactory.getInstance().newTextProtocolInstance(
                backendConnection.getLogicSchema(), getShowColumnsSQL(comFieldListPacket), backendConnection);
        BackendResponse backendResponse = databaseCommunicationEngine.execute();
        return backendResponse instanceof ErrorResponse
                ? Collections.<MySQLPacket>singletonList(MySQLErrPacketFactory.newInstance(1, ((ErrorResponse) backendResponse).getCause())) : getColumnDefinition41Packets(comFieldListPacket);
    }
    
    private String getShowColumnsSQL(final MySQLComFieldListPacket comFieldListPacket) {
        return String.format(SQL, comFieldListPacket.getTable(), comFieldListPacket.getSchemaName());
    }
    
    private Collection<MySQLPacket> getColumnDefinition41Packets(final MySQLComFieldListPacket comFieldListPacket) throws SQLException {
        Collection<MySQLPacket> result = new LinkedList<>();
        int currentSequenceId = 0;
        while (databaseCommunicationEngine.next()) {
            String columnName = databaseCommunicationEngine.getQueryData().getData().get(0).toString();
            result.add(new MySQLColumnDefinition41Packet(++currentSequenceId, 
                    comFieldListPacket.getSchemaName(), comFieldListPacket.getTable(), comFieldListPacket.getTable(), columnName, columnName, 100, MySQLColumnType.MYSQL_TYPE_VARCHAR, 0));
        }
        result.add(new MySQLEofPacket(++currentSequenceId));
        return result;
    }
}
