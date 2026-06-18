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

package org.apache.shardingsphere.globalclock.distsql.handler.query;

import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.globalclock.rule.GlobalClockRule;
import org.apache.shardingsphere.infra.config.rule.scope.GlobalRuleConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.test.it.distsql.handler.engine.query.DistSQLRuleQueryExecutorSettings;
import org.apache.shardingsphere.test.it.distsql.handler.engine.query.DistSQLRuleQueryExecutorTestCaseArgumentsProvider;
import org.apache.shardingsphere.test.it.distsql.handler.engine.query.DistSQLGlobalRuleQueryExecutorAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.SQLException;
import java.util.Collection;

import static org.mockito.Mockito.mock;

@DistSQLRuleQueryExecutorSettings("cases/show-global-clock-rule.xml")
class ShowGlobalClockRuleExecutorTest {
    
    @ParameterizedTest(name = "DistSQL -> {0}")
    @ArgumentsSource(DistSQLRuleQueryExecutorTestCaseArgumentsProvider.class)
    void assertExecuteQuery(@SuppressWarnings("unused") final String distSQL, final DistSQLStatement sqlStatement,
                            final GlobalRuleConfiguration currentRuleConfig, final Collection<LocalDataQueryResultRow> expected) throws SQLException {
        new DistSQLGlobalRuleQueryExecutorAssert(mock(GlobalClockRule.class)).assertQueryResultRows(sqlStatement, currentRuleConfig, expected);
    }
}
