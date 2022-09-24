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
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.distsql.handler.update.AlterShardingBroadcastTableRuleStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingBroadcastTableRulesStatement;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class AlterShardingBroadcastTableRuleStatementUpdaterTest {
    
    private final AlterShardingBroadcastTableRuleStatementUpdater updater = new AlterShardingBroadcastTableRuleStatementUpdater();
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() throws RuleDefinitionViolationException {
        updater.checkSQLStatement(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS), createSQLStatement(), null);
    }
    
    private AlterShardingBroadcastTableRulesStatement createSQLStatement() {
        return new AlterShardingBroadcastTableRulesStatement(Collections.singleton("t_1"));
    }
}
