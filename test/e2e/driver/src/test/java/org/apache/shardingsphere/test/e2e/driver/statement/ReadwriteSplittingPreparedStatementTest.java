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

package org.apache.shardingsphere.test.e2e.driver.statement;

import org.apache.shardingsphere.test.e2e.driver.AbstractReadwriteSplittingDriverTest;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadwriteSplittingPreparedStatementTest extends AbstractReadwriteSplittingDriverTest {
    
    @Test
    void assertQueryWithNull() {
        assertThrows(SQLException.class, () -> getReadwriteSplittingDataSource().getConnection().prepareStatement(null));
    }
    
    @Test
    void assertQueryWithEmptyString() {
        assertThrows(SQLException.class, () -> getReadwriteSplittingDataSource().getConnection().prepareStatement(""));
    }
    
    @Test
    void assertGetParameterMetaData() throws SQLException {
        try (PreparedStatement preparedStatement = getReadwriteSplittingDataSource().getConnection().prepareStatement("SELECT * FROM t_config where id = ?")) {
            assertThat(preparedStatement.getParameterMetaData().getParameterCount(), is(1));
        }
    }
    
    @Test
    void assertGetGeneratedKeys() throws SQLException {
        try (
                PreparedStatement preparedStatement = getReadwriteSplittingDataSource()
                        .getConnection().prepareStatement("INSERT INTO t_config(status) VALUES(?);", Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, "OK");
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            assertTrue(generatedKeys.next());
            int columnCount = generatedKeys.getMetaData().getColumnCount();
            for (int index = 0; index < columnCount; index++) {
                assertNotNull(generatedKeys.getObject(index + 1));
                assertNotNull(generatedKeys.getMetaData().getColumnLabel(index + 1));
                assertNotNull(generatedKeys.getMetaData().getColumnName(index + 1));
            }
            assertFalse(generatedKeys.next());
        }
    }
    
    @Test
    void assertGetGeneratedKeysWithPrimaryKeyIsNull() throws SQLException {
        try (
                PreparedStatement preparedStatement = getReadwriteSplittingDataSource()
                        .getConnection().prepareStatement("INSERT INTO t_config(id, status) VALUES(?, ?);", Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setObject(1, null);
            preparedStatement.setString(2, "OK");
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            assertTrue(generatedKeys.next());
            int columnCount = generatedKeys.getMetaData().getColumnCount();
            for (int index = 0; index < columnCount; index++) {
                assertNotNull(generatedKeys.getObject(index + 1));
                assertNotNull(generatedKeys.getMetaData().getColumnLabel(index + 1));
                assertNotNull(generatedKeys.getMetaData().getColumnName(index + 1));
            }
            assertFalse(generatedKeys.next());
        }
    }
    
    @Test
    void assertGetGeneratedKeysWithPrimaryKeyIsNullInTransactional() throws SQLException {
        try (
                Connection connection = getReadwriteSplittingDataSource()
                        .getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO t_config(id, status) VALUES(?, ?);", Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);
            Object lastGeneratedId = null;
            Object generatedId;
            for (int i = 1; i <= 3; i++) {
                preparedStatement.setObject(1, null);
                preparedStatement.setString(2, "OK");
                preparedStatement.executeUpdate();
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                assertTrue(generatedKeys.next());
                generatedId = generatedKeys.getObject(1);
                assertThat(generatedId, not(lastGeneratedId));
                lastGeneratedId = generatedId;
                assertFalse(generatedKeys.next());
            }
            connection.commit();
        }
    }
    
    @Test
    void assertGetAutoGeneratedKeysAfterExecuteBatch() throws SQLException {
        try (
                Connection connection = getReadwriteSplittingDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO t_config(status) VALUES(?);", Statement.RETURN_GENERATED_KEYS)) {
            int batchSize = 4;
            for (int i = 0; i < batchSize; i++) {
                preparedStatement.setString(1, "BATCH");
                preparedStatement.addBatch();
            }
            int[] result = preparedStatement.executeBatch();
            for (int each : result) {
                assertThat(each, is(1));
            }
            ResultSet generateKeyResultSet = preparedStatement.getGeneratedKeys();
            Object lastGeneratedId = null;
            Object generatedId;
            for (int i = 0; i < batchSize; i++) {
                assertTrue(generateKeyResultSet.next());
                generatedId = generateKeyResultSet.getObject(1);
                assertThat(generatedId, not(lastGeneratedId));
                lastGeneratedId = generatedId;
            }
            assertFalse(generateKeyResultSet.next());
        }
    }
    
    @Test
    void assertGetAutoGeneratedKeysAfterExecuteBatchMultiValues() throws SQLException {
        try (
                Connection connection = getReadwriteSplittingDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO t_config(status) VALUES(?),(?);", Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, "BATCH");
            preparedStatement.setString(2, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setString(1, "BATCH");
            preparedStatement.setString(2, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setString(1, "BATCH");
            preparedStatement.setString(2, "BATCH");
            preparedStatement.addBatch();
            preparedStatement.setString(1, "BATCH");
            preparedStatement.setString(2, "BATCH");
            preparedStatement.addBatch();
            int[] result = preparedStatement.executeBatch();
            for (int each : result) {
                assertThat(each, is(2));
            }
            ResultSet generateKeyResultSet = preparedStatement.getGeneratedKeys();
            Object lastGeneratedId = null;
            Object generatedId;
            for (int i = 0; i < 8; i++) {
                assertTrue(generateKeyResultSet.next());
                generatedId = generateKeyResultSet.getObject(1);
                assertThat(generatedId, not(lastGeneratedId));
                lastGeneratedId = generatedId;
            }
            assertFalse(generateKeyResultSet.next());
        }
    }
}
