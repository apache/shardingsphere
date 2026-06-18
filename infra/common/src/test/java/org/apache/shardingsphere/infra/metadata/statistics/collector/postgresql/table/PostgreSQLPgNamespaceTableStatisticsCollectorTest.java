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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.statistics.collector.postgresql.PostgreSQLTableStatisticsCollector;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PostgreSQLPgNamespaceTableStatisticsCollectorTest {
    
    private final PostgreSQLTableStatisticsCollector collector = TypedSPILoader.getService(PostgreSQLTableStatisticsCollector.class, "pg_catalog.pg_namespace");
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @Test
    void assertCollectWithMultipleSchemas() {
        Collection<ShardingSphereSchema> schemas = Arrays.asList(
                new ShardingSphereSchema("public", databaseType),
                new ShardingSphereSchema("foo_schema", databaseType),
                new ShardingSphereSchema("bar_schema", databaseType));
        ShardingSphereMetaData metaData = createMetaData(schemas);
        Collection<Map<String, Object>> actual = collector.collect("foo_db", "pg_catalog", "pg_namespace", metaData);
        assertThat(actual.size(), is(3));
        Map<String, Map<String, Object>> actualResults = actual.stream().collect(Collectors.toMap(each -> each.get("nspname").toString(), each -> each));
        assertThat(actualResults.get("public").get("oid"), is(0L));
        assertThat(actualResults.get("public").get("nspname"), is("public"));
        assertThat(actualResults.get("foo_schema").get("oid"), is(1L));
        assertThat(actualResults.get("foo_schema").get("nspname"), is("foo_schema"));
        assertThat(actualResults.get("bar_schema").get("oid"), is(2L));
        assertThat(actualResults.get("bar_schema").get("nspname"), is("bar_schema"));
    }
    
    @Test
    void assertGetSchemaName() {
        assertThat(collector.getSchemaName(), is("pg_catalog"));
    }
    
    @Test
    void assertGetTableName() {
        assertThat(collector.getTableName(), is("pg_namespace"));
    }
    
    private ShardingSphereMetaData createMetaData(final Collection<ShardingSphereSchema> schemas) {
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()), schemas, new ConfigurationProperties(new Properties()));
        return new ShardingSphereMetaData(Collections.singleton(database), new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()), databaseType);
    }
}
