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

package org.apache.shardingsphere.proxy.backend.connector.jdbc.datasource;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.database.connector.core.GlobalDataSourceRegistry;
import org.apache.shardingsphere.infra.exception.kernel.connection.OverallConnectionNotEnoughException;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.BackendDataSource;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.spi.ShardingSphereDistributedTransactionManager;

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
    
    @Override
    public List<Connection> getConnections(final String databaseName, final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        DataSource dataSource = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData()
                .getDatabase(databaseName).getResourceMetaData().getStorageUnits().get(dataSourceName).getDataSource();
        if (dataSourceName.contains(".")) {
            String dataSourceStr = dataSourceName.split("\\.")[0];
            if (GlobalDataSourceRegistry.getInstance().getCachedDataSources().containsKey(dataSourceStr)) {
                dataSource = GlobalDataSourceRegistry.getInstance().getCachedDataSources().get(dataSourceStr);
            }
        }
        Preconditions.checkNotNull(dataSource, "Can not get connection from datasource %s.", dataSourceName);
        if (1 == connectionSize) {
            return Collections.singletonList(createConnection(databaseName, dataSourceName, dataSource));
        }
        if (ConnectionMode.CONNECTION_STRICTLY == connectionMode) {
            return createConnections(databaseName, dataSourceName, dataSource, connectionSize);
        }
        synchronized (dataSource) {
            return createConnections(databaseName, dataSourceName, dataSource, connectionSize);
        }
    }
    
    private List<Connection> createConnections(final String databaseName, final String dataSourceName,
                                               final DataSource dataSource, final int connectionSize) throws SQLException {
        List<Connection> result = new ArrayList<>(connectionSize);
        for (int i = 0; i < connectionSize; i++) {
            try {
                result.add(createConnection(databaseName, dataSourceName, dataSource));
            } catch (final SQLException ex) {
                for (Connection each : result) {
                    each.close();
                }
                throw new OverallConnectionNotEnoughException(connectionSize, result.size(), ex);
            }
        }
        return result;
    }
    
    private Connection createConnection(final String databaseName, final String dataSourceName, final DataSource dataSource) throws SQLException {
        TransactionRule transactionRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TransactionRule.class);
        ShardingSphereDistributedTransactionManager distributedTransactionManager = transactionRule.getResource().getTransactionManager(transactionRule.getDefaultType());
        Connection result = isInDistributedTransaction(distributedTransactionManager) ? distributedTransactionManager.getConnection(databaseName, dataSourceName) : dataSource.getConnection();
        if (dataSourceName.contains(".")) {
            String catalog = dataSourceName.split("\\.")[1];
            result.setCatalog(catalog);
        }
        return result;
    }
    
    private boolean isInDistributedTransaction(final ShardingSphereDistributedTransactionManager distributedTransactionManager) {
        return null != distributedTransactionManager && distributedTransactionManager.isInTransaction();
    }
}
