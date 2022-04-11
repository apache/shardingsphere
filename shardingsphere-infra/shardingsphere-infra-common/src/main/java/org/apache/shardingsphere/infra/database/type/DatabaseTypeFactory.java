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

package org.apache.shardingsphere.infra.database.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.schema.SchemaConfiguration;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Database type factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseTypeFactory {
    
    /**
     * Get database type.
     * 
     * @param schemaConfigs schema configs
     * @param props props
     * @return database type
     */
    public static DatabaseType getDatabaseType(final Map<String, ? extends SchemaConfiguration> schemaConfigs, final ConfigurationProperties props) {
        Optional<DatabaseType> configuredDatabaseType = findConfiguredDatabaseType(props);
        if (configuredDatabaseType.isPresent()) {
            return configuredDatabaseType.get();
        }
        Collection<DataSource> dataSources = schemaConfigs.values().stream()
                .filter(DatabaseTypeFactory::isComplete).findFirst().map(optional -> optional.getDataSources().values()).orElseGet(Collections::emptyList);
        return DatabaseTypeRecognizer.getDatabaseType(dataSources);
    }
    
    private static Optional<DatabaseType> findConfiguredDatabaseType(final ConfigurationProperties props) {
        String configuredDatabaseType = props.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE);
        return configuredDatabaseType.isEmpty() ? Optional.empty() : Optional.of(DatabaseTypeRegistry.getTrunkDatabaseType(configuredDatabaseType));
    }
    
    private static boolean isComplete(final SchemaConfiguration schemaConfig) {
        return !schemaConfig.getRuleConfigurations().isEmpty() && !schemaConfig.getDataSources().isEmpty();
    }
}
