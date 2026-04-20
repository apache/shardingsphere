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

package org.apache.shardingsphere.proxy.backend.session;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PreparedStatementCacheContextTest {
    
    @Test
    void assertGetOrCreate() throws SQLException {
        PreparedStatementCacheContext cacheContext = new PreparedStatementCacheContext(8);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        int[] invokedCount = {0};
        PreparedStatement actualFirst = cacheContext.getOrCreate(connection, "SELECT 1", false, () -> {
            invokedCount[0]++;
            return preparedStatement;
        });
        PreparedStatement actualSecond = cacheContext.getOrCreate(connection, "SELECT 1", false, () -> {
            invokedCount[0]++;
            return mock(PreparedStatement.class);
        });
        assertThat(actualFirst, is(preparedStatement));
        assertThat(actualSecond, is(preparedStatement));
        assertThat(invokedCount[0], is(1));
        assertTrue(cacheContext.contains(preparedStatement));
    }
    
    @Test
    void assertEvictWithLRU() throws SQLException {
        PreparedStatementCacheContext cacheContext = new PreparedStatementCacheContext(2);
        Connection connection = mock(Connection.class);
        PreparedStatement first = mock(PreparedStatement.class);
        PreparedStatement second = mock(PreparedStatement.class);
        PreparedStatement third = mock(PreparedStatement.class);
        cacheContext.getOrCreate(connection, "SELECT 1", false, () -> first);
        cacheContext.getOrCreate(connection, "SELECT 2", false, () -> second);
        cacheContext.getOrCreate(connection, "SELECT 1", false, () -> first);
        cacheContext.getOrCreate(connection, "SELECT 3", false, () -> third);
        assertFalse(cacheContext.contains(second));
        assertTrue(cacheContext.contains(first));
        assertTrue(cacheContext.contains(third));
        verify(second).close();
    }
    
    @Test
    void assertInvalidate() throws SQLException {
        PreparedStatementCacheContext cacheContext = new PreparedStatementCacheContext(8);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        cacheContext.getOrCreate(connection, "SELECT 1", false, () -> preparedStatement);
        cacheContext.invalidate(preparedStatement);
        assertFalse(cacheContext.contains(preparedStatement));
        verify(preparedStatement).close();
    }
    
    @Test
    void assertCloseAll() throws SQLException {
        PreparedStatementCacheContext cacheContext = new PreparedStatementCacheContext(8);
        Connection connection = mock(Connection.class);
        PreparedStatement first = mock(PreparedStatement.class);
        PreparedStatement second = mock(PreparedStatement.class);
        cacheContext.getOrCreate(connection, "SELECT 1", false, () -> first);
        cacheContext.getOrCreate(connection, "SELECT 2", false, () -> second);
        cacheContext.closeAll();
        assertThat(cacheContext.size(), is(0));
        verify(first).close();
        verify(second).close();
    }
}
