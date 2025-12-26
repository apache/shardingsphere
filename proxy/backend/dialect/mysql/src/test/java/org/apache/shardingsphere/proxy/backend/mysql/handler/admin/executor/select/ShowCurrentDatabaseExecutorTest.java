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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select;

import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowCurrentDatabaseExecutorTest {
    
    @Test
    void assertExecute() throws SQLException {
        ShowCurrentDatabaseExecutor executor = new ShowCurrentDatabaseExecutor();
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        executor.execute(connectionSession, mock());
        assertTrue(executor.getMergedResult().next());
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("foo_db"));
        assertFalse(executor.getMergedResult().next());
    }
    
    @Test
    void assertGetQueryResultMetaData() throws SQLException {
        ShowCurrentDatabaseExecutor executor = new ShowCurrentDatabaseExecutor();
        QueryResultMetaData metaData = executor.getQueryResultMetaData();
        assertThat(metaData.getColumnCount(), is(1));
        assertThat(metaData.getColumnName(1), is(ShowCurrentDatabaseExecutor.FUNCTION_NAME));
        assertThat(metaData.getColumnLabel(1), is(ShowCurrentDatabaseExecutor.FUNCTION_NAME));
    }
}
