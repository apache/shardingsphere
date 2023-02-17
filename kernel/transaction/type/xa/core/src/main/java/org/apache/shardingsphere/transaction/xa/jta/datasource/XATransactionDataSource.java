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

package org.apache.shardingsphere.transaction.xa.jta.datasource;

import org.apache.shardingsphere.infra.context.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.util.reflection.ReflectionUtil;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.transaction.xa.jta.connection.XAConnectionWrapper;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.XADataSourceDefinition;
import org.apache.shardingsphere.transaction.xa.jta.datasource.swapper.DataSourceSwapper;
import org.apache.shardingsphere.transaction.xa.spi.SingleXAResource;
import org.apache.shardingsphere.transaction.xa.spi.XATransactionManagerProvider;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * XA transaction data source.
 */
public final class XATransactionDataSource implements AutoCloseable {
    
    private static final Set<String> CONTAINER_DATASOURCE_NAMES = new HashSet<>(Arrays.asList("AtomikosDataSourceBean", "BasicManagedDataSource"));
    
    private final DatabaseType databaseType;
    
    private final String resourceName;
    
    private final DataSource dataSource;
    
    private XADataSource xaDataSource;
    
    private XATransactionManagerProvider xaTransactionManagerProvider;
    
    public XATransactionDataSource(final DatabaseType databaseType, final String resourceName, final DataSource dataSource, final XATransactionManagerProvider xaTransactionManagerProvider) {
        this.databaseType = databaseType;
        this.resourceName = resourceName;
        this.dataSource = dataSource;
        if (!CONTAINER_DATASOURCE_NAMES.contains(dataSource.getClass().getSimpleName())) {
            xaDataSource = new DataSourceSwapper(TypedSPILoader.getService(XADataSourceDefinition.class, databaseType.getType())).swap(dataSource);
            this.xaTransactionManagerProvider = xaTransactionManagerProvider;
            xaTransactionManagerProvider.registerRecoveryResource(resourceName, xaDataSource);
        }
    }
    
    /**
     * Get connection.
     *
     * @return XA transaction connection
     * @throws SQLException SQL exception
     * @throws SystemException system exception
     * @throws RollbackException rollback exception
     * @param transactionConnectionContext
     */
    public Connection getConnection(final TransactionConnectionContext transactionConnectionContext) throws SQLException, SystemException, RollbackException {
        if (CONTAINER_DATASOURCE_NAMES.contains(dataSource.getClass().getSimpleName())) {
            return dataSource.getConnection();
        }
         
        String transactionId = xaTransactionManagerProvider.getTransactionId();
        Map<String, Connection> enlistedTransactions = transactionConnectionContext.getEnlistedTransactions();
        if (!enlistedTransactions.containsKey(transactionId)) {
            Connection connection = dataSource.getConnection();
            XAConnection xaConnection = TypedSPILoader.getService(XAConnectionWrapper.class, databaseType.getType()).wrap(xaDataSource, connection);
            Transaction transaction = xaTransactionManagerProvider.getTransactionManager().getTransaction();
            transaction.enlistResource(new SingleXAResource(resourceName, xaConnection.getXAResource()));
            transaction.registerSynchronization(new Synchronization() {
                
                @Override
                public void beforeCompletion() {
                    enlistedTransactions.remove(transactionId);
                }
                
                @Override
                public void afterCompletion(final int status) {
                    enlistedTransactions.clear();
                }
            });
            enlistedTransactions.put(transactionId, connection);
        }
        return enlistedTransactions.get(transactionId);
    }
    
    @Override
    public void close() {
        if (CONTAINER_DATASOURCE_NAMES.contains(dataSource.getClass().getSimpleName())) {
            close(dataSource);
        } else {
            xaTransactionManagerProvider.removeRecoveryResource(resourceName, xaDataSource);
        }
    }
    
    private void close(final DataSource dataSource) {
        try {
            ReflectionUtil.invokeMethod(dataSource.getClass().getDeclaredMethod("close"), dataSource);
        } catch (final ReflectiveOperationException ignored) {
        }
    }
}
