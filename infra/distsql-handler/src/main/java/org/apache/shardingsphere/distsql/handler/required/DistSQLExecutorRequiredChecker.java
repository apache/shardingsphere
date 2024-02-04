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

package org.apache.shardingsphere.distsql.handler.required;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.statement.rdl.rule.type.DropRuleStatement;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Optional;

/**
 * DistSQL executor required checker.
 */
@RequiredArgsConstructor
public final class DistSQLExecutorRequiredChecker {
    
    private final Object executor;
    
    /**
     * Check before DistSQL execute.
     * 
     * @param sqlStatement SQL statement
     * @param contextManager context manager
     * @param databaseName database name
     * @param rule rule
     */
    public void check(final SQLStatement sqlStatement, final ContextManager contextManager, final String databaseName, final ShardingSphereRule rule) {
        Optional.ofNullable(executor.getClass().getAnnotation(DistSQLExecutorClusterModeRequired.class)).ifPresent(optional -> checkClusterMode(contextManager));
        Optional.ofNullable(executor.getClass().getAnnotation(DistSQLExecutorCurrentRuleRequired.class)).ifPresent(optional -> checkCurrentRule(sqlStatement, databaseName, rule, optional));
    }
    
    private void checkClusterMode(final ContextManager contextManager) {
        ShardingSpherePreconditions.checkState(contextManager.getInstanceContext().isCluster(), () -> new UnsupportedSQLOperationException("Mode must be `Cluster`."));
    }
    
    private void checkCurrentRule(final SQLStatement sqlStatement, final String databaseName, final ShardingSphereRule rule, final DistSQLExecutorCurrentRuleRequired currentRuleRequired) {
        if (sqlStatement instanceof DropRuleStatement && ((DropRuleStatement) sqlStatement).isIfExists()) {
            return;
        }
        ShardingSpherePreconditions.checkNotNull(rule, () -> new MissingRequiredRuleException(currentRuleRequired.value(), databaseName));
    }
}
