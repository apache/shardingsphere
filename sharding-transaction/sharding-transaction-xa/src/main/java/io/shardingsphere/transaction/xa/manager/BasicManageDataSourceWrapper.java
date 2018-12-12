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

import com.atomikos.beans.PropertyException;
import com.atomikos.beans.PropertyUtils;
import com.atomikos.datasource.xa.jdbc.JdbcTransactionalResource;
import com.atomikos.icatch.config.Configuration;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.xa.convert.dialect.XADatabaseType;
import io.shardingsphere.transaction.xa.convert.dialect.XAPropertyFactory;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.dbcp.dbcp2.managed.BasicManagedDataSource;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import java.util.Properties;

/**
 * Basic manage data source wrapper.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class BasicManageDataSourceWrapper implements XADataSourceWrapper {
    
    private final TransactionManager transactionManager;
    
    private final BasicManagedDataSource delegate = new BasicManagedDataSource();
    
    @Override
    public DataSource wrap(final XADataSource xaDataSource, final String dataSourceName, final DataSourceParameter parameter) throws PropertyException {
        setBasicManagedDataSource(xaDataSource, dataSourceName, parameter);
        return delegate;
    }
    
    private void setBasicManagedDataSource(final XADataSource xaDataSource, final String dataSourceName, final DataSourceParameter parameter) throws PropertyException {
        setPoolProperties(parameter);
        setXAProperties(xaDataSource, parameter);
        registerRecoveryResource(dataSourceName, xaDataSource);
    }
    
    private void setPoolProperties(final DataSourceParameter parameter) {
        delegate.setMinIdle(parameter.getMinimumPoolSize());
        delegate.setMaxTotal(parameter.getMaximumPoolSize());
        delegate.setMaxWaitMillis(parameter.getConnectionTimeout());
        delegate.setTimeBetweenEvictionRunsMillis(parameter.getMaintenanceInterval());
        delegate.setMinEvictableIdleTimeMillis(parameter.getIdleTimeout());
        delegate.setMaxConnLifetimeMillis(parameter.getMaxLifetime());
        delegate.setTestOnBorrow(false);
    }
    
    private void setXAProperties(final XADataSource xaDataSource, final DataSourceParameter parameter) throws PropertyException {
        delegate.setTransactionManager(transactionManager);
        delegate.setXADataSource(xaDataSource.getClass().getName());
        Properties xaProperties = XAPropertyFactory.build(XADatabaseType.find(xaDataSource.getClass().getName()), parameter);
        PropertyUtils.setProperties(xaDataSource, xaProperties);
        delegate.setXaDataSourceInstance(xaDataSource);
    }
    
    private void registerRecoveryResource(final String dataSourceName, final XADataSource xaDataSource) {
        JdbcTransactionalResource transactionalResource = new JdbcTransactionalResource(dataSourceName, xaDataSource);
        synchronized (BasicManageDataSourceWrapper.class) {
            if (null == Configuration.getResource(dataSourceName)) {
                Configuration.addResource(transactionalResource);
            }
        }
    }
}
