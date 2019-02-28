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

package org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.parse;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parsing.SQLParsingEngine;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.MasterSlaveSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.ShardingSchema;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.constant.PostgreSQLColumnType;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.PostgreSQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.BinaryStatementRegistry;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.ConnectionScopeBinaryStatementRegistry;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.PostgreSQLBinaryStatementParameterType;

import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL command parse packet.
 *
 * @author zhangyonglun
 */
@Slf4j
public final class PostgreSQLComParsePacket implements PostgreSQLCommandPacket {
    
    @Getter
    private final char messageType = PostgreSQLCommandPacketType.PARSE.getValue();
    
    private final ConnectionScopeBinaryStatementRegistry postgreSQLBinaryStatementRegistry;
    
    private String statementId;
    
    private final String sql;
    
    private final List<PostgreSQLBinaryStatementParameterType> postgreSQLBinaryStatementParameterTypes = new ArrayList<>(64);
    
    private SQLParsingEngine sqlParsingEngine;
    
    public PostgreSQLComParsePacket(final PostgreSQLPacketPayload payload, final BackendConnection backendConnection) {
        postgreSQLBinaryStatementRegistry = BinaryStatementRegistry.getInstance().get(backendConnection);
        payload.readInt4();
        statementId = payload.readStringNul();
        sql = alterSQLToJDBCStyle(payload.readStringNul());
        if (!sql.isEmpty()) {
            getParameterTypes(payload);
            LogicSchema logicSchema = backendConnection.getLogicSchema();
            // TODO we should use none-sharding parsing engine in future.
            sqlParsingEngine = new SQLParsingEngine(DatabaseType.PostgreSQL, sql, getShardingRule(logicSchema), logicSchema.getMetaData().getTable());
        }
    }
    
    private void getParameterTypes(final PostgreSQLPacketPayload payload) {
        int parameterCount = payload.readInt2();
        for (int i = 0; i < parameterCount; i++) {
            postgreSQLBinaryStatementParameterTypes.add(new PostgreSQLBinaryStatementParameterType(PostgreSQLColumnType.valueOf(payload.readInt4())));
        }
    }
    
    private String alterSQLToJDBCStyle(final String sql) {
        return sql.replaceAll("\\$[0-9]+", "?");
    }
    
    private ShardingRule getShardingRule(final LogicSchema logicSchema) {
        return logicSchema instanceof MasterSlaveSchema ? ((MasterSlaveSchema) logicSchema).getDefaultShardingRule() : ((ShardingSchema) logicSchema).getShardingRule();
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
    }
    
    @Override
    public Optional<CommandResponsePackets> execute() {
        log.debug("PostgreSQLComParsePacket received for Sharding-Proxy: {}", sql);
        if (!sql.isEmpty()) {
            SQLStatement sqlStatement = sqlParsingEngine.parse(true);
            int parametersIndex = sqlStatement.getParametersIndex();
            postgreSQLBinaryStatementRegistry.register(statementId, sql, parametersIndex, postgreSQLBinaryStatementParameterTypes);
        }
        return Optional.of(new CommandResponsePackets(new PostgreSQLParseCompletePacket()));
    }
}
