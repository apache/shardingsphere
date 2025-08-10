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

package org.apache.shardingsphere.distsql.handler.executor.rql.rule;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.rql.rule.database.CountRuleStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Rule count executor.
 */
@Setter
public final class RuleCountExecutor implements DistSQLQueryExecutor<CountRuleStatement>, DistSQLExecutorDatabaseAware {
    
    private ShardingSphereDatabase database;
    
    @Override
    public Collection<String> getColumnNames(final CountRuleStatement sqlStatement) {
        return Arrays.asList("rule_name", "database", "count");
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final CountRuleStatement sqlStatement, final ContextManager contextManager) {
        Optional<CountResultRowBuilder> rowBuilder = TypedSPILoader.findService(CountResultRowBuilder.class, sqlStatement.getType());
        if (!rowBuilder.isPresent()) {
            return Collections.emptyList();
        }
        Optional<ShardingSphereRule> rule = database.getRuleMetaData().findSingleRule(rowBuilder.get().getRuleClass());
        return rule.isPresent() ? rowBuilder.get().generateRows(rule.get(), database.getName()) : Collections.emptyList();
    }
    
    @Override
    public Class<CountRuleStatement> getType() {
        return CountRuleStatement.class;
    }
}
