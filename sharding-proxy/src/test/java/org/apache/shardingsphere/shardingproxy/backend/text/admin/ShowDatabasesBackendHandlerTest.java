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

package org.apache.shardingsphere.shardingproxy.backend.text.admin;

import org.apache.shardingsphere.shardingproxy.backend.MockGlobalRegistryUtil;
import org.apache.shardingsphere.shardingproxy.backend.result.query.QueryData;
import org.apache.shardingsphere.shardingproxy.backend.result.query.QueryHeaderResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.sql.Types;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class ShowDatabasesBackendHandlerTest {
    
    private ShowDatabasesBackendHandler showDatabasesBackendHandler = new ShowDatabasesBackendHandler();
    
    @Before
    public void setUp() {
        MockGlobalRegistryUtil.setLogicSchemas("schema", 5);
    }
    
    @Test
    public void assertExecuteShowDatabaseBackendHandler() {
        QueryHeaderResponse actual = (QueryHeaderResponse) showDatabasesBackendHandler.execute();
        assertThat(actual, instanceOf(QueryHeaderResponse.class));
        assertThat(actual.getQueryHeaders().size(), is(1));
    }
    
    @Test
    public void assertShowDatabaseUsingStream() throws SQLException {
        showDatabasesBackendHandler.execute();
        int sequenceId = 4;
        while (showDatabasesBackendHandler.next()) {
            QueryData queryData = showDatabasesBackendHandler.getQueryData();
            assertThat(queryData.getColumnCount(), is(1));
            assertThat(queryData.getColumnTypes().size(), is(1));
            assertThat(queryData.getColumnTypes().iterator().next(), is(Types.VARCHAR));
            assertThat(queryData.getSequenceId(), is(sequenceId));
            assertThat(queryData.getData().size(), is(1));
            ++sequenceId;
        }
    }
}
