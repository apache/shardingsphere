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

package org.apache.shardingsphere.sqlfederation.distsql.handler.update;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.global.GlobalRuleDefinitionExecutor;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sqlfederation.api.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.distsql.segment.CacheOptionSegment;
import org.apache.shardingsphere.sqlfederation.distsql.statement.updatable.AlterSQLFederationRuleStatement;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;

/**
 * Alter SQL federation rule executor.
 */
@Setter
public final class AlterSQLFederationRuleExecutor implements GlobalRuleDefinitionExecutor<AlterSQLFederationRuleStatement, SQLFederationRule> {
    
    private SQLFederationRule rule;
    
    @Override
    public SQLFederationRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterSQLFederationRuleStatement sqlStatement) {
        boolean sqlFederationEnabled = null == sqlStatement.getSqlFederationEnabled() ? rule.getConfiguration().isSqlFederationEnabled() : sqlStatement.getSqlFederationEnabled();
        boolean allQueryUseSQLFederation = null == sqlStatement.getAllQueryUseSQLFederation() ? rule.getConfiguration().isAllQueryUseSQLFederation() : sqlStatement.getAllQueryUseSQLFederation();
        CacheOption executionPlanCache = null == sqlStatement.getExecutionPlanCache()
                ? rule.getConfiguration().getExecutionPlanCache()
                : createCacheOption(rule.getConfiguration().getExecutionPlanCache(), sqlStatement.getExecutionPlanCache());
        return new SQLFederationRuleConfiguration(sqlFederationEnabled, allQueryUseSQLFederation, executionPlanCache);
    }
    
    private CacheOption createCacheOption(final CacheOption cacheOption, final CacheOptionSegment segment) {
        int initialCapacity = null == segment.getInitialCapacity() ? cacheOption.getInitialCapacity() : segment.getInitialCapacity();
        long maximumSize = null == segment.getMaximumSize() ? cacheOption.getMaximumSize() : segment.getMaximumSize();
        return new CacheOption(initialCapacity, maximumSize);
    }
    
    @Override
    public Class<AlterSQLFederationRuleStatement> getType() {
        return AlterSQLFederationRuleStatement.class;
    }
    
    @Override
    public Class<SQLFederationRule> getRuleClass() {
        return SQLFederationRule.class;
    }
}
