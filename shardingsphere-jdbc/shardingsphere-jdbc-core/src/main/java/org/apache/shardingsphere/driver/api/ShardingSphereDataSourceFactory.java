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

package org.apache.shardingsphere.driver.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineExpressionParser;

import javax.sql.DataSource;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * ShardingSphere data source factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ShardingSphereDataSourceFactory {
    
    /**
     * property name for datasource type.
     */
    static final String DS_TYPE = "type";
    
    /**
     * property name for datasource template.
     */
    static final String DS_TEMPLATE = "template";
    
    /**
     * property name to indicate whether a datasource is a template, defaults to NOT.
     */
    static final String DS_IS_TEMPLATE = "isTemplate";

    static final String DATA_SOURCE_NAMES = "names";

    /**
     * Create ShardingSphere data source.
     *
     * @param dataSourceMap data source map
     * @param configurations rule configurations
     * @param props properties for data source
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final Collection<RuleConfiguration> configurations, final Properties props) throws SQLException {
        return new ShardingSphereDataSource(dataSourceMap, configurations, props);
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param dataSource data source
     * @param configurations rule configurations
     * @param props properties for data source
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final DataSource dataSource, final Collection<RuleConfiguration> configurations, final Properties props) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put(DefaultSchema.LOGIC_NAME, dataSource);
        return new ShardingSphereDataSource(dataSourceMap, configurations, props);
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param dataSourceConfigMap data source config properties map
     * @param configurations rule configurations
     * @param props properties for data source
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSourceByProps(final Map<String, Map<String, Object>> dataSourceConfigMap, final Collection<RuleConfiguration> configurations, final Properties props)
            throws SQLException {

        return new ShardingSphereDataSource(fromDataSourceConfig(dataSourceConfigMap), configurations, props);
    }

    /**Create datasources from datasource yaml configurations.
     * @param dataSourceConfigMap datasource config properties map
     * @return datasources map
     */
    public static Map<String, DataSource> fromDataSourceConfig(final Map<String, Map<String, Object>> dataSourceConfigMap) {
        Map<String, DataSource> dsmap = new HashMap<>();
        if (dataSourceConfigMap == null || dataSourceConfigMap.isEmpty()) {
            return dsmap;
        }
        Map<String, Map<String, Object>> convertedDataSourceConfigMap = new HashMap<>(dataSourceConfigMap.size() * 2);
        for (Map.Entry<String, Map<String, Object>> dsEntry : dataSourceConfigMap.entrySet()) {
            Map<String, Object> dsConfig = dsEntry.getValue();
            dsConfig = convertPropNames(dsConfig);
            convertedDataSourceConfigMap.put(dsEntry.getKey(), dsConfig);
        }
        for (Map.Entry<String, Map<String, Object>> dsEntry : convertedDataSourceConfigMap.entrySet()) {
            Map<String, Object> dsConfig = dsEntry.getValue();
            Object isTempObj = dsConfig.get(DS_IS_TEMPLATE);
            Boolean isTemplate = isTempObj == null ? null : Boolean.valueOf(isTempObj.toString());
            if (!Boolean.TRUE.equals(isTemplate)) {
                String templateKey = (String) dsConfig.get(DS_TEMPLATE);
                Map<String, Object> pDsConfig = dataSourceConfigMap.get(templateKey);
                String dsName = dsEntry.getKey();
                if (templateKey != null && pDsConfig == null) {
                    throw new RuntimeException("Datasource [" + dsName + "] need a template as [" + templateKey + "] that not exists!");
                }
                String type = (String) dsConfig.get(DS_TYPE);
                if (type == null && pDsConfig != null) {
                    type = (String) pDsConfig.get(DS_TYPE);
                }
                if (type == null) {
                    log.info("Datasource [{}] has no type define, configuration: {}", dsName, dsConfig);
                    continue;
                }
                try {
                    Class<?> dataSourceClass = Class.forName(type);
                    DataSource ds = (DataSource) dataSourceClass.newInstance();
                    if (pDsConfig != null) {
                        BeanUtils.copyProperties(ds, pDsConfig);
                    }
                    BeanUtils.copyProperties(ds, dsConfig);
                    dsmap.put(dsName, ds);
                } catch (ClassNotFoundException | IllegalAccessException e) {
                    throw new RuntimeException("Datasource [" + dsName + "] has a wrong type as [" + type + "]");
                } catch (InstantiationException e) {
                    log.error("Error to create datasource with type  [" + type + "]", e);
                    throw new RuntimeException("Error to create datasource with type  [" + type + "]: " + e.getMessage());
                } catch (InvocationTargetException e) {
                    log.error("Error to copy properties to datasource with type  [" + type + "]", e);
                    throw new RuntimeException("Error to copy properties to datasource with type  [" + type + "]" + e.getMessage());
                }
            }
        }
        log.info("Resolved datasources:{}", dsmap);
        return dsmap;
    }
    
    /** extract names property to datasource name list. names maybe: "ds1"; "ds1, ds2"; "ds$-&gt;{0..1}"; ["ds1", "ds2"].
     * @param flatDsProps datasource config properties
     * @return datasource names
     */
    public static List<String> extractDataSourceNames(final Map<String, Object> flatDsProps) {
        Object nameso = flatDsProps.get(DATA_SOURCE_NAMES);
        // names maybe: "ds1"; "ds1, ds2"; "ds$->{0..1}"; ["ds1", "ds2"]
        if (nameso == null) {
            return null;
        }
        if (nameso instanceof String[]) {
            return Arrays.asList((String[]) nameso);
        } else {
            String names = nameso.toString();
            if (names.contains("$")) {
                return new InlineExpressionParser(names).splitAndEvaluate();
            } else {
                String[] splited = names.split(",");
                for (int i = 0; i < splited.length; i++) {
                    splited[i] = splited[i].trim();
                }
                return Arrays.asList(splited);
            }
        }
    }

    /** convert property style names to java tyle names. e.g. max-pool-size -&gt; maxPoolSize
     * @param propConf configuration properties from property or yaml style
     * @return converted config properties
     */
    public static Map<String, Object> convertPropNames(final Map<String, Object> propConf) {
        Map<String, Object> tempMap = new HashMap<>(propConf.size() * 2);
        Iterator<Entry<String, Object>> it = propConf.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Object> ent = it.next();
            String key = ent.getKey();
            int idx = key.indexOf("-");
            if (idx > 0) {
                do {
                    key = key.substring(0, idx) + key.substring(idx + 1, idx + 2).toUpperCase() + key.substring(idx + 2);
                    idx = key.indexOf("-");
                } while (idx >= 0);
            }
            tempMap.put(key, ent.getValue());
        }
        return tempMap;
    }
    
}
