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

package org.apache.shardingsphere.single.distsql.handler.query;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.single.distsql.statement.rql.ShowDefaultSingleTableStorageUnitStatement;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.util.Collection;
import java.util.Collections;

/**
 * Show default single table storage unit executor.
 */
@Setter
public final class ShowDefaultSingleTableStorageUnitExecutor implements DistSQLQueryExecutor<ShowDefaultSingleTableStorageUnitStatement>, DistSQLExecutorRuleAware<SingleRule> {
    
    private SingleRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowDefaultSingleTableStorageUnitStatement sqlStatement) {
        return Collections.singleton("storage_unit_name");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowDefaultSingleTableStorageUnitStatement sqlStatement, final ContextManager contextManager) {
        return Collections.singleton(new LocalDataQueryResultRow(rule.getConfiguration().getDefaultDataSource().orElse("RANDOM")));
    }
    
    @Override
    public Class<ShowDefaultSingleTableStorageUnitStatement> getType() {
        return ShowDefaultSingleTableStorageUnitStatement.class;
    }
    
    @Override
    public Class<SingleRule> getRuleClass() {
        return SingleRule.class;
    }
}
