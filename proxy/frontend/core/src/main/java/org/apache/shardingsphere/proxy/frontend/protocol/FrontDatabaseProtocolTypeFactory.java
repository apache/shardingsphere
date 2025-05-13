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

package org.apache.shardingsphere.proxy.frontend.protocol;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;

import java.util.Collection;
import java.util.Optional;

/**
 * Front database protocol type factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FrontDatabaseProtocolTypeFactory {
    
    /**
     * Get front database protocol type.
     *
     * @return front database protocol type
     */
    public static DatabaseType getDatabaseType() {
        Optional<DatabaseType> configuredDatabaseType = findConfiguredDatabaseType();
        if (configuredDatabaseType.isPresent()) {
            return configuredDatabaseType.get();
        }
        Collection<DatabaseProtocolFrontendEngine> databaseProtocolFrontendEngine = ShardingSphereServiceLoader.getServiceInstances(DatabaseProtocolFrontendEngine.class);
        ShardingSpherePreconditions.checkNotEmpty(databaseProtocolFrontendEngine,
                () -> new IllegalArgumentException(String.format("Can not find any SPI implementation of `%s`.", DatabaseProtocolFrontendEngine.class.getName())));
        DatabaseProtocolFrontendEngine defaultDatabaseProtocolFrontendEngine = databaseProtocolFrontendEngine.iterator().next();
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        if (metaDataContexts.getMetaData().getAllDatabases().isEmpty()) {
            return defaultDatabaseProtocolFrontendEngine.getType();
        }
        Optional<ShardingSphereDatabase> database = metaDataContexts.getMetaData().getAllDatabases().stream().filter(ShardingSphereDatabase::containsDataSource).findFirst();
        return database.isPresent() ? database.get().getResourceMetaData().getStorageUnits().values().iterator().next().getStorageType() : defaultDatabaseProtocolFrontendEngine.getType();
    }
    
    private static Optional<DatabaseType> findConfiguredDatabaseType() {
        DatabaseType configuredDatabaseType = ProxyContext.getInstance()
                .getContextManager().getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE);
        return Optional.ofNullable(configuredDatabaseType);
    }
}
