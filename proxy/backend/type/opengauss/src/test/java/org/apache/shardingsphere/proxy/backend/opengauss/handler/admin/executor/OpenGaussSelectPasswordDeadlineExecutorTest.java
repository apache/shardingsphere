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

package org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.executor;

import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class OpenGaussSelectPasswordDeadlineExecutorTest {
    
    @Test
    void assertAccept() {
        assertTrue(OpenGaussSelectPasswordDeadlineExecutor.accept("pg_catalog.intervaltonum(pg_catalog.gs_password_deadline())"));
        assertTrue(OpenGaussSelectPasswordDeadlineExecutor.accept("pg_catalog.gs_password_deadline()"));
        assertTrue(OpenGaussSelectPasswordDeadlineExecutor.accept("gs_password_deadline()"));
    }
    
    @Test
    void assertExecute() throws SQLException {
        assertExecute("pg_catalog.intervaltonum(pg_catalog.gs_password_deadline())", "intervaltonum", "90");
        assertExecute("pg_catalog.gs_password_deadline()", "gs_password_deadline", "90");
        assertExecute("gs_password_deadline()", "gs_password_deadline", "90");
    }
    
    private void assertExecute(final String functionName, final String expectedColumnLabel, final String expectedValue) throws SQLException {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        OpenGaussSelectPasswordDeadlineExecutor executor = new OpenGaussSelectPasswordDeadlineExecutor(functionName);
        executor.execute(connectionSession);
        QueryResultMetaData actualMetaData = executor.getQueryResultMetaData();
        assertThat(actualMetaData.getColumnCount(), is(1));
        assertThat(actualMetaData.getColumnLabel(1), is(expectedColumnLabel));
        MergedResult actualResult = executor.getMergedResult();
        assertTrue(actualResult.next());
        assertThat(actualResult.getValue(1, String.class), is(expectedValue));
        assertFalse(actualResult.next());
    }
}
