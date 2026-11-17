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

package org.apache.shardingsphere.data.pipeline.cdc.sink.core;

import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.FinishedRecord;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CDCRecordPreparerTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("schemaTableNames")
    void assertPrepareWithSchemaMapping(final Collection<String> schemaTableNames, final String expectedSchema) {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_database");
        CDCRecordPreparer preparer = new CDCRecordPreparer(database, schemaTableNames);
        DataRecord dataRecord = new DataRecord(PipelineSQLOperationType.INSERT, "foo_table", new IngestPlaceholderPosition(), 0);
        Collection<DataRecordResult.Record> actual = preparer.prepare(Arrays.asList(new FinishedRecord(new IngestPlaceholderPosition()), dataRecord));
        assertThat(actual.size(), is(1));
        DataRecordResult.Record actualRecord = actual.iterator().next();
        assertThat(actualRecord.getMetaData().getDatabase(), is("foo_database"));
        assertThat(actualRecord.getMetaData().getSchema(), is(expectedSchema));
        assertThat(actualRecord.getMetaData().getTable(), is("foo_table"));
        verify(database).getName();
    }
    
    private static Stream<Arguments> schemaTableNames() {
        return Stream.of(
                Arguments.of(Collections.singletonList("foo_schema.foo_table"), "foo_schema"),
                Arguments.of(Collections.singletonList("foo_table"), ""),
                Arguments.of(Arrays.asList("foo_schema.foo_table", "bar_schema.foo_table"), "bar_schema"));
    }
    
    @Test
    void assertPreparePreservesRecordOrder() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_database");
        CDCRecordPreparer preparer = new CDCRecordPreparer(database, Arrays.asList("foo_schema.foo_table", "bar_schema.bar_table"));
        DataRecord firstRecord = new DataRecord(PipelineSQLOperationType.INSERT, "foo_table", new IngestPlaceholderPosition(), 0);
        DataRecord secondRecord = new DataRecord(PipelineSQLOperationType.UPDATE, "bar_table", new IngestPlaceholderPosition(), 0);
        Collection<DataRecordResult.Record> actual = preparer.prepare(Arrays.asList(firstRecord, new FinishedRecord(new IngestPlaceholderPosition()), secondRecord));
        assertThat(actual.size(), is(2));
        Iterator<DataRecordResult.Record> actualRecords = actual.iterator();
        assertThat(actualRecords.next().getMetaData().getTable(), is("foo_table"));
        assertThat(actualRecords.next().getMetaData().getTable(), is("bar_table"));
        verify(database, times(2)).getName();
    }
    
    @Test
    void assertPrepareWithoutDataRecords() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        CDCRecordPreparer preparer = new CDCRecordPreparer(database, Collections.singletonList("foo_schema.foo_table"));
        assertTrue(preparer.prepare(Collections.singletonList(new FinishedRecord(new IngestPlaceholderPosition()))).isEmpty());
        verifyNoInteractions(database);
    }
}
