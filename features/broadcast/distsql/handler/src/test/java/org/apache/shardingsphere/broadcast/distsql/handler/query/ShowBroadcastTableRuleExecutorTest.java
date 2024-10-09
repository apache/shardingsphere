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

package org.apache.shardingsphere.broadcast.distsql.handler.query;

import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.test.it.distsql.handler.engine.query.DistSQLDatabaseRuleQueryExecutorAssert;
import org.apache.shardingsphere.test.it.distsql.handler.engine.query.DistSQLRuleQueryExecutorSettings;
import org.apache.shardingsphere.test.it.distsql.handler.engine.query.DistSQLRuleQueryExecutorTestCaseArgumentsProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DistSQLRuleQueryExecutorSettings("cases/show-broadcast-table-rules.xml")
class ShowBroadcastTableRuleExecutorTest {
    
    @ParameterizedTest(name = "DistSQL -> {0}")
    @ArgumentsSource(DistSQLRuleQueryExecutorTestCaseArgumentsProvider.class)
    void assertExecuteQuery(@SuppressWarnings("unused") final String distSQL, final DistSQLStatement sqlStatement,
                            final DatabaseRuleConfiguration currentRuleConfig, final Collection<LocalDataQueryResultRow> expected) throws SQLException {
        new DistSQLDatabaseRuleQueryExecutorAssert(mockRule()).assertQueryResultRows(currentRuleConfig, sqlStatement, expected);
    }
    
    private BroadcastRule mockRule() {
        BroadcastRule result = mock(BroadcastRule.class);
        when(result.getTables()).thenReturn(Collections.singleton("t_address"));
        return result;
    }
}
