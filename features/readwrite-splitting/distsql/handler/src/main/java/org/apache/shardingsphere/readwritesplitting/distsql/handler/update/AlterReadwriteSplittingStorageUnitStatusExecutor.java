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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.update;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorClusterModeRequired;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.InvalidStorageUnitStatusException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.storage.service.QualifiedDataSourceStatusService;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingDataSourceType;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.AlterReadwriteSplittingStorageUnitStatusStatement;
import org.apache.shardingsphere.readwritesplitting.exception.ReadwriteSplittingRuleExceptionIdentifier;
import org.apache.shardingsphere.readwritesplitting.exception.actual.ReadwriteSplittingActualDataSourceNotFoundException;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceGroupRule;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;

import java.util.Optional;

/**
 * Alter readwrite-splitting storage unit status executor.
 */
@DistSQLExecutorClusterModeRequired
@Setter
public final class AlterReadwriteSplittingStorageUnitStatusExecutor
        implements
            DistSQLUpdateExecutor<AlterReadwriteSplittingStorageUnitStatusStatement>,
            DistSQLExecutorDatabaseAware,
            DistSQLExecutorRuleAware<ReadwriteSplittingRule> {
    
    private ShardingSphereDatabase database;
    
    private ReadwriteSplittingRule rule;
    
    @Override
    public void executeUpdate(final AlterReadwriteSplittingStorageUnitStatusStatement sqlStatement, final ContextManager contextManager) {
        checkBeforeUpdate(sqlStatement);
        updateStatus(contextManager, sqlStatement);
    }
    
    private void checkBeforeUpdate(final AlterReadwriteSplittingStorageUnitStatusStatement sqlStatement) {
        Optional<ReadwriteSplittingDataSourceGroupRule> dataSourceGroupRule = rule.getDataSourceRuleGroups().values().stream()
                .filter(each -> each.getName().equalsIgnoreCase(sqlStatement.getRuleName())).findAny();
        ShardingSpherePreconditions.checkState(dataSourceGroupRule.isPresent(), () -> new MissingRequiredRuleException("Readwrite-splitting", database.getName(), sqlStatement.getRuleName()));
        ShardingSpherePreconditions.checkContains(dataSourceGroupRule.get().getReadwriteSplittingGroup().getReadDataSources(), sqlStatement.getStorageUnitName(),
                () -> new ReadwriteSplittingActualDataSourceNotFoundException(ReadwriteSplittingDataSourceType.READ,
                        sqlStatement.getStorageUnitName(), new ReadwriteSplittingRuleExceptionIdentifier(database.getName(), dataSourceGroupRule.get().getName())));
        if (sqlStatement.isEnable()) {
            ShardingSpherePreconditions.checkContains(dataSourceGroupRule.get().getDisabledDataSourceNames(), sqlStatement.getStorageUnitName(),
                    () -> new InvalidStorageUnitStatusException("storage unit is not disabled"));
        } else {
            ShardingSpherePreconditions.checkNotContains(dataSourceGroupRule.get().getDisabledDataSourceNames(), sqlStatement.getStorageUnitName(),
                    () -> new InvalidStorageUnitStatusException("storage unit is already disabled"));
        }
    }
    
    private void updateStatus(final ContextManager contextManager, final AlterReadwriteSplittingStorageUnitStatusStatement sqlStatement) {
        DataSourceState status = sqlStatement.isEnable() ? DataSourceState.ENABLED : DataSourceState.DISABLED;
        new QualifiedDataSourceStatusService(contextManager.getMetaDataContexts().getPersistService().getRepository())
                .changeStatus(database.getName(), sqlStatement.getRuleName(), sqlStatement.getStorageUnitName(), status);
    }
    
    @Override
    public Class<ReadwriteSplittingRule> getRuleClass() {
        return ReadwriteSplittingRule.class;
    }
    
    @Override
    public Class<AlterReadwriteSplittingStorageUnitStatusStatement> getType() {
        return AlterReadwriteSplittingStorageUnitStatusStatement.class;
    }
}
