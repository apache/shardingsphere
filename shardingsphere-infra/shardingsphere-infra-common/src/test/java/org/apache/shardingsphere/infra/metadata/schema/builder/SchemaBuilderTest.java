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

package org.apache.shardingsphere.infra.metadata.schema.builder;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.fixture.rule.CommonFixtureRule;
import org.apache.shardingsphere.infra.metadata.schema.fixture.rule.DataNodeContainedFixtureRule;
import org.apache.shardingsphere.infra.metadata.schema.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class SchemaBuilderTest {
    
    @Test
    public void assertBuildOfAllShardingTables() throws SQLException {
        DatabaseType databaseType = mock(DatabaseType.class);
        Collection<ShardingSphereRule> rules = Arrays.asList(new CommonFixtureRule(), new DataNodeContainedFixtureRule());
        Collection<String> tableNames = rules.stream().filter(each -> each instanceof TableContainedRule)
                .flatMap(each -> ((TableContainedRule) each).getTables().stream()).collect(Collectors.toSet());
        Map<String, SchemaMetaData> actual = SchemaMetaDataBuilder.load(tableNames, new SchemaBuilderMaterials(
                databaseType, databaseType, Collections.singletonMap("logic_db", new MockedDataSource()), rules, new ConfigurationProperties(new Properties()), "sharding_db"));
        assertThat(actual.size(), is(1));
        ShardingSphereSchema schema = new ShardingSphereSchema(actual.values().iterator().next().getTables());
        assertThat(schema.getTables().keySet().size(), is(2));
        assertSchemaOfShardingTables(schema.getTables().values());
    }
    
    private void assertSchemaOfShardingTables(final Collection<TableMetaData> actual) {
        assertThat(actual.size(), is(2));
        Map<String, TableMetaData> tableMetaDataMap = actual.stream().collect(Collectors.toMap(TableMetaData::getName, value -> value));
        assertTrue(tableMetaDataMap.get("data_node_routed_table1").getColumns().isEmpty());
        assertTrue(tableMetaDataMap.get("data_node_routed_table2").getColumns().isEmpty());
    }
}
