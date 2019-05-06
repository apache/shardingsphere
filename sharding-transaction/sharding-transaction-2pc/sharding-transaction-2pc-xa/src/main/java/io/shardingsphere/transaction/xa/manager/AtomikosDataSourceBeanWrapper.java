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
import com.atomikos.jdbc.AtomikosDataSourceBean;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.xa.convert.datasource.XAPropertiesFactory;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.util.Properties;

/**
 * Atomikos data source bean wrapper.
 *
 * @author zhaojun
 */
public final class AtomikosDataSourceBeanWrapper implements XADataSourceWrapper {
    
    private final AtomikosDataSourceBean delegate = new AtomikosDataSourceBean();
    
    @Override
    public DataSource wrap(final DatabaseType databaseType, final XADataSource xaDataSource, final String dataSourceName, final DataSourceParameter parameter) throws PropertyException {
        setAtomikosDatasourceBean(databaseType, xaDataSource, dataSourceName, parameter);
        return delegate;
    }
    
    private void setAtomikosDatasourceBean(
            final DatabaseType databaseType, final XADataSource xaDataSource, final String dataSourceName, final DataSourceParameter parameter) throws PropertyException {
        setPoolProperties(parameter);
        setXAProperties(databaseType, xaDataSource, dataSourceName, parameter);
    }
    
    private void setPoolProperties(final DataSourceParameter parameter) {
        delegate.setMaintenanceInterval((int) (parameter.getMaintenanceIntervalMilliseconds() / 1000));
        delegate.setMinPoolSize(parameter.getMinPoolSize() < 0 ? 0 : parameter.getMinPoolSize());
        delegate.setMaxPoolSize(parameter.getMaxPoolSize());
        delegate.setBorrowConnectionTimeout((int) parameter.getConnectionTimeoutMilliseconds() / 1000);
        delegate.setReapTimeout((int) parameter.getMaxLifetimeMilliseconds() / 1000);
        delegate.setMaxIdleTime((int) parameter.getIdleTimeoutMilliseconds() / 1000);
    }
    
    private void setXAProperties(
            final DatabaseType databaseType, final XADataSource xaDataSource, final String dataSourceName, final DataSourceParameter dataSourceParameter) throws PropertyException {
        delegate.setXaDataSourceClassName(xaDataSource.getClass().getName());
        delegate.setUniqueResourceName(dataSourceName);
        Properties xaProperties = XAPropertiesFactory.createXAProperties(databaseType).build(dataSourceParameter);
        PropertyUtils.setProperties(xaDataSource, xaProperties);
        delegate.setXaProperties(xaProperties);
        delegate.setXaDataSource(xaDataSource);
    }
}
