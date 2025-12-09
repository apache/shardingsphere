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

package org.apache.shardingsphere.sqlfederation.mysql;

import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqlfederation.resultset.converter.SQLFederationColumnTypeConverter;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class MySQLSQLFederationColumnTypeConverterTest {
    
    private final SQLFederationColumnTypeConverter converter = DatabaseTypedSPILoader.getService(SQLFederationColumnTypeConverter.class, TypedSPILoader.getService(DatabaseType.class, "MySQL"));
    
    @Test
    void assertConvertColumnValueWhenBooleanTrue() {
        assertThat(converter.convertColumnValue(Boolean.TRUE), is(1));
    }
    
    @Test
    void assertConvertColumnValueWhenBooleanFalse() {
        assertThat(converter.convertColumnValue(Boolean.FALSE), is(0));
    }
    
    @Test
    void assertConvertColumnValueWithNonBoolean() {
        String value = "text";
        assertThat(converter.convertColumnValue(value), is(value));
    }
    
    @Test
    void assertConvertColumnValueWithNull() {
        assertNull(converter.convertColumnValue(null));
    }
    
    @Test
    void assertConvertColumnTypeWhenBoolean() {
        assertThat(converter.convertColumnType(SqlTypeName.BOOLEAN), is(SqlTypeName.VARCHAR.getJdbcOrdinal()));
    }
    
    @Test
    void assertConvertColumnTypeWhenAny() {
        assertThat(converter.convertColumnType(SqlTypeName.ANY), is(SqlTypeName.VARCHAR.getJdbcOrdinal()));
    }
    
    @Test
    void assertConvertColumnTypeWhenOthers() {
        assertThat(converter.convertColumnType(SqlTypeName.INTEGER), is(SqlTypeName.INTEGER.getJdbcOrdinal()));
    }
}
