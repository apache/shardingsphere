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

package org.apache.shardingsphere.shardingproxypg.backend.communication.jdbc.datasource;

import com.atomikos.beans.PropertyException;
import com.atomikos.beans.PropertyUtils;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.config.DatabaseAccessConfiguration;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.shardingproxypg.backend.communication.jdbc.recognizer.JDBCURLRecognizerEngine;
import org.apache.shardingsphere.shardingproxypg.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.transaction.xa.jta.datasource.XADataSourceFactory;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.XAPropertiesFactory;

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
    public DataSource build(final String dataSourceName, final YamlDataSourceParameter dataSourceParameter) throws Exception {
        AtomikosDataSourceBean result = new AtomikosDataSourceBean();
        setPoolProperties(result, dataSourceParameter);
        setXAProperties(result, dataSourceName, XADataSourceFactory.build(DatabaseType.MySQL), dataSourceParameter);
        return result;
    }
    
    private void setPoolProperties(final AtomikosDataSourceBean dataSourceBean, final YamlDataSourceParameter parameter) {
        dataSourceBean.setMaintenanceInterval((int) (parameter.getMaintenanceIntervalMilliseconds() / 1000));
        dataSourceBean.setMinPoolSize(parameter.getMinPoolSize() < 0 ? 0 : parameter.getMinPoolSize());
        dataSourceBean.setMaxPoolSize(parameter.getMaxPoolSize());
        dataSourceBean.setBorrowConnectionTimeout((int) parameter.getConnectionTimeoutMilliseconds() / 1000);
        dataSourceBean.setReapTimeout((int) parameter.getMaxLifetimeMilliseconds() / 1000);
        dataSourceBean.setMaxIdleTime((int) parameter.getIdleTimeoutMilliseconds() / 1000);
    }
    
    private void setXAProperties(final AtomikosDataSourceBean dataSourceBean,  
                                 final String dataSourceName, final XADataSource xaDataSource, final YamlDataSourceParameter dataSourceParameter) throws PropertyException {
        dataSourceBean.setXaDataSourceClassName(xaDataSource.getClass().getName());
        dataSourceBean.setUniqueResourceName(dataSourceName);
        Properties xaProperties = XAPropertiesFactory.createXAProperties(JDBCURLRecognizerEngine.getDatabaseType(dataSourceParameter.getUrl())).build(
                new DatabaseAccessConfiguration(dataSourceParameter.getUrl(), dataSourceParameter.getUsername(), dataSourceParameter.getPassword()));
        PropertyUtils.setProperties(xaDataSource, xaProperties);
        dataSourceBean.setXaProperties(xaProperties);
        dataSourceBean.setXaDataSource(xaDataSource);
    }
}
