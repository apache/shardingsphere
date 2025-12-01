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

package org.apache.shardingsphere.mode.node;

import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.infra.state.datasource.qualified.QualifiedDataSourceState;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QualifiedDataSourceStatePersistServiceTest {
    
    private QualifiedDataSourceStatePersistService qualifiedDataSourceStatePersistService;
    
    @Mock
    private PersistRepository repository;
    
    @BeforeEach
    void setUp() {
        qualifiedDataSourceStatePersistService = new QualifiedDataSourceStatePersistService(repository);
    }
    
    @Test
    void assertLoad() {
        when(repository.getChildrenKeys("/nodes/qualified_data_sources")).thenReturn(Arrays.asList("foo_db.foo_group.foo_ds", "bar_db.bar_group.bar_ds"));
        when(repository.query("/nodes/qualified_data_sources/foo_db.foo_group.foo_ds")).thenReturn("state: ENABLED");
        Map<String, QualifiedDataSourceState> actual = qualifiedDataSourceStatePersistService.load();
        assertThat(actual.size(), is(1));
        assertThat(actual.get("foo_db.foo_group.foo_ds").getState(), is(DataSourceState.ENABLED));
    }
    
    @Test
    void assertUpdate() {
        qualifiedDataSourceStatePersistService.update("foo_db", "foo_group", "foo_ds", DataSourceState.ENABLED);
        verify(repository).persist("/nodes/qualified_data_sources/foo_db.foo_group.foo_ds", "state: ENABLED" + System.lineSeparator());
    }
}
