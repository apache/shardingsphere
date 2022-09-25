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

package org.apache.shardingsphere.sharding.distsql.update;

import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.DropDefaultStrategyStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropDefaultShardingStrategyStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public final class DropDefaultShardingStrategyStatementUpdaterTest {
    
    private final DropDefaultStrategyStatementUpdater updater = new DropDefaultStrategyStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() {
        updater.checkSQLStatement(database, new DropDefaultShardingStrategyStatement(false, "TABLE"), null);
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithoutExistedAlgorithm() {
        updater.checkSQLStatement(database, createSQLStatement("table"), new ShardingRuleConfiguration());
    }
    
    @Test
    public void assertCheckSQLStatementWithIfExists() {
        updater.checkSQLStatement(database, new DropDefaultShardingStrategyStatement(true, "table"), new ShardingRuleConfiguration());
        updater.checkSQLStatement(database, new DropDefaultShardingStrategyStatement(true, "table"), null);
    }
    
    @Test
    public void assertUpdateCurrentRuleConfiguration() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        updater.updateCurrentRuleConfiguration(createSQLStatement("Database"), currentRuleConfig);
        assertNull(currentRuleConfig.getDefaultDatabaseShardingStrategy());
    }
    
    private DropDefaultShardingStrategyStatement createSQLStatement(final String defaultType) {
        return new DropDefaultShardingStrategyStatement(false, defaultType);
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "algorithm_name"));
        return result;
    }
}
