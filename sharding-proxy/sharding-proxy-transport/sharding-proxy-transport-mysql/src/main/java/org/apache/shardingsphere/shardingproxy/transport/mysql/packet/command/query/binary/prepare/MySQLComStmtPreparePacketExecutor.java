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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.prepare;

import org.apache.shardingsphere.core.parsing.SQLParsingEngine;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchemas;
import org.apache.shardingsphere.shardingproxy.backend.schema.MasterSlaveSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.ShardingSchema;
import org.apache.shardingsphere.shardingproxy.transport.api.packet.CommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.CommandPacketExecutor;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLColumnType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.MySQLBinaryStatementRegistry;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLEofPacket;

import java.util.Collection;
import java.util.LinkedList;

/**
 * COM_STMT_PREPARE command packet executor for MySQL.
 * 
 * @author zhangliang
 */
public final class MySQLComStmtPreparePacketExecutor implements CommandPacketExecutor<MySQLPacket> {
    
    private static final MySQLBinaryStatementRegistry PREPARED_STATEMENT_REGISTRY = MySQLBinaryStatementRegistry.getInstance();
    
    @Override
    public Collection<MySQLPacket> execute(final BackendConnection backendConnection, final CommandPacket commandPacket) {
        MySQLComStmtPreparePacket comStmtPreparePacket = (MySQLComStmtPreparePacket) commandPacket;
        LogicSchema logicSchema = backendConnection.getLogicSchema();
        // TODO we should use none-sharding parsing engine in future.
        SQLParsingEngine sqlParsingEngine = new SQLParsingEngine(
                LogicSchemas.getInstance().getDatabaseType(), comStmtPreparePacket.getSql(), getShardingRule(logicSchema), logicSchema.getMetaData().getTable());
        Collection<MySQLPacket> result = new LinkedList<>();
        int currentSequenceId = 0;
        SQLStatement sqlStatement = sqlParsingEngine.parse(true);
        int parametersIndex = sqlStatement.getParametersIndex();
        result.add(new MySQLComStmtPrepareOKPacket(
                ++currentSequenceId, PREPARED_STATEMENT_REGISTRY.register(comStmtPreparePacket.getSql(), parametersIndex), getNumColumns(sqlStatement), parametersIndex, 0));
        for (int i = 0; i < parametersIndex; i++) {
            // TODO add column name
            result.add(new MySQLColumnDefinition41Packet(++currentSequenceId, backendConnection.getSchemaName(),
                    sqlStatement.getTables().isSingleTable() ? sqlStatement.getTables().getSingleTableName() : "", "", "", "", 100, MySQLColumnType.MYSQL_TYPE_VARCHAR, 0));
        }
        if (parametersIndex > 0) {
            result.add(new MySQLEofPacket(++currentSequenceId));
        }
        // TODO add If numColumns > 0
        return result;
    }
    
    private ShardingRule getShardingRule(final LogicSchema logicSchema) {
        return logicSchema instanceof MasterSlaveSchema ? ((MasterSlaveSchema) logicSchema).getDefaultShardingRule() : ((ShardingSchema) logicSchema).getShardingRule();
    }
    
    private int getNumColumns(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return ((SelectStatement) sqlStatement).getItems().size();
        }
        if (sqlStatement instanceof InsertStatement) {
            return ((InsertStatement) sqlStatement).getColumns().size();
        }
        return 0;
    }
}
