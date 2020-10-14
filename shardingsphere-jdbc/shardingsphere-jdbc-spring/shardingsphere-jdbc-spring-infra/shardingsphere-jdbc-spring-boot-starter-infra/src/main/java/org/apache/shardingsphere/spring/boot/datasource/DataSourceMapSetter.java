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

import java.util.Map;
import java.util.Map.Entry;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.spring.boot.util.PropertyUtil;
import org.springframework.core.env.Environment;
import org.springframework.jndi.JndiObjectFactoryBean;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Data source map setter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceMapSetter {
    
    private static final String PREFIX = "spring.shardingsphere.datasource.";
    
//    private static final String DATA_SOURCE_NAME = "name";
//    
//    private static final String DATA_SOURCE_NAMES = "names";
//    
//    private static final String DATA_SOURCE_TYPE = "type";
    
//    private static final String JNDI_NAME_PROP = "jndi-name";
    private static final String JNDI_NAME_JAVA = "jndiName";
    
    /**
     * Get data source map.
     * 
     * @param environment spring boot environment
     * @return data source map
     */
    public static Map<String, DataSource> getDataSourceMap(final Environment environment) {
        @SuppressWarnings("unchecked")
        Map<String, Object> flatDsProps = PropertyUtil.handle(environment, PREFIX, Map.class);
        flatDsProps = ShardingSphereDataSourceFactory.convertPropNames(flatDsProps);
        Map<String, Map<String, Object>> dataSourceProps = PropertyUtil.translateDataSourceProps(flatDsProps);
        Map<String, DataSource> result = ShardingSphereDataSourceFactory.fromDataSourceConfig(dataSourceProps);
        for (Entry<String, Map<String, Object>> dsEntry : dataSourceProps.entrySet()) {
            Map<String, Object> dsProps = dsEntry.getValue();
            Object dsJndi = dsProps.get(JNDI_NAME_JAVA);
            if (dsJndi != null) {
                String jndiName = (String) dsJndi;
                try {
                    result.put(dsEntry.getKey(), getJNDIDataSource(jndiName));
                } catch (final NamingException ex) {
                    throw new ShardingSphereException("Can't find JNDI data source.", ex);
                }
            }
        }
//        List<String> dataSourceNames = ShardingSphereDataSourceFactory.extractDataSourceNames(flatDsProps);
//        for (String each : dataSourceNames) {
//            try {
//                result.put(each, getDataSource(environment, PREFIX, each));
//            } catch (final ReflectiveOperationException ex) {
//                throw new ShardingSphereException("Can't find data source type.", ex);
//            } catch (final NamingException ex) {
//                throw new ShardingSphereException("Can't find JNDI data source.", ex);
//            }
//        }
        return result;
    }

    
//    private static List<String> getDataSourceNames(final Environment environment, final String prefix) {
//        StandardEnvironment standardEnv = (StandardEnvironment) environment;
//        standardEnv.setIgnoreUnresolvableNestedPlaceholders(true);
//        String dataSourceNames = standardEnv.getProperty(prefix + DATA_SOURCE_NAME);
//        if (StringUtils.isEmpty(dataSourceNames)) {
//            dataSourceNames = standardEnv.getProperty(prefix + DATA_SOURCE_NAMES);
//        }
//        return new InlineExpressionParser(dataSourceNames).splitAndEvaluate();
//    }
//    
//    @SuppressWarnings("unchecked")
//    private static DataSource getDataSource(final Environment environment, final String prefix, final String dataSourceName) throws ReflectiveOperationException, NamingException {
//        Map<String, Object> dataSourceProps = null;//mergedDataSourceProps(PropertyUtil.handle(environment, prefix + dataSourceName.trim(), Map.class));
//        Preconditions.checkState(!dataSourceProps.isEmpty(), String.format("Wrong datasource [%s] properties.", dataSourceName));
//        if (dataSourceProps.containsKey(JNDI_NAME)) {
//            return getJNDIDataSource(dataSourceProps.get(JNDI_NAME).toString());
//        }
//        DataSource result = DataSourceUtil.getDataSource(dataSourceProps.get(DATA_SOURCE_TYPE).toString(), dataSourceProps);
//        DataSourcePropertiesSetterHolder.getDataSourcePropertiesSetterByType(dataSourceProps.get(DATA_SOURCE_TYPE).toString()).ifPresent(
//            propsSetter -> propsSetter.propertiesSet(environment, prefix, dataSourceName, result));
//        return result;
//    }
    
//    private static Map<String, Object> mergedDataSourceProps(final Map<String, Object> dataSourceProps) {
//        if (!dataSourceCommonProps.isEmpty()) {
//            dataSourceCommonProps.putAll(dataSourceProps);
//            return dataSourceCommonProps;
//        } else {
//            return dataSourceProps;
//        }
//    }
    
    private static DataSource getJNDIDataSource(final String jndiName) throws NamingException {
        JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
        bean.setResourceRef(true);
        bean.setJndiName(jndiName);
        bean.setProxyInterface(DataSource.class);
        bean.afterPropertiesSet();
        return (DataSource) bean.getObject();
    }
}
