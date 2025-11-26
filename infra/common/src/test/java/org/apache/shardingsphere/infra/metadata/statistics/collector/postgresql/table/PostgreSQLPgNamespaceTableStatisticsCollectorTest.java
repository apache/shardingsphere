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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.statistics.collector.postgresql.PostgreSQLTableStatisticsCollector;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLPgNamespaceTableStatisticsCollectorTest {
    
    private final PostgreSQLTableStatisticsCollector collector = TypedSPILoader.getService(PostgreSQLTableStatisticsCollector.class, "pg_catalog.pg_namespace");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData metaData;
    
    @SuppressWarnings("unchecked")
    @Test
    void assertCollectWithMultipleSchemas() {
        when(metaData.getDatabase("foo_db").getAllSchemas())
                .thenReturn(Arrays.asList(new ShardingSphereSchema("public"), new ShardingSphereSchema("foo_schema"), new ShardingSphereSchema("bar_schema")));
        Collection<Map<String, Object>> actual = collector.collect("foo_db", "pg_catalog", "pg_namespace", metaData);
        assertThat(actual.size(), is(3));
        Map<String, Object>[] results = actual.toArray(new Map[0]);
        assertThat(results[0].get("oid"), is(0L));
        assertThat(results[0].get("nspname"), is("public"));
        assertThat(results[1].get("oid"), is(1L));
        assertThat(results[1].get("nspname"), is("foo_schema"));
        assertThat(results[2].get("oid"), is(2L));
        assertThat(results[2].get("nspname"), is("bar_schema"));
    }
    
    @Test
    void assertGetSchemaName() {
        assertThat(collector.getSchemaName(), is("pg_catalog"));
    }
    
    @Test
    void assertGetTableName() {
        assertThat(collector.getTableName(), is("pg_namespace"));
    }
}
