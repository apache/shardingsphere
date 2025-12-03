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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class CircuitBreakerResultSetMetaDataTest {
    
    @Test
    void assertGetColumnCount() {
        assertThat(new CircuitBreakerResultSetMetaData().getColumnCount(), is(0));
    }
    
    @Test
    void assertIsAutoIncrement() {
        assertThat(new CircuitBreakerResultSetMetaData().isAutoIncrement(1), is(false));
    }
    
    @Test
    void assertIsCaseSensitive() {
        assertThat(new CircuitBreakerResultSetMetaData().isCaseSensitive(1), is(false));
    }
    
    @Test
    void assertIsSearchable() {
        assertThat(new CircuitBreakerResultSetMetaData().isSearchable(1), is(false));
    }
    
    @Test
    void assertIsCurrency() {
        assertThat(new CircuitBreakerResultSetMetaData().isCurrency(1), is(false));
    }
    
    @Test
    void assertIsNullable() {
        assertThat(new CircuitBreakerResultSetMetaData().isNullable(1), is(ResultSetMetaData.columnNullable));
    }
    
    @Test
    void assertIsSigned() {
        assertThat(new CircuitBreakerResultSetMetaData().isSigned(1), is(false));
    }
    
    @Test
    void assertGetColumnDisplaySize() {
        assertThat(new CircuitBreakerResultSetMetaData().getColumnDisplaySize(1), is(0));
    }
    
    @Test
    void assertGetColumnLabel() {
        assertThat(new CircuitBreakerResultSetMetaData().getColumnLabel(1), is(nullValue()));
    }
    
    @Test
    void assertGetColumnName() {
        assertThat(new CircuitBreakerResultSetMetaData().getColumnName(1), is(nullValue()));
    }
    
    @Test
    void assertGetSchemaName() {
        assertThat(new CircuitBreakerResultSetMetaData().getSchemaName(1), is(nullValue()));
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
        assertThat(new CircuitBreakerResultSetMetaData().getTableName(1), is(nullValue()));
    }
    
    @Test
    void assertGetCatalogName() {
        assertThat(new CircuitBreakerResultSetMetaData().getCatalogName(1), is(nullValue()));
    }
    
    @Test
    void assertGetColumnType() {
        assertThat(new CircuitBreakerResultSetMetaData().getColumnType(1), is(0));
    }
    
    @Test
    void assertGetColumnTypeName() {
        assertThat(new CircuitBreakerResultSetMetaData().getColumnTypeName(1), is(nullValue()));
    }
    
    @Test
    void assertIsReadOnly() {
        assertThat(new CircuitBreakerResultSetMetaData().isReadOnly(1), is(false));
    }
    
    @Test
    void assertIsWritable() {
        assertThat(new CircuitBreakerResultSetMetaData().isWritable(1), is(false));
    }
    
    @Test
    void assertIsDefinitelyWritable() {
        assertThat(new CircuitBreakerResultSetMetaData().isDefinitelyWritable(1), is(false));
    }
    
    @Test
    void assertGetColumnClassName() {
        assertThat(new CircuitBreakerResultSetMetaData().getColumnClassName(1), is(nullValue()));
    }
    
    @Test
    void assertUnwrap() {
        assertThat(new CircuitBreakerResultSetMetaData().unwrap(Object.class), is(nullValue()));
    }
    
    @Test
    void assertIsWrapperFor() {
        assertThat(new CircuitBreakerResultSetMetaData().isWrapperFor(Object.class), is(false));
    }
}
