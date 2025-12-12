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

package org.apache.shardingsphere.infra.metadata.database.schema.reviser.index;

import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"rawtypes", "unchecked"})
@ExtendWith(MockitoExtension.class)
class IndexReviseEngineTest {
    
    @Mock
    private MetaDataReviseEntry metaDataReviseEntry;
    
    @InjectMocks
    private IndexReviseEngine indexReviseEngine;
    
    @Test
    void assertReviseWithoutIndexReviser() {
        when(metaDataReviseEntry.getIndexReviser(any(), eq("foo_tbl"))).thenReturn(Optional.empty());
        Collection<IndexMetaData> actual = indexReviseEngine.revise("foo_tbl", Collections.singleton(new IndexMetaData("foo_idx")));
        assertThat(actual.size(), is(1));
        assertIndexMetaData(actual.iterator().next(), new IndexMetaData("foo_idx"));
    }
    
    @Test
    void assertReviseWithIndexReviser() {
        IndexReviser reviser = mock(IndexReviser.class);
        when(reviser.revise(eq("foo_tbl"), any(), any())).thenReturn(Optional.of(new IndexMetaData("foo_idx")));
        when(metaDataReviseEntry.getIndexReviser(any(), eq("foo_tbl"))).thenReturn(Optional.of(reviser));
        Collection<IndexMetaData> actual = indexReviseEngine.revise("foo_tbl", Arrays.asList(new IndexMetaData("idx_0"), new IndexMetaData("idx_1")));
        assertThat(actual.size(), is(2));
        assertIndexMetaData(actual.iterator().next(), new IndexMetaData("foo_idx"));
        assertIndexMetaData(actual.iterator().next(), new IndexMetaData("foo_idx"));
    }
    
    private void assertIndexMetaData(final IndexMetaData actual, final IndexMetaData expected) {
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.getColumns(), is(expected.getColumns()));
        assertThat(actual.isUnique(), is(expected.isUnique()));
    }
}
