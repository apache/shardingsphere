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

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateKeyGeneratorException;
import org.apache.shardingsphere.infra.distsql.exception.rule.KeyGeneratorInUsedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredKeyGeneratorMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.DropShardingKeyGeneratorStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingKeyGeneratorStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DropShardingKeyGeneratorStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    private final DropShardingKeyGeneratorStatementUpdater updater = new DropShardingKeyGeneratorStatementUpdater();
    
    @Before
    public void before() {
        when(shardingSphereMetaData.getName()).thenReturn("test");
    }
    
    @Test(expected = DuplicateKeyGeneratorException.class)
    public void assertExecuteWithDuplicate() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("uuid_key_generator", "uuid_key_generator"), null);
    }
    
    @Test(expected = RequiredKeyGeneratorMissedException.class)
    public void assertExecuteWithNotExist() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("uuid_key_generator"), new ShardingRuleConfiguration());
    }
    
    @Test
    public void assertExecuteWithNotExistWithIfExists() throws DistSQLException {
        DropShardingKeyGeneratorStatement sqlStatement = new DropShardingKeyGeneratorStatement(Collections.singletonList("uuid_key_generator"));
        sqlStatement.setContainsExistClause(true);
        updater.checkSQLStatement(shardingSphereMetaData, sqlStatement, new ShardingRuleConfiguration());
    }
    
    @Test
    public void assertDropSpecifiedKeyGenerator() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getKeyGenerators().put("uuid_key_generator", new ShardingSphereAlgorithmConfiguration("uuid", new Properties()));
        updater.updateCurrentRuleConfiguration(createSQLStatement("uuid_key_generator"), currentRuleConfig);
        assertTrue(currentRuleConfig.getKeyGenerators().isEmpty());
    }
    
    @Test(expected = KeyGeneratorInUsedException.class)
    public void assertExecuteWithUsed() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getKeyGenerators().put("uuid_key_generator", new ShardingSphereAlgorithmConfiguration("UUID", null));
        currentRuleConfig.getAutoTables().add(createShardingAutoTableRuleConfiguration());
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("uuid_key_generator"), currentRuleConfig);
    }
    
    private ShardingAutoTableRuleConfiguration createShardingAutoTableRuleConfiguration() {
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration("auto_table");
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "uuid_key_generator"));
        return result;
    }
    
    private DropShardingKeyGeneratorStatement createSQLStatement(final String... keyGeneratorNames) {
        return new DropShardingKeyGeneratorStatement(Arrays.asList(keyGeneratorNames));
    }
}
