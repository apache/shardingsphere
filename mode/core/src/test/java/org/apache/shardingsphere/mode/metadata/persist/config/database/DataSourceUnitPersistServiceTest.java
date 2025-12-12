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

package org.apache.shardingsphere.mode.metadata.persist.config.database;

import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSourceUnitPersistServiceTest {
    
    private DataSourceUnitPersistService persistService;
    
    @Mock
    private PersistRepository repository;
    
    @BeforeEach
    void setUp() {
        persistService = new DataSourceUnitPersistService(repository);
        
    }
    
    @Test
    void assertLoad() {
        when(repository.getChildrenKeys("/metadata/foo_db/data_sources/units")).thenReturn(Collections.singletonList("foo_ds"));
        when(repository.query("/metadata/foo_db/data_sources/units/foo_ds/active_version")).thenReturn("10");
        when(repository.query("/metadata/foo_db/data_sources/units/foo_ds/versions/10")).thenReturn("{dataSourceClassName: org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource}");
        Map<String, DataSourcePoolProperties> actual = persistService.load("foo_db");
        assertThat(actual.size(), is(1));
        assertThat(actual.get("foo_ds").getPoolClassName(), is("org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource"));
    }
    
    @Test
    void assertPersist() {
        Map<String, DataSourcePoolProperties> dataSourcePropsMap = new LinkedHashMap<>(1, 1F);
        dataSourcePropsMap.put("foo_ds", new DataSourcePoolProperties("org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource", Collections.emptyMap()));
        dataSourcePropsMap.put("bar_ds", new DataSourcePoolProperties("org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource", Collections.emptyMap()));
        when(repository.getChildrenKeys("/metadata/foo_db/data_sources/units/foo_ds/versions")).thenReturn(Collections.singletonList("10"));
        persistService.persist("foo_db", dataSourcePropsMap);
        verify(repository).persist("/metadata/foo_db/data_sources/units/foo_ds/active_version", "11");
    }
    
    @Test
    void assertDelete() {
        persistService.delete("foo_db", "foo_ds");
        verify(repository).delete("/metadata/foo_db/data_sources/units/foo_ds");
    }
}
