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
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.service.persist.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Meta data refresh engine.
 */
@RequiredArgsConstructor
public final class MetaDataRefreshEngine {
    
    private static final Collection<Class<? extends SQLStatement>> IGNORED_SQL_STATEMENT_CLASSES = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
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
        Class<? extends SQLStatement> sqlStatementClass = sqlStatementContext.getSqlStatement().getClass();
        if (IGNORED_SQL_STATEMENT_CLASSES.contains(sqlStatementClass)) {
            return;
        }
        Optional<MetaDataRefresher> schemaRefresher = TypedSPILoader.findService(MetaDataRefresher.class, sqlStatementClass.getSuperclass());
        if (schemaRefresher.isPresent()) {
            String schemaName = sqlStatementContext.getTablesContext().getSchemaName()
                    .orElseGet(() -> new DatabaseTypeRegistry(sqlStatementContext.getDatabaseType()).getDefaultSchemaName(database.getName())).toLowerCase();
            Collection<String> logicDataSourceNames = routeUnits.stream().map(each -> each.getDataSourceMapper().getLogicName()).collect(Collectors.toList());
            schemaRefresher.get().refresh(metaDataManagerPersistService, database, logicDataSourceNames, schemaName, sqlStatementContext.getDatabaseType(),
                    sqlStatementContext.getSqlStatement(), props);
            return;
        }
        IGNORED_SQL_STATEMENT_CLASSES.add(sqlStatementClass);
    }
}
