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

package org.apache.shardingsphere.broadcast.distsql.handler.update;

import lombok.Setter;
import org.apache.shardingsphere.broadcast.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.distsql.statement.CreateBroadcastTableRuleStatement;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleCreateExecutor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Create broadcast table rule executor.
 */
@Setter
public final class CreateBroadcastTableRuleExecutor implements DatabaseRuleCreateExecutor<CreateBroadcastTableRuleStatement, BroadcastRule, BroadcastRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private BroadcastRule rule;
    
    @Override
    public void checkBeforeUpdate(final CreateBroadcastTableRuleStatement sqlStatement) {
        ShardingSpherePreconditions.checkNotEmpty(database.getResourceMetaData().getStorageUnits(), () -> new EmptyStorageUnitException(database.getName()));
        if (!sqlStatement.isIfNotExists()) {
            checkDuplicate(sqlStatement);
        }
    }
    
    private void checkDuplicate(final CreateBroadcastTableRuleStatement sqlStatement) {
        ShardingSpherePreconditions.checkMustEmpty(getDuplicatedRuleNames(sqlStatement), () -> new DuplicateRuleException("Broadcast", sqlStatement.getTables()));
    }
    
    private Collection<String> getDuplicatedRuleNames(final CreateBroadcastTableRuleStatement sqlStatement) {
        Collection<String> result = new HashSet<>(null == rule ? Collections.emptySet() : rule.getTables());
        result.retainAll(sqlStatement.getTables());
        return result;
    }
    
    @Override
    public BroadcastRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateBroadcastTableRuleStatement sqlStatement) {
        BroadcastRuleConfiguration result = new BroadcastRuleConfiguration(new HashSet<>(null == rule ? Collections.emptySet() : rule.getTables()));
        result.getTables().addAll(getToBeCreatedRuleNames(sqlStatement));
        return result;
    }
    
    private Collection<String> getToBeCreatedRuleNames(final CreateBroadcastTableRuleStatement sqlStatement) {
        Collection<String> result = sqlStatement.getTables();
        if (sqlStatement.isIfNotExists()) {
            result.removeIf(getDuplicatedRuleNames(sqlStatement)::contains);
        }
        return result;
    }
    
    @Override
    public Class<BroadcastRule> getRuleClass() {
        return BroadcastRule.class;
    }
    
    @Override
    public Class<CreateBroadcastTableRuleStatement> getType() {
        return CreateBroadcastTableRuleStatement.class;
    }
}
