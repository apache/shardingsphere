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

package org.apache.shardingsphere.infra.metadata.engine;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.lock.LockNameUtil;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.mapper.SQLStatementEventMapper;
import org.apache.shardingsphere.infra.metadata.mapper.SQLStatementEventMapperFactory;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresher;
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.SchemaAlteredEvent;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Metadata refresh engine.
 */
public final class MetadataRefreshEngine {
    
    private final ShardingSphereMetaData metaData;
    
    private final SchemaBuilderMaterials materials;
    
    private final ShardingSphereLock shardingSphereLock;
    
    public MetadataRefreshEngine(final ShardingSphereMetaData metaData, final ConfigurationProperties properties, final ShardingSphereLock shardingSphereLock) {
        this.metaData = metaData;
        this.shardingSphereLock = shardingSphereLock;
        materials = new SchemaBuilderMaterials(metaData.getResource().getDatabaseType(), metaData.getResource().getDataSources(), metaData.getRuleMetaData().getRules(), properties);
    }
    
    /**
     * Refresh.
     *
     * @param sqlStatement SQL statement
     * @param routeDataSourceNames route data source names
     * @throws SQLException SQL exception
     */
    public void refresh(final SQLStatement sqlStatement, final Collection<String> routeDataSourceNames) throws SQLException {
        Optional<MetadataRefresher> metadataRefresher = MetadataRefresherFactory.newInstance(sqlStatement);
        if (metadataRefresher.isPresent()) {
            refreshSchema(sqlStatement, routeDataSourceNames, (SchemaRefresher) metadataRefresher.get());
        }
        Optional<SQLStatementEventMapper> sqlStatementEventMapper = SQLStatementEventMapperFactory.newInstance(sqlStatement);
        if (sqlStatementEventMapper.isPresent()) {
            ShardingSphereEventBus.getInstance().post(sqlStatementEventMapper.get().map(sqlStatement));
            // TODO Subscribe and handle DCLStatementEvent
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void refreshSchema(final SQLStatement sqlStatement, final Collection<String> routeDataSourceNames, final SchemaRefresher refresher) throws SQLException {
        if (Objects.nonNull(shardingSphereLock)) {
            refreshSchemaWithLock(sqlStatement, routeDataSourceNames, refresher);
        } else {
            refreshSchemaWithoutLock(sqlStatement, routeDataSourceNames, refresher);
        }
    }
    
    private void refreshSchemaWithLock(final SQLStatement sqlStatement, final Collection<String> routeDataSourceNames, final SchemaRefresher refresher) throws SQLException {
        try {
            if (!shardingSphereLock.tryLock(LockNameUtil.getMetadataRefreshLockName())) {
                throw new ShardingSphereException("Metadata refresh failed.");
            }
            refreshSchemaWithoutLock(sqlStatement, routeDataSourceNames, refresher);
            if (!shardingSphereLock.isReleased(LockNameUtil.getMetadataRefreshLockName())) {
                throw new ShardingSphereException("Metadata refresh failed.");
            }
        } finally {
            shardingSphereLock.releaseLock(LockNameUtil.getMetadataRefreshLockName());
        }
    }
    
    private void refreshSchemaWithoutLock(final SQLStatement sqlStatement, final Collection<String> routeDataSourceNames, final SchemaRefresher refresher) throws SQLException {
        refresher.refresh(metaData.getSchema(), routeDataSourceNames, sqlStatement, materials);
        ShardingSphereEventBus.getInstance().post(new SchemaAlteredEvent(metaData.getName(), metaData.getSchema()));
    }
}
