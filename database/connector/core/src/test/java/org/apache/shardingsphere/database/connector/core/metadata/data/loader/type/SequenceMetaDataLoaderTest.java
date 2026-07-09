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

package org.apache.shardingsphere.database.connector.core.metadata.data.loader.type;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.sequence.DialectSequenceOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.system.DialectSystemDatabase;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SequenceMetaDataLoaderTest {
    
    private final DatabaseType databaseType = mock(DatabaseType.class);
    
    @Test
    void assertLoadWithoutSequenceOption() throws SQLException {
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getSequenceOption()).thenReturn(Optional.empty());
        Connection connection = mock(Connection.class);
        try (MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            assertThat(new SequenceMetaDataLoader(databaseType).load(connection), is(Collections.emptyMap()));
        }
        verifyNoInteractions(connection);
    }
    
    @Test
    void assertLoad() throws SQLException {
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getSequenceOption()).thenReturn(Optional.of(new DialectSequenceOption("SELECT SEQUENCES")));
        DialectSystemDatabase dialectSystemDatabase = mock(DialectSystemDatabase.class);
        when(dialectSystemDatabase.getSystemSchemas()).thenReturn(Collections.singleton("pg_catalog"));
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mockResultSet(Arrays.asList(
                createRow("public", "order_seq"),
                createRow("PG_CATALOG", "system_seq"),
                createRow("public", ""),
                createRow("", "global_seq")));
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT SEQUENCES")).thenReturn(resultSet);
        try (MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectSystemDatabase.class, databaseType)).thenReturn(Optional.of(dialectSystemDatabase));
            Map<String, Collection<String>> actual = new SequenceMetaDataLoader(databaseType).load(connection);
            Map<String, Collection<String>> expected = new LinkedHashMap<>(2, 1F);
            expected.put("public", new LinkedHashSet<>(Collections.singleton("order_seq")));
            expected.put("", new LinkedHashSet<>(Collections.singleton("global_seq")));
            assertThat(actual, is(expected));
        }
    }
    
    private Map<String, String> createRow(final String sequenceSchema, final String sequenceName) {
        Map<String, String> result = new LinkedHashMap<>(2, 1F);
        result.put("SEQUENCE_SCHEMA", sequenceSchema);
        result.put("SEQUENCE_NAME", sequenceName);
        return result;
    }
    
    private ResultSet mockResultSet(final List<Map<String, String>> rows) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        AtomicInteger rowIndex = new AtomicInteger(-1);
        when(result.next()).thenAnswer(invocation -> rowIndex.incrementAndGet() < rows.size());
        when(result.getString("SEQUENCE_SCHEMA")).thenAnswer(invocation -> rows.get(rowIndex.get()).get("SEQUENCE_SCHEMA"));
        when(result.getString("SEQUENCE_NAME")).thenAnswer(invocation -> rows.get(rowIndex.get()).get("SEQUENCE_NAME"));
        return result;
    }
}
