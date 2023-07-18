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

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.h2.H2DatabaseType;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqltranslator.api.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQLRewriteEntryTest {
    
    @Test
    void assertRewriteForGenericSQLRewriteResult() {
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, TypedSPILoader.getService(DatabaseType.class, "H2"), mockResource(),
                mock(ShardingSphereRuleMetaData.class), Collections.singletonMap("test", mock(ShardingSphereSchema.class)));
        SQLRewriteEntry sqlRewriteEntry = new SQLRewriteEntry(
                database, new ShardingSphereRuleMetaData(Collections.singleton(new SQLTranslatorRule(new SQLTranslatorRuleConfiguration()))), new ConfigurationProperties(new Properties()));
        RouteContext routeContext = new RouteContext();
        GenericSQLRewriteResult sqlRewriteResult = (GenericSQLRewriteResult) sqlRewriteEntry.rewrite("SELECT ?", Collections.singletonList(1), mock(CommonSQLStatementContext.class), routeContext,
                mock(ConnectionContext.class), new HintValueContext());
        assertThat(sqlRewriteResult.getSqlRewriteUnit().getSql(), is("SELECT ?"));
        assertThat(sqlRewriteResult.getSqlRewriteUnit().getParameters(), is(Collections.singletonList(1)));
    }
    
    @Test
    void assertRewriteForRouteSQLRewriteResult() {
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, TypedSPILoader.getService(DatabaseType.class, "H2"), mockResource(),
                mock(ShardingSphereRuleMetaData.class), Collections.singletonMap("test", mock(ShardingSphereSchema.class)));
        SQLRewriteEntry sqlRewriteEntry = new SQLRewriteEntry(
                database, new ShardingSphereRuleMetaData(Collections.singleton(mock(SQLTranslatorRule.class))), new ConfigurationProperties(new Properties()));
        RouteContext routeContext = new RouteContext();
        RouteUnit firstRouteUnit = mock(RouteUnit.class);
        when(firstRouteUnit.getDataSourceMapper()).thenReturn(new RouteMapper("ds", "ds_0"));
        RouteUnit secondRouteUnit = mock(RouteUnit.class);
        when(secondRouteUnit.getDataSourceMapper()).thenReturn(new RouteMapper("ds", "ds_1"));
        routeContext.getRouteUnits().addAll(Arrays.asList(firstRouteUnit, secondRouteUnit));
        RouteSQLRewriteResult sqlRewriteResult = (RouteSQLRewriteResult) sqlRewriteEntry.rewrite("SELECT ?", Collections.singletonList(1), mock(CommonSQLStatementContext.class), routeContext,
                mock(ConnectionContext.class), new HintValueContext());
        assertThat(sqlRewriteResult.getSqlRewriteUnits().size(), is(2));
    }
    
    private ShardingSphereResourceMetaData mockResource() {
        ShardingSphereResourceMetaData result = mock(ShardingSphereResourceMetaData.class);
        Map<String, DatabaseType> databaseTypes = new LinkedHashMap<>(2, 1F);
        databaseTypes.put("ds_0", new H2DatabaseType());
        databaseTypes.put("ds_1", new MySQLDatabaseType());
        when(result.getStorageTypes()).thenReturn(databaseTypes);
        return result;
    }
}
