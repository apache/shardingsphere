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

import org.apache.shardingsphere.infra.binder.context.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
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
        CommonSQLStatementContext sqlStatementContext = mock(CommonSQLStatementContext.class);
        when(sqlStatementContext.getDatabaseType()).thenReturn(databaseType);
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        GenericSQLRewriteResult actual = new GenericSQLRewriteEngine(rule, database, mock(RuleMetaData.class))
                .rewrite(new SQLRewriteContext(database, sqlStatementContext, "SELECT 1", Collections.emptyList(), mock(ConnectionContext.class),
                        new HintValueContext()), queryContext);
        assertThat(actual.getSqlRewriteUnit().getSql(), is("SELECT 1"));
        assertThat(actual.getSqlRewriteUnit().getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    void assertRewriteStorageTypeIsEmpty() {
        SQLTranslatorRule rule = new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build());
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        when(database.getSchemas()).thenReturn(Collections.singletonMap("test", mock(ShardingSphereSchema.class)));
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.emptyMap());
        CommonSQLStatementContext sqlStatementContext = mock(CommonSQLStatementContext.class);
        DatabaseType databaseType = mock(DatabaseType.class);
        when(sqlStatementContext.getDatabaseType()).thenReturn(databaseType);
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        GenericSQLRewriteResult actual = new GenericSQLRewriteEngine(rule, database, mock(RuleMetaData.class))
                .rewrite(new SQLRewriteContext(database, sqlStatementContext, "SELECT 1", Collections.emptyList(), mock(ConnectionContext.class),
                        new HintValueContext()), queryContext);
        assertThat(actual.getSqlRewriteUnit().getSql(), is("SELECT 1"));
        assertThat(actual.getSqlRewriteUnit().getParameters(), is(Collections.emptyList()));
    }
    
    private Map<String, StorageUnit> mockStorageUnits(final DatabaseType databaseType) {
        StorageUnit result = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(result.getStorageType()).thenReturn(databaseType);
        return Collections.singletonMap("ds_0", result);
    }
}
