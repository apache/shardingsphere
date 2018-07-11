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

package io.shardingsphere.proxy.config;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.google.common.base.Optional;
import io.shardingsphere.core.rule.DataSourceParameter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Create Xa Raw DataSource Map using {@code AtomikosDataSourceBean}.
 *
 * @author zhaojun
 */
public class XaProxyRawDataSource extends ProxyRawDataSource {
    
    public XaProxyRawDataSource(final Map<String, DataSourceParameter> dataSourceParameters) {
        super(dataSourceParameters);
    }
    
    @Override
    protected Map<String, DataSource> buildInternal(final String key, final DataSourceParameter dataSourceParameter) {
        final Map<String, DataSource> result = new HashMap<>(128, 1);
        AtomikosDataSourceBean dataSourceBean = new AtomikosDataSourceBean();
        dataSourceBean.setUniqueResourceName(key);
        dataSourceBean.setXaDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
        dataSourceBean.setMaxPoolSize(dataSourceParameter.getMaximumPoolSize());
        dataSourceBean.setTestQuery("SELECT 1");
        Properties xaProperties = new Properties();
        xaProperties.setProperty("user", dataSourceParameter.getUsername());
        xaProperties.setProperty("password", Optional.fromNullable(dataSourceParameter.getPassword()).or(""));
        xaProperties.setProperty("URL", dataSourceParameter.getUrl());
        xaProperties.setProperty("pinGlobalTxToPhysicalConnection", "true");
        xaProperties.setProperty("autoReconnect", "true");
        dataSourceBean.setXaProperties(xaProperties);
        result.put(key, dataSourceBean);
        return result;
    }
}
