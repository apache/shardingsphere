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

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.infra.merge.fixture.rule.DecoratorRuleFixture;
import org.apache.shardingsphere.infra.merge.fixture.rule.IndependentRuleFixture;
import org.apache.shardingsphere.infra.merge.fixture.rule.MergerRuleFixture;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MergeEngineTest {
    
    @Mock
    private DatabaseType databaseType;
    
    @Mock
    private PhysicalSchemaMetaData schemaMetaData;
    
    @Mock
    private ConfigurationProperties props;
    
    @Mock
    private QueryResult queryResult;
    
    @Mock
    private SQLStatementContext<?> sqlStatementContext;
    
    @Test
    public void assertMergeWithIndependentRule() throws SQLException {
        when(queryResult.getValue(1, String.class)).thenReturn("test");
        MergeEngine mergeEngine = new MergeEngine(databaseType, schemaMetaData, props, Collections.singletonList(new IndependentRuleFixture()));
        MergedResult actual = mergeEngine.merge(Collections.singletonList(queryResult), sqlStatementContext);
        assertThat(actual.getValue(1, String.class), is("test"));
    }
    
    @Test
    public void assertMergeWithMergerRuleOnly() throws SQLException {
        MergeEngine mergeEngine = new MergeEngine(databaseType, schemaMetaData, props, Collections.singletonList(new MergerRuleFixture()));
        MergedResult actual = mergeEngine.merge(Collections.singletonList(queryResult), sqlStatementContext);
        assertThat(actual.getValue(1, String.class), is("merged_value"));
    }
    
    @Test
    public void assertMergeWithDecoratorRuleOnly() throws SQLException {
        MergeEngine mergeEngine = new MergeEngine(databaseType, schemaMetaData, props, Collections.singletonList(new DecoratorRuleFixture()));
        MergedResult actual = mergeEngine.merge(Collections.singletonList(queryResult), sqlStatementContext);
        assertThat(actual.getValue(1, String.class), is("decorated_value"));
    }
    
    @Test
    public void assertMergeWithMergerRuleAndDecoratorRuleTogether() throws SQLException {
        MergeEngine mergeEngine = new MergeEngine(databaseType, schemaMetaData, props, Arrays.asList(new MergerRuleFixture(), new DecoratorRuleFixture()));
        MergedResult actual = mergeEngine.merge(Collections.singletonList(queryResult), sqlStatementContext);
        assertThat(actual.getValue(1, String.class), is("decorated_merged_value"));
    }
}
