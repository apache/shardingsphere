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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.datasource.registry.GlobalDataSourceRegistry;
import org.apache.shardingsphere.infra.exception.OverallConnectionNotEnoughException;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.proxy.backend.communication.BackendDataSource;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.spi.ShardingSphereTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Backend data source of JDBC.
 */
public final class JDBCBackendDataSource implements BackendDataSource {
    
    /**
     * Get connections.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @param connectionSize size of connections to get
     * @param connectionMode connection mode
     * @return connections
     * @throws SQLException SQL exception
     */
    public List<Connection> getConnections(final String databaseName, final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        return getConnections(databaseName, dataSourceName, connectionSize, connectionMode,
                ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TransactionRule.class).getDefaultType());
    }
    
    /**
     * Get connections.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @param connectionSize size of connections to be get
     * @param connectionMode connection mode
     * @param transactionType transaction type
     * @return connections
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public List<Connection> getConnections(final String databaseName, final String dataSourceName,
                                           final int connectionSize, final ConnectionMode connectionMode, final TransactionType transactionType) throws SQLException {
        DataSource dataSource = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getResource().getDataSources().get(dataSourceName);
        if (dataSourceName.contains(".")) {
            String dataSourceStr = dataSourceName.split("\\.")[0];
            if (GlobalDataSourceRegistry.getInstance().getCachedDataSourceDataSources().containsKey(dataSourceStr)) {
                dataSource = GlobalDataSourceRegistry.getInstance().getCachedDataSourceDataSources().get(dataSourceStr);
            }
        }
        Preconditions.checkNotNull(dataSource, "Can not get connection from datasource %s.", dataSourceName);
        if (1 == connectionSize) {
            return Collections.singletonList(createConnection(databaseName, dataSourceName, dataSource, transactionType));
        }
        if (ConnectionMode.CONNECTION_STRICTLY == connectionMode) {
            return createConnections(databaseName, dataSourceName, dataSource, connectionSize, transactionType);
        }
        synchronized (dataSource) {
            return createConnections(databaseName, dataSourceName, dataSource, connectionSize, transactionType);
        }
    }
    
    private List<Connection> createConnections(final String databaseName, final String dataSourceName,
                                               final DataSource dataSource, final int connectionSize, final TransactionType transactionType) throws SQLException {
        List<Connection> result = new ArrayList<>(connectionSize);
        for (int i = 0; i < connectionSize; i++) {
            try {
                result.add(createConnection(databaseName, dataSourceName, dataSource, transactionType));
            } catch (final SQLException ex) {
                for (Connection each : result) {
                    each.close();
                }
                throw new OverallConnectionNotEnoughException(connectionSize, result.size());
            }
        }
        return result;
    }
    
    private Connection createConnection(final String databaseName, final String dataSourceName, final DataSource dataSource, final TransactionType transactionType) throws SQLException {
        TransactionRule transactionRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TransactionRule.class);
        ShardingSphereTransactionManager transactionManager = transactionRule.getResource().getTransactionManager(transactionType);
        Connection result = isInTransaction(transactionManager) ? transactionManager.getConnection(databaseName, dataSourceName) : dataSource.getConnection();
        if (dataSourceName.contains(".")) {
            String catalog = dataSourceName.split("\\.")[1];
            result.setCatalog(catalog);
        }
        return result;
    }
    
    private boolean isInTransaction(final ShardingSphereTransactionManager transactionManager) {
        return null != transactionManager && transactionManager.isInTransaction();
    }
}
