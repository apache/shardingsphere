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
import com.atomikos.jdbc.AtomikosDataSourceBean;
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
 * Wrap XADataSource to transactional dataSource pool.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
final class XATransactionDataSourceWrapper {
    
    private final TransactionManager transactionManager;
    
    /**
     * Wrap XADataSource to transactional dataSource pool.
     *
     * @param xaDataSource XA dataSource
     * @param dataSourceName dataSource name
     * @param dataSourceParameter dataSource parameter
     * @return transactional datasource pool
     * @throws PropertyException property exception
     */
    DataSource wrap(final XADataSource xaDataSource, final String dataSourceName, final DataSourceParameter dataSourceParameter) throws PropertyException {
        switch (dataSourceParameter.getProxyDatasourceType()) {
            case TOMCAT_DBCP2:
                return createBasicManagedDataSource(xaDataSource, dataSourceName, dataSourceParameter);
            default:
                return createAtomikosDatasourceBean(xaDataSource, dataSourceName, dataSourceParameter);
        }
    }
    
    private BasicManagedDataSource createBasicManagedDataSource(final XADataSource xaDataSource, final String dataSourceName, final DataSourceParameter parameter) throws PropertyException {
        BasicManagedDataSource result = new BasicManagedDataSource();
        result.setTransactionManager(transactionManager);
        result.setMaxTotal(parameter.getMaximumPoolSize());
        result.setMaxWaitMillis(parameter.getConnectionTimeout());
        result.setMaxIdle((int) parameter.getIdleTimeout());
        result.setMaxConnLifetimeMillis(parameter.getMaxLifetime());
        result.setXADataSource(xaDataSource.getClass().getName());
        Properties xaProperties = XAPropertyFactory.build(XADatabaseType.find(xaDataSource.getClass().getName()), parameter);
        PropertyUtils.setProperties(xaDataSource, xaProperties);
        result.setXaDataSourceInstance(xaDataSource);
        registerRecoveryResource(dataSourceName, xaDataSource);
        return result;
    }
    
    private void registerRecoveryResource(final String dataSourceName, final XADataSource xaDataSource) {
        JdbcTransactionalResource transactionalResource = new JdbcTransactionalResource(dataSourceName, xaDataSource);
        synchronized (XATransactionDataSourceWrapper.class) {
            if (null == Configuration.getResource(dataSourceName)) {
                Configuration.addResource(transactionalResource);
            }
        }
    }
    
    private AtomikosDataSourceBean createAtomikosDatasourceBean(final XADataSource xaDataSource, final String dataSourceName, final DataSourceParameter parameter) throws PropertyException {
        AtomikosDataSourceBean result = new AtomikosDataSourceBean();
        result.setUniqueResourceName(dataSourceName);
        result.setMaxPoolSize(parameter.getMaximumPoolSize());
        result.setMaxIdleTime((int) parameter.getIdleTimeout());
        result.setBorrowConnectionTimeout((int) parameter.getConnectionTimeout());
        result.setMaxLifetime((int) parameter.getMaxLifetime());
        result.setTestQuery("SELECT 1");
        result.setXaDataSourceClassName(xaDataSource.getClass().getName());
        Properties xaProperties = XAPropertyFactory.build(XADatabaseType.find(xaDataSource.getClass().getName()), parameter);
        PropertyUtils.setProperties(xaDataSource, xaProperties);
        result.setXaDataSource(xaDataSource);
        result.setXaProperties(xaProperties);
        return result;
    }
}
