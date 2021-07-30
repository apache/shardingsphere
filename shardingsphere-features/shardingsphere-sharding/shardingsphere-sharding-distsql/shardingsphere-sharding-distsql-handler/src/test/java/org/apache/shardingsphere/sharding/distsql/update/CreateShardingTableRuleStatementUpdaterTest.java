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
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.sharding.distsql.handler.update.CreateShardingTableRuleStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.mockito.Mockito.mock;

public final class CreateShardingTableRuleStatementUpdaterTest {
    
    private final CreateShardingTableRuleStatementUpdater updater = new CreateShardingTableRuleStatementUpdater();
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithDuplicateTables() throws DistSQLException {
        TableRuleSegment ruleSegment = new TableRuleSegment("t_order", Collections.emptyList(), null, null, null, null);
        updater.checkSQLStatement("foo", createSQLStatement(ruleSegment, ruleSegment), null, mock(ShardingSphereResource.class), Collections.emptySet());
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckSQLStatementWithoutToBeCreatedShardingAlgorithms() throws DistSQLException {
        TableRuleSegment ruleSegment = new TableRuleSegment("t_order", Collections.emptyList(), null, new AlgorithmSegment("INVALID_TYPE", new Properties()), null, null);
        updater.checkSQLStatement("foo", createSQLStatement(ruleSegment), null, mock(ShardingSphereResource.class), Collections.emptySet());
    }
    
    private CreateShardingTableRuleStatement createSQLStatement(final TableRuleSegment... ruleSegments) {
        return new CreateShardingTableRuleStatement(Arrays.asList(ruleSegments));
    }
}
