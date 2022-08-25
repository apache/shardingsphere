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

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateAuditorException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.CreateShardingAuditorStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingAuditorSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingAuditorStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Properties;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateShardingAuditorStatementUpdaterTest {
    
    private final CreateShardingAuditorStatementUpdater updater = new CreateShardingAuditorStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Before
    public void before() {
        when(database.getName()).thenReturn("test");
    }
    
    @Test(expected = DuplicateAuditorException.class)
    public void assertExecuteWithDuplicate() throws DistSQLException {
        ShardingAuditorSegment auditorSegment = new ShardingAuditorSegment("sharding_key_required_auditor", new AlgorithmSegment("DML_SHARDING_CONDITIONS", new Properties()));
        updater.checkSQLStatement(database, createSQLStatement(auditorSegment, auditorSegment), null);
    }
    
    @Test(expected = DuplicateAuditorException.class)
    public void assertExecuteWithExist() throws DistSQLException {
        ShardingAuditorSegment auditorSegment = new ShardingAuditorSegment("sharding_key_required_auditor", new AlgorithmSegment("DML_SHARDING_CONDITIONS", new Properties()));
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getAuditors().put("sharding_key_required_auditor", new AlgorithmConfiguration("DML_SHARDING_CONDITIONS", new Properties()));
        updater.checkSQLStatement(database, createSQLStatement(auditorSegment), ruleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithInvalidAlgorithm() throws DistSQLException {
        ShardingAuditorSegment auditorSegment = new ShardingAuditorSegment("invalid_auditor", new AlgorithmSegment("INVALID_ALGORITHM", new Properties()));
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        updater.checkSQLStatement(database, createSQLStatement(auditorSegment), ruleConfig);
    }
    
    private CreateShardingAuditorStatement createSQLStatement(final ShardingAuditorSegment... ruleSegments) {
        return new CreateShardingAuditorStatement(Arrays.asList(ruleSegments));
    }
}
