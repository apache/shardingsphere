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
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.StatementRoutingEngine;
import io.shardingsphere.proxy.backend.common.ProxyMode;
import io.shardingsphere.proxy.backend.common.jdbc.JDBCBackendHandler;
import io.shardingsphere.proxy.backend.mysql.MySQLPacketQueryResult;
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
        super(sql);
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
    protected Statement createStatement(final Connection connection, final String actualSQL, final boolean isReturnGeneratedKeys) throws SQLException {
        return connection.createStatement();
    }
    
    @Override
    protected Callable<CommandResponsePackets> createExecuteWorker(final Statement statement, final boolean isReturnGeneratedKeys, final String actualSQL) {
        return new JDBCTextExecuteWorker(actualSQL, statement, isReturnGeneratedKeys, getJdbcResourceManager(), this);
    }
    
    @Override
    protected QueryResult newQueryResult(final CommandResponsePackets packet, final int index) {
        MySQLPacketQueryResult result = new MySQLPacketQueryResult(packet);
        if (ProxyMode.MEMORY_STRICTLY == ruleRegistry.getProxyMode()) {
            result.setResultSet(getJdbcResourceManager().getResultSets().get(index));
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
