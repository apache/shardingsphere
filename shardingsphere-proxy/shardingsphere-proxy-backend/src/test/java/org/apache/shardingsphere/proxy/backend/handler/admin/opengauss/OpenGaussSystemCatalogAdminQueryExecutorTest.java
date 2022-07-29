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

package org.apache.shardingsphere.proxy.backend.handler.admin.opengauss;

import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public final class OpenGaussSystemCatalogAdminQueryExecutorTest {
    
    @Test
    public void assertExecuteSelectFromPgDatabase() throws SQLException {
        try (MockedStatic<ProxyContext> mockedStatic = mockStatic(ProxyContext.class)) {
            mockedStatic.when(ProxyContext::getInstance).thenReturn(mock(ProxyContext.class, RETURNS_DEEP_STUBS));
            when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(Arrays.asList("foo", "bar", "sharding_db", "other_db"));
            OpenGaussSystemCatalogAdminQueryExecutor executor = new OpenGaussSystemCatalogAdminQueryExecutor("select datname, datcompatibility from pg_database where datname = 'sharding_db'");
            ConnectionSession connectionSession = mock(ConnectionSession.class);
            when(connectionSession.getDatabaseType()).thenReturn(new OpenGaussDatabaseType());
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
    }
    
    @Test
    public void assertExecuteSelectVersion() throws SQLException {
        try (MockedStatic<ProxyContext> mockedStatic = mockStatic(ProxyContext.class)) {
            mockedStatic.when(ProxyContext::getInstance).thenReturn(mock(ProxyContext.class, RETURNS_DEEP_STUBS));
            OpenGaussSystemCatalogAdminQueryExecutor executor = new OpenGaussSystemCatalogAdminQueryExecutor("select VERSION()");
            ConnectionSession connectionSession = mock(ConnectionSession.class);
            when(connectionSession.getDatabaseType()).thenReturn(new OpenGaussDatabaseType());
            executor.execute(connectionSession);
            QueryResultMetaData actualMetaData = executor.getQueryResultMetaData();
            assertThat(actualMetaData.getColumnCount(), is(1));
            assertThat(actualMetaData.getColumnType(1), is(Types.VARCHAR));
            MergedResult actualResult = executor.getMergedResult();
            assertTrue(actualResult.next());
            assertThat((String) actualResult.getValue(1, String.class), containsString("ShardingSphere-Proxy"));
        }
    }
    
    @Test
    public void assertExecuteSelectGsPasswordDeadlineAndIntervalToNum() throws SQLException {
        try (MockedStatic<ProxyContext> mockedStatic = mockStatic(ProxyContext.class)) {
            mockedStatic.when(ProxyContext::getInstance).thenReturn(mock(ProxyContext.class, RETURNS_DEEP_STUBS));
            OpenGaussSystemCatalogAdminQueryExecutor executor = new OpenGaussSystemCatalogAdminQueryExecutor("select intervaltonum(gs_password_deadline())");
            ConnectionSession connectionSession = mock(ConnectionSession.class);
            when(connectionSession.getDatabaseType()).thenReturn(new OpenGaussDatabaseType());
            executor.execute(connectionSession);
            QueryResultMetaData actualMetaData = executor.getQueryResultMetaData();
            assertThat(actualMetaData.getColumnCount(), is(1));
            assertThat(actualMetaData.getColumnType(1), is(Types.INTEGER));
            MergedResult actualResult = executor.getMergedResult();
            assertTrue(actualResult.next());
            assertThat(actualResult.getValue(1, Integer.class), is(90));
        }
    }
    
    @Test
    public void assertExecuteSelectGsPasswordNotifyTime() throws SQLException {
        try (MockedStatic<ProxyContext> mockedStatic = mockStatic(ProxyContext.class)) {
            mockedStatic.when(ProxyContext::getInstance).thenReturn(mock(ProxyContext.class, RETURNS_DEEP_STUBS));
            OpenGaussSystemCatalogAdminQueryExecutor executor = new OpenGaussSystemCatalogAdminQueryExecutor("select gs_password_notifytime()");
            ConnectionSession connectionSession = mock(ConnectionSession.class);
            when(connectionSession.getDatabaseType()).thenReturn(new OpenGaussDatabaseType());
            executor.execute(connectionSession);
            QueryResultMetaData actualMetaData = executor.getQueryResultMetaData();
            assertThat(actualMetaData.getColumnCount(), is(1));
            assertThat(actualMetaData.getColumnType(1), is(Types.INTEGER));
            MergedResult actualResult = executor.getMergedResult();
            assertTrue(actualResult.next());
            assertThat(actualResult.getValue(1, Integer.class), is(7));
        }
    }
}
