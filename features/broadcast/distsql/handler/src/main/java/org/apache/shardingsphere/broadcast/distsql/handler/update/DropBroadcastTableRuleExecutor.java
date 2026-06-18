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

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.Setter;
import org.apache.shardingsphere.broadcast.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.distsql.statement.DropBroadcastTableRuleStatement;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Drop broadcast table rule executor.
 */
@DistSQLExecutorCurrentRuleRequired(BroadcastRule.class)
@Setter
public final class DropBroadcastTableRuleExecutor implements DatabaseRuleDropExecutor<DropBroadcastTableRuleStatement, BroadcastRule, BroadcastRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private BroadcastRule rule;
    
    @Override
    public void checkBeforeUpdate(final DropBroadcastTableRuleStatement sqlStatement) {
        if (!sqlStatement.isIfExists()) {
            checkBroadcastTableExist(sqlStatement);
        }
    }
    
    private void checkBroadcastTableExist(final DropBroadcastTableRuleStatement sqlStatement) {
        Collection<String> currentTableNames = new CaseInsensitiveSet<>(rule.getConfiguration().getTables());
        Collection<String> notExistedTableNames = sqlStatement.getTables().stream().filter(each -> !currentTableNames.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkMustEmpty(notExistedTableNames, () -> new MissingRequiredRuleException("Broadcast", database.getName(), notExistedTableNames));
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropBroadcastTableRuleStatement sqlStatement) {
        return !Collections.disjoint(rule.getConfiguration().getTables(), sqlStatement.getTables());
    }
    
    @Override
    public BroadcastRuleConfiguration buildToBeAlteredRuleConfiguration(final DropBroadcastTableRuleStatement sqlStatement) {
        BroadcastRuleConfiguration result = new BroadcastRuleConfiguration(new HashSet<>(rule.getConfiguration().getTables()));
        Collection<String> toBeDroppedTableNames = new CaseInsensitiveSet<>(sqlStatement.getTables());
        result.getTables().removeIf(toBeDroppedTableNames::contains);
        return result;
    }
    
    @Override
    public Class<BroadcastRule> getRuleClass() {
        return BroadcastRule.class;
    }
    
    @Override
    public Class<DropBroadcastTableRuleStatement> getType() {
        return DropBroadcastTableRuleStatement.class;
    }
}
