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

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.routing.PreparedStatementRoutingEngine;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.proxy.backend.mysql.MySQLPacketStatementExecuteQueryResult;
import io.shardingsphere.proxy.backend.resource.ProxyJDBCResourceFactory;
import io.shardingsphere.proxy.backend.resource.ProxyPrepareJDBCResource;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.statement.PreparedStatementRegistry;
import io.shardingsphere.proxy.transport.mysql.packet.command.statement.execute.BinaryResultSetRowPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.statement.execute.PreparedStatementParameter;
import lombok.Getter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Statement execute backend handler.
 *
 * @author zhangyonglun
 * @author zhaojun
 */
public final class StatementExecuteBackendHandler extends ExecuteBackendHandler implements BackendHandler {
    
    private final List<PreparedStatementParameter> preparedStatementParameters;
    
    @Getter
    private final List<ColumnType> columnTypes;
    
    public StatementExecuteBackendHandler(final List<PreparedStatementParameter> preparedStatementParameters, final int statementId,
                                          final DatabaseType databaseType, final boolean showSQL) {
        super(PreparedStatementRegistry.getInstance().getSQL(statementId), databaseType, showSQL);
        super.setJdbcResource(ProxyJDBCResourceFactory.newPrepareResource());
        this.preparedStatementParameters = preparedStatementParameters;
        columnTypes = new CopyOnWriteArrayList<>();
    }
    
    @Override
    protected SQLRouteResult doSqlShardingRoute() {
        PreparedStatementRoutingEngine routingEngine = new PreparedStatementRoutingEngine(getSql(),
                RuleRegistry.getInstance().getShardingRule(), RuleRegistry.getInstance().getShardingMetaData(), getDatabaseType(), isShowSQL());
        return routingEngine.route(getComStmtExecuteParameters());
    }
    
    /**
     * Get PreparedStatement Parameter values.
     *
     * @return parameter value list
     */
    public List<Object> getComStmtExecuteParameters() {
        List<Object> result = new ArrayList<>(32);
        for (PreparedStatementParameter each : preparedStatementParameters) {
            result.add(each.getValue());
        }
        return result;
    }
    
    @Override
    protected Callable<CommandResponsePackets> newSubmitTask(final Statement statement, final SQLStatement sqlStatement, final String unitSql) {
        return new StatementExecuteWorker(this, sqlStatement, (PreparedStatement) statement);
    }
    
    @Override
    protected PreparedStatement prepareResource(final String dataSourceName, final String unitSql, final SQLStatement sqlStatement) throws SQLException {
        DataSource dataSource = RuleRegistry.getInstance().getDataSourceMap().get(dataSourceName);
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = sqlStatement instanceof InsertStatement ? connection.prepareStatement(unitSql, Statement.RETURN_GENERATED_KEYS) : connection.prepareStatement(unitSql);
        for (int i = 0; i < preparedStatementParameters.size(); i++) {
            statement.setObject(i + 1, preparedStatementParameters.get(i).getValue());
        }
        ProxyPrepareJDBCResource prepareProxyJDBCResource = (ProxyPrepareJDBCResource) getJdbcResource();
        prepareProxyJDBCResource.addConnection(connection);
        prepareProxyJDBCResource.addPrepareStatement(statement);
        return statement;
    }
    
    @Override
    protected QueryResult newQueryResult(final CommandResponsePackets packet, final int index) {
        MySQLPacketStatementExecuteQueryResult mySQLPacketStatementExecuteQueryResult = new MySQLPacketStatementExecuteQueryResult(packet, columnTypes);
        if (ProxyMode.MEMORY_STRICTLY == ProxyMode.valueOf(RuleRegistry.getInstance().getProxyMode())) {
            mySQLPacketStatementExecuteQueryResult.setResultSet(getJdbcResource().getResultSets().get(index));
        } else {
            mySQLPacketStatementExecuteQueryResult.setResultList(getResultLists().get(index));
        }
        return mySQLPacketStatementExecuteQueryResult;
    }
    
    @Override
    protected DatabaseProtocolPacket newDatabaseProtocolPacket(final int sequenceId, final List<Object> data) {
        return new BinaryResultSetRowPacket(sequenceId, getColumnCount(), data, columnTypes);
    }
}
