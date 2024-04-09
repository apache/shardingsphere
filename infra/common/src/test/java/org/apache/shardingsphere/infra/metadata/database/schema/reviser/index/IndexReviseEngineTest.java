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

import org.apache.shardingsphere.infra.database.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEntry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.equalToObject;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

class IndexReviseEngineTest<T extends ShardingSphereRule> {
    
    @Mock
    private T rule;
    
    @Mock
    private MetaDataReviseEntry<T> metaDataReviseEntry;
    
    @InjectMocks
    private IndexReviseEngine<T> indexReviseEngine;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void assertReviseIsPresentIsFalse() {
        when(metaDataReviseEntry.getIndexReviser(any(), anyString())).thenReturn(Optional.empty());
        Collection<IndexMetaData> indexMetaDataCollection = Collections.singletonList(new IndexMetaData("index"));
        Collection<IndexMetaData> actual = indexReviseEngine.revise("tableName", indexMetaDataCollection);
        Assertions.assertNotNull(actual);
        assertThat(actual.size(), is(1));
        assertThat(actual, equalToObject(indexMetaDataCollection));
    }
    
    @Test
    void assertReviseIsPresentIsTrue() {
        IndexReviser<T> reviser = mock(IndexReviser.class);
        IndexMetaData indexMetaData = new IndexMetaData("index");
        doReturn(Optional.of(reviser)).when(metaDataReviseEntry).getIndexReviser(any(), anyString());
        when(reviser.revise(anyString(), any(), any())).thenReturn(Optional.of(indexMetaData));
        Collection<IndexMetaData> indexMetaDataCollection = Arrays.asList(new IndexMetaData("index1"), new IndexMetaData("index2"));
        Collection<IndexMetaData> actual = indexReviseEngine.revise("tableName", indexMetaDataCollection);
        assertThat(actual.size(), equalTo(1));
        assertTrue(actual.contains(indexMetaData));
    }
    
}
