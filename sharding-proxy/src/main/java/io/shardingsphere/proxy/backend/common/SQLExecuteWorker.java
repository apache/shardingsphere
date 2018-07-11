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

package io.shardingsphere.proxy.backend.common;

import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.proxy.transport.mysql.constant.StatusFlag;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;

/**
 * SQL execute worker.
 *
 * @author zhangyonglun
 * @author zhaojun
 */
public final class SQLExecuteWorker extends ExecuteWorker implements Callable<CommandResponsePackets> {
    
    private static final Integer FETCH_ONE_ROW_A_TIME = Integer.MIN_VALUE;
    
    private final Statement statement;
    
    private final String sql;
    
    public SQLExecuteWorker(final SQLExecuteBackendHandler sqlExecuteBackendHandler, final SQLStatement sqlStatement, final Statement statement, final String sql) {
        super(sqlExecuteBackendHandler, sqlStatement);
        this.statement = statement;
        this.sql = sql;
    }
    
    @Override
    protected CommandResponsePackets executeQueryWithStreamResultSet() throws SQLException {
        statement.setFetchSize(FETCH_ONE_ROW_A_TIME);
        ResultSet resultSet = statement.executeQuery(sql);
        getExecuteBackendHandler().getJdbcResource().addResultSet(resultSet);
        return getQueryDatabaseProtocolPackets(resultSet);
    }
    
    @Override
    protected CommandResponsePackets executeQueryWithNonStreamResultSet() throws SQLException {
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
            getExecuteBackendHandler().getResultLists().add(resultList);
            return getQueryDatabaseProtocolPackets(resultSet);
        }
    }
    
    @Override
    protected CommandResponsePackets executeUpdate() throws SQLException {
        int affectedRows;
        long lastInsertId = 0;
        if (getSqlStatement() instanceof InsertStatement) {
            affectedRows = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            lastInsertId = getGeneratedKey(statement);
        } else {
            affectedRows = statement.executeUpdate(sql);
        }
        return new CommandResponsePackets(new OKPacket(1, affectedRows, lastInsertId, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
    }
    
    @Override
    protected CommandResponsePackets executeCommon() throws SQLException {
        boolean hasResultSet = statement.execute(sql);
        if (hasResultSet) {
            return getCommonDatabaseProtocolPackets(statement.getResultSet());
        } else {
            return new CommandResponsePackets(new OKPacket(1, statement.getUpdateCount(), 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
        }
    }
}
