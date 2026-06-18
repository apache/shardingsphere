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

package org.apache.shardingsphere.infra.metadata.statistics.collector.shardingsphere;

import org.apache.shardingsphere.infra.metadata.statistics.collector.DialectDatabaseStatisticsCollector;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingSphereStatisticsCollectorTest {
    
    private final DialectDatabaseStatisticsCollector collector = new ShardingSphereStatisticsCollector();
    
    @Test
    void assertCollectRowColumnValuesWithoutAvailableCollector() {
        assertFalse(collector.collectRowColumnValues("foo_db", "foo_schema", "foo_tbl", mock()).isPresent());
    }
    
    @Test
    void assertCollectRowColumnValuesWithAvailableCollector() {
        ShardingSphereTableStatisticsCollector tableStatisticsCollector = mock();
        try (MockedStatic<TypedSPILoader> mockedLoader = mockStatic(TypedSPILoader.class)) {
            when(tableStatisticsCollector.collect(anyString(), anyString(), anyString(), any())).thenReturn(Collections.singleton(Collections.singletonMap("foo_db", "foo_schema")));
            mockedLoader.when(() -> TypedSPILoader.findService(ShardingSphereTableStatisticsCollector.class, "test_schema.error_table")).thenReturn(Optional.of(tableStatisticsCollector));
            Optional<Collection<Map<String, Object>>> actual = collector.collectRowColumnValues("foo_db", "test_schema", "error_table", mock());
            assertTrue(actual.isPresent());
            assertThat(actual.get(), is(Collections.singleton(Collections.singletonMap("foo_db", "foo_schema"))));
        }
    }
    
    @Test
    void assertIsStatisticsTablesWithEmptySchemaTables() {
        assertFalse(collector.isStatisticsTables(Collections.emptyMap()));
    }
    
    @Test
    void assertIsStatisticsTablesWithoutStatisticsSchemaTables() {
        assertFalse(collector.isStatisticsTables(Collections.singletonMap("foo_schema", Collections.singletonList("foo_tbl"))));
    }
}
