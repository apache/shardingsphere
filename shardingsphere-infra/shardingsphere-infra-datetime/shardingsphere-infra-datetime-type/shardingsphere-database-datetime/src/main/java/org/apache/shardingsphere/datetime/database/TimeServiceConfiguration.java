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

package org.apache.shardingsphere.datetime.database;

import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRecognizer;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Time service configuration.
 */
@Getter
public final class TimeServiceConfiguration {
    
    private static final TimeServiceConfiguration CONFIG = new TimeServiceConfiguration();
    
    private DatabaseType databaseType;
    
    private DataSource dataSource;
    
    private TimeServiceConfiguration() {
        init();
    }
    
    private void init() {
        URL url = Objects.requireNonNull(getClass().getClassLoader().getResource("time-service.yaml"));
        Map<String, Map<String, Object>> dataSourceConfigs;
        try {
            dataSourceConfigs = YamlEngine.unmarshal(new File(url.getFile()), YamlRootConfiguration.class).getDataSources();
        } catch (final IOException ex) {
            throw new ShardingSphereException("please check your time-service.properties", ex);
        }
        Collection<DataSource> dataSources = new YamlDataSourceConfigurationSwapper().swapToDataSources(dataSourceConfigs).values();
        databaseType = DatabaseTypeRecognizer.getDatabaseType(dataSources);
        dataSource = dataSources.iterator().next();
    }
    
    /**
     * Get configuration instance.
     * 
     * @return time service configuration
     */
    public static TimeServiceConfiguration getInstance() {
        return CONFIG;
    }
}
