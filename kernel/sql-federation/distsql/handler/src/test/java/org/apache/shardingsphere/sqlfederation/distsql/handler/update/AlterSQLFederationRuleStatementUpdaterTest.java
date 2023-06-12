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

import org.apache.shardingsphere.sqlfederation.api.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.distsql.segment.CacheOptionSegment;
import org.apache.shardingsphere.sqlfederation.distsql.statement.updatable.AlterSQLFederationRuleStatement;
import org.apache.shardingsphere.sqlfederation.rule.builder.DefaultSQLFederationRuleConfigurationBuilder;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlterSQLFederationRuleStatementUpdaterTest {
    
    @Test
    void assertExecute() {
        AlterSQLFederationRuleStatementUpdater updater = new AlterSQLFederationRuleStatementUpdater();
        AlterSQLFederationRuleStatement sqlStatement = new AlterSQLFederationRuleStatement(true, new CacheOptionSegment(64, 512L));
        SQLFederationRuleConfiguration actual = updater.buildAlteredRuleConfiguration(getSQLFederationRuleConfiguration(), sqlStatement);
        assertTrue(actual.isSqlFederationEnabled());
        assertThat(actual.getExecutionPlanCache().getInitialCapacity(), is(64));
        assertThat(actual.getExecutionPlanCache().getMaximumSize(), is(512L));
    }
    
    private SQLFederationRuleConfiguration getSQLFederationRuleConfiguration() {
        return new DefaultSQLFederationRuleConfigurationBuilder().build();
    }
}
