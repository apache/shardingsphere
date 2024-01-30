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

package org.apache.shardingsphere.distsql.handler.type.rdl.rule.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.engine.database.DatabaseRuleDefinitionExecuteEngine;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.engine.global.GlobalRuleDefinitionExecuteEngine;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.engine.legacy.LegacyGlobalRuleDefinitionExecuteEngine;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.spi.global.GlobalRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.handler.util.DatabaseNameUtils;
import org.apache.shardingsphere.distsql.statement.rdl.rule.RuleDefinitionStatement;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.readwritesplitting.distsql.segment.ReadwriteSplittingRuleSegment;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

/**
 * Rule definition execute engine.
 */
@RequiredArgsConstructor
public abstract class RuleDefinitionExecuteEngine {
    
    private final RuleDefinitionStatement sqlStatement;
    
    private final String currentDatabaseName;
    
    private final ContextManager contextManager;
    
    /**
     * Execute update.
     */
    @SuppressWarnings("rawtypes")
    public void executeUpdate() {
        Optional<DatabaseRuleDefinitionExecutor> databaseExecutor = TypedSPILoader.findService(DatabaseRuleDefinitionExecutor.class, sqlStatement.getClass());
        if (databaseExecutor.isPresent()) {
            new DatabaseRuleDefinitionExecuteEngine(
                    sqlStatement, contextManager, getDatabase(DatabaseNameUtils.getDatabaseName(sqlStatement, currentDatabaseName)), databaseExecutor.get()).executeUpdate();
        } else {
            String modeType = contextManager.getInstanceContext().getModeConfiguration().getType();
            GlobalRuleDefinitionExecutor globalExecutor = TypedSPILoader.getService(GlobalRuleDefinitionExecutor.class, sqlStatement.getClass());
            if ("Cluster".equals(modeType) || "Standalone".equals(modeType)) {
                new GlobalRuleDefinitionExecuteEngine(sqlStatement, contextManager, globalExecutor).executeUpdate();
            } else {
                new LegacyGlobalRuleDefinitionExecuteEngine(sqlStatement, contextManager).executeUpdate();
            }
        }
    }
    
    /**
     * Checks the data sources in the given ShardingSphereDatabase.
     *
     * @param database The ShardingSphereDatabase to check.
     * @throws EmptyStorageUnitException If the storage units in the database are empty.
     */
    public static void checkDataSources(final ShardingSphereDatabase database) {
        ShardingSpherePreconditions.checkState(!database.getResourceMetaData().getStorageUnits().isEmpty(), () -> new EmptyStorageUnitException(database.getName()));
    }
    
    /**
     * Checks the existence of required data sources in the provided ShardingSphereDatabase.
     *
     * @param databaseName          The name of the database being checked.
     * @param segments              Collection of ReadwriteSplittingRuleSegment instances.
     * @param database              The ShardingSphereDatabase instance to check against.
     * @throws MissingRequiredStorageUnitsException if any required data sources do not exist.
     */
    public static void checkDataSources(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments, final ShardingSphereDatabase database) {
        Collection<String> requiredDataSources = new LinkedHashSet<>();
        segments.forEach(each -> {
            requiredDataSources.add(each.getWriteDataSource());
            requiredDataSources.addAll(each.getReadDataSources());
        });
        Collection<String> notExistedDataSources = database.getResourceMetaData().getNotExistedDataSources(requiredDataSources);
        ShardingSpherePreconditions.checkState(notExistedDataSources.isEmpty(), () -> new MissingRequiredStorageUnitsException(databaseName, notExistedDataSources));
    }
    
    protected abstract ShardingSphereDatabase getDatabase(String databaseName);
}
