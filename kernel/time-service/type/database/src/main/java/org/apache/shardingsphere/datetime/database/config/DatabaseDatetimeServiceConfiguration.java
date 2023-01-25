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

package org.apache.shardingsphere.datetime.database.config;

import lombok.Getter;
import org.apache.shardingsphere.datetime.database.exception.DatetimeConfigurationFileNotFoundException;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.yaml.snakeyaml.Yaml;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * Database datetime service configuration.
 */
@Getter
public final class DatabaseDatetimeServiceConfiguration {
    
    private static final DatabaseDatetimeServiceConfiguration INSTANCE = new DatabaseDatetimeServiceConfiguration();
    
    private static final String CONFIG_FILE = "datetime-database-config.yaml";
    
    private final DataSource dataSource;
    
    private final DatabaseType storageType;
    
    private DatabaseDatetimeServiceConfiguration() {
        dataSource = DataSourcePoolCreator.create(new YamlDataSourceConfigurationSwapper().swapToDataSourceProperties(loadDataSourceConfiguration()));
        storageType = DatabaseTypeEngine.getStorageType(Collections.singletonList(dataSource));
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> loadDataSourceConfiguration() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            return new Yaml().loadAs(inputStream, Map.class);
        } catch (final IOException ex) {
            throw new DatetimeConfigurationFileNotFoundException(CONFIG_FILE);
        }
    }
    
    /**
     * Get time service configuration instance.
     * 
     * @return time service configuration
     */
    public static DatabaseDatetimeServiceConfiguration getInstance() {
        return INSTANCE;
    }
}
