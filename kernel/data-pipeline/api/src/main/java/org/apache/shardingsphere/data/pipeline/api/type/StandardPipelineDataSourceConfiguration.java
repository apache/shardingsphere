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

package org.apache.shardingsphere.data.pipeline.api.type;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.spi.JdbcQueryPropertiesExtension;
import org.apache.shardingsphere.database.connector.core.jdbcurl.appender.JdbcUrlAppender;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.StandardJdbcUrlParser;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Pipeline data source configuration for standard JDBC.
 */
@EqualsAndHashCode(of = "parameter")
@Getter
public final class StandardPipelineDataSourceConfiguration implements PipelineDataSourceConfiguration {
    
    public static final String TYPE = "JDBC";
    
    private static final String DATA_SOURCE_CLASS_NAME = "dataSourceClassName";
    
    private final String parameter;
    
    private final DatabaseType databaseType;
    
    private final String url;
    
    private final String username;
    
    private final String password;
    
    @Getter(AccessLevel.NONE)
    private final DataSourcePoolProperties dataSourcePoolProps;
    
    @SuppressWarnings("unchecked")
    public StandardPipelineDataSourceConfiguration(final String poolYamlContent) {
        this(YamlEngine.unmarshal(poolYamlContent, Map.class));
    }
    
    public StandardPipelineDataSourceConfiguration(final Map<String, Object> poolProps) {
        parameter = YamlEngine.marshal(poolProps);
        Map<String, Object> newPoolProps = new HashMap<>(poolProps);
        for (String each : Arrays.asList("minPoolSize", "minimumIdle")) {
            newPoolProps.put(each, "1");
        }
        if (newPoolProps.containsKey("jdbcUrl")) {
            newPoolProps.put("url", newPoolProps.get("jdbcUrl"));
            newPoolProps.remove("jdbcUrl");
        }
        databaseType = DatabaseTypeFactory.get(String.valueOf(newPoolProps.get("url")));
        newPoolProps.remove(DATA_SOURCE_CLASS_NAME);
        newPoolProps.put(DATA_SOURCE_CLASS_NAME, "com.zaxxer.hikari.HikariDataSource");
        appendJdbcQueryProperties(newPoolProps);
        url = String.valueOf(newPoolProps.get("url"));
        username = String.valueOf(newPoolProps.get("username"));
        password = String.valueOf(newPoolProps.get("password"));
        dataSourcePoolProps = new YamlDataSourceConfigurationSwapper().swapToDataSourcePoolProperties(newPoolProps);
    }
    
    private void appendJdbcQueryProperties(final Map<String, Object> poolProps) {
        Optional<JdbcQueryPropertiesExtension> extension = DatabaseTypedSPILoader.findService(JdbcQueryPropertiesExtension.class, databaseType);
        if (!extension.isPresent()) {
            return;
        }
        String jdbcUrl = String.valueOf(poolProps.get("url"));
        Properties queryProps = new StandardJdbcUrlParser().parseQueryProperties(jdbcUrl.contains("?") ? jdbcUrl.substring(jdbcUrl.indexOf("?") + 1) : "");
        extension.get().extendQueryProperties(queryProps);
        String url = new JdbcUrlAppender().appendQueryProperties(jdbcUrl, queryProps);
        poolProps.put("url", url);
    }
    
    @Override
    public Object getDataSourceConfiguration() {
        return dataSourcePoolProps;
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
