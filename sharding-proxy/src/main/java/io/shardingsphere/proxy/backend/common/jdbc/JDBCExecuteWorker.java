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

package io.shardingsphere.proxy.backend.common.jdbc;

import io.shardingsphere.core.routing.router.masterslave.MasterVisitedManager;
import io.shardingsphere.proxy.backend.common.ProxyMode;
import io.shardingsphere.proxy.backend.common.ResultList;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.ColumnDefinition41Packet;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.FieldCountPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;

/**
 * Execute worker via JDBC to connect databases.
 * 
 * @author zhaojun
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class JDBCExecuteWorker implements Callable<CommandResponsePackets> {
    
    private static final Integer FETCH_ONE_ROW_A_TIME = Integer.MIN_VALUE;
    
    private final Statement statement;
    
    private final boolean isReturnGeneratedKeys;
    
    private final JDBCResourceManager jdbcResourceManager;
    
    @Getter
    private final JDBCBackendHandler jdbcBackendHandler;
    
    @Override
    public CommandResponsePackets call() {
        try {
            return execute();
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex));
        } finally {
            // TODO confirm here, maybe can remove
            MasterVisitedManager.clear();
        }
    }
    
    private CommandResponsePackets execute() throws SQLException {
        if (ProxyMode.MEMORY_STRICTLY == RuleRegistry.getInstance().getProxyMode()) {
            statement.setFetchSize(FETCH_ONE_ROW_A_TIME);
        }
        if (executeSQL()) {
            ResultSet resultSet = statement.getResultSet();
            if (ProxyMode.MEMORY_STRICTLY == RuleRegistry.getInstance().getProxyMode()) {
                jdbcResourceManager.addResultSet(resultSet);
            } else {
                ResultList resultList = new ResultList();
                while (resultSet.next()) {
                    for (int columnIndex = 1; columnIndex <= resultSet.getMetaData().getColumnCount(); columnIndex++) {
                        resultList.add(resultSet.getObject(columnIndex));
                    }
                }
                resultList.setIterator(resultList.getResultList().iterator());
                getJdbcBackendHandler().getResultLists().add(resultList);
            }
            return getHeaderPackets(resultSet.getMetaData());
        } else {
            return new CommandResponsePackets(new OKPacket(1, statement.getUpdateCount(), isReturnGeneratedKeys ? getGeneratedKey() : 0));
        }
    }
    
    protected abstract boolean executeSQL() throws SQLException;
    
    private CommandResponsePackets getHeaderPackets(final ResultSetMetaData resultSetMetaData) throws SQLException {
        int currentSequenceId = 0;
        int columnCount = resultSetMetaData.getColumnCount();
        jdbcBackendHandler.setColumnCount(columnCount);
        if (0 == columnCount) {
            return new CommandResponsePackets(new OKPacket(++currentSequenceId));
        }
        CommandResponsePackets result = new CommandResponsePackets(new FieldCountPacket(++currentSequenceId, columnCount));
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            setColumnType(ColumnType.valueOfJDBCType(resultSetMetaData.getColumnType(columnIndex)));
            result.addPacket(new ColumnDefinition41Packet(++currentSequenceId, resultSetMetaData, columnIndex));
        }
        result.addPacket(new EofPacket(++currentSequenceId));
        return result;
    }
    
    // TODO why only prepareStatement need this?
    protected void setColumnType(final ColumnType columnType) {
    }
    
    private long getGeneratedKey() throws SQLException {
        ResultSet resultSet = statement.getGeneratedKeys();
        return resultSet.next() ? resultSet.getLong(1) : 0L;
    }
}
