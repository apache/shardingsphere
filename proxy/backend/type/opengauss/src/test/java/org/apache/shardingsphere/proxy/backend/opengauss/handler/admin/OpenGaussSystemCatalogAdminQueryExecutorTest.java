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

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

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
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getProtocolType()).thenReturn(new OpenGaussDatabaseType());
        ShardingSphereRuleMetaData shardingSphereRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(shardingSphereRuleMetaData);
        when(shardingSphereRuleMetaData.getSingleRule(AuthorityRule.class)).thenReturn(mock(AuthorityRule.class));
        when(shardingSphereRuleMetaData.getSingleRule(AuthorityRule.class).getConfiguration()).thenReturn(mock(AuthorityRuleConfiguration.class));
        OpenGaussSystemCatalogAdminQueryExecutor executor = new OpenGaussSystemCatalogAdminQueryExecutor("select datname, datcompatibility from pg_database where datname = 'sharding_db'");
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
    
    @Test
    void assertExecuteSelectVersion() throws SQLException {
        when(ProxyContext.getInstance()).thenReturn(mock(ProxyContext.class, RETURNS_DEEP_STUBS));
        ShardingSphereRuleMetaData shardingSphereRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(shardingSphereRuleMetaData);
        when(shardingSphereRuleMetaData.getSingleRule(AuthorityRule.class)).thenReturn(mock(AuthorityRule.class));
        when(shardingSphereRuleMetaData.getSingleRule(AuthorityRule.class).getConfiguration()).thenReturn(mock(AuthorityRuleConfiguration.class));
        OpenGaussSystemCatalogAdminQueryExecutor executor = new OpenGaussSystemCatalogAdminQueryExecutor("select VERSION()");
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
    
    @Test
    void assertExecuteSelectGsPasswordDeadlineAndIntervalToNum() throws SQLException {
        when(ProxyContext.getInstance()).thenReturn(mock(ProxyContext.class, RETURNS_DEEP_STUBS));
        ShardingSphereRuleMetaData shardingSphereRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(shardingSphereRuleMetaData);
        when(shardingSphereRuleMetaData.getSingleRule(AuthorityRule.class)).thenReturn(mock(AuthorityRule.class));
        when(shardingSphereRuleMetaData.getSingleRule(AuthorityRule.class).getConfiguration()).thenReturn(mock(AuthorityRuleConfiguration.class));
        OpenGaussSystemCatalogAdminQueryExecutor executor = new OpenGaussSystemCatalogAdminQueryExecutor("select intervaltonum(gs_password_deadline())");
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
    
    @Test
    void assertExecuteSelectGsPasswordNotifyTime() throws SQLException {
        when(ProxyContext.getInstance()).thenReturn(mock(ProxyContext.class, RETURNS_DEEP_STUBS));
        ShardingSphereRuleMetaData shardingSphereRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(shardingSphereRuleMetaData);
        when(shardingSphereRuleMetaData.getSingleRule(AuthorityRule.class)).thenReturn(mock(AuthorityRule.class));
        when(shardingSphereRuleMetaData.getSingleRule(AuthorityRule.class).getConfiguration()).thenReturn(mock(AuthorityRuleConfiguration.class));
        OpenGaussSystemCatalogAdminQueryExecutor executor = new OpenGaussSystemCatalogAdminQueryExecutor("select gs_password_notifytime()");
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
}
