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

package org.apache.shardingsphere.mode.metadata.refresher;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.persist.service.divided.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.RenameTableStatement;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Meta data refresh engine.
 */
@RequiredArgsConstructor
public final class MetaDataRefreshEngine {
    
    private static final Collection<Class<? extends DDLStatement>> DDL_STATEMENT_CLASSES = Arrays.asList(CreateTableStatement.class, AlterTableStatement.class, DropTableStatement.class,
            CreateViewStatement.class, AlterViewStatement.class, DropViewStatement.class, CreateIndexStatement.class, AlterIndexStatement.class, DropIndexStatement.class, CreateSchemaStatement.class,
            AlterSchemaStatement.class, DropSchemaStatement.class, RenameTableStatement.class);
    
    private final MetaDataManagerPersistService metaDataManagerPersistService;
    
    private final ShardingSphereDatabase database;
    
    private final ConfigurationProperties props;
    
    /**
     * Refresh meta data.
     *
     * @param sqlStatementContext SQL statement context
     * @param routeUnits route units
     * @throws SQLException SQL exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void refresh(final SQLStatementContext sqlStatementContext, final Collection<RouteUnit> routeUnits) throws SQLException {
        Class sqlStatementClass = sqlStatementContext.getSqlStatement().getClass().getSuperclass();
        if (!DDL_STATEMENT_CLASSES.contains(sqlStatementClass)) {
            return;
        }
        Optional<MetaDataRefresher> schemaRefresher = TypedSPILoader.findService(MetaDataRefresher.class, sqlStatementClass);
        if (schemaRefresher.isPresent()) {
            Collection<String> logicDataSourceNames = routeUnits.stream().map(each -> each.getDataSourceMapper().getLogicName()).collect(Collectors.toList());
            String schemaName = sqlStatementContext instanceof TableAvailable ? getSchemaName(sqlStatementContext) : null;
            DatabaseType databaseType = routeUnits.stream().map(each -> database.getResourceMetaData().getStorageUnits().get(each.getDataSourceMapper().getActualName()))
                    .filter(Objects::nonNull).findFirst().map(StorageUnit::getStorageType).orElseGet(sqlStatementContext::getDatabaseType);
            schemaRefresher.get().refresh(metaDataManagerPersistService, database, logicDataSourceNames, schemaName, databaseType, sqlStatementContext.getSqlStatement(), props);
        }
    }
    
    /**
     * Refresh meta data for federation.
     *
     * @param sqlStatementContext SQL statement context
     */
    @SuppressWarnings("unchecked")
    public void refresh(final SQLStatementContext sqlStatementContext) {
        getFederationMetaDataRefresher(sqlStatementContext).ifPresent(optional -> optional.refresh(
                metaDataManagerPersistService, database, getSchemaName(sqlStatementContext), sqlStatementContext.getDatabaseType(), sqlStatementContext.getSqlStatement()));
    }
    
    private String getSchemaName(final SQLStatementContext sqlStatementContext) {
        return ((TableAvailable) sqlStatementContext).getTablesContext().getSchemaName()
                .orElseGet(() -> new DatabaseTypeRegistry(sqlStatementContext.getDatabaseType()).getDefaultSchemaName(database.getName())).toLowerCase();
    }
    
    /**
     * SQL statement is federation or not.
     *
     * @param sqlStatementContext SQL statement context
     * @return is federation or not
     */
    public boolean isFederation(final SQLStatementContext sqlStatementContext) {
        return getFederationMetaDataRefresher(sqlStatementContext).isPresent();
    }
    
    @SuppressWarnings("rawtypes")
    private Optional<FederationMetaDataRefresher> getFederationMetaDataRefresher(final SQLStatementContext sqlStatementContext) {
        return TypedSPILoader.findService(FederationMetaDataRefresher.class, sqlStatementContext.getSqlStatement().getClass().getSuperclass());
    }
    
    /**
     * Is refresh meta data required.
     *
     * @param sqlStatementContext SQL statement context
     * @return is refresh meta data required or not
     */
    public static boolean isRefreshMetaDataRequired(final SQLStatementContext sqlStatementContext) {
        return DDL_STATEMENT_CLASSES.contains(sqlStatementContext.getSqlStatement().getClass().getSuperclass());
    }
}
