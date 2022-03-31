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

package org.apache.shardingsphere.proxy.backend.text.admin.opengauss;

import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.sql.SQLException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public final class OpenGaussSelectDatabaseExecutorTest {
    
    private static final String SQL = "select datname, datcompatibility from pg_database where datname = 'sharding_db'";
    
    @Test
    public void assertExecute() throws SQLException {
        try (MockedStatic<ProxyContext> mockedStatic = mockStatic(ProxyContext.class)) {
            mockedStatic.when(ProxyContext::getInstance).thenReturn(mock(ProxyContext.class, RETURNS_DEEP_STUBS));
            assertExecute0();
        }
    }
    
    private void assertExecute0() throws SQLException {
        when(ProxyContext.getInstance().getAllSchemaNames()).thenReturn(Arrays.asList("foo", "bar", "sharding_db", "other_db"));
        OpenGaussSelectDatabaseExecutor executor = new OpenGaussSelectDatabaseExecutor(SQL);
        executor.execute(null);
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
