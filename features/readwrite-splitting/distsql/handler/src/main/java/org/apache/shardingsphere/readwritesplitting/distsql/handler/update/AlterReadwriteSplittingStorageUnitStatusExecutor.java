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
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorClusterModeRequired;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.storage.service.StorageNodeStatusService;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.exception.InvalidStorageUnitStatusException;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.exception.MissingRequiredReadStorageUnitException;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.AlterReadwriteSplittingStorageUnitStatusStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceRule;
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
        Optional<ReadwriteSplittingDataSourceRule> dataSourceRule = rule.getDataSourceRules().values().stream().filter(each -> each.getName().equalsIgnoreCase(sqlStatement.getRuleName())).findAny();
        ShardingSpherePreconditions.checkState(dataSourceRule.isPresent(), () -> new MissingRequiredRuleException("Readwrite-splitting", database.getName(), sqlStatement.getRuleName()));
        ShardingSpherePreconditions.checkState(dataSourceRule.get().getReadwriteSplittingGroup().getReadDataSources().contains(sqlStatement.getStorageUnitName()),
                () -> new MissingRequiredReadStorageUnitException(dataSourceRule.get().getName(), sqlStatement.getStorageUnitName()));
        if (sqlStatement.isEnable()) {
            ShardingSpherePreconditions.checkState(dataSourceRule.get().getDisabledDataSourceNames().contains(sqlStatement.getStorageUnitName()),
                    () -> new InvalidStorageUnitStatusException("the storage unit is not disabled"));
        } else {
            ShardingSpherePreconditions.checkState(!dataSourceRule.get().getDisabledDataSourceNames().contains(sqlStatement.getStorageUnitName()),
                    () -> new InvalidStorageUnitStatusException("the storage unit is already disabled"));
        }
    }
    
    private void updateStatus(final ContextManager contextManager, final AlterReadwriteSplittingStorageUnitStatusStatement sqlStatement) {
        DataSourceState status = sqlStatement.isEnable() ? DataSourceState.ENABLED : DataSourceState.DISABLED;
        new StorageNodeStatusService(contextManager.getMetaDataContexts().getPersistService().getRepository())
                .changeMemberStorageNodeStatus(database.getName(), sqlStatement.getRuleName(), sqlStatement.getStorageUnitName(), status);
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
