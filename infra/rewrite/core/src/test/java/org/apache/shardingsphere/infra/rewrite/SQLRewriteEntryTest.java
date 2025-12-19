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

package org.apache.shardingsphere.infra.rewrite;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sqltranslator.context.SQLTranslatorContext;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQLRewriteEntryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertRewriteForGenericSQLRewriteResult() {
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, mockResourceMetaData(),
                mock(RuleMetaData.class), Collections.singleton(new ShardingSphereSchema("test")));
        SQLRewriteEntry sqlRewriteEntry = new SQLRewriteEntry(
                database, new RuleMetaData(Collections.singleton(new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build()))), new ConfigurationProperties(new Properties()));
        RouteContext routeContext = new RouteContext();
        GenericSQLRewriteResult sqlRewriteResult = (GenericSQLRewriteResult) sqlRewriteEntry.rewrite(createQueryContext(), routeContext);
        assertThat(sqlRewriteResult.getSqlRewriteUnit().getSql(), is("SELECT ?"));
        assertThat(sqlRewriteResult.getSqlRewriteUnit().getParameters(), is(Collections.singletonList(1)));
    }
    
    private QueryContext createQueryContext() {
        QueryContext result = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(result.getSql()).thenReturn("SELECT ?");
        when(result.getParameters()).thenReturn(Collections.singletonList(1));
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes());
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        when(result.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(result.getHintValueContext()).thenReturn(new HintValueContext());
        when(result.getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        return result;
    }
    
    @Test
    void assertRewriteForRouteSQLRewriteResult() {
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                "foo_db", databaseType, mockResourceMetaData(), mock(RuleMetaData.class), Collections.singleton(new ShardingSphereSchema("test")));
        SQLTranslatorRule sqlTranslatorRule = mock(SQLTranslatorRule.class);
        when(sqlTranslatorRule.translate(any(), any(), any(), any(), any(), any())).thenReturn(Optional.of(new SQLTranslatorContext("", Collections.emptyList())));
        SQLRewriteEntry sqlRewriteEntry = new SQLRewriteEntry(database, new RuleMetaData(Collections.singleton(sqlTranslatorRule)), new ConfigurationProperties(new Properties()));
        RouteContext routeContext = new RouteContext();
        RouteUnit firstRouteUnit = mock(RouteUnit.class);
        when(firstRouteUnit.getDataSourceMapper()).thenReturn(new RouteMapper("ds", "ds_0"));
        RouteUnit secondRouteUnit = mock(RouteUnit.class);
        when(secondRouteUnit.getDataSourceMapper()).thenReturn(new RouteMapper("ds", "ds_1"));
        routeContext.getRouteUnits().addAll(Arrays.asList(firstRouteUnit, secondRouteUnit));
        RouteSQLRewriteResult sqlRewriteResult = (RouteSQLRewriteResult) sqlRewriteEntry.rewrite(createQueryContext(), routeContext);
        assertThat(sqlRewriteResult.getSqlRewriteUnits().size(), is(2));
    }
    
    private ResourceMetaData mockResourceMetaData() {
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
        StorageUnit storageUnit1 = mock(StorageUnit.class);
        when(storageUnit1.getStorageType()).thenReturn(databaseType);
        StorageUnit storageUnit2 = mock(StorageUnit.class);
        when(storageUnit2.getStorageType()).thenReturn(mock());
        storageUnits.put("ds_0", storageUnit1);
        storageUnits.put("ds_1", storageUnit2);
        ResourceMetaData result = mock(ResourceMetaData.class);
        when(result.getStorageUnits()).thenReturn(storageUnits);
        return result;
    }
}
