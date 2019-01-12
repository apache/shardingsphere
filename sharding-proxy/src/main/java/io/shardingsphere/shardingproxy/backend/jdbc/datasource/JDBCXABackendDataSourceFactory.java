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

package io.shardingsphere.shardingproxy.backend.jdbc.datasource;

import com.atomikos.beans.PropertyException;
import com.atomikos.beans.PropertyUtils;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import io.shardingsphere.core.config.DataSourceParameter;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.transaction.xa.jta.datasource.XADataSourceFactory;
import io.shardingsphere.transaction.xa.jta.datasource.XAPropertiesFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.util.Properties;

/**
 * Backend data source factory using {@code AtomikosDataSourceBean} for JDBC and XA protocol.
 *
 * @author zhaojun
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JDBCXABackendDataSourceFactory implements JDBCBackendDataSourceFactory {
    
    private static final JDBCXABackendDataSourceFactory INSTANCE = new JDBCXABackendDataSourceFactory();
    
    /**
     * Get instance of {@code JDBCXABackendDataSourceFactory}.
     *
     * @return JDBC XA backend data source factory
     */
    public static JDBCBackendDataSourceFactory getInstance() {
        return INSTANCE;
    }
    
    @Override
    public DataSource build(final String dataSourceName, final DataSourceParameter dataSourceParameter) throws Exception {
        AtomikosDataSourceBean result = new AtomikosDataSourceBean();
        setPoolProperties(result, dataSourceParameter);
        // TODO judge database type
        setXAProperties(result, DatabaseType.MySQL, dataSourceName, XADataSourceFactory.build(DatabaseType.MySQL), dataSourceParameter);
        return result;
    }
    
    private void setPoolProperties(final AtomikosDataSourceBean dataSourceBean, final DataSourceParameter parameter) {
        dataSourceBean.setMaintenanceInterval((int) (parameter.getMaintenanceIntervalMilliseconds() / 1000));
        dataSourceBean.setMinPoolSize(parameter.getMinPoolSize() < 0 ? 0 : parameter.getMinPoolSize());
        dataSourceBean.setMaxPoolSize(parameter.getMaxPoolSize());
        dataSourceBean.setBorrowConnectionTimeout((int) parameter.getConnectionTimeoutMilliseconds() / 1000);
        dataSourceBean.setReapTimeout((int) parameter.getMaxLifetimeMilliseconds() / 1000);
        dataSourceBean.setMaxIdleTime((int) parameter.getIdleTimeoutMilliseconds() / 1000);
    }
    
    private void setXAProperties(final AtomikosDataSourceBean dataSourceBean,
                                 final DatabaseType databaseType, final String dataSourceName, final XADataSource xaDataSource, final DataSourceParameter parameter) throws PropertyException {
        dataSourceBean.setXaDataSourceClassName(xaDataSource.getClass().getName());
        dataSourceBean.setUniqueResourceName(dataSourceName);
        Properties xaProperties = XAPropertiesFactory.createXAProperties(databaseType).build(parameter);
        PropertyUtils.setProperties(xaDataSource, xaProperties);
        dataSourceBean.setXaProperties(xaProperties);
        dataSourceBean.setXaDataSource(xaDataSource);
    }
}
