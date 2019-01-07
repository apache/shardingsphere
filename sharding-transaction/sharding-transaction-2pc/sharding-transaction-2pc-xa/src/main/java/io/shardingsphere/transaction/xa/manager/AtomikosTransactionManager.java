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

package io.shardingsphere.transaction.xa.manager;

import com.atomikos.icatch.jta.UserTransactionManager;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.core.context.XATransactionContext;
import io.shardingsphere.transaction.spi.xa.XATransactionManager;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

/**
 * Atomikos XA transaction manager.
 *
 * @author zhaojun
 */
public final class AtomikosTransactionManager implements XATransactionManager {
    
    private final UserTransactionManager underlyingTransactionManager;
    
    public AtomikosTransactionManager() {
        underlyingTransactionManager = new UserTransactionManager();
        init();
    }
    
    private void init() {
        try {
            underlyingTransactionManager.init();
        } catch (SystemException ex) {
            throw new ShardingException(ex);
        }
    }
    
    @Override
    public void destroy() {
        underlyingTransactionManager.setForceShutdown(true);
        underlyingTransactionManager.close();
    }
    
    @Override
    public void begin(final XATransactionContext transactionContext) throws ShardingException {
        try {
            underlyingTransactionManager.begin();
        } catch (final SystemException | NotSupportedException ex) {
            throw new ShardingException(ex);
        }
    }
    
    @Override
    public void commit(final XATransactionContext transactionContext) throws ShardingException {
        try {
            underlyingTransactionManager.commit();
        } catch (final RollbackException | HeuristicMixedException | HeuristicRollbackException | SystemException ex) {
            throw new ShardingException(ex);
        }
    }
    
    @Override
    public void rollback(final XATransactionContext transactionContext) throws ShardingException {
        try {
            if (Status.STATUS_NO_TRANSACTION != getStatus()) {
                underlyingTransactionManager.rollback();
            }
        } catch (final SystemException ex) {
            throw new ShardingException(ex);
        }
    }
    
    @Override
    public int getStatus() throws ShardingException {
        try {
            return underlyingTransactionManager.getStatus();
        } catch (final SystemException ex) {
            throw new ShardingException(ex);
        }
    }
    
    @Override
    public DataSource wrapDataSource(final DatabaseType databaseType, final XADataSource xaDataSource, final String dataSourceName, final DataSourceParameter dataSourceParameter) {
        try {
            return new AtomikosDataSourceBeanWrapper().wrap(databaseType, xaDataSource, dataSourceName, dataSourceParameter);
        } catch (final Exception ex) {
            throw new ShardingException("Failed to wrap XADataSource to transactional datasource pool", ex);
        }
    }
    
    @Override
    public TransactionManager getUnderlyingTransactionManager() {
        return underlyingTransactionManager;
    }
}
