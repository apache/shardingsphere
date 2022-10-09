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

package org.apache.shardingsphere.infra.merge;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.ConnectionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.fixture.rule.DecoratorRuleFixture;
import org.apache.shardingsphere.infra.merge.fixture.rule.MergerRuleFixture;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.test.fixture.rule.MockedRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MergeEngineTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private QueryResult queryResult;
    
    @Test
    public void assertMergeWithIndependentRule() throws SQLException {
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.singleton(new MockedRule()));
        when(queryResult.getValue(1, String.class)).thenReturn("test");
        MergedResult actual = new MergeEngine(database, new ConfigurationProperties(new Properties()), mock(ConnectionContext.class)).merge(Collections.singletonList(queryResult),
                mock(SQLStatementContext.class));
        assertThat(actual.getValue(1, String.class), is("test"));
    }
    
    @Test
    public void assertMergeWithMergerRuleOnly() throws SQLException {
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.singleton(new MergerRuleFixture()));
        MergedResult actual = new MergeEngine(database, new ConfigurationProperties(new Properties()), mock(ConnectionContext.class)).merge(Collections.singletonList(queryResult),
                mock(SQLStatementContext.class));
        assertThat(actual.getValue(1, String.class), is("merged_value"));
    }
    
    @Test
    public void assertMergeWithDecoratorRuleOnly() throws SQLException {
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.singleton(new DecoratorRuleFixture()));
        MergedResult actual = new MergeEngine(database, new ConfigurationProperties(new Properties()), mock(ConnectionContext.class)).merge(Collections.singletonList(queryResult),
                mock(SQLStatementContext.class));
        assertThat(actual.getValue(1, String.class), is("decorated_value"));
    }
    
    @Test
    public void assertMergeWithMergerRuleAndDecoratorRuleTogether() throws SQLException {
        when(database.getRuleMetaData().getRules()).thenReturn(Arrays.asList(new MergerRuleFixture(), new DecoratorRuleFixture()));
        MergedResult actual = new MergeEngine(database, new ConfigurationProperties(new Properties()), mock(ConnectionContext.class)).merge(Collections.singletonList(queryResult),
                mock(SQLStatementContext.class));
        assertThat(actual.getValue(1, String.class), is("decorated_merged_value"));
    }
}
