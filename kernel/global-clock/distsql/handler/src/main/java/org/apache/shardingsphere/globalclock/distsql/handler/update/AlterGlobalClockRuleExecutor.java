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

package org.apache.shardingsphere.globalclock.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.type.rdl.rule.global.GlobalRuleDefinitionExecutor;
import org.apache.shardingsphere.globalclock.api.config.GlobalClockRuleConfiguration;
import org.apache.shardingsphere.globalclock.distsql.statement.updatable.AlterGlobalClockRuleStatement;

/**
 * Alter global clock rule executor.
 */
public final class AlterGlobalClockRuleExecutor implements GlobalRuleDefinitionExecutor<AlterGlobalClockRuleStatement, GlobalClockRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final GlobalClockRuleConfiguration currentRuleConfig, final AlterGlobalClockRuleStatement sqlStatement) {
    }
    
    @Override
    public GlobalClockRuleConfiguration buildAlteredRuleConfiguration(final GlobalClockRuleConfiguration currentRuleConfig, final AlterGlobalClockRuleStatement sqlStatement) {
        return new GlobalClockRuleConfiguration(sqlStatement.getType(), sqlStatement.getProvider(), sqlStatement.isEnabled(), sqlStatement.getProps());
    }
    
    @Override
    public Class<GlobalClockRuleConfiguration> getRuleConfigurationClass() {
        return GlobalClockRuleConfiguration.class;
    }
    
    @Override
    public Class<AlterGlobalClockRuleStatement> getType() {
        return AlterGlobalClockRuleStatement.class;
    }
}
