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

package org.apache.shardingsphere.sharding.metadata.data;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.statistics.collector.ShardingSphereStatisticsCollector;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingStatisticsTableCollectorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private ShardingSphereStatisticsCollector statisticsCollector;
    
    @BeforeEach
    void setUp() {
        statisticsCollector = TypedSPILoader.getService(ShardingSphereStatisticsCollector.class, "sharding_table_statistics");
    }
    
    @Test
    void assertCollectWithoutShardingRule() throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(databaseType);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singleton(database), mock(ResourceMetaData.class), mock(RuleMetaData.class), new ConfigurationProperties(new Properties()));
        Optional<ShardingSphereTableData> actual = statisticsCollector.collect("foo_db", mock(ShardingSphereTable.class), metaData);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertCollectWithShardingRule() throws SQLException {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getShardingTables()).thenReturn(Collections.singletonMap("foo_tbl", new ShardingTable(Arrays.asList("ds_0", "ds_1"), "foo_tbl")));
        Map<String, StorageUnit> storageUnits = new HashMap<>(2, 1F);
        storageUnits.put("ds_0", mock(StorageUnit.class, RETURNS_DEEP_STUBS));
        storageUnits.put("ds_1", mock(StorageUnit.class, RETURNS_DEEP_STUBS));
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                "foo_db", databaseType, new ResourceMetaData(Collections.emptyMap(), storageUnits), new RuleMetaData(Collections.singleton(rule)), Collections.emptyList());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), new ConfigurationProperties(new Properties()));
        Optional<ShardingSphereTableData> actual = statisticsCollector.collect("foo_db", mock(ShardingSphereTable.class), metaData);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("sharding_table_statistics"));
        List<ShardingSphereRowData> actualRows = new ArrayList<>(actual.get().getRows());
        assertThat(actualRows.size(), is(2));
        assertThat(actualRows.get(0).getRows(), is(Arrays.asList(1, "foo_db", "foo_tbl", "ds_0", "foo_tbl", new BigDecimal("0"), new BigDecimal("0"))));
        assertThat(actualRows.get(1).getRows(), is(Arrays.asList(2, "foo_db", "foo_tbl", "ds_1", "foo_tbl", new BigDecimal("0"), new BigDecimal("0"))));
    }
}
