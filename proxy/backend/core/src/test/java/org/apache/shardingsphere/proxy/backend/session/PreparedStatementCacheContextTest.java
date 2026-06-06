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
import static org.mockito.Mockito.when;

class PreparedStatementCacheContextTest {
    
    @Test
    void assertGetOrCreateWithSamePreparedStatementCacheKey() throws SQLException {
        PreparedStatementCacheContext cacheContext = new PreparedStatementCacheContext(8);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        PreparedStatementCacheKey preparedStatementCacheKey = new PreparedStatementCacheKey("statement-1");
        int[] invokedCount = {0};
        PreparedStatement actualFirst = cacheContext.getOrCreate(connection, "SELECT 1", false, preparedStatementCacheKey, () -> {
            invokedCount[0]++;
            return preparedStatement;
        });
        PreparedStatement actualSecond = cacheContext.getOrCreate(connection, "SELECT 1", false, preparedStatementCacheKey, () -> {
            invokedCount[0]++;
            return mock(PreparedStatement.class);
        });
        assertThat(actualFirst, is(preparedStatement));
        assertThat(actualSecond, is(preparedStatement));
        assertThat(invokedCount[0], is(1));
        assertTrue(cacheContext.contains(preparedStatement));
    }
    
    @Test
    void assertGetOrCreateWithDifferentPreparedStatementCacheKey() throws SQLException {
        PreparedStatementCacheContext cacheContext = new PreparedStatementCacheContext(8);
        Connection connection = mock(Connection.class);
        PreparedStatement firstPreparedStatement = mock(PreparedStatement.class);
        PreparedStatement secondPreparedStatement = mock(PreparedStatement.class);
        PreparedStatement actualFirst = cacheContext.getOrCreate(connection, "SELECT 1", false, new PreparedStatementCacheKey("statement-1"), () -> firstPreparedStatement);
        PreparedStatement actualSecond = cacheContext.getOrCreate(connection, "SELECT 1", false, new PreparedStatementCacheKey("statement-2"), () -> secondPreparedStatement);
        assertThat(actualFirst, is(firstPreparedStatement));
        assertThat(actualSecond, is(secondPreparedStatement));
    }
    
    @Test
    void assertEvictWithLRU() throws SQLException {
        PreparedStatementCacheContext cacheContext = new PreparedStatementCacheContext(2);
        Connection connection = mock(Connection.class);
        PreparedStatement first = mock(PreparedStatement.class);
        PreparedStatement second = mock(PreparedStatement.class);
        PreparedStatement third = mock(PreparedStatement.class);
        cacheContext.getOrCreate(connection, "SELECT 1", false, new PreparedStatementCacheKey("statement-1"), () -> first);
        cacheContext.getOrCreate(connection, "SELECT 2", false, new PreparedStatementCacheKey("statement-2"), () -> second);
        cacheContext.getOrCreate(connection, "SELECT 1", false, new PreparedStatementCacheKey("statement-1"), () -> first);
        cacheContext.getOrCreate(connection, "SELECT 3", false, new PreparedStatementCacheKey("statement-3"), () -> third);
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
        cacheContext.getOrCreate(connection, "SELECT 1", false, new PreparedStatementCacheKey("statement-1"), () -> preparedStatement);
        cacheContext.invalidate(preparedStatement);
        assertFalse(cacheContext.contains(preparedStatement));
        verify(preparedStatement).close();
    }
    
    @Test
    void assertInvalidateByPreparedStatementCacheKey() throws SQLException {
        PreparedStatementCacheContext cacheContext = new PreparedStatementCacheContext(8);
        Connection connection = mock(Connection.class);
        PreparedStatement first = mock(PreparedStatement.class);
        PreparedStatement second = mock(PreparedStatement.class);
        PreparedStatementCacheKey firstPreparedStatementCacheKey = new PreparedStatementCacheKey("statement-1");
        PreparedStatementCacheKey secondPreparedStatementCacheKey = new PreparedStatementCacheKey("statement-2");
        cacheContext.getOrCreate(connection, "SELECT 1", false, firstPreparedStatementCacheKey, () -> first);
        cacheContext.getOrCreate(connection, "SELECT 1", false, secondPreparedStatementCacheKey, () -> second);
        cacheContext.invalidate(firstPreparedStatementCacheKey);
        assertFalse(cacheContext.contains(first));
        assertTrue(cacheContext.contains(second));
        verify(first).close();
    }
    
    @Test
    void assertGetOrCreateWithClosedPreparedStatement() throws SQLException {
        PreparedStatementCacheContext cacheContext = new PreparedStatementCacheContext(8);
        Connection connection = mock(Connection.class);
        PreparedStatement firstPreparedStatement = mock(PreparedStatement.class);
        PreparedStatement secondPreparedStatement = mock(PreparedStatement.class);
        when(firstPreparedStatement.isClosed()).thenReturn(true);
        PreparedStatementCacheKey preparedStatementCacheKey = new PreparedStatementCacheKey("statement-1");
        cacheContext.getOrCreate(connection, "SELECT 1", false, preparedStatementCacheKey, () -> firstPreparedStatement);
        PreparedStatement actualPreparedStatement = cacheContext.getOrCreate(connection, "SELECT 1", false, preparedStatementCacheKey, () -> secondPreparedStatement);
        assertThat(actualPreparedStatement, is(secondPreparedStatement));
        verify(firstPreparedStatement).close();
    }
    
    @Test
    void assertCloseAll() throws SQLException {
        PreparedStatementCacheContext cacheContext = new PreparedStatementCacheContext(8);
        Connection connection = mock(Connection.class);
        PreparedStatement first = mock(PreparedStatement.class);
        PreparedStatement second = mock(PreparedStatement.class);
        cacheContext.getOrCreate(connection, "SELECT 1", false, new PreparedStatementCacheKey("statement-1"), () -> first);
        cacheContext.getOrCreate(connection, "SELECT 2", false, new PreparedStatementCacheKey("statement-2"), () -> second);
        cacheContext.closeAll();
        assertThat(cacheContext.size(), is(0));
        verify(first).close();
        verify(second).close();
    }
}
