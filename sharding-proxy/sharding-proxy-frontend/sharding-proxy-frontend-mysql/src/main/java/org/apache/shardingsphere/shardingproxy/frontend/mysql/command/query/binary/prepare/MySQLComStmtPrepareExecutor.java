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

package org.apache.shardingsphere.shardingproxy.frontend.mysql.command.query.binary.prepare;

import org.apache.shardingsphere.core.optimize.api.segment.Tables;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.frontend.api.CommandExecutor;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLColumnType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.MySQLBinaryStatementRegistry;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.prepare.MySQLComStmtPrepareOKPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.shardingproxy.transport.packet.DatabasePacket;

import java.util.Collection;
import java.util.LinkedList;

/**
 * COM_STMT_PREPARE command executor for MySQL.
 * 
 * @author zhangyonglun
 * @author zhangliang
 */
public final class MySQLComStmtPrepareExecutor implements CommandExecutor {
    
    private static final MySQLBinaryStatementRegistry PREPARED_STATEMENT_REGISTRY = MySQLBinaryStatementRegistry.getInstance();
    
    private final MySQLComStmtPreparePacket packet;
    
    private final LogicSchema logicSchema;
    
    private final String schemaName;
    
    public MySQLComStmtPrepareExecutor(final MySQLComStmtPreparePacket packet, final BackendConnection backendConnection) {
        this.packet = packet;
        logicSchema = backendConnection.getLogicSchema();
        schemaName = backendConnection.getSchemaName();
    }
    
    @Override
    public Collection<DatabasePacket> execute() {
        Collection<DatabasePacket> result = new LinkedList<>();
        int currentSequenceId = 0;
        SQLStatement sqlStatement = logicSchema.getParseEngine().parse(packet.getSql(), true);
        int parametersCount = sqlStatement.getParametersCount();
        result.add(new MySQLComStmtPrepareOKPacket(++currentSequenceId, PREPARED_STATEMENT_REGISTRY.register(packet.getSql(), parametersCount), getNumColumns(), parametersCount, 0));
        Tables tables = new Tables(sqlStatement);
        for (int i = 0; i < parametersCount; i++) {
            // TODO add column name
            result.add(new MySQLColumnDefinition41Packet(++currentSequenceId, schemaName,
                    tables.isSingleTable() ? tables.getSingleTableName() : "", "", "", "", 100, MySQLColumnType.MYSQL_TYPE_VARCHAR, 0));
        }
        if (parametersCount > 0) {
            result.add(new MySQLEofPacket(++currentSequenceId));
        }
        // TODO add If numColumns > 0
        return result;
    }
    
    // TODO Set columnsCount=0 is a workaround to escape jdbc check for now, there's no issues found during a few tests.
    private int getNumColumns() {
        return 0;
    }
}
