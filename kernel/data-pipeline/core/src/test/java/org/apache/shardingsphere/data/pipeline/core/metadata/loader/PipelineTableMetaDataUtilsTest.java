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

package org.apache.shardingsphere.data.pipeline.core.metadata.loader;

import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineIndexMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PipelineTableMetaDataUtilsTest {
    
    @Mock
    private PipelineTableMetaData tableMetaData;
    
    @Mock
    private PipelineTableMetaDataLoader metaDataLoader;
    
    @BeforeEach
    void setUp() {
        when(metaDataLoader.getTableMetaData("foo_schema", "foo_tbl")).thenReturn(tableMetaData);
    }
    
    @Test
    void assertGetUniqueKeyColumnsWithPrimaryKeys() {
        when(tableMetaData.getPrimaryKeyColumns()).thenReturn(Collections.singletonList("foo_pk"));
        PipelineColumnMetaData columnMetaData = mock(PipelineColumnMetaData.class);
        when(tableMetaData.getColumnMetaData("foo_pk")).thenReturn(columnMetaData);
        List<PipelineColumnMetaData> actual = PipelineTableMetaDataUtils.getUniqueKeyColumns("foo_schema", "foo_tbl", metaDataLoader);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), is(columnMetaData));
    }
    
    @Test
    void assertGetUniqueKeyColumnsWithUniqueIndexNullable() {
        PipelineIndexMetaData pipelineIndexMetaData = mock(PipelineIndexMetaData.class, RETURNS_DEEP_STUBS);
        PipelineColumnMetaData columnMetaData = mock(PipelineColumnMetaData.class);
        when(columnMetaData.isNullable()).thenReturn(true);
        when(pipelineIndexMetaData.getColumns()).thenReturn(Collections.singletonList(columnMetaData));
        when(tableMetaData.getUniqueIndexes()).thenReturn(Collections.singletonList(pipelineIndexMetaData));
        List<PipelineColumnMetaData> actual = PipelineTableMetaDataUtils.getUniqueKeyColumns("foo_schema", "foo_tbl", metaDataLoader);
        assertThat(actual.size(), is(0));
    }
    
    @Test
    void assertGetUniqueKeyColumnsWithUniqueIndexNotNull() {
        PipelineIndexMetaData pipelineIndexMetaData = mock(PipelineIndexMetaData.class, RETURNS_DEEP_STUBS);
        PipelineColumnMetaData columnMetaData = mock(PipelineColumnMetaData.class);
        when(columnMetaData.isNullable()).thenReturn(false);
        when(pipelineIndexMetaData.getColumns()).thenReturn(Collections.singletonList(columnMetaData));
        when(tableMetaData.getUniqueIndexes()).thenReturn(Collections.singletonList(pipelineIndexMetaData));
        List<PipelineColumnMetaData> actual = PipelineTableMetaDataUtils.getUniqueKeyColumns("foo_schema", "foo_tbl", metaDataLoader);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), is(columnMetaData));
    }
    
    @Test
    void assertGetUniqueKeyColumnsWithUniqueIndexes() {
        PipelineIndexMetaData pipelineIndexMetaData1 = mock(PipelineIndexMetaData.class, RETURNS_DEEP_STUBS);
        PipelineColumnMetaData columnMetaData1 = mock(PipelineColumnMetaData.class);
        when(columnMetaData1.isNullable()).thenReturn(true);
        when(pipelineIndexMetaData1.getColumns()).thenReturn(Collections.singletonList(columnMetaData1));
        PipelineIndexMetaData pipelineIndexMetaData2 = mock(PipelineIndexMetaData.class, RETURNS_DEEP_STUBS);
        PipelineColumnMetaData columnMetaData2 = mock(PipelineColumnMetaData.class);
        when(pipelineIndexMetaData2.getColumns()).thenReturn(Collections.singletonList(columnMetaData2));
        when(tableMetaData.getUniqueIndexes()).thenReturn(Arrays.asList(pipelineIndexMetaData1, pipelineIndexMetaData2));
        List<PipelineColumnMetaData> actual = PipelineTableMetaDataUtils.getUniqueKeyColumns("foo_schema", "foo_tbl", metaDataLoader);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), is(columnMetaData2));
    }
    
    @Test
    void assertGetUniqueKeyColumnsWithEmptyKeys() {
        assertTrue(PipelineTableMetaDataUtils.getUniqueKeyColumns("foo_schema", "foo_tbl", metaDataLoader).isEmpty());
    }
}
