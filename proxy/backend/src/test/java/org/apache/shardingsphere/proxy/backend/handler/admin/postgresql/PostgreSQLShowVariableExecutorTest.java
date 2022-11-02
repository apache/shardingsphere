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

package org.apache.shardingsphere.proxy.backend.handler.admin.postgresql;

import org.apache.shardingsphere.infra.autogen.version.ShardingSphereVersion;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLShowStatement;
import org.junit.Test;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class PostgreSQLShowVariableExecutorTest {
    
    @Test
    public void assertExecuteShowAll() throws SQLException {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        PostgreSQLShowVariableExecutor executor = new PostgreSQLShowVariableExecutor(new PostgreSQLShowStatement("ALL"));
        executor.execute(connectionSession);
        QueryResultMetaData actualMetaData = executor.getQueryResultMetaData();
        assertThat(actualMetaData.getColumnCount(), is(3));
        assertThat(actualMetaData.getColumnLabel(1), is("name"));
        assertThat(actualMetaData.getColumnLabel(2), is("setting"));
        assertThat(actualMetaData.getColumnLabel(3), is("description"));
        MergedResult actualResult = executor.getMergedResult();
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("application_name", "PostgreSQL");
        expected.put("client_encoding", "UTF8");
        expected.put("integer_datetimes", "on");
        expected.put("TimeZone", "Etc/UTC");
        expected.put("transaction_isolation", "read committed");
        expected.put("transaction_read_only", "off");
        expected.put("server_version", ShardingSphereVersion.VERSION);
        for (Entry<String, String> entry : expected.entrySet()) {
            assertTrue(actualResult.next());
            assertThat(actualResult.getValue(1, String.class), is(entry.getKey()));
            assertThat(actualResult.getValue(2, String.class), is(entry.getValue()));
        }
        assertFalse(actualResult.next());
    }
    
    @Test
    public void assertExecuteShowOne() throws SQLException {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        PostgreSQLShowVariableExecutor executor = new PostgreSQLShowVariableExecutor(new PostgreSQLShowStatement("client_encoding"));
        executor.execute(connectionSession);
        QueryResultMetaData actualMetaData = executor.getQueryResultMetaData();
        assertThat(actualMetaData.getColumnCount(), is(1));
        assertThat(actualMetaData.getColumnLabel(1), is("client_encoding"));
        MergedResult actualResult = executor.getMergedResult();
        assertTrue(actualResult.next());
        assertThat(actualResult.getValue(1, String.class), is("UTF8"));
        assertFalse(actualResult.next());
    }
}
