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

package org.apache.shardingsphere.database.connector.hive.jdbcurl;

import org.apache.hive.jdbc.HiveConnection;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HiveJdbcUrlFetcherTest {
    
    private final HiveJdbcUrlFetcher fetcher = new HiveJdbcUrlFetcher();
    
    @Test
    void assertFetch() throws SQLException {
        HiveConnection hiveConnection = mock(HiveConnection.class);
        when(hiveConnection.getConnectedUrl()).thenReturn("jdbc:hive2://localhost:10000/db");
        Connection connection = mock(Connection.class);
        when(connection.unwrap(HiveConnection.class)).thenReturn(hiveConnection);
        assertThat(fetcher.fetch(connection), is("jdbc:hive2://localhost:10000/db"));
    }
    
    @Test
    void assertGetConnectionClass() {
        assertThat(fetcher.getConnectionClass(), is(HiveConnection.class));
    }
}
