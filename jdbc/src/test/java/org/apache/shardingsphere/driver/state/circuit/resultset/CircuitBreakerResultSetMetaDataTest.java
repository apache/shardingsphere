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

package org.apache.shardingsphere.driver.state.circuit.resultset;

import org.junit.jupiter.api.Test;

import java.sql.ResultSetMetaData;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class CircuitBreakerResultSetMetaDataTest {
    
    @Test
    void assertGetColumnCount() {
        assertThat(new CircuitBreakerResultSetMetaData().getColumnCount(), is(0));
    }
    
    @Test
    void assertIsAutoIncrement() {
        assertFalse(new CircuitBreakerResultSetMetaData().isAutoIncrement(1));
    }
    
    @Test
    void assertIsCaseSensitive() {
        assertFalse(new CircuitBreakerResultSetMetaData().isCaseSensitive(1));
    }
    
    @Test
    void assertIsSearchable() {
        assertFalse(new CircuitBreakerResultSetMetaData().isSearchable(1));
    }
    
    @Test
    void assertIsCurrency() {
        assertFalse(new CircuitBreakerResultSetMetaData().isCurrency(1));
    }
    
    @Test
    void assertIsNullable() {
        assertThat(new CircuitBreakerResultSetMetaData().isNullable(1), is(ResultSetMetaData.columnNullable));
    }
    
    @Test
    void assertIsSigned() {
        assertFalse(new CircuitBreakerResultSetMetaData().isSigned(1));
    }
    
    @Test
    void assertGetColumnDisplaySize() {
        assertThat(new CircuitBreakerResultSetMetaData().getColumnDisplaySize(1), is(0));
    }
    
    @Test
    void assertGetColumnLabel() {
        assertNull(new CircuitBreakerResultSetMetaData().getColumnLabel(1));
    }
    
    @Test
    void assertGetColumnName() {
        assertNull(new CircuitBreakerResultSetMetaData().getColumnName(1));
    }
    
    @Test
    void assertGetSchemaName() {
        assertNull(new CircuitBreakerResultSetMetaData().getSchemaName(1));
    }
    
    @Test
    void assertGetPrecision() {
        assertThat(new CircuitBreakerResultSetMetaData().getPrecision(1), is(0));
    }
    
    @Test
    void assertGetScale() {
        assertThat(new CircuitBreakerResultSetMetaData().getScale(1), is(0));
    }
    
    @Test
    void assertGetTableName() {
        assertNull(new CircuitBreakerResultSetMetaData().getTableName(1));
    }
    
    @Test
    void assertGetCatalogName() {
        assertNull(new CircuitBreakerResultSetMetaData().getCatalogName(1));
    }
    
    @Test
    void assertGetColumnType() {
        assertThat(new CircuitBreakerResultSetMetaData().getColumnType(1), is(0));
    }
    
    @Test
    void assertGetColumnTypeName() {
        assertNull(new CircuitBreakerResultSetMetaData().getColumnTypeName(1));
    }
    
    @Test
    void assertIsReadOnly() {
        assertFalse(new CircuitBreakerResultSetMetaData().isReadOnly(1));
    }
    
    @Test
    void assertIsWritable() {
        assertFalse(new CircuitBreakerResultSetMetaData().isWritable(1));
    }
    
    @Test
    void assertIsDefinitelyWritable() {
        assertFalse(new CircuitBreakerResultSetMetaData().isDefinitelyWritable(1));
    }
    
    @Test
    void assertGetColumnClassName() {
        assertNull(new CircuitBreakerResultSetMetaData().getColumnClassName(1));
    }
    
    @Test
    void assertUnwrap() {
        assertNull(new CircuitBreakerResultSetMetaData().unwrap(Object.class));
    }
    
    @Test
    void assertIsWrapperFor() {
        assertFalse(new CircuitBreakerResultSetMetaData().isWrapperFor(Object.class));
    }
}
