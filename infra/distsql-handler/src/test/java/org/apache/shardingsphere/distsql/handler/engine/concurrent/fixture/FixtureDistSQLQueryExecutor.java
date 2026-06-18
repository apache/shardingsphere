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

package org.apache.shardingsphere.distsql.handler.engine.concurrent.fixture;

import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * Fixture DistSQL query executor.
 */
public final class FixtureDistSQLQueryExecutor implements DistSQLQueryExecutor<FixtureDistSQLQueryStatement>, DistSQLExecutorRuleAware<ShardingSphereRule> {
    
    private ShardingSphereRule rule;
    
    @Override
    public void setRule(final ShardingSphereRule rule) {
        this.rule = rule;
    }
    
    @Override
    public Class<ShardingSphereRule> getRuleClass() {
        return ShardingSphereRule.class;
    }
    
    @Override
    public Collection<String> getColumnNames(final FixtureDistSQLQueryStatement sqlStatement) {
        checkRule(sqlStatement);
        return Collections.singleton("rule");
    }
    
    private void checkRule(final FixtureDistSQLQueryStatement sqlStatement) {
        if (rule != sqlStatement.getExpectedRule()) {
            throw new IllegalStateException(String.format("Current rule `%s` does not match expected rule `%s`", rule, sqlStatement.getExpectedRule()));
        }
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final FixtureDistSQLQueryStatement sqlStatement, final ContextManager contextManager) throws SQLException {
        checkRule(sqlStatement);
        return Collections.emptyList();
    }
    
    @Override
    public Class<FixtureDistSQLQueryStatement> getType() {
        return FixtureDistSQLQueryStatement.class;
    }
}
