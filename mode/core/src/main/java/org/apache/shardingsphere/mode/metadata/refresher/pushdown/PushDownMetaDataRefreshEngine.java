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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.refresher.util.SchemaRefreshUtils;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Push down meta data refresh engine.
 */
@RequiredArgsConstructor
public final class PushDownMetaDataRefreshEngine {
    
    private final SQLStatementContext sqlStatementContext;
    
    @SuppressWarnings("rawtypes")
    private final PushDownMetaDataRefresher refresher;
    
    public PushDownMetaDataRefreshEngine(final SQLStatementContext sqlStatementContext) {
        this.sqlStatementContext = sqlStatementContext;
        refresher = findPushDownMetaDataRefresher(sqlStatementContext).orElse(null);
    }
    
    @SuppressWarnings("rawtypes")
    private Optional<PushDownMetaDataRefresher> findPushDownMetaDataRefresher(final SQLStatementContext sqlStatementContext) {
        Optional<PushDownMetaDataRefresher> refresher = TypedSPILoader.findService(PushDownMetaDataRefresher.class, sqlStatementContext.getSqlStatement().getClass());
        return refresher.isPresent() ? refresher : TypedSPILoader.findService(PushDownMetaDataRefresher.class, sqlStatementContext.getSqlStatement().getClass().getSuperclass());
    }
    
    /**
     * Whether to need refresh meta data.
     *
     * @return is need refresh meta data or not
     */
    public boolean isNeedRefresh() {
        return null != refresher;
    }
    
    /**
     * Refresh push down meta data.
     * 
     * @param metaDataManagerPersistService meta data manager persist service
     * @param database database
     * @param props configuration properties
     * @param routeUnits route units
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("unchecked")
    public void refresh(final MetaDataManagerPersistService metaDataManagerPersistService,
                        final ShardingSphereDatabase database, final ConfigurationProperties props, final Collection<RouteUnit> routeUnits) throws SQLException {
        Collection<String> logicDataSourceNames = routeUnits.stream().map(each -> each.getDataSourceMapper().getLogicName()).collect(Collectors.toList());
        String schemaName = SchemaRefreshUtils.getSchemaName(database, sqlStatementContext);
        DatabaseType databaseType = routeUnits.stream().map(each -> database.getResourceMetaData().getStorageUnits().get(each.getDataSourceMapper().getActualName()))
                .filter(Objects::nonNull).findFirst().map(StorageUnit::getStorageType).orElseGet(() -> sqlStatementContext.getSqlStatement().getDatabaseType());
        refresher.refresh(metaDataManagerPersistService, database,
                logicDataSourceNames.isEmpty() ? null : logicDataSourceNames.iterator().next(), schemaName, databaseType, sqlStatementContext.getSqlStatement(), props);
    }
}
