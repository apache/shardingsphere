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

package org.apache.shardingsphere.data.pipeline.core.util;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PipelineJdbcUtilsTest {
    
    @Test
    void assertIsIntegerColumn() {
        assertTrue(PipelineJdbcUtils.isIntegerColumn(Types.INTEGER));
        assertTrue(PipelineJdbcUtils.isIntegerColumn(Types.BIGINT));
        assertTrue(PipelineJdbcUtils.isIntegerColumn(Types.SMALLINT));
        assertTrue(PipelineJdbcUtils.isIntegerColumn(Types.TINYINT));
        assertFalse(PipelineJdbcUtils.isIntegerColumn(Types.VARCHAR));
    }
    
    @Test
    void assertIsStringColumn() {
        assertTrue(PipelineJdbcUtils.isStringColumn(Types.CHAR));
        assertTrue(PipelineJdbcUtils.isStringColumn(Types.VARCHAR));
        assertTrue(PipelineJdbcUtils.isStringColumn(Types.LONGVARCHAR));
        assertTrue(PipelineJdbcUtils.isStringColumn(Types.NCHAR));
        assertTrue(PipelineJdbcUtils.isStringColumn(Types.NVARCHAR));
        assertTrue(PipelineJdbcUtils.isStringColumn(Types.LONGNVARCHAR));
        assertFalse(PipelineJdbcUtils.isStringColumn(Types.INTEGER));
    }
    
    @Test
    void assertIsBinaryColumn() {
        assertTrue(PipelineJdbcUtils.isBinaryColumn(Types.BINARY));
        assertTrue(PipelineJdbcUtils.isBinaryColumn(Types.VARBINARY));
        assertTrue(PipelineJdbcUtils.isBinaryColumn(Types.LONGVARBINARY));
        assertFalse(PipelineJdbcUtils.isBinaryColumn(Types.VARCHAR));
    }
    
    @Test
    void assertCancelStatementWhenIsClosed() throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.isClosed()).thenReturn(true);
        PipelineJdbcUtils.cancelStatement(statement);
        verify(statement, times(0)).cancel();
    }
    
    @Test
    void assertCancelStatementWhenIsNotClosed() throws SQLException {
        Statement statement = mock(Statement.class);
        PipelineJdbcUtils.cancelStatement(statement);
        verify(statement).cancel();
    }
    
    @Test
    void assertCancelStatementWhenSQLExceptionThrown() throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.isClosed()).thenThrow(SQLException.class);
        PipelineJdbcUtils.cancelStatement(statement);
        verify(statement, times(0)).cancel();
    }
}
