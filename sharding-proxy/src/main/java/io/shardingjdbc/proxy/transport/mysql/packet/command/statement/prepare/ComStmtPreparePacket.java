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

package io.shardingjdbc.proxy.transport.mysql.packet.command.statement.prepare;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.constant.ShardingConstant;
import io.shardingjdbc.core.parsing.SQLParsingEngine;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.proxy.config.ShardingRuleRegistry;
import io.shardingjdbc.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingjdbc.proxy.transport.mysql.constant.ColumnType;
import io.shardingjdbc.proxy.transport.mysql.constant.StatusFlag;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingjdbc.proxy.transport.mysql.packet.command.CommandPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.command.statement.PreparedStatementRegistry;
import io.shardingjdbc.proxy.transport.mysql.packet.command.text.query.ColumnDefinition41Packet;
import io.shardingjdbc.proxy.transport.mysql.packet.generic.EofPacket;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

/**
 * COM_STMT_PREPARE command packet.
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-stmt-prepare.html">COM_STMT_PREPARE</a>
 *
 * @author zhangliang
 */
@Slf4j
public final class ComStmtPreparePacket extends CommandPacket {
    
    private final String sql;
    
    public ComStmtPreparePacket(final int sequenceId, final MySQLPacketPayload mysqlPacketPayload) {
        super(sequenceId);
        sql = mysqlPacketPayload.readStringEOF();
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeStringEOF(sql);
    }
    
    @Override
    public List<DatabaseProtocolPacket> execute() {
        log.debug("COM_STMT_PREPARE received for Sharding-Proxy: {}", sql);
        List<DatabaseProtocolPacket> result = new LinkedList<>();
        int currentSequenceId = 0;
        SQLStatement sqlStatement = new SQLParsingEngine(DatabaseType.MySQL, sql, ShardingRuleRegistry.getInstance().getShardingRule()).parse(true);
        result.add(new ComStmtPrepareOKPacket(++currentSequenceId, PreparedStatementRegistry.getInstance().register(sql), getNumColumns(sqlStatement), sqlStatement.getParametersIndex(), 0));
        for (int i = 0; i < sqlStatement.getParametersIndex(); i++) {
            // TODO add column name
            result.add(new ColumnDefinition41Packet(++currentSequenceId, ShardingConstant.LOGIC_SCHEMA_NAME, 
                    sqlStatement.getTables().isSingleTable() ? sqlStatement.getTables().getSingleTableName() : "", "", "", "", 100, ColumnType.MYSQL_TYPE_VARCHAR, 0));
        }
        if (sqlStatement.getParametersIndex() > 0) {
            result.add(new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
        }
        // TODO add If numColumns > 0
        return result;
    }
    
    private int getNumColumns(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            // TODO select * cannot know items num
            return ((SelectStatement) sqlStatement).getItems().size();
        }
        if (sqlStatement instanceof InsertStatement) {
            return ((InsertStatement) sqlStatement).getColumns().size();
        }
        return 0;
    }
}
