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

package org.apache.shardingsphere.sqlfederation.executor.enumerable.enumerator.memory;

import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.table.DialectDriverQuerySystemCatalogOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.TableStatistics;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MemoryTableStatisticsBuilderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertBuildDatabaseData() {
        DialectDriverQuerySystemCatalogOption option = mock(DialectDriverQuerySystemCatalogOption.class);
        when(option.isDatabaseDataTable("pg_database")).thenReturn(true);
        when(option.getDatCompatibility()).thenReturn("PG");
        ShardingSphereMetaData metaData = createMetaData(
                Arrays.asList(mockDatabase("foo_db", Collections.emptyList()), mockDatabase("bar_db", Collections.emptyList())), new RuleMetaData(Collections.emptyList()));
        TableStatistics actual = MemoryTableStatisticsBuilder.buildTableStatistics(mockTable("pg_database"), metaData, option);
        assertThat(actual.getRows().size(), is(2));
        List<Object[]> rows = actual.getRows().stream().map(each -> each.getRows().toArray(new Object[0])).collect(Collectors.toList());
        Collection<Object> databaseNames = rows.stream().map(each -> each[0]).collect(Collectors.toList());
        assertThat(databaseNames, containsInAnyOrder("foo_db", "bar_db"));
        rows.forEach(each -> assertThat(each[11], is("PG")));
    }
    
    @Test
    void assertBuildTableData() {
        DialectDriverQuerySystemCatalogOption option = mock(DialectDriverQuerySystemCatalogOption.class);
        when(option.isTableDataTable("pg_tables")).thenReturn(true);
        ShardingSphereSchema schema1 = new ShardingSphereSchema("public", Collections.singleton(mockTable("t_order")), Collections.emptyList(), databaseType);
        ShardingSphereSchema schema2 = new ShardingSphereSchema("logic", Collections.singleton(mockTable("t_user")), Collections.emptyList(), databaseType);
        ShardingSphereDatabase database = mockDatabase("foo_db", Arrays.asList(schema1, schema2));
        ShardingSphereMetaData metaData = createMetaData(Collections.singleton(database), new RuleMetaData(Collections.emptyList()));
        TableStatistics actual = MemoryTableStatisticsBuilder.buildTableStatistics(mockTable("pg_tables"), metaData, option);
        assertThat(actual.getRows().size(), is(2));
        List<Object[]> rows = actual.getRows().stream().map(each -> each.getRows().toArray(new Object[0])).collect(Collectors.toList());
        Collection<String> schemaNames = rows.stream().map(each -> (String) each[0]).collect(Collectors.toList());
        Collection<String> tableNames = rows.stream().map(each -> (String) each[1]).collect(Collectors.toList());
        assertThat(schemaNames, containsInAnyOrder("public", "logic"));
        assertThat(tableNames, containsInAnyOrder("t_order", "t_user"));
    }
    
    @Test
    void assertBuildRoleData() {
        DialectDriverQuerySystemCatalogOption option = mock(DialectDriverQuerySystemCatalogOption.class);
        when(option.isRoleDataTable("pg_roles")).thenReturn(true);
        AuthorityRule authorityRule = mock(AuthorityRule.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        when(authorityRule.getGrantees()).thenReturn(new LinkedHashSet<>(Arrays.asList(new Grantee("alice", ""), new Grantee("bob", ""))));
        ShardingSphereMetaData metaData = createMetaData(Collections.emptyList(), new RuleMetaData(Collections.singleton(authorityRule)));
        TableStatistics actual = MemoryTableStatisticsBuilder.buildTableStatistics(mockTable("pg_roles"), metaData, option);
        assertThat(actual.getRows().size(), is(2));
        List<Object[]> rows = actual.getRows().stream().map(each -> each.getRows().toArray(new Object[0])).collect(Collectors.toList());
        Collection<String> usernames = rows.stream().map(each -> (String) each[0]).collect(Collectors.toList());
        assertThat(usernames, containsInAnyOrder("alice", "bob"));
    }
    
    @Test
    void assertBuildDefaultWhenNoMatch() {
        DialectDriverQuerySystemCatalogOption option = mock(DialectDriverQuerySystemCatalogOption.class);
        TableStatistics actual = MemoryTableStatisticsBuilder.buildTableStatistics(mockTable("other"), createMetaData(Collections.emptyList(), new RuleMetaData(Collections.emptyList())), option);
        assertThat(actual.getRows(), empty());
        assertThat(actual.getName(), is("other"));
    }
    
    private ShardingSphereTable mockTable(final String name) {
        return new ShardingSphereTable(name, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereDatabase mockDatabase(final String name, final Collection<ShardingSphereSchema> schemas) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getName()).thenReturn(name);
        when(result.getAllSchemas()).thenReturn(schemas);
        when(result.getProtocolType()).thenReturn(databaseType);
        return result;
    }
    
    private ShardingSphereMetaData createMetaData(final Collection<ShardingSphereDatabase> databases, final RuleMetaData ruleMetaData) {
        return new ShardingSphereMetaData(databases, new ResourceMetaData(Collections.emptyMap()), ruleMetaData, new ConfigurationProperties(new Properties()));
    }
}
