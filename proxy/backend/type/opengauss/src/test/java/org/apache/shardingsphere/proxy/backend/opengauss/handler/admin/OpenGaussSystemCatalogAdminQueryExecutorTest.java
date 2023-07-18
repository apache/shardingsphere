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

package org.apache.shardingsphere.proxy.backend.opengauss.handler.admin;

import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.opengauss.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussSelectStatement;
import org.apache.shardingsphere.sqlfederation.api.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class OpenGaussSystemCatalogAdminQueryExecutorTest {
    
    @Test
    void assertExecuteSelectFromPgDatabase() throws SQLException {
        when(ProxyContext.getInstance()).thenReturn(mock(ProxyContext.class, RETURNS_DEEP_STUBS));
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(Arrays.asList("foo", "bar", "sharding_db", "other_db"));
        ConfigurationProperties properties = new ConfigurationProperties(new Properties());
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps()).thenReturn(properties);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getProtocolType()).thenReturn(new OpenGaussDatabaseType());
        Map<String, ShardingSphereDatabase> databases = createShardingSphereDatabaseMap();
        SQLFederationRule sqlFederationRule = new SQLFederationRule(new SQLFederationRuleConfiguration(false, new CacheOption(1, 1)), databases, properties);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(mock(ShardingSphereRuleMetaData.class));
        OpenGaussSelectStatement sqlStatement = createSelectStatementForPgDatabase();
        ShardingSphereMetaData metaData =
                new ShardingSphereMetaData(databases, mock(ShardingSphereResourceMetaData.class), new ShardingSphereRuleMetaData(Collections.singletonList(sqlFederationRule)), properties);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData()).thenReturn(metaData);
        SelectStatementContext sqlStatementContext = new SelectStatementContext(metaData, Collections.emptyList(), sqlStatement, "sharding_db");
        OpenGaussSystemCatalogAdminQueryExecutor executor = new OpenGaussSystemCatalogAdminQueryExecutor(sqlStatementContext,
                "select datname, datcompatibility from pg_database where datname = 'sharding_db'", "sharding_db", Collections.emptyList());
        executor.execute(connectionSession);
        QueryResultMetaData actualMetaData = executor.getQueryResultMetaData();
        assertThat(actualMetaData.getColumnCount(), is(2));
        assertThat(actualMetaData.getColumnName(1), is("datname"));
        assertThat(actualMetaData.getColumnName(2), is("datcompatibility"));
        MergedResult actualResult = executor.getMergedResult();
        assertTrue(actualResult.next());
        assertThat(actualResult.getValue(1, String.class), is("sharding_db"));
        assertThat(actualResult.getValue(2, String.class), is("PG"));
    }
    
    private OpenGaussSelectStatement createSelectStatementForPgDatabase() {
        OpenGaussSelectStatement result = new OpenGaussSelectStatement();
        result.setProjections(new ProjectionsSegment(0, 0));
        result.getProjections().getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("datname"))));
        result.getProjections().getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("datcompatibility"))));
        result.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("pg_database"))));
        result.setWhere(new WhereSegment(0, 0,
                new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("datname")), new LiteralExpressionSegment(0, 0, "sharding_db"), "=", "datname = 'sharding_db'")));
        return result;
    }
    
    private Map<String, ShardingSphereDatabase> createShardingSphereDatabaseMap() {
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(1, 1F);
        Collection<ShardingSphereColumn> columns = Arrays.asList(
                new ShardingSphereColumn("datname", 12, false, false, false, true, false),
                new ShardingSphereColumn("datdba", -5, false, false, false, true, false),
                new ShardingSphereColumn("encoding", 4, false, false, false, true, false),
                new ShardingSphereColumn("datcollate", 12, false, false, false, true, false),
                new ShardingSphereColumn("datctype", 12, false, false, false, true, false),
                new ShardingSphereColumn("datistemplate", -7, false, false, false, true, false),
                new ShardingSphereColumn("datallowconn", -7, false, false, false, true, false),
                new ShardingSphereColumn("datconnlimit", 4, false, false, false, true, false),
                new ShardingSphereColumn("datlastsysoid", -5, false, false, false, true, false),
                new ShardingSphereColumn("datfrozenxid", 1111, false, false, false, true, false),
                new ShardingSphereColumn("dattablespace", -5, false, false, false, true, false),
                new ShardingSphereColumn("datcompatibility", 12, false, false, false, true, false),
                new ShardingSphereColumn("datacl", 2003, false, false, false, true, false),
                new ShardingSphereColumn("datfrozenxid64", 1111, false, false, false, true, false),
                new ShardingSphereColumn("datminmxid", 1111, false, false, false, true, false));
        ShardingSphereSchema schema = new ShardingSphereSchema(
                Collections.singletonMap("pg_database", new ShardingSphereTable("pg_database", columns, Collections.emptyList(), Collections.emptyList())), Collections.emptyMap());
        result.put("sharding_db", new ShardingSphereDatabase("sharding_db", new OpenGaussDatabaseType(), mock(ShardingSphereResourceMetaData.class), mock(ShardingSphereRuleMetaData.class),
                Collections.singletonMap("pg_catalog", schema)));
        return result;
    }
    
    @Test
    void assertExecuteSelectVersion() throws SQLException {
        when(ProxyContext.getInstance()).thenReturn(mock(ProxyContext.class, RETURNS_DEEP_STUBS));
        ShardingSphereRuleMetaData shardingSphereRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(shardingSphereRuleMetaData);
        ConfigurationProperties properties = new ConfigurationProperties(new Properties());
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps()).thenReturn(properties);
        Map<String, ShardingSphereDatabase> databases = createShardingSphereDatabaseMap();
        SQLFederationRule sqlFederationRule = new SQLFederationRule(new SQLFederationRuleConfiguration(false, new CacheOption(1, 1)), databases, properties);
        OpenGaussSelectStatement sqlStatement = createSelectStatementForVersion();
        ShardingSphereMetaData metaData =
                new ShardingSphereMetaData(databases, mock(ShardingSphereResourceMetaData.class), new ShardingSphereRuleMetaData(Collections.singletonList(sqlFederationRule)), properties);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData()).thenReturn(metaData);
        SelectStatementContext sqlStatementContext = new SelectStatementContext(metaData, Collections.emptyList(), sqlStatement, "sharding_db");
        OpenGaussSystemCatalogAdminQueryExecutor executor =
                new OpenGaussSystemCatalogAdminQueryExecutor(sqlStatementContext, "select VERSION()", "sharding_db", Collections.emptyList());
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getProtocolType()).thenReturn(new OpenGaussDatabaseType());
        executor.execute(connectionSession);
        QueryResultMetaData actualMetaData = executor.getQueryResultMetaData();
        assertThat(actualMetaData.getColumnCount(), is(1));
        assertThat(actualMetaData.getColumnType(1), is(Types.VARCHAR));
        MergedResult actualResult = executor.getMergedResult();
        assertTrue(actualResult.next());
        assertThat((String) actualResult.getValue(1, String.class), containsString("ShardingSphere-Proxy"));
    }
    
    private OpenGaussSelectStatement createSelectStatementForVersion() {
        OpenGaussSelectStatement result = new OpenGaussSelectStatement();
        result.setProjections(new ProjectionsSegment(0, 0));
        result.getProjections().getProjections().add(new ExpressionProjectionSegment(0, 0, "VERSION()", new FunctionSegment(0, 0, "VERSION", "VERSION()")));
        return result;
    }
    
    @Test
    void assertExecuteSelectGsPasswordDeadlineAndIntervalToNum() throws SQLException {
        when(ProxyContext.getInstance()).thenReturn(mock(ProxyContext.class, RETURNS_DEEP_STUBS));
        ShardingSphereRuleMetaData shardingSphereRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(shardingSphereRuleMetaData);
        ConfigurationProperties properties = new ConfigurationProperties(new Properties());
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps()).thenReturn(properties);
        Map<String, ShardingSphereDatabase> databases = createShardingSphereDatabaseMap();
        SQLFederationRule sqlFederationRule = new SQLFederationRule(new SQLFederationRuleConfiguration(false, new CacheOption(1, 1)), databases, properties);
        OpenGaussSelectStatement sqlStatement = createSelectStatementForGsPasswordDeadlineAndIntervalToNum();
        ShardingSphereMetaData metaData =
                new ShardingSphereMetaData(databases, mock(ShardingSphereResourceMetaData.class), new ShardingSphereRuleMetaData(Collections.singletonList(sqlFederationRule)), properties);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData()).thenReturn(metaData);
        SelectStatementContext sqlStatementContext = new SelectStatementContext(metaData, Collections.emptyList(), sqlStatement, "sharding_db");
        OpenGaussSystemCatalogAdminQueryExecutor executor =
                new OpenGaussSystemCatalogAdminQueryExecutor(sqlStatementContext, "select intervaltonum(gs_password_deadline())", "sharding_db", Collections.emptyList());
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getProtocolType()).thenReturn(new OpenGaussDatabaseType());
        executor.execute(connectionSession);
        QueryResultMetaData actualMetaData = executor.getQueryResultMetaData();
        assertThat(actualMetaData.getColumnCount(), is(1));
        assertThat(actualMetaData.getColumnType(1), is(Types.INTEGER));
        MergedResult actualResult = executor.getMergedResult();
        assertTrue(actualResult.next());
        assertThat(actualResult.getValue(1, Integer.class), is(90));
    }
    
    private OpenGaussSelectStatement createSelectStatementForGsPasswordDeadlineAndIntervalToNum() {
        OpenGaussSelectStatement result = new OpenGaussSelectStatement();
        result.setProjections(new ProjectionsSegment(0, 0));
        FunctionSegment intervalToNumFunction = new FunctionSegment(0, 0, "intervaltonum", "intervaltonum(gs_password_deadline())");
        intervalToNumFunction.getParameters().add(new FunctionSegment(0, 0, "gs_password_deadline", "gs_password_deadline()"));
        result.getProjections().getProjections().add(new ExpressionProjectionSegment(0, 0, "intervaltonum(gs_password_deadline())", intervalToNumFunction));
        return result;
    }
    
    @Test
    void assertExecuteSelectGsPasswordNotifyTime() throws SQLException {
        when(ProxyContext.getInstance()).thenReturn(mock(ProxyContext.class, RETURNS_DEEP_STUBS));
        ShardingSphereRuleMetaData shardingSphereRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(shardingSphereRuleMetaData);
        ConfigurationProperties properties = new ConfigurationProperties(new Properties());
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps()).thenReturn(properties);
        Map<String, ShardingSphereDatabase> databases = createShardingSphereDatabaseMap();
        SQLFederationRule sqlFederationRule = new SQLFederationRule(new SQLFederationRuleConfiguration(false, new CacheOption(1, 1)), databases, properties);
        OpenGaussSelectStatement sqlStatement = createSelectStatementForGsPasswordNotifyTime();
        ShardingSphereMetaData metaData =
                new ShardingSphereMetaData(databases, mock(ShardingSphereResourceMetaData.class), new ShardingSphereRuleMetaData(Collections.singletonList(sqlFederationRule)), properties);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData()).thenReturn(metaData);
        SelectStatementContext sqlStatementContext = new SelectStatementContext(metaData, Collections.emptyList(), sqlStatement, "sharding_db");
        OpenGaussSystemCatalogAdminQueryExecutor executor =
                new OpenGaussSystemCatalogAdminQueryExecutor(sqlStatementContext, "select gs_password_notifytime()", "sharding_db", Collections.emptyList());
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getProtocolType()).thenReturn(new OpenGaussDatabaseType());
        executor.execute(connectionSession);
        QueryResultMetaData actualMetaData = executor.getQueryResultMetaData();
        assertThat(actualMetaData.getColumnCount(), is(1));
        assertThat(actualMetaData.getColumnType(1), is(Types.INTEGER));
        MergedResult actualResult = executor.getMergedResult();
        assertTrue(actualResult.next());
        assertThat(actualResult.getValue(1, Integer.class), is(7));
    }
    
    private OpenGaussSelectStatement createSelectStatementForGsPasswordNotifyTime() {
        OpenGaussSelectStatement result = new OpenGaussSelectStatement();
        result.setProjections(new ProjectionsSegment(0, 0));
        result.getProjections().getProjections()
                .add(new ExpressionProjectionSegment(0, 0, "gs_password_notifytime()", new FunctionSegment(0, 0, "gs_password_notifytime", "gs_password_notifytime()")));
        return result;
    }
}
