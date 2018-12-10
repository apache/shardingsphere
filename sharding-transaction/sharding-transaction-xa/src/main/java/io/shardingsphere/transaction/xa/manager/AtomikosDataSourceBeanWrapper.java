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
    
    @Override
    public DataSource wrap(final XADataSource xaDataSource, final String dataSourceName, final DataSourceParameter parameter) throws PropertyException {
        return createAtomikosDatasourceBean(xaDataSource, dataSourceName, parameter);
    }
    
    private AtomikosDataSourceBean createAtomikosDatasourceBean(final XADataSource xaDataSource, final String dataSourceName, final DataSourceParameter parameter) throws PropertyException {
        AtomikosDataSourceBean result = new AtomikosDataSourceBean();
        result.setUniqueResourceName(dataSourceName);
        result.setMaxPoolSize(parameter.getMaximumPoolSize());
        result.setMaxIdleTime((int) parameter.getIdleTimeout() / 1000);
        result.setBorrowConnectionTimeout((int) parameter.getConnectionTimeout() / 1000);
        result.setMaxLifetime((int) parameter.getMaxLifetime() / 1000);
        result.setTestQuery("SELECT 1");
        result.setXaDataSourceClassName(xaDataSource.getClass().getName());
        Properties xaProperties = XAPropertyFactory.build(XADatabaseType.find(xaDataSource.getClass().getName()), parameter);
        PropertyUtils.setProperties(xaDataSource, xaProperties);
        result.setXaDataSource(xaDataSource);
        result.setXaProperties(xaProperties);
        return result;
    }
}
