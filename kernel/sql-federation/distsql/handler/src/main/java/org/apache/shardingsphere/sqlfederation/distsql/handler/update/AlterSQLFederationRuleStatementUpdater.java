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

import org.apache.shardingsphere.distsql.handler.ral.update.GlobalRuleRALUpdater;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sqlfederation.api.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.distsql.segment.CacheOptionSegment;
import org.apache.shardingsphere.sqlfederation.distsql.statement.updatable.AlterSQLFederationRuleStatement;

/**
 * Alter SQL federation rule statement updater.
 */
public final class AlterSQLFederationRuleStatementUpdater implements GlobalRuleRALUpdater<AlterSQLFederationRuleStatement, SQLFederationRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final SQLFederationRuleConfiguration currentRuleConfig, final AlterSQLFederationRuleStatement sqlStatement) {
    }
    
    @Override
    public SQLFederationRuleConfiguration buildAlteredRuleConfiguration(final SQLFederationRuleConfiguration currentRuleConfig, final AlterSQLFederationRuleStatement sqlStatement) {
        boolean sqlFederationEnabled = null == sqlStatement.getSqlFederationEnabled() ? currentRuleConfig.isSqlFederationEnabled() : sqlStatement.getSqlFederationEnabled();
        CacheOption executionPlanCache = null == sqlStatement.getExecutionPlanCache()
                ? currentRuleConfig.getExecutionPlanCache()
                : createCacheOption(currentRuleConfig.getExecutionPlanCache(), sqlStatement.getExecutionPlanCache());
        return new SQLFederationRuleConfiguration(sqlFederationEnabled, executionPlanCache);
    }
    
    private CacheOption createCacheOption(final CacheOption cacheOption, final CacheOptionSegment segment) {
        int initialCapacity = null == segment.getInitialCapacity() ? cacheOption.getInitialCapacity() : segment.getInitialCapacity();
        long maximumSize = null == segment.getMaximumSize() ? cacheOption.getMaximumSize() : segment.getMaximumSize();
        return new CacheOption(initialCapacity, maximumSize);
    }
    
    @Override
    public Class<SQLFederationRuleConfiguration> getRuleConfigurationClass() {
        return SQLFederationRuleConfiguration.class;
    }
    
    @Override
    public Class<AlterSQLFederationRuleStatement> getType() {
        return AlterSQLFederationRuleStatement.class;
    }
}
