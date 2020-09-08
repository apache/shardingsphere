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

package org.apache.shardingsphere.proxy.backend.schema;

import lombok.Getter;
import org.apache.shardingsphere.infra.context.schema.DataSourceParameter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.factory.JDBCRawBackendDataSourceFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.recognizer.JDBCDriverURLRecognizerEngine;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Proxy data source context.
 */
@Getter
public final class ProxyDataSourceContext {
    
    private final DatabaseType databaseType;
    
    private final Map<String, Map<String, DataSource>> dataSourcesMap;
    
    public ProxyDataSourceContext(final Map<String, Map<String, DataSourceParameter>> dataSourceParametersMap) {
        databaseType = containsDataSourceParameter(dataSourceParametersMap) ? getDatabaseType(dataSourceParametersMap) : new MySQLDatabaseType();
        dataSourcesMap = createDataSourcesMap(dataSourceParametersMap);
    }
    
    private boolean containsDataSourceParameter(final Map<String, Map<String, DataSourceParameter>> dataSourceParametersMap) {
        return !dataSourceParametersMap.isEmpty() && !dataSourceParametersMap.values().iterator().next().isEmpty();
    }
    
    private static DatabaseType getDatabaseType(final Map<String, Map<String, DataSourceParameter>> dataSourceParametersMap) {
        String databaseTypeName = JDBCDriverURLRecognizerEngine.getJDBCDriverURLRecognizer(dataSourceParametersMap.values().iterator().next().values().iterator().next().getUrl()).getDatabaseType();
        return DatabaseTypes.getActualDatabaseType(databaseTypeName);
    }
    
    private static Map<String, Map<String, DataSource>> createDataSourcesMap(final Map<String, Map<String, DataSourceParameter>> dataSourceParametersMap) {
        return dataSourceParametersMap.entrySet().stream().collect(
                Collectors.toMap(Entry::getKey, entry -> createDataSources(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private static Map<String, DataSource> createDataSources(final Map<String, DataSourceParameter> dataSourceParameters) {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSourceParameters.size(), 1);
        for (Entry<String, DataSourceParameter> entry : dataSourceParameters.entrySet()) {
            result.put(entry.getKey(), JDBCRawBackendDataSourceFactory.getInstance().build(entry.getKey(), entry.getValue()));
        }
        return result;
    }
}
