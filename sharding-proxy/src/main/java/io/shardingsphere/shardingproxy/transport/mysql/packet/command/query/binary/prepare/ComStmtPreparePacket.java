/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.prepare;

import com.google.common.base.Optional;
import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.SQLParsingEngine;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import io.shardingsphere.shardingproxy.runtime.schema.ShardingSchema;
import io.shardingsphere.shardingproxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.ColumnDefinition41Packet;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.BinaryStatementRegistry;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.EofPacket;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * COM_STMT_PREPARE command packet.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-stmt-prepare.html">COM_STMT_PREPARE</a>
 *
 * @author zhangliang
 */
@Slf4j
public final class ComStmtPreparePacket implements CommandPacket {
    
    private static final BinaryStatementRegistry PREPARED_STATEMENT_REGISTRY = BinaryStatementRegistry.getInstance();
    
    @Getter
    private final int sequenceId;
    
    private final String currentSchema;
    
    private final String sql;
    
    private final SQLParsingEngine sqlParsingEngine;
    
    public ComStmtPreparePacket(final int sequenceId, final String currentSchema, final MySQLPacketPayload payload) {
        this.sequenceId = sequenceId;
        this.currentSchema = currentSchema;
        sql = payload.readStringEOF();
        LogicSchema logicSchema = GlobalRegistry.getInstance().getLogicSchema(currentSchema);
        sqlParsingEngine = new SQLParsingEngine(DatabaseType.MySQL, sql, getShardingRule(logicSchema), logicSchema.getMetaData().getTable());
    }
    
    private ShardingRule getShardingRule(final LogicSchema logicSchema) {
        return logicSchema instanceof ShardingSchema ? ((ShardingSchema) logicSchema).getShardingRule() : new ShardingRule(new ShardingRuleConfiguration(), logicSchema.getDataSources().keySet());
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeStringEOF(sql);
    }
    
    @Override
    public Optional<CommandResponsePackets> execute() {
        log.debug("COM_STMT_PREPARE received for Sharding-Proxy: {}", sql);
        int currentSequenceId = 0;
        SQLStatement sqlStatement = sqlParsingEngine.parse(true);
        int parametersIndex = sqlStatement.getParametersIndex();
        CommandResponsePackets result = new CommandResponsePackets(
                new ComStmtPrepareOKPacket(++currentSequenceId, PREPARED_STATEMENT_REGISTRY.register(sql, parametersIndex), getNumColumns(sqlStatement), parametersIndex, 0));
        for (int i = 0; i < parametersIndex; i++) {
            // TODO add column name
            result.getPackets().add(new ColumnDefinition41Packet(++currentSequenceId, currentSchema,
                    sqlStatement.getTables().isSingleTable() ? sqlStatement.getTables().getSingleTableName() : "", "", "", "", 100, ColumnType.MYSQL_TYPE_VARCHAR, 0));
        }
        if (parametersIndex > 0) {
            result.getPackets().add(new EofPacket(++currentSequenceId));
        }
        // TODO add If numColumns > 0
        return Optional.of(result);
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
