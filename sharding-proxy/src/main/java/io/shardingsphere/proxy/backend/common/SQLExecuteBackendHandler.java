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
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.StatementRoutingEngine;
import io.shardingsphere.proxy.backend.mysql.MySQLPacketQueryResult;
import io.shardingsphere.proxy.backend.resource.ProxyJDBCResource;
import io.shardingsphere.proxy.backend.resource.ProxyJDBCResourceFactory;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.TextResultSetRowPacket;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * SQL execute backend handler.
 *
 * @author zhangliang
 * @author panjuan
 * @author zhaojun
 */
public final class SQLExecuteBackendHandler extends ExecuteBackendHandler implements BackendHandler {
    
    public SQLExecuteBackendHandler(final String sql, final DatabaseType databaseType, final boolean showSQL) {
        super(sql, databaseType, showSQL);
        super.setJdbcResource(ProxyJDBCResourceFactory.newResource());
    }
    
    @Override
    protected SQLRouteResult doSqlShardingRoute() {
        StatementRoutingEngine routingEngine = new StatementRoutingEngine(RuleRegistry.getInstance().getShardingRule(),
                RuleRegistry.getInstance().getShardingMetaData(), getDatabaseType(), isShowSQL());
        return routingEngine.route(getSql());
    }
    
    @Override
    protected Statement prepareResource(final String dataSourceName, final String unitSql, final SQLStatement sqlStatement) throws SQLException {
        DataSource dataSource = RuleRegistry.getInstance().getDataSourceMap().get(dataSourceName);
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ProxyJDBCResource proxyJDBCResource = (ProxyJDBCResource) getJdbcResource();
        proxyJDBCResource.addConnection(connection);
        proxyJDBCResource.addStatement(statement);
        return statement;
    }
    
    @Override
    protected Callable<CommandResponsePackets> newSubmitTask(final Statement statement, final SQLStatement sqlStatement, final String unitSql) {
        return new SQLExecuteWorker(this, sqlStatement, statement, unitSql);
    }
    
    @Override
    protected QueryResult newQueryResult(final CommandResponsePackets packet, final int index) {
        MySQLPacketQueryResult mySQLPacketQueryResult = new MySQLPacketQueryResult(packet);
        if (ProxyMode.MEMORY_STRICTLY == ProxyMode.valueOf(RuleRegistry.getInstance().getProxyMode())) {
            mySQLPacketQueryResult.setResultSet(getJdbcResource().getResultSets().get(index));
        } else {
            mySQLPacketQueryResult.setResultList(getResultLists().get(index));
        }
        return mySQLPacketQueryResult;
    }
    
    @Override
    protected DatabaseProtocolPacket newDatabaseProtocolPacket(final int sequenceId, final List<Object> data) {
        return new TextResultSetRowPacket(sequenceId, data);
    }
}
