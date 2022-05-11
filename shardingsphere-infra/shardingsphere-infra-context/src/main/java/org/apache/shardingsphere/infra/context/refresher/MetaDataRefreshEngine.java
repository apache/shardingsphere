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

package org.apache.shardingsphere.infra.context.refresher;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Meta data refresh engine.
 */
@RequiredArgsConstructor
public final class MetaDataRefreshEngine {
    
    private static final Set<Class<? extends SQLStatement>> IGNORABLE_SQL_STATEMENT_CLASSES = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    private final ShardingSphereMetaData metaData;
    
    private final FederationDatabaseMetaData federationMetaData;
    
    private final Map<String, OptimizerPlannerContext> optimizerPlanners;
    
    private final ConfigurationProperties props;
    
    /**
     * Refresh.
     *
     * @param sqlStatementContext SQL statement context
     * @param logicDataSourceNamesSupplier logic data source names supplier
     * @throws SQLException SQL exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void refresh(final SQLStatementContext<?> sqlStatementContext, final Supplier<Collection<String>> logicDataSourceNamesSupplier) throws SQLException {
        Class<? extends SQLStatement> sqlStatementClass = sqlStatementContext.getSqlStatement().getClass();
        if (IGNORABLE_SQL_STATEMENT_CLASSES.contains(sqlStatementClass)) {
            return;
        }
        Optional<MetaDataRefresher> schemaRefresher = MetaDataRefresherFactory.findInstance(sqlStatementClass);
        if (schemaRefresher.isPresent()) {
            String schemaName = sqlStatementContext.getTablesContext().getSchemaName().orElseGet(() -> sqlStatementContext.getDatabaseType().getDefaultSchema(metaData.getDatabaseName()));
            schemaRefresher.get().refresh(metaData, federationMetaData, optimizerPlanners, logicDataSourceNamesSupplier.get(), schemaName, sqlStatementContext.getSqlStatement(), props);
        } else {
            IGNORABLE_SQL_STATEMENT_CLASSES.add(sqlStatementClass);
        }
    }
}
