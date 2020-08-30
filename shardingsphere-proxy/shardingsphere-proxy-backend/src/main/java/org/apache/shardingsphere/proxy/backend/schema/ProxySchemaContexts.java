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

package org.apache.shardingsphere.proxy.backend.schema;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.SchemaContexts;
import org.apache.shardingsphere.kernel.context.impl.StandardSchemaContexts;
import org.apache.shardingsphere.proxy.backend.BackendDataSource;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.context.impl.StandardTransactionContexts;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.spi.ShardingTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Proxy schema contexts.
 */
@Getter
public final class ProxySchemaContexts {
    
    private static final ProxySchemaContexts INSTANCE = new ProxySchemaContexts();
    
    private SchemaContexts schemaContexts = new StandardSchemaContexts();
    
    private TransactionContexts transactionContexts = new StandardTransactionContexts();
    
    private final JDBCBackendDataSource backendDataSource = new JDBCBackendDataSource();
    
    private ProxySchemaContexts() { }
    
    /**
     * Get instance of proxy schema schemas.
     *
     * @return instance of ShardingSphere schemas.
     */
    public static ProxySchemaContexts getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize proxy schema contexts.
     *
     * @param schemaContexts schema contexts
     * @param transactionContexts transaction manager engine contexts
     */
    public void init(final SchemaContexts schemaContexts, final TransactionContexts transactionContexts) {
        this.schemaContexts = schemaContexts;
        this.transactionContexts = transactionContexts;
    }
    
    /**
     * Check schema exists.
     *
     * @param schema schema
     * @return schema exists or not
     */
    public boolean schemaExists(final String schema) {
        return null != schemaContexts && schemaContexts.getSchemaContexts().containsKey(schema);
    }
    
    /**
     * Get ShardingSphere schema.
     *
     * @param schemaName schema name
     * @return ShardingSphere schema
     */
    public SchemaContext getSchema(final String schemaName) {
        return Strings.isNullOrEmpty(schemaName) ? null : schemaContexts.getSchemaContexts().get(schemaName);
    }
    
    /**
     * Get schema names.
     *
     * @return schema names
     */
    public List<String> getSchemaNames() {
        return new LinkedList<>(schemaContexts.getSchemaContexts().keySet());
    }
    
    /**
     * Get data source sample.
     * 
     * @return data source sample
     */
    public Optional<DataSource> getDataSourceSample() {
        List<String> schemaNames = getSchemaNames();
        if (schemaNames.isEmpty()) {
            return Optional.empty();
        }
        Map<String, DataSource> dataSources = Objects.requireNonNull(getSchema(schemaNames.get(0))).getSchema().getDataSources();
        return dataSources.values().stream().findFirst();
    }
    
    public final class JDBCBackendDataSource implements BackendDataSource {
        
        /**
         * Get connection.
         *
         * @param schemaName scheme name
         * @param dataSourceName data source name
         * @return connection
         * @throws SQLException SQL exception
         */
        public Connection getConnection(final String schemaName, final String dataSourceName) throws SQLException {
            return getConnections(schemaName, dataSourceName, 1, ConnectionMode.MEMORY_STRICTLY).get(0);
        }
        
        /**
         * Get connections.
         *
         * @param schemaName scheme name
         * @param dataSourceName data source name
         * @param connectionSize size of connections to get
         * @param connectionMode connection mode
         * @return connections
         * @throws SQLException SQL exception
         */
        public List<Connection> getConnections(final String schemaName, final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
            return getConnections(schemaName, dataSourceName, connectionSize, connectionMode, TransactionType.LOCAL);
        }
        
        /**
         * Get connections.
         *
         * @param schemaName scheme name
         * @param dataSourceName data source name
         * @param connectionSize size of connections to be get
         * @param connectionMode connection mode
         * @param transactionType transaction type
         * @return connections
         * @throws SQLException SQL exception
         */
        @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
        public List<Connection> getConnections(final String schemaName, final String dataSourceName, 
                                               final int connectionSize, final ConnectionMode connectionMode, final TransactionType transactionType) throws SQLException {
            DataSource dataSource = schemaContexts.getSchemaContexts().get(schemaName).getSchema().getDataSources().get(dataSourceName);
            Preconditions.checkNotNull(dataSource, "Can not get connection from datasource %s.", dataSourceName);
            if (1 == connectionSize) {
                return Collections.singletonList(createConnection(schemaName, dataSourceName, dataSource, transactionType));
            }
            if (ConnectionMode.CONNECTION_STRICTLY == connectionMode) {
                return createConnections(schemaName, dataSourceName, dataSource, connectionSize, transactionType);
            }
            synchronized (dataSource) {
                return createConnections(schemaName, dataSourceName, dataSource, connectionSize, transactionType);
            }
        }
        
        private List<Connection> createConnections(final String schemaName, final String dataSourceName, 
                                                   final DataSource dataSource, final int connectionSize, final TransactionType transactionType) throws SQLException {
            List<Connection> result = new ArrayList<>(connectionSize);
            for (int i = 0; i < connectionSize; i++) {
                try {
                    result.add(createConnection(schemaName, dataSourceName, dataSource, transactionType));
                } catch (final SQLException ex) {
                    for (Connection each : result) {
                        each.close();
                    }
                    throw new SQLException(String.format("Could't get %d connections one time, partition succeed connection(%d) have released!", connectionSize, result.size()), ex);
                }
            }
            return result;
        }
        
        private Connection createConnection(final String schemaName, final String dataSourceName, final DataSource dataSource, final TransactionType transactionType) throws SQLException {
            ShardingTransactionManager shardingTransactionManager = transactionContexts.getEngines().get(schemaName).getTransactionManager(transactionType);
            return isInShardingTransaction(shardingTransactionManager) ? shardingTransactionManager.getConnection(dataSourceName) : dataSource.getConnection();
        }
        
        private boolean isInShardingTransaction(final ShardingTransactionManager shardingTransactionManager) {
            return null != shardingTransactionManager && shardingTransactionManager.isInTransaction();
        }
    }
}
