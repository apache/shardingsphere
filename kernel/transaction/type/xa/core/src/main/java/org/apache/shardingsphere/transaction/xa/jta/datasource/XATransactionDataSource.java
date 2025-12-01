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

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.util.reflection.ReflectionUtils;
import org.apache.shardingsphere.transaction.xa.jta.connection.XAConnectionWrapper;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * XA transaction data source.
 */
public final class XATransactionDataSource implements AutoCloseable {
    
    private static final Set<String> CONTAINER_DATASOURCE_NAMES = new HashSet<>(Arrays.asList("AtomikosDataSourceBean", "BasicManagedDataSource"));
    
    private final ThreadLocal<Map<Transaction, Collection<Connection>>> enlistedTransactions = ThreadLocal.withInitial(HashMap::new);
    
    private final ThreadLocal<AtomicInteger> uniqueName = ThreadLocal.withInitial(AtomicInteger::new);
    
    private final String resourceName;
    
    private final DataSource dataSource;
    
    private XADataSource xaDataSource;
    
    private XAConnectionWrapper xaConnectionWrapper;
    
    private XATransactionManagerProvider xaTransactionManagerProvider;
    
    public XATransactionDataSource(final DatabaseType databaseType, final String resourceName, final DataSource dataSource, final XATransactionManagerProvider xaTransactionManagerProvider) {
        this.resourceName = resourceName;
        this.dataSource = dataSource;
        if (!CONTAINER_DATASOURCE_NAMES.contains(dataSource.getClass().getSimpleName())) {
            DialectTransactionOption transactionOption = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getTransactionOption();
            xaDataSource = new DataSourceSwapper(databaseType, transactionOption.getXaDriverClassNames()).swap(dataSource);
            xaConnectionWrapper = DatabaseTypedSPILoader.getService(XAConnectionWrapper.class, databaseType);
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
     */
    public Connection getConnection() throws SQLException, SystemException, RollbackException {
        if (CONTAINER_DATASOURCE_NAMES.contains(dataSource.getClass().getSimpleName())) {
            return dataSource.getConnection();
        }
        Transaction transaction = xaTransactionManagerProvider.getTransactionManager().getTransaction();
        Connection connection = dataSource.getConnection();
        enlistResource(connection, transaction);
        return connection;
    }
    
    private void enlistResource(final Connection connection, final Transaction transaction) throws SQLException, RollbackException, SystemException {
        XAConnection xaConnection = xaConnectionWrapper.wrap(xaDataSource, connection);
        transaction.enlistResource(new SingleXAResource(resourceName, String.valueOf(uniqueName.get().getAndIncrement()), xaConnection.getXAResource()));
        registerSynchronization(transaction);
        enlistedTransactions.get().computeIfAbsent(transaction, key -> new LinkedList<>());
        enlistedTransactions.get().get(transaction).add(connection);
    }
    
    private void registerSynchronization(final Transaction transaction) throws RollbackException, SystemException {
        transaction.registerSynchronization(new Synchronization() {
            
            @Override
            public void beforeCompletion() {
                enlistedTransactions.get().remove(transaction);
                uniqueName.remove();
            }
            
            @Override
            public void afterCompletion(final int status) {
                enlistedTransactions.get().clear();
            }
        });
    }
    
    @Override
    public void close() {
        if (CONTAINER_DATASOURCE_NAMES.contains(dataSource.getClass().getSimpleName())) {
            close(dataSource);
        } else {
            xaTransactionManagerProvider.removeRecoveryResource(resourceName, xaDataSource);
        }
        enlistedTransactions.remove();
    }
    
    private void close(final DataSource dataSource) {
        try {
            ReflectionUtils.invokeMethod(dataSource.getClass().getDeclaredMethod("close"), dataSource);
        } catch (final ReflectiveOperationException ignored) {
        }
    }
}
