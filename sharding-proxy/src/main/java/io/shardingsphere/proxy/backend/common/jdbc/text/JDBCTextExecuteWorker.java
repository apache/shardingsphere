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

package io.shardingsphere.proxy.backend.common.jdbc.text;

import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.proxy.backend.common.ResultList;
import io.shardingsphere.proxy.backend.common.jdbc.JDBCExecuteWorker;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQL execute worker.
 *
 * @author zhangyonglun
 * @author zhaojun
 */
public final class JDBCTextExecuteWorker extends JDBCExecuteWorker {
    
    private static final Integer FETCH_ONE_ROW_A_TIME = Integer.MIN_VALUE;
    
    private final Statement statement;
    
    private final String sql;
    
    private final boolean isReturnGeneratedKeys;
    
    public JDBCTextExecuteWorker(final JDBCTextBackendHandler jdbcTextBackendHandler, final SQLType sqlType, final boolean isReturnGeneratedKeys, final Statement statement, final String sql) {
        super(jdbcTextBackendHandler, sqlType);
        this.statement = statement;
        this.sql = sql;
        this.isReturnGeneratedKeys = isReturnGeneratedKeys;
    }
    
    @Override
    protected CommandResponsePackets executeQueryWithStreamResultSet() throws SQLException {
        statement.setFetchSize(FETCH_ONE_ROW_A_TIME);
        ResultSet resultSet = statement.executeQuery(sql);
        getJdbcBackendHandler().getJdbcResourceManager().addResultSet(resultSet);
        return getQueryDatabaseProtocolPackets(resultSet);
    }
    
    @Override
    protected CommandResponsePackets executeQueryWithMemoryResultSet() throws SQLException {
        try (
                ResultSet resultSet = statement.executeQuery(sql)
        ) {
            ResultList resultList = new ResultList();
            while (resultSet.next()) {
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    resultList.add(resultSet.getObject(i));
                }
            }
            resultList.setIterator(resultList.getResultList().iterator());
            getJdbcBackendHandler().getResultLists().add(resultList);
            return getQueryDatabaseProtocolPackets(resultSet);
        }
    }
    
    @Override
    protected CommandResponsePackets executeUpdate() throws SQLException {
        int affectedRows;
        long lastInsertId = 0;
        if (isReturnGeneratedKeys) {
            affectedRows = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            lastInsertId = getGeneratedKey(statement);
        } else {
            affectedRows = statement.executeUpdate(sql);
        }
        return new CommandResponsePackets(new OKPacket(1, affectedRows, lastInsertId));
    }
    
    @Override
    protected CommandResponsePackets executeCommon() throws SQLException {
        boolean hasResultSet = statement.execute(sql);
        if (hasResultSet) {
            return getCommonDatabaseProtocolPackets(statement.getResultSet());
        } else {
            return new CommandResponsePackets(new OKPacket(1, statement.getUpdateCount(), 0));
        }
    }
}
