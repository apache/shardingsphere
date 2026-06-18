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

import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

/**
 * Fixture database rule definition executor.
 */
public final class FixtureDatabaseRuleDefinitionExecutor implements DatabaseRuleDefinitionExecutor<FixtureDatabaseRuleDefinitionStatement, ShardingSphereRule> {
    
    private ShardingSphereRule rule;
    
    @Override
    public void setDatabase(final ShardingSphereDatabase database) {
    }
    
    @Override
    public void setRule(final ShardingSphereRule rule) {
        this.rule = rule;
    }
    
    @Override
    public Class<ShardingSphereRule> getRuleClass() {
        return ShardingSphereRule.class;
    }
    
    @Override
    public void checkBeforeUpdate(final FixtureDatabaseRuleDefinitionStatement sqlStatement) {
        if (rule != sqlStatement.getExpectedRule()) {
            throw new IllegalStateException(String.format("Current rule `%s` does not match expected rule `%s`", rule, sqlStatement.getExpectedRule()));
        }
    }
    
    @Override
    public Class<FixtureDatabaseRuleDefinitionStatement> getType() {
        return FixtureDatabaseRuleDefinitionStatement.class;
    }
}
