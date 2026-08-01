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

package org.apache.shardingsphere.database.protocol.postgresql.type;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ColumnTypeOIDLoaderTest {
    
    @Test
    void assertLoadFromResultSetMetaData() throws SQLException {
        Connection connection = mock(Connection.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(3);
        when(metaData.getColumnType(1)).thenReturn(Types.STRUCT);
        when(metaData.getColumnType(2)).thenReturn(Types.STRUCT);
        when(metaData.getColumnType(3)).thenReturn(Types.VARCHAR);
        when(metaData.getColumnTypeName(1)).thenReturn("record_type");
        when(metaData.getColumnTypeName(2)).thenReturn("unknown_type");
        ColumnTypeOIDResolver resolver = mock(ColumnTypeOIDResolver.class);
        when(resolver.findTypeOID(connection, "record_type")).thenReturn(Optional.of(2249));
        when(resolver.findTypeOID(connection, "unknown_type")).thenReturn(Optional.empty());
        Map<Integer, Integer> actual = ColumnTypeOIDLoader.load(connection, metaData, resolver);
        assertThat(actual, is(Collections.singletonMap(1, 2249)));
        verify(metaData, never()).getColumnTypeName(3);
    }
}
