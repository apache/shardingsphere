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

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.StatementRoutingEngine;
import io.shardingsphere.proxy.backend.common.ProxyMode;
import io.shardingsphere.proxy.backend.common.jdbc.JDBCBackendHandler;
import io.shardingsphere.proxy.backend.mysql.MySQLPacketQueryResult;
import io.shardingsphere.proxy.backend.resource.ProxyJDBCResource;
import io.shardingsphere.proxy.backend.resource.ProxyJDBCResourceFactory;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.TextResultSetRowPacket;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Text protocol backend handler via JDBC to connect databases.
 *
 * @author zhangliang
 * @author panjuan
 * @author zhaojun
 */
public final class JDBCTextBackendHandler extends JDBCBackendHandler {
   
    private final DatabaseType databaseType;
    
    private final RuleRegistry ruleRegistry;
    
    public JDBCTextBackendHandler(final String sql, final DatabaseType databaseType) {
        super(sql, ProxyJDBCResourceFactory.newResource());
        this.databaseType = databaseType;
        ruleRegistry = RuleRegistry.getInstance();
    }
    
    @Override
    protected SQLRouteResult doShardingRoute() {
        StatementRoutingEngine routingEngine = new StatementRoutingEngine(
                ruleRegistry.getShardingRule(), ruleRegistry.getShardingMetaData(), databaseType, ruleRegistry.isShowSQL(), ruleRegistry.getShardingDataSourceMetaData());
        return routingEngine.route(getSql());
    }
    
    @Override
    protected Statement prepareResource(final Connection connection, final String actualSQL, final SQLStatement sqlStatement) throws SQLException {
        Statement result = connection.createStatement();
        ProxyJDBCResource proxyJDBCResource = (ProxyJDBCResource) getJdbcResource();
        proxyJDBCResource.addConnection(connection);
        proxyJDBCResource.addStatement(result);
        return result;
    }
    
    @Override
    protected Callable<CommandResponsePackets> newSubmitTask(final Statement statement, final SQLStatement sqlStatement, final String unitSQL) {
        return new JDBCTextExecuteWorker(this, sqlStatement, statement, unitSQL);
    }
    
    @Override
    protected QueryResult newQueryResult(final CommandResponsePackets packet, final int index) {
        MySQLPacketQueryResult result = new MySQLPacketQueryResult(packet);
        if (ProxyMode.MEMORY_STRICTLY == ruleRegistry.getProxyMode()) {
            result.setResultSet(getJdbcResource().getResultSets().get(index));
        } else {
            result.setResultList(getResultLists().get(index));
        }
        return result;
    }
    
    @Override
    protected DatabaseProtocolPacket newDatabaseProtocolPacket(final int sequenceId, final List<Object> data) {
        return new TextResultSetRowPacket(sequenceId, data);
    }
}
