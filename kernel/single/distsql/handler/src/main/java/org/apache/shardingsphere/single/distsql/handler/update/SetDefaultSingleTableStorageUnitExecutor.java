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
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleAlterExecutor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.distsql.statement.rdl.SetDefaultSingleTableStorageUnitStatement;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Set default single table storage unit executor.
 */
@Setter
public final class SetDefaultSingleTableStorageUnitExecutor implements DatabaseRuleAlterExecutor<SetDefaultSingleTableStorageUnitStatement, SingleRule, SingleRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private SingleRule rule;
    
    @Override
    public void checkBeforeUpdate(final SetDefaultSingleTableStorageUnitStatement sqlStatement) {
        checkDefaultStorageUnitExist(sqlStatement);
    }
    
    private void checkDefaultStorageUnitExist(final SetDefaultSingleTableStorageUnitStatement sqlStatement) {
        if (!Strings.isNullOrEmpty(sqlStatement.getDefaultStorageUnit())) {
            ShardingSpherePreconditions.checkContains(getAllStorageUnitNames(), sqlStatement.getDefaultStorageUnit(),
                    () -> new MissingRequiredStorageUnitsException(database.getName(), Collections.singleton(sqlStatement.getDefaultStorageUnit())));
        }
    }
    
    private Collection<String> getAllStorageUnitNames() {
        Collection<String> result = new HashSet<>(database.getResourceMetaData().getStorageUnits().keySet());
        result.addAll(
                database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class).stream().flatMap(each -> each.getDataSourceMapper().keySet().stream()).collect(Collectors.toSet()));
        return result;
    }
    
    @Override
    public SingleRuleConfiguration buildToBeAlteredRuleConfiguration(final SetDefaultSingleTableStorageUnitStatement sqlStatement) {
        return new SingleRuleConfiguration(new LinkedList<>(), sqlStatement.getDefaultStorageUnit());
    }
    
    @Override
    public SingleRuleConfiguration buildToBeDroppedRuleConfiguration(final SingleRuleConfiguration toBeAlteredRuleConfig) {
        return toBeAlteredRuleConfig.getDefaultDataSource().isPresent() ? null : new SingleRuleConfiguration(new LinkedList<>(), rule.getConfiguration().getDefaultDataSource().orElse(null));
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
