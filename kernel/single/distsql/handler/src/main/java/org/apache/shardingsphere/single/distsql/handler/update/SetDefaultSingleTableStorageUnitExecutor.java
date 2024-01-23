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

package org.apache.shardingsphere.single.distsql.handler.update;

import com.google.common.base.Strings;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.database.DatabaseRuleCreateExecutor;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.distsql.statement.rdl.SetDefaultSingleTableStorageUnitStatement;

import java.util.Collection;
import java.util.Collections;

/**
 * Set default single table storage unit executor.
 */
public final class SetDefaultSingleTableStorageUnitExecutor implements DatabaseRuleCreateExecutor<SetDefaultSingleTableStorageUnitStatement, SingleRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final SetDefaultSingleTableStorageUnitStatement sqlStatement, final SingleRuleConfiguration currentRuleConfig) {
        checkStorageUnitExist(database, sqlStatement);
    }
    
    private void checkStorageUnitExist(final ShardingSphereDatabase database, final SetDefaultSingleTableStorageUnitStatement sqlStatement) {
        if (!Strings.isNullOrEmpty(sqlStatement.getDefaultStorageUnit())) {
            Collection<String> storageUnitNames = database.getResourceMetaData().getStorageUnits().keySet();
            ShardingSpherePreconditions.checkState(storageUnitNames.contains(sqlStatement.getDefaultStorageUnit()),
                    () -> new MissingRequiredStorageUnitsException(database.getName(), Collections.singleton(sqlStatement.getDefaultStorageUnit())));
        }
    }
    
    @Override
    public SingleRuleConfiguration buildToBeCreatedRuleConfiguration(final SingleRuleConfiguration currentRuleConfig, final SetDefaultSingleTableStorageUnitStatement sqlStatement) {
        SingleRuleConfiguration result = new SingleRuleConfiguration();
        result.setDefaultDataSource(sqlStatement.getDefaultStorageUnit());
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final SingleRuleConfiguration currentRuleConfig, final SingleRuleConfiguration toBeCreatedRuleConfig) {
        currentRuleConfig.setDefaultDataSource(toBeCreatedRuleConfig.getDefaultDataSource().orElse(null));
    }
    
    @Override
    public Class<SingleRuleConfiguration> getRuleConfigurationClass() {
        return SingleRuleConfiguration.class;
    }
    
    @Override
    public Class<SetDefaultSingleTableStorageUnitStatement> getType() {
        return SetDefaultSingleTableStorageUnitStatement.class;
    }
}
