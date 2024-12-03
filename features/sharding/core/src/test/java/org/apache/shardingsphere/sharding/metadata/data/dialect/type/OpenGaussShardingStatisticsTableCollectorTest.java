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

package org.apache.shardingsphere.sharding.metadata.data.dialect.type;

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
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenGaussShardingStatisticsTableCollectorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    private ShardingSphereStatisticsCollector statisticsCollector;
    
    @BeforeEach
    void setUp() {
        statisticsCollector = TypedSPILoader.getService(ShardingSphereStatisticsCollector.class, "sharding_table_statistics");
    }
    
    @Test
    void assertCollectWithoutExistedTables() throws SQLException {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getShardingTables()).thenReturn(Collections.singletonMap("foo_tbl", new ShardingTable(Arrays.asList("ds_0", "ds_1"), "foo_tbl")));
        Map<String, StorageUnit> storageUnits = new HashMap<>(2, 1F);
        storageUnits.put("ds_0", mockStorageUnit(mock(ResultSet.class), false));
        storageUnits.put("ds_1", mockStorageUnit(mockResultSet(), false));
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
    
    @Test
    void assertCollectWithExistedTables() throws SQLException {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getShardingTables()).thenReturn(Collections.singletonMap("foo_tbl", new ShardingTable(Arrays.asList("ds_0", "ds_1"), "foo_tbl")));
        Map<String, StorageUnit> storageUnits = new HashMap<>(2, 1F);
        storageUnits.put("ds_0", mockStorageUnit(mock(ResultSet.class), true));
        storageUnits.put("ds_1", mockStorageUnit(mockResultSet(), true));
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                "foo_db", databaseType, new ResourceMetaData(Collections.emptyMap(), storageUnits), new RuleMetaData(Collections.singleton(rule)), Collections.emptyList());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), new ConfigurationProperties(new Properties()));
        Optional<ShardingSphereTableData> actual = statisticsCollector.collect("foo_db", mock(ShardingSphereTable.class), metaData);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("sharding_table_statistics"));
        List<ShardingSphereRowData> actualRows = new ArrayList<>(actual.get().getRows());
        assertThat(actualRows.size(), is(2));
        assertThat(actualRows.get(0).getRows(), is(Arrays.asList(2, "foo_db", "foo_tbl", "ds_1", "foo_tbl", new BigDecimal("10"), new BigDecimal("100"))));
        assertThat(actualRows.get(1).getRows(), is(Arrays.asList(1, "foo_db", "foo_tbl", "ds_0", "foo_tbl", new BigDecimal("0"), new BigDecimal("0"))));
    }
    
    private StorageUnit mockStorageUnit(final ResultSet resultSet, final boolean tableExisted) throws SQLException {
        StorageUnit result = mock(StorageUnit.class);
        when(result.getStorageType()).thenReturn(databaseType);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        ResultSet tableExistedResultSet = mock(ResultSet.class);
        when(tableExistedResultSet.next()).thenReturn(tableExisted);
        when(connection.getMetaData().getTables(any(), any(), any(), any())).thenReturn(tableExistedResultSet);
        when(connection.prepareStatement("SELECT RELTUPLES AS TABLE_ROWS, PG_TABLE_SIZE(?) AS DATA_LENGTH FROM PG_CLASS WHERE RELNAME = ?").executeQuery()).thenReturn(resultSet);
        when(result.getDataSource()).thenReturn(new MockedDataSource(connection));
        return result;
    }
    
    private ResultSet mockResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true);
        when(result.getBigDecimal("TABLE_ROWS")).thenReturn(new BigDecimal("10"));
        when(result.getBigDecimal("DATA_LENGTH")).thenReturn(new BigDecimal("100"));
        return result;
    }
}
