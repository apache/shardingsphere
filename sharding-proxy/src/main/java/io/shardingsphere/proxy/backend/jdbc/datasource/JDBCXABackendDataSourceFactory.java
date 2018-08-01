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

package io.shardingsphere.proxy.backend.jdbc.datasource;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.google.common.base.Optional;
import io.shardingsphere.core.rule.DataSourceParameter;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Backend data source factory using {@code AtomikosDataSourceBean} for JDBC and XA protocol.
 *
 * @author zhaojun
 * @author zhangliang
 */
public final class JDBCXABackendDataSourceFactory implements JDBCBackendDataSourceFactory {
    
    @Override
    public DataSource build(final String dataSourceName, final DataSourceParameter dataSourceParameter) {
        AtomikosDataSourceBean result = new AtomikosDataSourceBean();
        result.setUniqueResourceName(dataSourceName);
        result.setXaDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
        result.setMaxPoolSize(dataSourceParameter.getMaximumPoolSize());
        result.setTestQuery("SELECT 1");
        result.setXaProperties(getProperties(dataSourceParameter));
        return result;
    }
    
    private Properties getProperties(final DataSourceParameter dataSourceParameter) {
        Properties result = new Properties();
        result.setProperty("user", dataSourceParameter.getUsername());
        result.setProperty("password", Optional.fromNullable(dataSourceParameter.getPassword()).or(""));
        result.setProperty("URL", dataSourceParameter.getUrl());
        result.setProperty("pinGlobalTxToPhysicalConnection", Boolean.TRUE.toString());
        result.setProperty("autoReconnect", Boolean.TRUE.toString());
        return result;
    }
}
