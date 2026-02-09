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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropViewStatement;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Push down meta data refresh engine.
 */
@RequiredArgsConstructor
public final class PushDownMetaDataRefreshEngine {
    
    private static final Collection<Class<?>> SUPPORTED_REFRESH_TYPES = new HashSet<>(Arrays.asList(CreateViewStatement.class, AlterViewStatement.class, DropViewStatement.class,
            CreateIndexStatement.class, AlterIndexStatement.class, DropIndexStatement.class,
            CreateSchemaStatement.class, AlterSchemaStatement.class, DropSchemaStatement.class,
            CreateTableStatement.class, AlterTableStatement.class, DropTableStatement.class, RenameTableStatement.class));
    
    private final SQLStatementContext sqlStatementContext;
    
    /**
     * Whether to need refresh meta data.
     *
     * @return is need refresh meta data or not
     */
    public boolean isNeedRefresh() {
        return SUPPORTED_REFRESH_TYPES.contains(sqlStatementContext.getSqlStatement().getClass());
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
        Optional<PushDownMetaDataRefresher> refresher = findPushDownMetaDataRefresher(sqlStatementContext);
        if (!refresher.isPresent()) {
            return;
        }
        Collection<String> logicDataSourceNames = routeUnits.stream().map(each -> each.getDataSourceMapper().getLogicName()).collect(Collectors.toList());
        String schemaName = SchemaRefreshUtils.getSchemaName(database, sqlStatementContext);
        DatabaseType databaseType = routeUnits.stream().map(each -> database.getResourceMetaData().getStorageUnits().get(each.getDataSourceMapper().getActualName()))
                .filter(Objects::nonNull).findFirst().map(StorageUnit::getStorageType).orElseGet(() -> sqlStatementContext.getSqlStatement().getDatabaseType());
        refresher.get().refresh(metaDataManagerPersistService, database,
                logicDataSourceNames.isEmpty() ? null : logicDataSourceNames.iterator().next(), schemaName, databaseType, sqlStatementContext.getSqlStatement(), props);
    }
    
    @SuppressWarnings("rawtypes")
    private Optional<PushDownMetaDataRefresher> findPushDownMetaDataRefresher(final SQLStatementContext sqlStatementContext) {
        Optional<PushDownMetaDataRefresher> refresher = TypedSPILoader.findService(PushDownMetaDataRefresher.class, sqlStatementContext.getSqlStatement().getClass());
        return refresher.isPresent() ? refresher : TypedSPILoader.findService(PushDownMetaDataRefresher.class, sqlStatementContext.getSqlStatement().getClass().getSuperclass());
    }
}
