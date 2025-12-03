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

package org.apache.shardingsphere.sqlfederation.executor.enumerable.enumerator.memory;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.util.ResultSetUtils;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.sqlfederation.compiler.sql.type.SQLFederationDataTypeBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class MemoryDataTypeConverterTest {
    
    @Test
    void assertCreateColumnTypes() {
        DatabaseType databaseType = mock(DatabaseType.class);
        List<ShardingSphereColumn> columns = Arrays.asList(
                new ShardingSphereColumn("skipped", Types.OTHER, false, false, false, true, false, true),
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, true));
        try (MockedStatic<SQLFederationDataTypeBuilder> mockedStatic = mockStatic(SQLFederationDataTypeBuilder.class)) {
            mockedStatic.when(() -> SQLFederationDataTypeBuilder.getSqlTypeClass(databaseType, columns.get(0))).thenThrow(new IllegalArgumentException("unsupported"));
            mockedStatic.when(() -> SQLFederationDataTypeBuilder.getSqlTypeClass(databaseType, columns.get(1))).thenReturn(Integer.class);
            Map<Integer, Class<?>> actual = MemoryDataTypeConverter.createColumnTypes(columns, databaseType);
            assertThat(actual.size(), is(1));
            assertNull(actual.get(0));
            assertThat(actual.get(1), is(Integer.class));
        }
    }
    
    @Test
    void assertConvertToTargetTypeConvertsAndLeavesMissingIndexNull() {
        Map<Integer, Class<?>> columnTypes = Collections.singletonMap(0, Integer.class);
        Object[] rows = new Object[] {"10", "unused"};
        try (MockedStatic<ResultSetUtils> mockedStatic = mockStatic(ResultSetUtils.class)) {
            mockedStatic.when(() -> ResultSetUtils.convertValue("10", Integer.class)).thenReturn(10);
            Object[] actual = MemoryDataTypeConverter.convertToTargetType(columnTypes, rows);
            assertThat(actual.length, is(2));
            assertThat(actual[0], is(10));
            assertNull(actual[1]);
        }
    }
    
    @Test
    void assertConvertToTargetTypeReturnsOriginalWhenConversionFails() {
        Map<Integer, Class<?>> columnTypes = Collections.singletonMap(0, Integer.class);
        Object[] rows = new Object[] {"bad"};
        try (MockedStatic<ResultSetUtils> mockedStatic = mockStatic(ResultSetUtils.class)) {
            mockedStatic.when(() -> ResultSetUtils.convertValue("bad", Integer.class)).thenThrow(new SQLFeatureNotSupportedException("unsupported"));
            Object[] actual = MemoryDataTypeConverter.convertToTargetType(columnTypes, rows);
            assertThat(actual, arrayContaining("bad"));
        }
    }
}
