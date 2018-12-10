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
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.xa.convert.dialect.XADatabaseType;
import io.shardingsphere.transaction.xa.convert.dialect.XAPropertyFactory;

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
    public DataSource wrap(final XADataSource xaDataSource, final String dataSourceName, final DataSourceParameter parameter) throws PropertyException {
        setAtomikosDatasourceBean(xaDataSource, dataSourceName, parameter);
        return delegate;
    }
    
    private void setAtomikosDatasourceBean(final XADataSource xaDataSource, final String dataSourceName, final DataSourceParameter parameter) throws PropertyException {
        setPoolProperties(parameter);
        setXAProperties(xaDataSource, dataSourceName, parameter);
    }
    
    private void setPoolProperties(final DataSourceParameter parameter) {
        delegate.setMaintenanceInterval((int) (parameter.getMaintenanceInterval() / 1000));
        delegate.setMinPoolSize(parameter.getMinimumPoolSize());
        delegate.setMaxPoolSize(parameter.getMaximumPoolSize());
        delegate.setBorrowConnectionTimeout((int) parameter.getConnectionTimeout() / 1000);
        delegate.setReapTimeout((int) parameter.getMaxLifetime() / 1000);
        delegate.setMaxIdleTime((int) parameter.getIdleTimeout() / 1000);
    }
    
    private void setXAProperties(final XADataSource xaDataSource, final String dataSourceName, final DataSourceParameter parameter) throws PropertyException {
        delegate.setXaDataSourceClassName(xaDataSource.getClass().getName());
        delegate.setUniqueResourceName(dataSourceName);
        Properties xaProperties = XAPropertyFactory.build(XADatabaseType.find(xaDataSource.getClass().getName()), parameter);
        PropertyUtils.setProperties(xaDataSource, xaProperties);
        delegate.setXaProperties(xaProperties);
        delegate.setXaDataSource(xaDataSource);
    }
}
