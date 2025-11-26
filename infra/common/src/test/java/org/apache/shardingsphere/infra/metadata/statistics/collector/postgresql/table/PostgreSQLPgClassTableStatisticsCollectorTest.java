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

package org.apache.shardingsphere.infra.metadata.statistics.collector.postgresql.table;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.collector.postgresql.PostgreSQLTableStatisticsCollector;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLPgClassTableStatisticsCollectorTest {
    
    private final PostgreSQLTableStatisticsCollector collector = TypedSPILoader.getService(PostgreSQLTableStatisticsCollector.class, "pg_catalog.pg_class");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData metaData;
    
    @Test
    void assertCollectWithPublicSchemaExists() {
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("foo_tbl");
        when(metaData.getDatabase("foo_db").getSchema("public").getAllTables()).thenReturn(Collections.singleton(table));
        Collection<Map<String, Object>> actual = collector.collect("foo_db", "pg_catalog", "pg_class", metaData);
        assertThat(actual.size(), is(1));
        Map<String, Object> rowData = actual.iterator().next();
        assertThat(rowData.get("oid"), is(0L));
        assertThat(rowData.get("relnamespace"), is(0L));
        assertThat(rowData.get("relname"), is("foo_tbl"));
        assertThat(rowData.get("relkind"), is("r"));
    }
    
    @Test
    void assertCollectWithPublicSchemaNotExists() {
        when(metaData.getDatabase("foo_db").getSchema("public")).thenReturn(null);
        Collection<Map<String, Object>> actual = collector.collect("foo_db", "pg_catalog", "pg_class", metaData);
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertGetSchemaName() {
        assertThat(collector.getSchemaName(), is("pg_catalog"));
    }
    
    @Test
    void assertGetTableName() {
        assertThat(collector.getTableName(), is("pg_class"));
    }
}
