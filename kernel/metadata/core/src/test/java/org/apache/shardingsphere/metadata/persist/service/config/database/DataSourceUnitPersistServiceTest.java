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

package org.apache.shardingsphere.metadata.persist.service.config.database;

import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSourceUnitPersistServiceTest {
    
    private DataSourceUnitPersistService persistService;
    
    @Mock
    private PersistRepository repository;
    
    @BeforeEach
    void setUp() {
        persistService = new DataSourceUnitPersistService(repository);
        when(repository.query("/metadata/foo_db/data_sources/units/foo_ds/active_version")).thenReturn("10");
    }
    
    @Test
    void assertLoad() {
        when(repository.getChildrenKeys("/metadata/foo_db/data_sources/units")).thenReturn(Arrays.asList("foo_ds", "bar_ds"));
        when(repository.query("/metadata/foo_db/data_sources/units/foo_ds/versions/10")).thenReturn("{dataSourceClassName: org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource}");
        Map<String, DataSourcePoolProperties> actual = persistService.load("foo_db");
        assertThat(actual.size(), is(1));
        assertThat(actual.get("foo_ds").getPoolClassName(), is("org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource"));
    }
}
