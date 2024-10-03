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

package org.apache.shardingsphere.sqltranslator.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecuteEngine;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sqltranslator.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.distsql.statement.queryable.ShowSQLTranslatorRuleStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowSQLTranslatorRuleExecutorTest {
    
    @Test
    void assertExecute() throws SQLException {
        DistSQLQueryExecuteEngine engine = new DistSQLQueryExecuteEngine(new ShowSQLTranslatorRuleStatement(), null, mockContextManager(), mock(DistSQLConnectionContext.class));
        engine.executeQuery();
        List<LocalDataQueryResultRow> actual = new ArrayList<>(engine.getRows());
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getCell(1), is("NATIVE"));
        assertThat(actual.get(0).getCell(2), is(""));
        assertThat(actual.get(0).getCell(3), is("true"));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        SQLTranslatorRule rule = mock(SQLTranslatorRule.class);
        when(rule.getConfiguration()).thenReturn(new SQLTranslatorRuleConfiguration("NATIVE", new Properties(), true));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
}
