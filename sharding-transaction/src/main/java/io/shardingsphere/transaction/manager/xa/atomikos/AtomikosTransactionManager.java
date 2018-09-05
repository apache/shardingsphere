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

package io.shardingsphere.transaction.manager.xa.atomikos;

import com.atomikos.beans.PropertyUtils;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.google.common.base.Optional;
import io.shardingsphere.core.event.transaction.xa.XATransactionEvent;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.manager.xa.XATransactionManager;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Atomikos XA transaction manager.
 *
 * @author zhaojun
 */
public final class AtomikosTransactionManager implements XATransactionManager {
    
    private static final UserTransactionManager USER_TRANSACTION_MANAGER = new UserTransactionManager();
    
    static {
        try {
            USER_TRANSACTION_MANAGER.init();
        } catch (final SystemException ex) {
            throw new ShardingException(ex);
        }
    }
    
    @Override
    public void begin(final XATransactionEvent event) throws SQLException {
        try {
            USER_TRANSACTION_MANAGER.begin();
        } catch (final SystemException | NotSupportedException ex) {
            throw new SQLException(ex);
        }
    }
    
    @Override
    public void commit(final XATransactionEvent event) throws SQLException {
        try {
            USER_TRANSACTION_MANAGER.commit();
        } catch (final RollbackException | HeuristicMixedException | HeuristicRollbackException | SystemException ex) {
            throw new SQLException(ex);
        }
    }
    
    @Override
    public void rollback(final XATransactionEvent event) throws SQLException {
        try {
            USER_TRANSACTION_MANAGER.rollback();
        } catch (final SystemException ex) {
            throw new SQLException(ex);
        }
    }
    
    @Override
    public int getStatus() throws SQLException {
        try {
            return USER_TRANSACTION_MANAGER.getStatus();
        } catch (final SystemException ex) {
            throw new SQLException(ex);
        }
    }
    
    @Override
    public DataSource wrapDataSource(final XADataSource xaDataSource, final String dataSourceName, final DataSourceParameter dataSourceParameter) throws Exception {
        AtomikosDataSourceBean result = new AtomikosDataSourceBean();
        result.setUniqueResourceName(dataSourceName);
        result.setMaxPoolSize(dataSourceParameter.getMaximumPoolSize());
        result.setTestQuery("SELECT 1");
        Properties xaProperties;
        // TODO zhaojun: generic data source properties, can use MySQL only for now 
        if (xaDataSource.getClass().getName().equals("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource")) {
            xaProperties = getMySQLXAProperties(dataSourceParameter);
        } else {
            xaProperties = new Properties();
        }
        PropertyUtils.setProperties(xaDataSource, xaProperties);
        result.setXaDataSource(xaDataSource);
        result.setXaProperties(xaProperties);
        return result;
    }
    
    private Properties getMySQLXAProperties(final DataSourceParameter dataSourceParameter) {
        Properties result = new Properties();
        result.setProperty("user", dataSourceParameter.getUsername());
        result.setProperty("password", Optional.fromNullable(dataSourceParameter.getPassword()).or(""));
        result.setProperty("URL", dataSourceParameter.getUrl());
        result.setProperty("pinGlobalTxToPhysicalConnection", Boolean.TRUE.toString());
        result.setProperty("autoReconnect", Boolean.TRUE.toString());
        result.setProperty("useServerPrepStmts", Boolean.TRUE.toString());
        result.setProperty("cachePrepStmts", Boolean.TRUE.toString());
        result.setProperty("prepStmtCacheSize", "250");
        result.setProperty("prepStmtCacheSqlLimit", "2048");
        result.setProperty("useLocalSessionState", Boolean.TRUE.toString());
        result.setProperty("rewriteBatchedStatements", Boolean.TRUE.toString());
        result.setProperty("cacheResultSetMetadata", Boolean.TRUE.toString());
        result.setProperty("cacheServerConfiguration", Boolean.TRUE.toString());
        result.setProperty("elideSetAutoCommits", Boolean.TRUE.toString());
        result.setProperty("maintainTimeStats", Boolean.FALSE.toString());
        result.setProperty("netTimeoutForStreamingResults", "0");
        return result;
    }
}
