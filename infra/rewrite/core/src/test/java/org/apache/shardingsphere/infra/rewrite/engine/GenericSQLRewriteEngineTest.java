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

package org.apache.shardingsphere.infra.rewrite.engine;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GenericSQLRewriteEngineTest {
    
    @Test
    void assertRewrite() {
        DatabaseType databaseType = mock(DatabaseType.class);
        SQLTranslatorRule rule = new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build());
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getProtocolType()).thenReturn(databaseType);
        Map<String, StorageUnit> storageUnits = mockStorageUnits(databaseType);
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        QueryContext queryContext = mockQueryContext(sqlStatementContext);
        GenericSQLRewriteResult actual = new GenericSQLRewriteEngine(rule, database, mock(RuleMetaData.class)).rewrite(new SQLRewriteContext(database, queryContext), queryContext);
        assertThat(actual.getSqlRewriteUnit().getSql(), is("SELECT 1"));
        assertThat(actual.getSqlRewriteUnit().getParameters(), is(Collections.emptyList()));
    }
    
    private QueryContext mockQueryContext(final SQLStatementContext sqlStatementContext) {
        QueryContext result = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(result.getSql()).thenReturn("SELECT 1");
        when(result.getParameters()).thenReturn(Collections.emptyList());
        when(result.getHintValueContext()).thenReturn(new HintValueContext());
        return result;
    }
    
    @Test
    void assertRewriteStorageTypeIsEmpty() {
        SQLTranslatorRule rule = new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build());
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(), new ResourceMetaData(Collections.emptyMap()), mock(), Collections.singleton(new ShardingSphereSchema("test")));
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        DatabaseType databaseType = mock(DatabaseType.class);
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        QueryContext queryContext = mockQueryContext(sqlStatementContext);
        GenericSQLRewriteResult actual = new GenericSQLRewriteEngine(rule, database, mock(RuleMetaData.class)).rewrite(new SQLRewriteContext(database, queryContext), queryContext);
        assertThat(actual.getSqlRewriteUnit().getSql(), is("SELECT 1"));
        assertThat(actual.getSqlRewriteUnit().getParameters(), is(Collections.emptyList()));
    }
    
    private Map<String, StorageUnit> mockStorageUnits(final DatabaseType databaseType) {
        StorageUnit result = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(result.getStorageType()).thenReturn(databaseType);
        return Collections.singletonMap("ds_0", result);
    }
}
