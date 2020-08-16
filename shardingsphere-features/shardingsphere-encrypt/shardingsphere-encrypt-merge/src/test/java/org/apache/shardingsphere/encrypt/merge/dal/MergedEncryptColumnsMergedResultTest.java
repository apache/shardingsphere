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

package org.apache.shardingsphere.encrypt.merge.dal;

import org.apache.shardingsphere.encrypt.merge.dal.impl.MergedEncryptColumnsMergedResult;
import org.apache.shardingsphere.encrypt.metadata.EncryptColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MergedEncryptColumnsMergedResultTest {
    
    @Test
    public void assertNextWithTableEncryptColumnMetaDataListEmpty() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.next()).thenReturn(true);
        when(queryResult.getValue(1, String.class)).thenReturn("1");
        TableMetaData tableMetaData = new TableMetaData(Collections.emptyList(), Collections.emptyList());
        Map<String, TableMetaData> tables = new HashMap<>(1);
        tables.put("test", tableMetaData);
        SchemaMetaData schemaMetaData = new SchemaMetaData(tables);
        MergedEncryptColumnsMergedResult mergedEncryptColumnsMergedResult = getMergedEncryptColumnsMergedResult(queryResult, schemaMetaData);
        boolean actual = mergedEncryptColumnsMergedResult.next();
        assertTrue(actual);
    }
    
    @Test
    public void assertNextWithHasNext() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        SchemaMetaData schemaMetaData = mock(SchemaMetaData.class);
        MergedEncryptColumnsMergedResult mergedEncryptColumnsMergedResult = getMergedEncryptColumnsMergedResult(queryResult, schemaMetaData);
        boolean actual = mergedEncryptColumnsMergedResult.next();
        assertFalse(actual);
    }
    
    @Test
    public void assertNextWithAssistedQuery() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.next()).thenReturn(true).thenReturn(false);
        when(queryResult.getValue(1, String.class)).thenReturn("assistedQuery");
        Map<String, TableMetaData> tables = new HashMap<>(1);
        EncryptColumnMetaData encryptColumnMetaData = new EncryptColumnMetaData("id", Types.VARCHAR, "varchar", true, "cipher", "plain", "assistedQuery");
        TableMetaData tableMetaData = new TableMetaData(Collections.singletonList(encryptColumnMetaData), Collections.emptyList());
        tables.put("test", tableMetaData);
        SchemaMetaData schemaMetaData = new SchemaMetaData(tables);
        MergedEncryptColumnsMergedResult mergedEncryptColumnsMergedResult = getMergedEncryptColumnsMergedResult(queryResult, schemaMetaData);
        boolean actual = mergedEncryptColumnsMergedResult.next();
        assertFalse(actual);
    }
    
    @Test
    public void assertGetValueWithCipherColumn() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.next()).thenReturn(true).thenReturn(false);
        when(queryResult.getValue(1, String.class)).thenReturn("cipher");
        Map<String, TableMetaData> tables = new HashMap<>(1);
        EncryptColumnMetaData encryptColumnMetaData = new EncryptColumnMetaData("id", Types.VARCHAR, "varchar", true, "cipher", "plain", "assistedQuery");
        TableMetaData tableMetaData = new TableMetaData(Collections.singletonList(encryptColumnMetaData), Collections.emptyList());
        tables.put("test", tableMetaData);
        SchemaMetaData schemaMetaData = new SchemaMetaData(tables);
        MergedEncryptColumnsMergedResult mergedEncryptColumnsMergedResult = getMergedEncryptColumnsMergedResult(queryResult, schemaMetaData);
        assertThat(mergedEncryptColumnsMergedResult.getValue(1, String.class), is("id"));
    }
    
    @Test
    public void assertGetValueWithOtherColumn() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.next()).thenReturn(true).thenReturn(false);
        when(queryResult.getValue(1, String.class)).thenReturn("assistedQuery");
        Map<String, TableMetaData> tables = new HashMap<>(1);
        EncryptColumnMetaData encryptColumnMetaData = new EncryptColumnMetaData("id", Types.VARCHAR, "varchar", true, "cipher", "plain", "assistedQuery");
        TableMetaData tableMetaData = new TableMetaData(Collections.singletonList(encryptColumnMetaData), Collections.emptyList());
        tables.put("test", tableMetaData);
        SchemaMetaData schemaMetaData = new SchemaMetaData(tables);
        MergedEncryptColumnsMergedResult mergedEncryptColumnsMergedResult = getMergedEncryptColumnsMergedResult(queryResult, schemaMetaData);
        assertThat(mergedEncryptColumnsMergedResult.getValue(1, String.class), is("assistedQuery"));
    }
    
    @Test
    public void assertGetValueWithOtherIndex() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.next()).thenReturn(true).thenReturn(false);
        when(queryResult.getValue(2, String.class)).thenReturn("id");
        Map<String, TableMetaData> tables = new HashMap<>(1);
        EncryptColumnMetaData encryptColumnMetaData = new EncryptColumnMetaData("id", Types.VARCHAR, "varchar", true, "cipher", "plain", "assistedQuery");
        TableMetaData tableMetaData = new TableMetaData(Collections.singletonList(encryptColumnMetaData), Collections.emptyList());
        tables.put("test", tableMetaData);
        SchemaMetaData schemaMetaData = new SchemaMetaData(tables);
        MergedEncryptColumnsMergedResult mergedEncryptColumnsMergedResult = getMergedEncryptColumnsMergedResult(queryResult, schemaMetaData);
        assertThat(mergedEncryptColumnsMergedResult.getValue(2, String.class), is("id"));
    }
    
    @Test
    public void assertWasNull() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        SchemaMetaData schemaMetaData = mock(SchemaMetaData.class);
        MergedEncryptColumnsMergedResult mergedEncryptColumnsMergedResult = getMergedEncryptColumnsMergedResult(queryResult, schemaMetaData);
        boolean actual = mergedEncryptColumnsMergedResult.wasNull();
        assertFalse(actual);
    }
    
    private MergedEncryptColumnsMergedResult getMergedEncryptColumnsMergedResult(final QueryResult queryResult, final SchemaMetaData schemaMetaData) {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        IdentifierValue identifierValue = new IdentifierValue("test");
        TableNameSegment tableNameSegment = new TableNameSegment(1, 4, identifierValue);
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(tableNameSegment);
        when(sqlStatementContext.getAllTables()).thenReturn(Collections.singletonList(simpleTableSegment));
        return new MergedEncryptColumnsMergedResult(queryResult, sqlStatementContext, schemaMetaData);
    }
    
}
