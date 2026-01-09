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

package org.apache.shardingsphere.test.it.distsql.handler.engine.query;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecuteEngine;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.scope.DatabaseRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * DistSQL database rule query executor assert.
 */
@RequiredArgsConstructor
public final class DistSQLDatabaseRuleQueryExecutorAssert {
    
    private final DatabaseRule mockedRule;
    
    /**
     * Assert query result rows.
     *
     * @param ruleConfig rule configuration
     * @param sqlStatement SQL statement
     * @param expected expected query result rows
     * @throws SQLException SQL exception
     */
    public void assertQueryResultRows(final DatabaseRuleConfiguration ruleConfig, final DistSQLStatement sqlStatement, final Collection<LocalDataQueryResultRow> expected) throws SQLException {
        DistSQLQueryExecuteEngine engine = new DistSQLQueryExecuteEngine(sqlStatement, "foo_db", mockContextManager(ruleConfig), mock(DistSQLConnectionContext.class));
        engine.executeQuery();
        Collection<LocalDataQueryResultRow> actual = new ArrayList<>(engine.getRows());
        assertThat(actual, deepEqual(new ArrayList<>(expected)));
    }
    
    private ContextManager mockContextManager(final DatabaseRuleConfiguration ruleConfig) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(mockedRule.getConfiguration()).thenReturn(ruleConfig);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(mockedRule)));
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MockedDatabaseType"));
        when(result.getDatabase("foo_db")).thenReturn(database);
        return result;
    }
}
