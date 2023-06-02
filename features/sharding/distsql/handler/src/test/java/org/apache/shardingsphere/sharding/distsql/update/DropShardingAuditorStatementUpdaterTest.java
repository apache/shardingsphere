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

import org.apache.shardingsphere.distsql.handler.exception.algorithm.AlgorithmInUsedException;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.DropShardingAuditorStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingAuditorStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DropShardingAuditorStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    private final DropShardingAuditorStatementUpdater updater = new DropShardingAuditorStatementUpdater();
    
    @BeforeEach
    void before() {
        when(database.getName()).thenReturn("foo_db");
    }
    
    @Test
    void assertExecuteWithNotExist() {
        assertThrows(MissingRequiredAlgorithmException.class, () -> updater.checkSQLStatement(database, createSQLStatement("sharding_key_required_auditor"), new ShardingRuleConfiguration()));
    }
    
    @Test
    void assertExecuteWithNotExistWithIfExists() {
        DropShardingAuditorStatement sqlStatement = new DropShardingAuditorStatement(true, Collections.singletonList("sharding_key_required_auditor"));
        updater.checkSQLStatement(database, sqlStatement, new ShardingRuleConfiguration());
    }
    
    @Test
    void assertDropSpecifiedAuditor() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getAuditors().put("sharding_key_required_auditor", new AlgorithmConfiguration("DML_SHARDING_CONDITIONS", new Properties()));
        updater.updateCurrentRuleConfiguration(createSQLStatement("sharding_key_required_auditor"), currentRuleConfig);
        assertTrue(currentRuleConfig.getAuditors().isEmpty());
    }
    
    @Test
    void assertExecuteWithUsed() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getAuditors().put("sharding_key_required_auditor", new AlgorithmConfiguration("DML_SHARDING_CONDITIONS", null));
        currentRuleConfig.getAutoTables().add(createShardingAutoTableRuleConfiguration());
        assertThrows(AlgorithmInUsedException.class, () -> updater.checkSQLStatement(database, createSQLStatement("sharding_key_required_auditor"), currentRuleConfig));
    }
    
    private ShardingAutoTableRuleConfiguration createShardingAutoTableRuleConfiguration() {
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration("auto_table", null);
        result.setAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singletonList("sharding_key_required_auditor"), true));
        return result;
    }
    
    private DropShardingAuditorStatement createSQLStatement(final String... auditorNames) {
        return new DropShardingAuditorStatement(false, Arrays.asList(auditorNames));
    }
}
