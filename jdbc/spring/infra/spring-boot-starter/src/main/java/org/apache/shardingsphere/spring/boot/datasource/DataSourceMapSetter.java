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

package org.apache.shardingsphere.spring.boot.datasource;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.util.expr.InlineExpressionParser;
import org.apache.shardingsphere.spring.boot.exception.DataSourceJndiNotFoundServerException;
import org.apache.shardingsphere.spring.boot.util.PropertyUtil;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data source map setter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceMapSetter {
    
    private static final String PREFIX = "spring.shardingsphere.datasource.";
    
    private static final String DATA_SOURCE_NAME = "name";
    
    private static final String DATA_SOURCE_NAMES = "names";
    
    private static final String DATA_SOURCE_TYPE = "type";
    
    private static final String JNDI_NAME = "jndi-name";
    
    /**
     * Get data source map.
     * 
     * @param environment spring boot environment
     * @return data source map
     */
    public static Map<String, DataSource> getDataSourceMap(final Environment environment) {
        Map<String, DataSource> result = new LinkedHashMap<>();
        for (String each : getDataSourceNames(environment)) {
            try {
                result.put(each, getDataSource(environment, each));
            } catch (final NamingException ex) {
                throw new DataSourceJndiNotFoundServerException(ex);
            }
        }
        return result;
    }
    
    private static List<String> getDataSourceNames(final Environment environment) {
        StandardEnvironment standardEnv = (StandardEnvironment) environment;
        standardEnv.setIgnoreUnresolvableNestedPlaceholders(true);
        String dataSourceNames = standardEnv.getProperty(PREFIX + DATA_SOURCE_NAME);
        if (Strings.isNullOrEmpty(dataSourceNames)) {
            dataSourceNames = standardEnv.getProperty(PREFIX + DATA_SOURCE_NAMES);
        }
        return new InlineExpressionParser(dataSourceNames).splitAndEvaluate();
    }
    
    @SuppressWarnings("unchecked")
    private static DataSource getDataSource(final Environment environment, final String dataSourceName) throws NamingException {
        Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, String.join("", PREFIX, dataSourceName), Map.class);
        Preconditions.checkState(!dataSourceProps.isEmpty(), "Wrong datasource [%s] properties.", dataSourceName);
        if (dataSourceProps.containsKey(JNDI_NAME)) {
            return getJNDIDataSource(dataSourceProps.get(JNDI_NAME).toString());
        }
        return DataSourcePoolCreator.create(new DataSourceProperties(dataSourceProps.get(DATA_SOURCE_TYPE).toString(), PropertyUtil.getCamelCaseKeys(dataSourceProps)));
    }
    
    private static DataSource getJNDIDataSource(final String jndiName) throws NamingException {
        JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
        bean.setResourceRef(true);
        bean.setJndiName(jndiName);
        bean.setProxyInterface(DataSource.class);
        bean.afterPropertiesSet();
        return (DataSource) AopProxyUtils.getTarget(bean.getObject());
    }
}
