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
import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleCreateExecutor;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.distsql.statement.rdl.SetDefaultSingleTableStorageUnitStatement;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.util.Collection;
import java.util.Collections;

/**
 * Set default single table storage unit executor.
 */
@Setter
public final class SetDefaultSingleTableStorageUnitExecutor implements DatabaseRuleCreateExecutor<SetDefaultSingleTableStorageUnitStatement, SingleRule, SingleRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private SingleRule rule;
    
    @Override
    public void checkBeforeUpdate(final SetDefaultSingleTableStorageUnitStatement sqlStatement) {
        checkStorageUnitExist(sqlStatement);
    }
    
    private void checkStorageUnitExist(final SetDefaultSingleTableStorageUnitStatement sqlStatement) {
        if (!Strings.isNullOrEmpty(sqlStatement.getDefaultStorageUnit())) {
            Collection<String> storageUnitNames = database.getResourceMetaData().getStorageUnits().keySet();
            ShardingSpherePreconditions.checkState(storageUnitNames.contains(sqlStatement.getDefaultStorageUnit()),
                    () -> new MissingRequiredStorageUnitsException(database.getName(), Collections.singleton(sqlStatement.getDefaultStorageUnit())));
        }
    }
    
    @Override
    public SingleRuleConfiguration buildToBeCreatedRuleConfiguration(final SetDefaultSingleTableStorageUnitStatement sqlStatement) {
        SingleRuleConfiguration result = new SingleRuleConfiguration();
        result.setDefaultDataSource(sqlStatement.getDefaultStorageUnit());
        result.getTables().addAll(rule.getConfiguration().getTables());
        return result;
    }
    
    @Override
    public Class<SingleRule> getRuleClass() {
        return SingleRule.class;
    }
    
    @Override
    public Class<SetDefaultSingleTableStorageUnitStatement> getType() {
        return SetDefaultSingleTableStorageUnitStatement.class;
    }
}
