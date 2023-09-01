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

package org.apache.shardingsphere.proxy.backend.handler.distsql.fixture;

import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

public final class FixtureRuleDefinitionCreateUpdater implements RuleDefinitionCreateUpdater<CreateFixtureRuleStatement, FixtureRuleConfiguration> {
    
    @Override
    public FixtureRuleConfiguration buildToBeCreatedRuleConfiguration(final FixtureRuleConfiguration currentRuleConfig, final CreateFixtureRuleStatement sqlStatement) {
        return new FixtureRuleConfiguration();
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final FixtureRuleConfiguration currentRuleConfig, final FixtureRuleConfiguration toBeCreatedRuleConfig) {
    }
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final CreateFixtureRuleStatement sqlStatement, final FixtureRuleConfiguration currentRuleConfig) {
    }
    
    @Override
    public Class<FixtureRuleConfiguration> getRuleConfigurationClass() {
        return FixtureRuleConfiguration.class;
    }
    
    @Override
    public Class<CreateFixtureRuleStatement> getType() {
        return CreateFixtureRuleStatement.class;
    }
}
